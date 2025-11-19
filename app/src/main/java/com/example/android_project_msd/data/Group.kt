package com.example.android_project_msd.data

import com.google.firebase.Timestamp

data class Group(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val ownerId: String = "",
    val ownerEmail: String = "",
    val ownerName: String = "",
    val members: List<String> = emptyList(),
    val memberEmails: List<String> = emptyList(),
    val memberNames: List<String> = emptyList(),
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)



