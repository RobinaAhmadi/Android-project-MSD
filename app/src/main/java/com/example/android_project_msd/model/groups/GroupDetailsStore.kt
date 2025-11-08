package com.example.android_project_msd.model.groups

data class SimpleMember(val name: String, val email: String)

object GroupDetailsStore {
    private val membersByGroup = mutableMapOf<String, List<SimpleMember>>()

    fun saveInitialMembers(groupId: String, members: List<SimpleMember>) {
        membersByGroup[groupId] = members
    }

    fun getMembers(groupId: String): List<SimpleMember> = membersByGroup[groupId] ?: emptyList()
}
