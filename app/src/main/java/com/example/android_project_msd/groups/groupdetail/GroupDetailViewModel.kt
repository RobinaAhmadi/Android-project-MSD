package com.example.android_project_msd.groups.groupdetail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_project_msd.data.GroupRepository
import com.example.android_project_msd.data.NotificationRepository
import com.example.android_project_msd.data.UserRepository
import com.example.android_project_msd.data.UserSession
import com.example.android_project_msd.groups.grouplist.Group
import com.example.android_project_msd.data.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import com.example.android_project_msd.groups.data.DebtCalculator
import com.example.android_project_msd.groups.data.Expense
import com.example.android_project_msd.groups.data.Settlement
import com.example.android_project_msd.notifications.AppNotification
import com.example.android_project_msd.notifications.NotificationCenter
import com.example.android_project_msd.notifications.NotificationType
import java.util.Locale

data class GroupMember(
    val id: String,
    val name: String, // Actual name stored in Firestore
    val email: String,
    val balance: Double, // Positive = owed money, Negative = owes money
    val displayName: String = name
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
    private val expenseRepository = ExpenseRepository()
    private val _ui = MutableStateFlow(GroupDetailUiState())
    val ui = _ui.asStateFlow()
    private var currentGroupId: String? = null
    private var expensesJob: Job? = null
    private var knownExpenseIds: Set<String> = emptySet()

    // Get current user's display name from members list (will be "You")
    private val currentUserName: String
        get() = _ui.value.members.find { it.id == UserSession.currentUserId }?.name ?: ""

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
                stopExpenseListener()
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
                            name = user.name,
                            email = user.email,
                            balance = 0.0,
                            displayName = if (isCurrentUser) "You" else user.name
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

            startExpenseListener(groupId)
        }
    }

    fun addExpense(description: String, amount: Double, paidBy: String, splitAmong: List<String>) {
        val groupId = currentGroupId ?: return
        val newExpense = Expense(
            id = System.currentTimeMillis().toString(),
            description = description,
            amount = amount,
            paidBy = paidBy,
            splitAmong = splitAmong,
            date = getCurrentDate(),
            createdAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            expenseRepository.addExpense(groupId, newExpense)
                .onSuccess {
                    val group = _ui.value.group
                    val displayPaidBy = toDisplayName(paidBy)
                    val displaySplitAmong = splitAmong.map { toDisplayName(it) }
                    NotificationCenter.notifyExpenseAdded(
                        groupId = group?.id,
                        groupName = group?.name,
                        description = description,
                        amount = amount,
                        paidBy = displayPaidBy,
                        splitAmong = displaySplitAmong
                    )
                    persistMemberNotifications(
                        buildExpenseNotifications(
                            description = description,
                            amount = amount,
                            paidBy = paidBy,
                            splitAmong = splitAmong
                        )
                    )
                }
                .onFailure { error ->
                    Log.e("GroupDetailVM", "Failed to add expense: ${error.message}", error)
                }
        }
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
            date = getCurrentDate(),
            createdAt = System.currentTimeMillis()
        )

        val groupId = currentGroupId ?: return
        viewModelScope.launch {
            expenseRepository.addExpense(groupId, payment)
                .onSuccess {
                    val group = _ui.value.group
                    val fromDisplay = toDisplayName(fromName)
                    val toDisplay = toDisplayName(toName)
                    NotificationCenter.notifyPaymentRecorded(
                        groupId = group?.id,
                        groupName = group?.name,
                        from = fromDisplay,
                        to = toDisplay,
                        amount = amount
                    )
                    persistMemberNotifications(
                        buildPaymentNotifications(
                            fromName = fromName,
                            toName = toName,
                            amount = amount
                        )
                    )
                }
                .onFailure { error ->
                    Log.e("GroupDetailVM", "Failed to record payment: ${error.message}", error)
                }
        }
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

    private fun startExpenseListener(groupId: String) {
        expensesJob?.cancel()
        expensesJob = viewModelScope.launch {
            expenseRepository.observeExpenses(groupId).collect { expenses ->
                handleIncomingExpenses(expenses)
                _ui.value = _ui.value.copy(expenses = expenses)
                recalcDebts()
            }
        }
    }

    private fun stopExpenseListener() {
        expensesJob?.cancel()
        expensesJob = null
        knownExpenseIds = emptySet()
        _ui.value = _ui.value.copy(expenses = emptyList())
    }

    //Notification reminder of settlement
    fun sendReminderForSettlement(settlement: Settlement) {
        val group = _ui.value.group ?: return

        if (settlement.toPerson != currentUserName) return

        NotificationCenter.sendReminder(
            groupId = group.id,
            groupName = group.name,
            from = toDisplayName(currentUserName),
            to = toDisplayName(settlement.fromPerson),
            amount = settlement.amount
        )

        viewModelScope.launch {
            persistMemberNotifications(buildReminderNotification(settlement))
        }
    }

    override fun onCleared() {
        super.onCleared()
        expensesJob?.cancel()
    }

    private fun toDisplayName(actualName: String): String {
        if (actualName.isBlank()) return actualName
        val member = _ui.value.members.find { it.name == actualName }
        return member?.displayName ?: actualName
    }

    private suspend fun persistMemberNotifications(targets: Map<String, AppNotification>) {
        if (targets.isEmpty()) return
        notificationRepository.pushUserNotifications(targets)
            .onFailure { error ->
                Log.e("GroupDetailVM", "Failed to sync notifications: ${error.message}", error)
            }
    }

    private fun buildExpenseNotifications(
        description: String,
        amount: Double,
        paidBy: String,
        splitAmong: List<String>
    ): Map<String, AppNotification> {
        val group = _ui.value.group ?: return emptyMap()
        val members = _ui.value.members
        if (members.isEmpty()) return emptyMap()

        val membersByName = members.associateBy { it.name }
        val actualSplitNames = if (splitAmong.isEmpty()) members.map { it.name } else splitAmong
        val splitTargets = actualSplitNames.mapNotNull { membersByName[it] }
        if (splitTargets.isEmpty()) return emptyMap()

        val payerMember = membersByName[paidBy]
        val perPerson = if (splitTargets.isNotEmpty()) amount / splitTargets.size else 0.0
        val payerDisplay = payerMember?.name ?: paidBy
        val formattedAmount = formatAmount(amount)

        val recipients = buildList {
            addAll(splitTargets)
            if (payerMember != null && splitTargets.none { it.id == payerMember.id }) {
                add(payerMember)
            }
        }

        return recipients.associate { member ->
            val splitDisplay = actualSplitNames.map { name ->
                if (name == member.name) "You" else name
            }

            val youOweLine = if (splitTargets.any { it.id == member.id } &&
                member.id != payerMember?.id &&
                perPerson > 0.0
            ) {
                val formattedShare = formatAmount(perPerson)
                "You owe $payerDisplay: $formattedShare DKK"
            } else {
                null
            }

            member.id to AppNotification(
                groupId = group.id,
                type = NotificationType.EXPENSE_ADDED,
                title = "New expense in ${group.name}",
                line1 = "$payerDisplay added \"$description\" (${formattedAmount} DKK)",
                line2 = "Split among: ${splitDisplay.joinToString()}",
                youOweLine = youOweLine,
                recipients = splitDisplay
            )
        }
    }

    private fun buildPaymentNotifications(
        fromName: String,
        toName: String,
        amount: Double
    ): Map<String, AppNotification> {
        val group = _ui.value.group ?: return emptyMap()
        val membersByName = _ui.value.members.associateBy { it.name }
        val payer = membersByName[fromName] ?: return emptyMap()
        val receiver = membersByName[toName] ?: return emptyMap()
        val formattedAmount = formatAmount(amount)

        val notifications = mutableMapOf<String, AppNotification>()

        notifications[payer.id] = AppNotification(
            groupId = group.id,
            type = NotificationType.PAYMENT_RECORDED,
            title = "Payment recorded in ${group.name}",
            line1 = "You paid $formattedAmount DKK to ${receiver.name}",
            recipients = listOf("You", receiver.name)
        )

        notifications[receiver.id] = AppNotification(
            groupId = group.id,
            type = NotificationType.PAYMENT_RECORDED,
            title = "Payment recorded in ${group.name}",
            line1 = "${payer.name} paid you $formattedAmount DKK",
            recipients = listOf(payer.name, "You")
        )

        return notifications
    }

    private fun buildReminderNotification(
        settlement: Settlement
    ): Map<String, AppNotification> {
        val group = _ui.value.group ?: return emptyMap()
        val membersByName = _ui.value.members.associateBy { it.name }
        val targetMember = membersByName[settlement.fromPerson] ?: return emptyMap()
        val senderName = currentUserName.ifBlank { "Group member" }
        val formattedAmount = formatAmount(settlement.amount)

        val notification = AppNotification(
            groupId = group.id,
            type = NotificationType.REMINDER,
            title = "Payment reminder",
            line1 = "$senderName reminds you to pay $formattedAmount DKK in ${group.name}",
            recipients = listOf("You")
        )

        return mapOf(targetMember.id to notification)
    }

    private fun formatAmount(value: Double): String =
        String.format(Locale.getDefault(), "%.2f", value)

    private fun handleIncomingExpenses(expenses: List<Expense>) {
        val newOnes = expenses.filter { it.id !in knownExpenseIds }
        knownExpenseIds = expenses.map { it.id }.toSet()
        if (newOnes.isEmpty()) return

        val group = _ui.value.group
        val currentName = currentUserName
        if (currentName.isBlank()) return

        val memberNames = if (_ui.value.members.isEmpty()) emptyList() else _ui.value.members.map { it.name }

        newOnes.forEach { expense ->
            val splitTargets = if (expense.splitAmong.isEmpty()) memberNames else expense.splitAmong
            val isRecipient = splitTargets.contains(currentName) && expense.paidBy != currentName
            if (isRecipient) {
                NotificationCenter.notifyExpenseAdded(
                    groupId = group?.id,
                    groupName = group?.name,
                    description = expense.description,
                    amount = expense.amount,
                    paidBy = toDisplayName(expense.paidBy),
                    splitAmong = splitTargets.map { toDisplayName(it) }
                )
            }
        }
    }
}
