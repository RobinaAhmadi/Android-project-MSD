package com.example.android_project_msd.groups.data

import com.example.android_project_msd.groups.grouplist.Group
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object GroupsRepository {
    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups = _groups.asStateFlow()

    init {
        _groups.value = listOf(
            Group(id = "1", name = "Weekend Trip", description = "Barcelona 2025", memberCount = 4, balance = 250.50),
            Group(id = "2", name = "Apartment", description = "Monthly expenses", memberCount = 3, balance = -120.00)
        )
    }

    fun addGroup(name: String, description: String) {
        val newGroup = Group(
            id = System.currentTimeMillis().toString(),
            name = name,
            description = description,
            memberCount = 1,
            balance = 0.0
        )
        _groups.value = listOf(newGroup) + _groups.value
    }

    fun removeGroup(id: String) {
        _groups.value = _groups.value.filterNot { it.id == id }
    }
}

