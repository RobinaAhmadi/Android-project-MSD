package com.example.android_project_msd.groups.grouplist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_project_msd.data.GroupRepository
import com.example.android_project_msd.data.UserSession
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
                    Group(
                        id = firebaseGroup.id,
                        name = firebaseGroup.name,
                        description = firebaseGroup.description,
                        memberCount = firebaseGroup.members.size,
                        balance = 0.0 // TODO: Calculate from expenses
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
}
