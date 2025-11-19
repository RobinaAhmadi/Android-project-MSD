package com.example.android_project_msd.groups.groupdetail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_project_msd.data.GroupRepository
import com.example.android_project_msd.data.NotificationRepository
import com.example.android_project_msd.data.UserRepository
import com.example.android_project_msd.data.UserSession
import com.example.android_project_msd.groups.grouplist.Group
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.android_project_msd.groups.data.DebtCalculator
import com.example.android_project_msd.groups.data.Expense
import com.example.android_project_msd.groups.data.Settlement
import com.example.android_project_msd.notifications.NotificationCenter

data class GroupMember(
    val id: String,
    val name: String,
    val email: String,
    val balance: Double // Positive = owed money, Negative = owes money
)

data class GroupDetailUiState(
    val group: Group? = null,
    val members: List<GroupMember> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val userBalance: Double = 0.0,
    val settlements: List<Settlement> = emptyList(),
    val isDebtsSettled: Boolean = true
)

class GroupDetailViewModel : ViewModel() {
    private val notificationRepository = NotificationRepository()
    private val groupRepository = GroupRepository()
    private val userRepository = UserRepository()
    private val _ui = MutableStateFlow(GroupDetailUiState())
    val ui = _ui.asStateFlow()
    private var currentGroupId: String? = null

    // Get current user's display name from members list (will be "You")
    private val currentUserName: String
        get() = _ui.value.members.find { it.id == UserSession.currentUserId }?.name ?: "You"

    fun loadGroup(groupId: String) {
        currentGroupId = groupId
        refreshGroup()
    }

    fun refreshGroup() {
        val groupId = currentGroupId ?: return

        viewModelScope.launch {
            Log.d("GroupDetailVM", "Loading group: $groupId")

            // Get group from Firebase
            val firebaseGroup = groupRepository.getGroupById(groupId)
            if (firebaseGroup == null) {
                Log.w("GroupDetailVM", "Group not found in Firebase")
                _ui.value = GroupDetailUiState(
                    group = null,
                    members = emptyList(),
                    expenses = emptyList(),
                    userBalance = 0.0,
                    settlements = emptyList(),
                    isDebtsSettled = true
                )
                return@launch
            }

            Log.d("GroupDetailVM", "Group found: ${firebaseGroup.name}, ${firebaseGroup.members.size} members")

            // Convert Firebase Group to UI Group
            val uiGroup = Group(
                id = firebaseGroup.id,
                name = firebaseGroup.name,
                description = firebaseGroup.description,
                memberCount = firebaseGroup.members.size,
                balance = 0.0
            )

            // Load member details from users collection
            val members = mutableListOf<GroupMember>()
            val currentUserId = UserSession.currentUserId

            for ((index, memberId) in firebaseGroup.members.withIndex()) {
                val user = userRepository.getUserById(memberId)
                if (user != null) {
                    val isCurrentUser = memberId == currentUserId
                    members.add(
                        GroupMember(
                            id = memberId,
                            name = if (isCurrentUser) "You" else user.name,
                            email = user.email,
                            balance = 0.0
                        )
                    )
                    Log.d("GroupDetailVM", "Member ${index + 1}: ${user.name} (${user.email})")
                } else {
                    Log.w("GroupDetailVM", "User not found for memberId: $memberId")
                }
            }

            Log.d("GroupDetailVM", "Loaded ${members.size} members")

            _ui.value = GroupDetailUiState(
                group = uiGroup,
                members = members,
                expenses = emptyList(),
                userBalance = 0.0,
                settlements = emptyList(),
                isDebtsSettled = true
            )
        }
    }

    fun addExpense(description: String, amount: Double, paidBy: String, splitAmong: List<String>) {
        val newExpense = Expense(
            id = System.currentTimeMillis().toString(),
            description = description,
            amount = amount,
            paidBy = paidBy,
            splitAmong = splitAmong,
            date = getCurrentDate()
        )

        val currentExpenses = _ui.value.expenses.toMutableList()
        currentExpenses.add(0, newExpense)

        _ui.value = _ui.value.copy(expenses = currentExpenses)
        recalcDebts()

        //Notification
        val group = _ui.value.group
        NotificationCenter.notifyExpenseAdded(
            groupId = group?.id,
            groupName = group?.name,
            description = description,
            amount = amount,
            paidBy = paidBy,
            splitAmong = splitAmong
        )
    }

    fun addMember(email: String) {
        val trimmedEmail = email.trim()
        val groupId = currentGroupId ?: return
        val group = _ui.value.group ?: return

        // Check if member already exists
        if (_ui.value.members.any { it.email.equals(trimmedEmail, ignoreCase = true) }) {
            Log.w("GroupDetailVM", "Member with email $trimmedEmail already exists")
            return
        }

        val ownerId = UserSession.currentUserId
        if (ownerId == null) {
            Log.w("GroupDetailVM", "No user logged in")
            return
        }

        // Send invitation instead of adding directly
        viewModelScope.launch {
            Log.d("GroupDetailVM", "Sending invitation to $trimmedEmail for group ${group.name}")

            notificationRepository.createGroupInvitation(
                fromUserId = ownerId,
                toEmail = trimmedEmail,
                groupId = groupId,
                groupName = group.name,
                groupDescription = group.description
            ).fold(
                onSuccess = { _ ->
                    Log.d("GroupDetailVM", "Invitation sent successfully to $trimmedEmail")
                    // Note: Member will be added when they accept the invitation
                    // We don't add them to the local list here
                },
                onFailure = { error ->
                    Log.e("GroupDetailVM", "Failed to send invitation to $trimmedEmail: ${error.message}", error)
                }
            )
        }
    }

    // Records a payment made between two members
    fun recordPayment(fromName: String, toName: String, amount: Double) {
        if (amount <= 0) return

        // Create a special "payment" expense
        val payment = Expense(
            id = System.currentTimeMillis().toString(),
            description = "Payment from $fromName to $toName",
            amount = amount,
            paidBy = fromName,  // Person paying is the payer
            splitAmong = listOf(toName),  // Person receiving splits it
            date = getCurrentDate()
        )

        val list = _ui.value.expenses.toMutableList()
        list.add(0, payment)
        _ui.value = _ui.value.copy(expenses = list)
        recalcDebts()

        //Notification
        val group = _ui.value.group
        NotificationCenter.notifyPaymentRecorded(
            groupId = group?.id,
            groupName = group?.name,
            from = fromName,
            to = toName,
            amount = amount
        )

    }

    // Debt calculation main function
    private fun recalcDebts() {
        val members = _ui.value.members
        if (members.isEmpty()) return

        val memberNames = members.map { it.name }

        // Use DebtCalculator to calculate balances
        val balances = DebtCalculator.calculateBalances(
            expenses = _ui.value.expenses,
            members = memberNames
        )

        // Calculate optimal settlements
        val settlements = DebtCalculator.calculateSettlements(balances)

        // Check if all debts are settled
        val isSettled = DebtCalculator.areAllDebtsSettled(balances)

        // Update members with their new balances
        val updatedMembers = members.map { member ->
            member.copy(balance = balances[member.name] ?: 0.0)
        }

        // Get current user's balance
        val userBalance = DebtCalculator.getPersonTotalBalance(balances, currentUserName)

        _ui.value = _ui.value.copy(
            members = updatedMembers,
            userBalance = userBalance,
            settlements = settlements,
            isDebtsSettled = isSettled
        )
    }

    // X owe you / You owe X
    fun getSettlementsForPerson(personName: String): List<Settlement> {
        return _ui.value.settlements.filter {
            it.fromPerson == personName || it.toPerson == personName
        }
    }

    //Fetches the current date
    private fun getCurrentDate(): String {
        val now = System.currentTimeMillis()
        val today = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        return today.format(java.util.Date(now))
    }

    //Notification reminder of settlement
    fun sendReminderForSettlement(settlement: Settlement) {
        val group = _ui.value.group ?: return

        if (settlement.toPerson != currentUserName) return

        NotificationCenter.sendReminder(
            groupId = group.id,
            groupName = group.name,
            from = currentUserName,
            to = settlement.fromPerson,
            amount = settlement.amount
        )
    }
}