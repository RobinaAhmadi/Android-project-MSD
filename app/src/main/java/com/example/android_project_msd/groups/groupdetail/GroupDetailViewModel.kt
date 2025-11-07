package com.example.android_project_msd.groups.groupdetail

import androidx.lifecycle.ViewModel
import com.example.android_project_msd.groups.grouplist.Group
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.android_project_msd.groups.data.GroupsRepository
import com.example.android_project_msd.groups.data.GroupDetailsStore

data class GroupMember(
    val id: String,
    val name: String,
    val email: String,
    val balance: Double // Positive = owed money, Negative = owes money
)

data class Expense(
    val id: String,
    val description: String,
    val amount: Double,
    val paidBy: String,
    val splitAmong: List<String>,
    val date: String
)

data class GroupDetailUiState(
    val group: Group? = null,
    val members: List<GroupMember> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val userBalance: Double = 0.0
)

class GroupDetailViewModel : ViewModel() {
    private val _ui = MutableStateFlow(GroupDetailUiState())
    val ui = _ui.asStateFlow()
    private val currentUserName = "You"

    fun loadGroup(groupId: String) {
        val repoGroup = GroupsRepository.groups.value.firstOrNull { it.id == groupId }
        val savedMembers = GroupDetailsStore.getMembers(groupId)

        if (repoGroup != null && savedMembers.isNotEmpty()) {
            val members = listOf(
                GroupMember(id = "you", name = currentUserName, email = "you@email.com", balance = 0.0)
            ) + savedMembers.mapIndexed { idx, m ->
                GroupMember(id = "m$idx", name = m.name, email = m.email, balance = 0.0)
            }
            _ui.value = GroupDetailUiState(
                group = repoGroup.copy(memberCount = members.size),
                members = members,
                expenses = emptyList(),
                userBalance = 0.0
            )
            return
        }

        // Fallback demo if not found or no saved members
        _ui.value = GroupDetailUiState(
            group = repoGroup ?: Group(groupId, "Weekend Trip", "Barcelona 2025", 4, 0.0),
            members = listOf(
                GroupMember("1", currentUserName, "you@email.com", 0.0),
                GroupMember("2", "Sarah Johnson", "sarah@email.com", 0.0),
                GroupMember("3", "Mike Chen", "mike@email.com", 0.0),
                GroupMember("4", "Emma Wilson", "emma@email.com", 0.0)
            ),
            expenses = emptyList(),
            userBalance = 0.0
        )
    }

    fun addExpense(description: String, amount: Double, paidBy: String, splitAmong: List<String>) {
        val newExpense = Expense(
            id = System.currentTimeMillis().toString(),
            description = description,
            amount = amount,
            paidBy = paidBy,
            splitAmong = splitAmong,
            date = "Today"
        )

        val currentExpenses = _ui.value.expenses.toMutableList()
        currentExpenses.add(0, newExpense)

        _ui.value = _ui.value.copy(expenses = currentExpenses)
        recalcBalances()
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
        recalcBalances()
    }

    fun recordPayment(fromName: String, toName: String, amount: Double) {
        if (amount <= 0) return
        val payment = Expense(
            id = System.currentTimeMillis().toString(),
            description = "Payment",
            amount = amount,
            paidBy = toName,
            splitAmong = listOf(fromName),
            date = "Today"
        )
        val list = _ui.value.expenses.toMutableList()
        list.add(0, payment)
        _ui.value = _ui.value.copy(expenses = list)
        recalcBalances()
    }

    private fun recalcBalances() {
        val members = _ui.value.members
        if (members.isEmpty()) return
        val nameToBal = mutableMapOf<String, Double>().apply { members.forEach { this[it.name] = 0.0 } }
        _ui.value.expenses.forEach { exp ->
            val participants = if (exp.splitAmong.isEmpty()) members.map { it.name } else exp.splitAmong
            val share = if (participants.isNotEmpty()) exp.amount / participants.size else 0.0
            participants.forEach { p -> nameToBal[p] = (nameToBal[p] ?: 0.0) - share }
            nameToBal[exp.paidBy] = (nameToBal[exp.paidBy] ?: 0.0) + exp.amount
        }
        val newMembers = members.map { m -> m.copy(balance = kotlin.math.round((nameToBal[m.name] ?: 0.0) * 100) / 100.0) }
        val userBal = nameToBal[currentUserName] ?: 0.0
        _ui.value = _ui.value.copy(members = newMembers, userBalance = kotlin.math.round(userBal * 100) / 100.0)
    }
}

