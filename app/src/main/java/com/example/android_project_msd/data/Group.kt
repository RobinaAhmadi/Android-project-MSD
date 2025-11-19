package com.example.android_project_msd.data

import com.google.firebase.Timestamp

data class Group(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val ownerId: String = "", // User ID of the creator
    val ownerEmail: String = "",
    val ownerName: String = "",
    val members: List<String> = emptyList(), // List of user IDs
    val memberEmails: List<String> = emptyList(), // List of emails for easy display
    val memberNames: List<String> = emptyList(), // List of names for easy display
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class GroupMember(
    val userId: String = "",
    val email: String = "",
    val name: String = ""
)

