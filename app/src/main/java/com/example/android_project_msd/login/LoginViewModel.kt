package com.example.android_project_msd.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_project_msd.data.UserRepository
import com.example.android_project_msd.data.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository = UserRepository()
    private val _ui = MutableStateFlow(LoginUiState())
    val ui = _ui.asStateFlow()

    fun updateEmail(v: String) { _ui.value = _ui.value.copy(email = v) }
    fun updatePassword(v: String) { _ui.value = _ui.value.copy(password = v) }

    fun signIn(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val email = _ui.value.email.trim()
        val password = _ui.value.password

        if (email.isBlank() || password.isBlank()) {
            val msg = "Email and password required"
            _ui.value = _ui.value.copy(error = msg)
            onError(msg)
            return
        }

        viewModelScope.launch {
            _ui.value = _ui.value.copy(isLoading = true, error = null)
            try {
                val result = userRepository.signIn(email, password)

                result.fold(
                    onSuccess = { user ->
                        UserSession.login(user.id, user.email)
                        _ui.value = _ui.value.copy(isLoading = false, error = null)
                        onSuccess()
                    },
                    onFailure = { error ->
                        val msg = when {
                            error.message?.contains("no user record") == true ||
                            error.message?.contains("user-not-found") == true ->
                                "No account found with this email"
                            error.message?.contains("wrong-password") == true ||
                            error.message?.contains("invalid-credential") == true ->
                                "Incorrect password"
                            else -> "Login failed: ${error.message}"
                        }
                        _ui.value = _ui.value.copy(isLoading = false, error = msg)
                        onError(msg)
                    }
                )
            } catch (e: Exception) {
                val msg = "Login error: ${e.message}"
                _ui.value = _ui.value.copy(isLoading = false, error = msg)
                onError(msg)
            }
        }
    }
}
