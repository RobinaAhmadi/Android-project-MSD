package com.example.android_project_msd.notifications

import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Locale

object NotificationCenter {
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())

    private fun push(notification: AppNotification) {
        _notifications.value = listOf(notification) + _notifications.value
    }

    fun notifyExpenseAdded(
        groupId: String?,
        groupName: String?,
        description: String,
        amount: Double,
        paidBy: String,
        splitAmong: List<String>
    ) {
        val title = "New expense in ${groupName ?: "group"}"
        val line1 = "$paidBy added \"$description\" (${amount} DKK)"
        val line2 = "Split among: ${splitAmong.joinToString()}"

        val youOweLine: String? = if (splitAmong.contains("You") && paidBy != "You") {
            val perPerson = if (splitAmong.isNotEmpty()) amount / splitAmong.size else 0.0
            val formatted = String.format(Locale.getDefault(), "%.2f", perPerson)
            "You owe $paidBy: $formatted DKK"
        } else {
            null
        }

        push(
            AppNotification(
                groupId = groupId,
                type = NotificationType.EXPENSE_ADDED,
                title = title,
                line1 = line1,
                line2 = line2,
                youOweLine = youOweLine,
                recipients = splitAmong
            )
        )
    }

    fun notifyPaymentRecorded(
        groupId: String?,
        groupName: String?,
        from: String,
        to: String,
        amount: Double,
    ) {
        val title = "Payment recorded in ${groupName ?: "group"}"
        val line1 = "$from paid $amount DKK to $to"

        push(
            AppNotification(
                groupId = groupId,
                type = NotificationType.PAYMENT_RECORDED,
                title = title,
                line1 = line1,
                recipients = listOf(from, to)
            )
        )
    }

    fun sendReminder(
        groupId: String?,
        groupName: String?,
        from: String,
        to: String,
        amount: Double,
    ) {
        val title = "Payment reminder"
        val line1 = "$from reminds $to to pay $amount DKK in ${groupName ?: "the group"}"

        push(
            AppNotification(
                groupId = groupId,
                type = NotificationType.REMINDER,
                title = title,
                line1 = line1,
                recipients = listOf(to)
            )
        )
    }
}
