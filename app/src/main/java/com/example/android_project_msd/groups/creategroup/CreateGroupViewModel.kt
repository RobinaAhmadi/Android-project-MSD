package com.example.android_project_msd.groups.creategroup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.android_project_msd.data.GroupRepository
import com.example.android_project_msd.data.NotificationRepository
import com.example.android_project_msd.data.UserSession
import android.util.Log

data class CreateGroupMember(
    val id: String,
    val name: String,
    val email: String
)

data class CreateGroupFullUiState(
    val groupName: String = "",
    val description: String = "",
    val members: List<CreateGroupMember> = emptyList(),
    val currency: String = "DKK",
    val showAddMemberDialog: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val canCreate: Boolean
        get() = groupName.isNotBlank() && !isLoading
}

class CreateGroupFullViewModel : ViewModel() {
    private val groupRepository = GroupRepository()
    private val notificationRepository = NotificationRepository()
    private val _ui = MutableStateFlow(CreateGroupFullUiState())
    val ui = _ui.asStateFlow()

    fun update(block: (CreateGroupFullUiState) -> CreateGroupFullUiState) {
        _ui.value = block(_ui.value)
    }

    fun addMember(email: String) {
        val trimmedEmail = email.trim()

        // Check if already added
        if (_ui.value.members.any { it.email.equals(trimmedEmail, ignoreCase = true) }) {
            _ui.value = _ui.value.copy(error = "Member already added")
            return
        }

        val newMember = CreateGroupMember(
            id = System.currentTimeMillis().toString(),
            name = trimmedEmail, // Use email as name for now, Firebase will update it
            email = trimmedEmail
        )

        val currentMembers = _ui.value.members.toMutableList()
        currentMembers.add(newMember)

        _ui.value = _ui.value.copy(members = currentMembers, error = null)
    }

    fun removeMember(memberId: String) {
        val updatedMembers = _ui.value.members.filter { it.id != memberId }
        _ui.value = _ui.value.copy(members = updatedMembers)
    }

    fun showAddMemberDialog() {
        _ui.value = _ui.value.copy(showAddMemberDialog = true)
    }

    fun hideAddMemberDialog() {
        _ui.value = _ui.value.copy(showAddMemberDialog = false)
    }

    fun createGroup(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        if (!_ui.value.canCreate) {
            onError("Please enter a group name")
            return
        }

        val ownerId = UserSession.currentUserId
        if (ownerId == null) {
            onError("You must be logged in to create a group")
            return
        }

        viewModelScope.launch {
            _ui.value = _ui.value.copy(isLoading = true, error = null)

            try {
                // Create group with owner
                val result = groupRepository.createGroup(
                    name = _ui.value.groupName.trim(),
                    description = _ui.value.description.trim(),
                    ownerId = ownerId
                )

                result.fold(
                    onSuccess = { group ->
                        Log.d("CreateGroupVM", "Group created successfully: ${group.id}")

                        // Send invitations to members instead of adding them directly
                        if (_ui.value.members.isNotEmpty()) {
                            Log.d("CreateGroupVM", "Sending ${_ui.value.members.size} invitations")

                            viewModelScope.launch {
                                for (member in _ui.value.members) {
                                    Log.d("CreateGroupVM", "Sending invitation to ${member.email}")

                                    notificationRepository.createGroupInvitation(
                                        fromUserId = ownerId,
                                        toEmail = member.email,
                                        groupId = group.id,
                                        groupName = group.name,
                                        groupDescription = group.description
                                    ).fold(
                                        onSuccess = { invitation ->
                                            Log.d("CreateGroupVM", "Invitation sent successfully to ${member.email}, ID: ${invitation.id}")
                                        },
                                        onFailure = { e ->
                                            Log.e("CreateGroupVM", "Failed to send invitation to ${member.email}: ${e.message}", e)
                                        }
                                    )
                                }
                            }
                        } else {
                            Log.d("CreateGroupVM", "No members to invite")
                        }

                        _ui.value = _ui.value.copy(isLoading = false, error = null)
                        onSuccess(group.id)
                    },
                    onFailure = { error ->
                        val msg = "Error creating group: ${error.message}"
                        _ui.value = _ui.value.copy(isLoading = false, error = msg)
                        onError(msg)
                    }
                )
            } catch (e: Exception) {
                val msg = "Error creating group: ${e.message}"
                _ui.value = _ui.value.copy(isLoading = false, error = msg)
                onError(msg)
            }
        }
    }
}


