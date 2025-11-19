package com.example.android_project_msd.groups.groupsettings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_project_msd.data.GroupRepository
import com.example.android_project_msd.data.UserRepository
import com.example.android_project_msd.data.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GroupSettingsMember(
    val id: String,
    val name: String,
    val email: String
)

data class GroupSettingsGroup(
    val id: String,
    val name: String,
    val description: String,
    val ownerId: String
)

data class GroupSettingsUiState(
    val group: GroupSettingsGroup? = null,
    val members: List<GroupSettingsMember> = emptyList(),
    val isOwner: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

class GroupSettingsViewModel : ViewModel() {
    private val groupRepository = GroupRepository()
    private val userRepository = UserRepository()
    private val _ui = MutableStateFlow(GroupSettingsUiState())
    val ui = _ui.asStateFlow()

    private var currentGroupId: String? = null

    fun loadGroup(groupId: String) {
        currentGroupId = groupId

        viewModelScope.launch {
            _ui.value = _ui.value.copy(isLoading = true, error = null)

            try {
                val currentUserId = UserSession.currentUserId
                if (currentUserId == null) {
                    _ui.value = _ui.value.copy(
                        isLoading = false,
                        error = "You must be logged in"
                    )
                    return@launch
                }

                // Get group from Firebase
                val firebaseGroup = groupRepository.getGroupById(groupId)
                if (firebaseGroup == null) {
                    _ui.value = _ui.value.copy(
                        isLoading = false,
                        error = "Group not found"
                    )
                    return@launch
                }

                // Convert to UI model
                val group = GroupSettingsGroup(
                    id = firebaseGroup.id,
                    name = firebaseGroup.name,
                    description = firebaseGroup.description,
                    ownerId = firebaseGroup.ownerId
                )

                // Load members
                val members = mutableListOf<GroupSettingsMember>()
                for (memberId in firebaseGroup.members) {
                    val user = userRepository.getUserById(memberId)
                    if (user != null) {
                        members.add(
                            GroupSettingsMember(
                                id = memberId,
                                name = user.name,
                                email = user.email
                            )
                        )
                    }
                }

                val isOwner = firebaseGroup.ownerId == currentUserId

                _ui.value = _ui.value.copy(
                    group = group,
                    members = members,
                    isOwner = isOwner,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                Log.e("GroupSettingsVM", "Error loading group: ${e.message}", e)
                _ui.value = _ui.value.copy(
                    isLoading = false,
                    error = "Failed to load group: ${e.message}"
                )
            }
        }
    }

    fun updateGroup(newName: String, newDescription: String) {
        val groupId = currentGroupId ?: return

        viewModelScope.launch {
            try {
                val result = groupRepository.updateGroup(
                    groupId = groupId,
                    name = newName.trim(),
                    description = newDescription.trim()
                )

                result.fold(
                    onSuccess = {
                        // Update local state
                        _ui.value = _ui.value.copy(
                            group = _ui.value.group?.copy(
                                name = newName.trim(),
                                description = newDescription.trim()
                            )
                        )
                        Log.d("GroupSettingsVM", "Group updated successfully")
                    },
                    onFailure = { error ->
                        Log.e("GroupSettingsVM", "Error updating group: ${error.message}")
                        _ui.value = _ui.value.copy(
                            error = "Failed to update group: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e("GroupSettingsVM", "Error updating group: ${e.message}", e)
                _ui.value = _ui.value.copy(
                    error = "Failed to update group: ${e.message}"
                )
            }
        }
    }

    fun removeMember(memberId: String) {
        val groupId = currentGroupId ?: return

        viewModelScope.launch {
            try {
                val result = groupRepository.removeMemberFromGroup(
                    groupId = groupId,
                    userId = memberId
                )

                result.fold(
                    onSuccess = {
                        // Remove member from local state
                        _ui.value = _ui.value.copy(
                            members = _ui.value.members.filter { it.id != memberId }
                        )
                        Log.d("GroupSettingsVM", "Member removed successfully")
                    },
                    onFailure = { error ->
                        Log.e("GroupSettingsVM", "Error removing member: ${error.message}")
                        _ui.value = _ui.value.copy(
                            error = "Failed to remove member: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e("GroupSettingsVM", "Error removing member: ${e.message}", e)
                _ui.value = _ui.value.copy(
                    error = "Failed to remove member: ${e.message}"
                )
            }
        }
    }

    fun deleteGroup(onSuccess: () -> Unit) {
        val groupId = currentGroupId ?: return
        val userId = UserSession.currentUserId ?: return

        viewModelScope.launch {
            try {
                val result = groupRepository.deleteGroup(
                    groupId = groupId,
                    userId = userId
                )

                result.fold(
                    onSuccess = {
                        Log.d("GroupSettingsVM", "Group deleted successfully")
                        onSuccess()
                    },
                    onFailure = { error ->
                        Log.e("GroupSettingsVM", "Error deleting group: ${error.message}")
                        _ui.value = _ui.value.copy(
                            error = "Failed to delete group: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e("GroupSettingsVM", "Error deleting group: ${e.message}", e)
                _ui.value = _ui.value.copy(
                    error = "Failed to delete group: ${e.message}"
                )
            }
        }
    }
}