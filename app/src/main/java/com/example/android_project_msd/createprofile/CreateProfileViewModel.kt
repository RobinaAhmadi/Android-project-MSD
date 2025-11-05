package com.example.android_project_msd.createprofile

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CreateProfileUiState(
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val countryCode: String = "+45",
    val password: String = "",
    val cardHolder: String = "",
    val cardNumber: String = "",
    val expiry: String = "",
    val cvv: String = "",
    val showPassword: Boolean = false
) {
    val isEmailValid: Boolean get() = email.contains("@")
    val isPasswordValid: Boolean get() = password.length >= 6
    val isExpiryValid: Boolean get() = expiry.length >= 4
    val isCvvValid: Boolean get() = cvv.length in 3..4
    val canSubmit: Boolean
        get() = username.isNotBlank() && isEmailValid && isPasswordValid
}

class CreateProfileViewModel : ViewModel() {
    private val _ui = MutableStateFlow(CreateProfileUiState())
    val ui = _ui.asStateFlow()

    fun update(block: (CreateProfileUiState) -> CreateProfileUiState) {
        _ui.value = block(_ui.value)
    }

    fun toggleShowPassword() {
        _ui.value = _ui.value.copy(showPassword = !_ui.value.showPassword)
    }

    fun submit(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (_ui.value.canSubmit) {
            onSuccess()
        } else {
            onError("Please fill all required fields correctly.")
        }
    }
}
