package com.example.android_project_msd.data

import com.google.firebase.Timestamp

enum class InvitationStatus {
    PENDING,
    ACCEPTED,
    DECLINED
}

data class GroupInvitation(
    val id: String = "",
    val type: String = "GROUP_INVITATION",
    val fromUserId: String = "",
    val fromUserName: String = "",
    val fromUserEmail: String = "",
    val toUserId: String = "",
    val toUserEmail: String = "",
    val groupId: String = "",
    val groupName: String = "",
    val groupDescription: String = "",
    val status: String = InvitationStatus.PENDING.name,
    val message: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val respondedAt: Timestamp? = null
)

