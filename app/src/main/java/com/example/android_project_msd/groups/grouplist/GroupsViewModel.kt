package com.example.android_project_msd.groups.grouplist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_project_msd.data.GroupRepository
import com.example.android_project_msd.data.ExpenseRepository
import com.example.android_project_msd.data.UserSession
import com.example.android_project_msd.groups.data.DebtCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class Group(
    val id: String,
    val name: String,
    val description: String,
    val memberCount: Int,
    val balance: Double = 0.0 // Positive = you're owed, Negative = you owe
)

data class GroupsUiState(
    val groups: List<Group> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class GroupsViewModel : ViewModel() {
    private val groupRepository = GroupRepository()
    private val expenseRepository = ExpenseRepository()
    private val _ui = MutableStateFlow(GroupsUiState())
    val ui = _ui.asStateFlow()

    init {
        loadGroups()
    }

    fun loadGroups() {
        val userId = UserSession.currentUserId
        if (userId == null) {
            Log.w("GroupsVM", "No user logged in")
            return
        }

        viewModelScope.launch {
            _ui.value = _ui.value.copy(isLoading = true)
            Log.d("GroupsVM", "Loading groups for user: $userId")

            try {
                val firebaseGroups = groupRepository.getGroupsForUser(userId)
                Log.d("GroupsVM", "Loaded ${firebaseGroups.size} groups")

                val groups = firebaseGroups.map { firebaseGroup ->
                    val expenseResult = expenseRepository.getExpensesOnce(firebaseGroup.id)
                    val expenses = expenseResult.getOrElse { emptyList() }

                    val memberNames = firebaseGroup.memberNames
                    val balances = DebtCalculator.calculateBalances(
                        expenses = expenses,
                        members = memberNames
                    )

                    val currentUserName = getCurrentUserName(firebaseGroup.members, memberNames, userId)
                    val balance = currentUserName?.let { balances[it] } ?: 0.0

                    Group(
                        id = firebaseGroup.id,
                        name = firebaseGroup.name,
                        description = firebaseGroup.description,
                        memberCount = firebaseGroup.members.size,
                        balance = balance
                    )
                }

                _ui.value = _ui.value.copy(
                    groups = groups,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                Log.e("GroupsVM", "Error loading groups: ${e.message}", e)
                _ui.value = _ui.value.copy(
                    isLoading = false,
                    error = "Failed to load groups: ${e.message}"
                )
            }
        }
    }

    fun createGroup(name: String, description: String) {
        // This is handled by CreateGroupViewModel now
    }

    fun deleteGroup(groupId: String) {
        val userId = UserSession.currentUserId ?: return

        viewModelScope.launch {
            groupRepository.deleteGroup(groupId, userId).fold(
                onSuccess = {
                    loadGroups() // Reload after delete
                },
                onFailure = { error ->
                    Log.e("GroupsVM", "Error deleting group: ${error.message}")
                }
            )
        }
    }

    private fun getCurrentUserName(
        memberIds: List<String>,
        memberNames: List<String>,
        userId: String
    ): String? {
        val index = memberIds.indexOf(userId)
        return if (index != -1 && index < memberNames.size) {
            memberNames[index]
        } else {
            null
        }
    }
}
