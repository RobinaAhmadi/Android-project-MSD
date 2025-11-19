package com.example.android_project_msd.createprofile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_project_msd.data.UserRepository
import com.example.android_project_msd.data.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val isEmailValid: Boolean get() = email.contains("@")
    val isPasswordValid: Boolean get() = password.length >= 6

    val canSubmit: Boolean
        get() = username.isNotBlank() && isEmailValid && isPasswordValid && phone.replace(" ", "").replace("+", "").isNotBlank() && cardHolder.isNotBlank() && cardNumber.replace(" ", "").isNotBlank() && expiry.isNotBlank() && cvv.isNotBlank()
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
        if (!u.canSubmit) {
            val msg = "Please fill all required fields correctly."
            _ui.value = _ui.value.copy(error = msg)
            onError(msg)
            return
        }

        if (u.password.length < 6) {
            val msg = "Password must be at least 6 characters"
            _ui.value = _ui.value.copy(error = msg)
            onError(msg)
            return
        }

        viewModelScope.launch {
            _ui.value = _ui.value.copy(isLoading = true, error = null)
            try {
                val trimmedEmail = u.email.trim()


                // Create user in Firebase
                val result = userRepository.createUser(
                    email = trimmedEmail,
                    password = u.password,
                    name = u.username,
                    phoneNumber = u.countryCode + u.phone.replace(" ", ""),
                    cardHolderName = u.cardHolder,
                    cardNumber = u.cardNumber.replace(" ", ""),
                    expiryDate = u.expiry,
                    cvv = u.cvv
                )

                result.fold(
                    onSuccess = { user ->
                        UserSession.login(user.id, user.email)
                        _ui.value = _ui.value.copy(isLoading = false, error = null)
                        onSuccess()
                    },
                    onFailure = { error ->
                        val msg = when {
                            error.message?.contains("email address is already in use") == true ->
                                "An account with this email already exists."
                            error.message?.contains("password") == true ->
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
