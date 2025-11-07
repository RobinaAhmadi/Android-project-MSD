package com.example.android_project_msd.groups.grouplist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_project_msd.groups.data.GroupsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
        viewModelScope.launch {
            GroupsRepository.groups.collect { list ->
                _ui.value = GroupsUiState(groups = list)
            }
        }
    }

    fun createGroup(name: String, description: String) {
        GroupsRepository.addGroup(name, description)
    }

    fun deleteGroup(groupId: String) {
        GroupsRepository.removeGroup(groupId)
    }
}
