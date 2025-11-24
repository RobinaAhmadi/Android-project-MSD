package com.example.android_project_msd.createprofile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_project_msd.data.UserRepository
import com.example.android_project_msd.data.UserSession
import com.example.android_project_msd.notifications.NotificationCenter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CreateProfileUiState(
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val countryCode: String = "+45",
    val password: String = "",
    val showPassword: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    // Better email validation
    val isEmailValid: Boolean
        get() = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
            .matches(email.trim())

    val isPasswordValid: Boolean get() = password.length >= 6

    val hasPhoneDigits: Boolean
        get() = phone.filter { it.isDigit() }.isNotEmpty()

    val canSubmit: Boolean
        get() = username.isNotBlank() &&
                isEmailValid &&
                isPasswordValid &&
                hasPhoneDigits
}

class CreateProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository = UserRepository()
    private val _ui = MutableStateFlow(CreateProfileUiState())
    val ui = _ui.asStateFlow()

    fun update(block: (CreateProfileUiState) -> CreateProfileUiState) {
        _ui.value = block(_ui.value)
    }

    fun toggleShowPassword() {
        _ui.value = _ui.value.copy(showPassword = !_ui.value.showPassword)
    }

    fun submit(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val u = _ui.value

        when {
            u.username.isBlank() -> {
                val msg = "Please enter your name."
                _ui.value = u.copy(error = msg)
                onError(msg); return
            }
            !u.isEmailValid -> {
                val msg = "Please enter a valid email address."
                _ui.value = u.copy(error = msg)
                onError(msg); return
            }
            !u.hasPhoneDigits -> {
                val msg = "Please enter a valid phone number."
                _ui.value = u.copy(error = msg)
                onError(msg); return
            }
            !u.isPasswordValid -> {
                val msg = "Password must be at least 6 characters."
                _ui.value = u.copy(error = msg)
                onError(msg); return
            }
        }

        viewModelScope.launch {
            _ui.value = u.copy(isLoading = true, error = null)
            try {
                val trimmedEmail = u.email.trim()

                val result = userRepository.createUser(
                    email = trimmedEmail,
                    password = u.password,
                    name = u.username,
                    phoneNumber = u.countryCode + u.phone.filter { it.isDigit() },
                    cardHolderName = "",
                    cardNumber = "",
                    expiryDate = "",
                    cvv = ""
                )

                result.fold(
                    onSuccess = { user ->
                        UserSession.login(user.id, user.email)
                        NotificationCenter.startSync(user.id)
                        _ui.value = _ui.value.copy(isLoading = false, error = null)
                        onSuccess()
                    },
                    onFailure = { error ->
                        val msg = when {
                            error.message?.contains("email address is already in use", ignoreCase = true) == true ->
                                "An account with this email already exists."
                            error.message?.contains("password", ignoreCase = true) == true ->
                                "Password must be at least 6 characters."
                            else -> "Error creating account: ${error.message}"
                        }
                        _ui.value = _ui.value.copy(isLoading = false, error = msg)
                        onError(msg)
                    }
                )
            } catch (e: Exception) {
                val msg = "Error creating account: ${e.message}"
                _ui.value = _ui.value.copy(isLoading = false, error = msg)
                onError(msg)
            }
        }
    }
}
