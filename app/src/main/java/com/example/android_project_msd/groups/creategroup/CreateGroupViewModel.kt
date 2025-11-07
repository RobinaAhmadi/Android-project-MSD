package com.example.android_project_msd.groups.creategroup

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.android_project_msd.groups.groupdetail.GroupDetailActivity

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
    val showAddMemberDialog: Boolean = false
) {
    val canCreate: Boolean
        get() = groupName.isNotBlank()
}

class CreateGroupFullViewModel : ViewModel() {
    private val _ui = MutableStateFlow(CreateGroupFullUiState())
    val ui = _ui.asStateFlow()

    fun update(block: (CreateGroupFullUiState) -> CreateGroupFullUiState) {
        _ui.value = block(_ui.value)
    }

    fun addMember(name: String, email: String) {
        val newMember = CreateGroupMember(
            id = System.currentTimeMillis().toString(),
            name = name,
            email = email
        )

        val currentMembers = _ui.value.members.toMutableList()
        currentMembers.add(newMember)

        _ui.value = _ui.value.copy(members = currentMembers)
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

    fun createGroup(onSuccess: () -> Unit) {
        if (_ui.value.canCreate) {
            com.example.android_project_msd.groups.data.GroupsRepository.addGroup(
                name = _ui.value.groupName,
                description = _ui.value.description
            )
            onSuccess()
        }
    }
}
