package com.example.android_project_msd.profile

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_project_msd.data.UserSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val usersCollection = firestore.collection("users")

    private val _uiState = MutableStateFlow(ProfileState())
    val uiState = _uiState.asStateFlow()

    init {
        // Load profile when screen starts
        viewModelScope.launch {
            loadProfile()
        }
    }

    private suspend fun loadProfile() {
        val userId = UserSession.currentUserId ?: return
        _uiState.update { it.copy(isLoading = true) }
        try {
            val snapshot = usersCollection.document(userId).get().await()
            if (snapshot.exists()) {
                val name = snapshot.getString("name") ?: ""
                val email = snapshot.getString("email") ?: ""
                val phone = snapshot.getString("phoneNumber") ?: ""
                val alertNew = snapshot.getBoolean("alertOnNewPayment") ?: false
                val alertMissing = snapshot.getBoolean("alertOnMissingPayment") ?: false
                val imageUrl = snapshot.getString("profileImageUrl")

                _uiState.update {
                    it.copy(
                        name = name,
                        email = email,
                        phone = phone,
                        alertOnNewPayment = alertNew,
                        alertOnMissingPayment = alertMissing,
                        profileImageUrl = imageUrl,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Failed to load profile", e)
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    // ---- field updaters ----

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value) }
    }

    fun onPhoneChange(value: String) {
        _uiState.update { it.copy(phone = value) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(newPassword = value) }
    }

    fun onNewPaymentToggle(value: Boolean) {
        _uiState.update { it.copy(alertOnNewPayment = value) }
    }

    fun onMissingPaymentToggle(value: Boolean) {
        _uiState.update { it.copy(alertOnMissingPayment = value) }
    }

    // Toggle edit mode: if leaving edit -> save
    fun onToggleEditMode() {
        val editingNow = uiState.value.isEditing
        if (!editingNow) {
            _uiState.update { it.copy(isEditing = true) }
        } else {
            saveProfile()
        }
    }

    private fun saveProfile() {
        val userId = UserSession.currentUserId ?: return
        val current = uiState.value

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val data = hashMapOf(
                    "name" to current.name,
                    "email" to current.email,
                    "phoneNumber" to current.phone,
                    "alertOnNewPayment" to current.alertOnNewPayment,
                    "alertOnMissingPayment" to current.alertOnMissingPayment
                )

                usersCollection.document(userId)
                    .set(data, SetOptions.merge())
                    .await()

                // Optional: update FirebaseAuth password if changed
                if (current.newPassword.isNotBlank()) {
                    try {
                        auth.currentUser?.updatePassword(current.newPassword)?.await()
                    } catch (e: Exception) {
                        Log.e("ProfileViewModel", "Failed to update password", e)
                    }
                }

                _uiState.update {
                    it.copy(
                        isEditing = false,
                        isLoading = false,
                        newPassword = ""
                    )
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to save profile", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // Called when the user picks an image from gallery
    fun onProfileImageSelected(uri: Uri) {
        val userId = UserSession.currentUserId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingImage = true) }

            try {
                val ref = storage.reference.child("profile_pictures/$userId.jpg")
                ref.putFile(uri).await()
                val url = ref.downloadUrl.await().toString()

                usersCollection.document(userId)
                    .set(mapOf("profileImageUrl" to url), SetOptions.merge())
                    .await()

                _uiState.update {
                    it.copy(
                        profileImageUrl = url,
                        isUploadingImage = false
                    )
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to upload profile image", e)
                _uiState.update { it.copy(isUploadingImage = false) }
            }
        }
    }
}
