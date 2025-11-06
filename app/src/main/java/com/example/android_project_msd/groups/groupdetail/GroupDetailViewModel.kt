package com.example.android_project_msd.groups.groupdetail

import androidx.lifecycle.ViewModel
import com.example.android_project_msd.groups.grouplist.Group
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

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

    fun loadGroup(groupId: String) {
        // Load dummy data for demonstration
        _ui.value = GroupDetailUiState(
            group = Group(
                id = groupId,
                name = "Weekend Trip",
                description = "Barcelona 2025",
                memberCount = 4,
                balance = 250.50
            ),
            members = listOf(
                GroupMember(
                    id = "1",
                    name = "You",
                    email = "you@email.com",
                    balance = 250.50
                ),
                GroupMember(
                    id = "2",
                    name = "Sarah Johnson",
                    email = "sarah@email.com",
                    balance = -80.0
                ),
                GroupMember(
                    id = "3",
                    name = "Mike Chen",
                    email = "mike@email.com",
                    balance = -120.50
                ),
                GroupMember(
                    id = "4",
                    name = "Emma Wilson",
                    email = "emma@email.com",
                    balance = -50.0
                )
            ),
            expenses = listOf(
                Expense(
                    id = "1",
                    description = "Hotel booking",
                    amount = 800.0,
                    paidBy = "You",
                    splitAmong = listOf("You", "Sarah Johnson", "Mike Chen", "Emma Wilson"),
                    date = "Nov 3, 2025"
                ),
                Expense(
                    id = "2",
                    description = "Dinner at restaurant",
                    amount = 320.0,
                    paidBy = "Sarah Johnson",
                    splitAmong = listOf("You", "Sarah Johnson", "Mike Chen", "Emma Wilson"),
                    date = "Nov 2, 2025"
                ),
                Expense(
                    id = "3",
                    description = "Taxi to airport",
                    amount = 150.0,
                    paidBy = "You",
                    splitAmong = listOf("You", "Mike Chen"),
                    date = "Nov 1, 2025"
                )
            ),
            userBalance = 250.50
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

        // TODO: Recalculate balances based on new expense
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
    }
}