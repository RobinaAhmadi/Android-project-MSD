package com.example.android_project_msd.controller.login

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.android_project_msd.model.auth.AuthRepository
import retrofit2.HttpException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class LoginViewModel(
    private val repository: AuthRepository
) : ViewModel() {
    private val _ui = MutableStateFlow(LoginUiState())
    val ui = _ui.asStateFlow()

    fun updateEmail(v: String) {
        _ui.update { it.copy(email = v) }
    }

    fun updatePassword(v: String) {
        _ui.update { it.copy(password = v) }
    }

    fun signIn(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val email = _ui.value.email
        val password = _ui.value.password

        if (email.isBlank() || password.isBlank()) {
            val msg = "Email and password required"
            _ui.update { it.copy(error = msg) }
            onError(msg)
            return
        }

        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            repository.login(email, password)
                .onSuccess {
                    _ui.update { it.copy(isLoading = false, error = null) }
                    onSuccess()
                }
                .onFailure { throwable ->
                    val msg = when (throwable) {
                        is IllegalStateException -> throwable.message ?: "Wrong email or password"
                        is HttpException -> {
                            if (throwable.code() == 401) "Wrong email or password"
                            else "Server error (${throwable.code()})"
                        }
                        else -> throwable.message ?: "Unable to sign in"
                    }
                    _ui.update { it.copy(isLoading = false, error = msg) }
                    onError(msg)
                }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                LoginViewModel(AuthRepository.getInstance(app))
            }
        }
    }
}
