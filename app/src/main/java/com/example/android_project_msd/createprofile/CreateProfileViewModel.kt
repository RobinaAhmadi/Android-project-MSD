package com.example.android_project_msd.createprofile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_project_msd.data.AppDatabase
import com.example.android_project_msd.data.User
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
    val showPassword: Boolean = false
) {
    val isEmailValid: Boolean get() = email.contains("@")
    val isPasswordValid: Boolean get() = password.length >= 1
    val isExpiryValid: Boolean get() = expiry.length >= 1
    val isCvvValid: Boolean get() = cvv.length in 3..4
    val canSubmit: Boolean
        get() = username.isNotBlank() && isEmailValid && isPasswordValid && phone.isNotBlank() && cardHolder.isNotBlank() && cardNumber.isNotBlank() && expiry.isNotBlank() && cvv.isNotBlank()
}

class CreateProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = AppDatabase.getDatabase(application).userDao()
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
            onError("Please fill all required fields correctly.")
            return
        }

        viewModelScope.launch {
            val existingUser = userDao.getUserByEmail(u.email)
            if (existingUser != null) {
                onError("An account with this email already exists.")
            } else {
                val newUser = User(
                    name = u.username,
                    email = u.email,
                    phoneNumber = u.countryCode + u.phone,
                    passwordHash = u.password, // Husk at hashe dette i en rigtig app!
                    cardHolderName = u.cardHolder,
                    cardNumber = u.cardNumber,
                    expiryDate = u.expiry,
                    cvv = u.cvv
                )
                userDao.insert(newUser)

                // Hent den nyoprettede bruger for at få ID'et
                val createdUser = userDao.getUserByEmail(u.email)
                if (createdUser != null) {
                    UserSession.login(createdUser.id, createdUser.email)
                }

                onSuccess()
            }
        }
    }
}
