package com.example.android_project_msd.notifications

enum class NotificationType {
    EXPENSE_ADDED,
    PAYMENT_RECORDED,
    REMINDER,
    GROUP_CREATED
}

data class AppNotification(
    val id: String = System.currentTimeMillis().toString(),
    val groupId: String?,
    val type: NotificationType,
    val title: String,
    val line1: String,
    val line2: String? = null,
    val youOweLine: String? = null,
    val recipients: List<String>,
    val timestamp: Long = System.currentTimeMillis()
)
