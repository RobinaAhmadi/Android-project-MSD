package com.example.android_project_msd.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_project_msd.data.UserRepository
import com.example.android_project_msd.data.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository = UserRepository()
    private val _uiState = MutableStateFlow(ProfileState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val userId = UserSession.currentUserId
            if (userId != null) {
                val user = userRepository.getUserById(userId)
                if (user != null) {
                    _uiState.update {
                        it.copy(
                            name = user.name,
                            phone = user.phoneNumber,
                            email = user.email,
                            alertOnNewPayment = true, // Disse kan tilføjes til User-tabellen senere
                            alertOnMissingPayment = false,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }

    fun onPhoneChange(newPhone: String) {
        _uiState.update { it.copy(phone = newPhone) }
    }

    fun onEmailChange(newEmail: String) {
        _uiState.update { it.copy(email = newEmail) }
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.update { it.copy(newPassword = newPassword) }
    }

    fun onNewPaymentToggle(isEnabled: Boolean) {
        _uiState.update { it.copy(alertOnNewPayment = isEnabled) }
    }

    fun onMissingPaymentToggle(isEnabled: Boolean) {
        _uiState.update { it.copy(alertOnMissingPayment = isEnabled) }
    }

    fun onToggleEditMode() {
        val isCurrentlyEditing = _uiState.value.isEditing
        if (isCurrentlyEditing) {
            saveProfile()
        } else {
            _uiState.update { it.copy(isEditing = true) }
        }
    }

    private fun saveProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val userId = UserSession.currentUserId
            if (userId != null) {
                val currentUser = userRepository.getUserById(userId)
                if (currentUser != null) {
                    val state = _uiState.value
                    val updatedUser = currentUser.copy(
                        name = state.name,
                        phoneNumber = state.phone,
                        email = state.email
                    )
                    userRepository.updateUser(updatedUser)

                    // Note: Password updates should be done through Firebase Auth
                    // If you want to update password, you need to use:
                    // FirebaseAuth.getInstance().currentUser?.updatePassword(newPassword)
                }
            }

            _uiState.update {
                it.copy(
                    isEditing = false,
                    isLoading = false,
                    newPassword = ""
                )
            }
        }
    }
}



