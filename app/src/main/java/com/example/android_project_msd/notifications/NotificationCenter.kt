package com.example.android_project_msd.notifications

import android.util.Log
import com.example.android_project_msd.data.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Locale

object NotificationCenter {
    private const val TAG = "NotificationCenter"
    private val notificationRepository = NotificationRepository()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    private var remoteJob: Job? = null
    private var remoteUserId: String? = null

    val notifications = _notifications.asStateFlow()

    private fun push(notification: AppNotification) {
        _notifications.value = listOf(notification) + _notifications.value
    }

    fun startSync(userId: String) {
        if (userId.isBlank()) return
        if (remoteUserId == userId && remoteJob?.isActive == true) return

        remoteJob?.cancel()
        remoteUserId = userId
        remoteJob = scope.launch {
            notificationRepository.observeUserNotifications(userId).collect { remote ->
                _notifications.value = remote
            }
        }
    }

    fun stopSync() {
        remoteJob?.cancel()
        remoteJob = null
        remoteUserId = null
        _notifications.value = emptyList()
    }

    fun clearNotifications() {
        _notifications.value = emptyList()
        val userId = remoteUserId ?: return
        scope.launch {
            notificationRepository.clearUserNotifications(userId)
                .onFailure { error ->
                    Log.e(TAG, "Failed to clear notifications: ${error.message}", error)
                }
        }
    }

    fun notifyExpenseAdded(
        groupId: String?,
        groupName: String?,
        description: String,
        amount: Double,
        paidBy: String,
        splitAmong: List<String>
    ): AppNotification {
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

        return AppNotification(
            groupId = groupId,
            type = NotificationType.EXPENSE_ADDED,
            title = title,
            line1 = line1,
            line2 = line2,
            youOweLine = youOweLine,
            recipients = splitAmong
        ).also(::push)
    }

    fun notifyPaymentRecorded(
        groupId: String?,
        groupName: String?,
        from: String,
        to: String,
        amount: Double,
    ): AppNotification {
        val title = "Payment recorded in ${groupName ?: "group"}"
        val line1 = "$from paid $amount DKK to $to"

        return AppNotification(
            groupId = groupId,
            type = NotificationType.PAYMENT_RECORDED,
            title = title,
            line1 = line1,
            recipients = listOf(from, to)
        ).also(::push)
    }

    fun sendReminder(
        groupId: String?,
        groupName: String?,
        from: String,
        to: String,
        amount: Double,
    ): AppNotification {
        val title = "Payment reminder"
        val line1 = "$from reminds $to to pay $amount DKK in ${groupName ?: "the group"}"

        return AppNotification(
            groupId = groupId,
            type = NotificationType.REMINDER,
            title = title,
            line1 = line1,
            recipients = listOf(to)
        ).also(::push)
    }
}
