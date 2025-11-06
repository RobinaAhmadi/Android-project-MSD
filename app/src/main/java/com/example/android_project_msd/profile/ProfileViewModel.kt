package com.example.android_project_msd.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(1000)
            _uiState.update {
                it.copy(
                    name = "Rosin B",
                    phone = "24 24 24 24",
                    email = "nunjugugu@",
                    alertOnNewPayment = true,
                    alertOnMissingPayment = false,
                    isLoading = false
                )
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
            delay(1500)


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