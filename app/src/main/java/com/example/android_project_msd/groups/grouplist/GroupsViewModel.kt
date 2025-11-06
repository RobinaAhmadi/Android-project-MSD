package com.example.android_project_msd.groups.grouplist

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Group(
    val id: String,
    val name: String,
    val description: String,
    val memberCount: Int,
    val balance: Double // Positive = you're owed, Negative = you owe
)

data class GroupsUiState(
    val groups: List<Group> = emptyList()
)

class GroupsViewModel : ViewModel() {
    private val _ui = MutableStateFlow(GroupsUiState())
    val ui = _ui.asStateFlow()

    init {
        // Load with some dummy data for demonstration
        loadDummyGroups()
    }

    private fun loadDummyGroups() {
        // You can remove this later when you have real data
        _ui.value = GroupsUiState(
            groups = listOf(
                Group(
                    id = "1",
                    name = "Weekend Trip",
                    description = "Barcelona 2025",
                    memberCount = 4,
                    balance = 250.50
                ),
                Group(
                    id = "2",
                    name = "Apartment",
                    description = "Monthly expenses",
                    memberCount = 3,
                    balance = -120.00
                ),
                Group(
                    id = "3",
                    name = "Study Group",
                    description = "Coffee and snacks",
                    memberCount = 5,
                    balance = 45.25
                )
            )
        )
    }

    fun createGroup(name: String, description: String) {
        val newGroup = Group(
            id = System.currentTimeMillis().toString(),
            name = name,
            description = description,
            memberCount = 1, // Just the creator initially
            balance = 0.0
        )

        val currentGroups = _ui.value.groups.toMutableList()
        currentGroups.add(0, newGroup) // Add to the top

        _ui.value = _ui.value.copy(groups = currentGroups)
    }

    fun deleteGroup(groupId: String) {
        val updatedGroups = _ui.value.groups.filter { it.id != groupId }
        _ui.value = _ui.value.copy(groups = updatedGroups)
    }
}