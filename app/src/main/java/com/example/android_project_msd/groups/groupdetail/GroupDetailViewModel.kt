package com.example.android_project_msd.groups.groupdetail

import androidx.lifecycle.ViewModel
import com.example.android_project_msd.groups.grouplist.Group
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.android_project_msd.groups.data.GroupsRepository
import com.example.android_project_msd.groups.data.GroupDetailsStore
import com.example.android_project_msd.groups.data.DebtCalculator
import com.example.android_project_msd.groups.data.Expense
import com.example.android_project_msd.groups.data.Settlement

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
    private val _ui = MutableStateFlow(GroupDetailUiState())
    val ui = _ui.asStateFlow()
    private val currentUserName = "You"

    fun loadGroup(groupId: String) {
        val repoGroup = GroupsRepository.groups.value.firstOrNull { it.id == groupId }
        val savedMembers = GroupDetailsStore.getMembers(groupId)

        if (repoGroup != null) {
            val members = listOf(
                GroupMember(id = "you", name = currentUserName, email = "you@email.com", balance = 0.0)
            ) + savedMembers.mapIndexed { idx, m ->
                GroupMember(id = "m$idx", name = m.name, email = m.email, balance = 0.0)
            }

            _ui.value = GroupDetailUiState(
                group = repoGroup.copy(memberCount = members.size),
                members = members,
                expenses = emptyList(),
                userBalance = 0.0,
                settlements = emptyList(),
                isDebtsSettled = true
            )
            return
        }

        // If group not found, show empty state
        _ui.value = GroupDetailUiState(
            group = null,
            members = emptyList(),
            expenses = emptyList(),
            userBalance = 0.0,
            settlements = emptyList(),
            isDebtsSettled = true
        )
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
    }

    fun addMember(name: String, email: String) {
        val newMember = GroupMember(
            id = System.currentTimeMillis().toString(),
            name = name,
            email = email,
            balance = 0.0
        )

        val currentMembers = _ui.value.members.toMutableList()
        currentMembers.add(newMember)

        _ui.value = _ui.value.copy(
            members = currentMembers,
            group = _ui.value.group?.copy(memberCount = currentMembers.size)
        )
        recalcDebts()
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
}