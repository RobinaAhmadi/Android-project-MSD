package com.example.android_project_msd.controller.createprofile

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.android_project_msd.model.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    val showPassword: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null
) {
    val isEmailValid: Boolean get() = email.contains("@")
    val isPasswordValid: Boolean get() = password.length >= 6
    val isExpiryValid: Boolean get() = expiry.length >= 4
    val isCvvValid: Boolean get() = cvv.length in 3..4
    val canSubmit: Boolean
        get() = username.isNotBlank() && isEmailValid && isPasswordValid
}

class CreateProfileViewModel(
    private val repository: AuthRepository
) : ViewModel() {
    private val _ui = MutableStateFlow(CreateProfileUiState())
    val ui = _ui.asStateFlow()

    fun update(block: (CreateProfileUiState) -> CreateProfileUiState) {
        _ui.update(block)
    }

    fun toggleShowPassword() {
        _ui.update { it.copy(showPassword = !it.showPassword) }
    }

    fun submit(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val current = _ui.value
        if (!current.canSubmit) {
            val msg = "Please fill all required fields correctly."
            _ui.update { it.copy(error = msg) }
            onError(msg)
            return
        }

        viewModelScope.launch {
            _ui.update { it.copy(isSubmitting = true, error = null) }
            repository.register(
                email = current.email,
                password = current.password,
                displayName = current.username
            ).onSuccess {
                _ui.update { it.copy(isSubmitting = false, error = null) }
                onSuccess()
            }.onFailure { throwable ->
                val msg = throwable.message ?: "Unable to create account"
                _ui.update { it.copy(isSubmitting = false, error = msg) }
                onError(msg)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                CreateProfileViewModel(AuthRepository.getInstance(app))
            }
        }
    }
}
