package com.example.android_project_msd.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_project_msd.auth.MockAuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class LoginViewModel : ViewModel() {
    private val _ui = MutableStateFlow(LoginUiState())
    val ui = _ui.asStateFlow()

    fun updateEmail(v: String) { _ui.value = _ui.value.copy(email = v) }
    fun updatePassword(v: String) { _ui.value = _ui.value.copy(password = v) }

    fun signIn(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val email = _ui.value.email
        val password = _ui.value.password

        if (email.isBlank() || password.isBlank()) {
            val msg = "Email and password required"
            _ui.value = _ui.value.copy(error = msg)
            onError(msg)
            return
        }

        viewModelScope.launch {
            _ui.value = _ui.value.copy(isLoading = true, error = null)
            delay(300)
            val ok = MockAuthRepository.authenticate(email, password)
            if (ok) {
                _ui.value = _ui.value.copy(isLoading = false, error = null)
                onSuccess()
            } else {
                val msg = "Invalid credentials"
                _ui.value = _ui.value.copy(isLoading = false, error = msg)
                onError(msg)
            }
        }
    }
}

