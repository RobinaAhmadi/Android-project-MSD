package com.example.android_project_msd.notifications

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_project_msd.data.GroupInvitation
import com.example.android_project_msd.data.InvitationStatus
import com.example.android_project_msd.data.NotificationRepository
import com.example.android_project_msd.data.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationsViewModel(application: Application) : AndroidViewModel(application) {
    private val notificationRepository = NotificationRepository()

    private val _invitations = MutableStateFlow<List<GroupInvitation>>(emptyList())
    val invitations = _invitations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadInvitations() {
        val userId = UserSession.currentUserId
        if (userId == null) {
            Log.w("NotificationsVM", "No user logged in")
            _isLoading.value = false
            _error.value = "No user logged in"
            return
        }

        Log.d("NotificationsVM", "Loading invitations for user: $userId")

        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Use real-time listener
                notificationRepository.getMyInvitationsFlow(userId).collect { invitationsList ->
                    Log.d("NotificationsVM", "Received ${invitationsList.size} invitations")
                    _invitations.value = invitationsList
                    _isLoading.value = false
                    _error.value = null
                }
            } catch (e: Exception) {
                Log.e("NotificationsVM", "Error loading invitations: ${e.message}", e)
                _isLoading.value = false
                _error.value = "Failed to load invitations: ${e.message}"
            }
        }
    }

    fun acceptInvitation(invitationId: String) {
        val userId = UserSession.currentUserId
        if (userId == null) {
            Log.w("NotificationsVM", "No user logged in")
            return
        }

        viewModelScope.launch {
            Log.d("NotificationsVM", "Accepting invitation: $invitationId")

            val result = notificationRepository.respondToInvitation(
                invitationId = invitationId,
                response = InvitationStatus.ACCEPTED,
                userId = userId
            )

            result.fold(
                onSuccess = {
                    Log.d("NotificationsVM", "Invitation accepted successfully")
                    // Invitation will be removed from list via real-time listener
                },
                onFailure = { error ->
                    Log.e("NotificationsVM", "Error accepting invitation: ${error.message}")
                    _error.value = "Failed to accept invitation: ${error.message}"
                }
            )
        }
    }

    fun declineInvitation(invitationId: String) {
        val userId = UserSession.currentUserId
        if (userId == null) {
            Log.w("NotificationsVM", "No user logged in")
            return
        }

        viewModelScope.launch {
            Log.d("NotificationsVM", "Declining invitation: $invitationId")

            val result = notificationRepository.respondToInvitation(
                invitationId = invitationId,
                response = InvitationStatus.DECLINED,
                userId = userId
            )

            result.fold(
                onSuccess = {
                    Log.d("NotificationsVM", "Invitation declined successfully")
                    // Invitation will be removed from list via real-time listener
                },
                onFailure = { error ->
                    Log.e("NotificationsVM", "Error declining invitation: ${error.message}")
                    _error.value = "Failed to decline invitation: ${error.message}"
                }
            )
        }
    }
}

