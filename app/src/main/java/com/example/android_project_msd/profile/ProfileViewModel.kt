package com.example.android_project_msd.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Arver fra ViewModel for at overleve konfigurationsændringer (som rotation)
class ProfileViewModel : ViewModel() {

    // _uiState er privat og kan ændres (Mutable)
    // Dette er den ENESTE kilde til sandhed
    private val _uiState = MutableStateFlow(ProfileState())

    // uiState er offentlig og kan kun læses (Immutable)
    // Vores UI vil lytte til denne
    val uiState = _uiState.asStateFlow()

    init {
        // Kør, så snart ViewModel oprettes
        loadUserProfile()
    }

    // Simulerer at hente brugerdata fra en database eller API
    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(1000) // Simulerer netværkskald
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

    // --- Disse funktioner kaldes af UI'en (Events flyder op) ---

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
            // Brugeren trykkede "Confirm", så vi gemmer
            saveProfile()
        } else {
            // Brugeren trykkede "Edit"
            _uiState.update { it.copy(isEditing = true) }
        }
    }

    private fun saveProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(1500) // Simulerer at gemme til database

            // Når det er gemt, stopper vi "Edit" mode og fjerner kodeordet fra state
            _uiState.update {
                it.copy(
                    isEditing = false,
                    isLoading = false,
                    newPassword = "" // Ryd kodeordsfelt
                )
            }
            // Her ville du også vise en "Gemt!"-besked (f.eks. med en Snackbar)
        }
    }
}