package com.example.android_project_msd.view.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.android_project_msd.controller.profile.ProfileViewModel

@Composable
fun ProfileScreen(modifier: Modifier = Modifier, viewModel: ProfileViewModel = viewModel()) {
    // Lyt til state-ændringer fra vores ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // UI'en reagerer automatisk på ændringer i uiState
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("User Profile", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            // Indtastningsfelter
            ProfileTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = "Name",
                enabled = uiState.isEditing
            )
            ProfileTextField(
                value = uiState.phone,
                onValueChange = viewModel::onPhoneChange,
                label = "Phone",
                enabled = uiState.isEditing
            )
            ProfileTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = "Email",
                enabled = uiState.isEditing,
                keyboardType = KeyboardType.Email
            )

            // Kodeordsfelt (kun synligt i redigeringstilstand)
            if (uiState.isEditing) {
                ProfileTextField(
                    value = uiState.newPassword,
                    onValueChange = viewModel::onPasswordChange,
                    label = "New Password",
                    enabled = true,
                    isPassword = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Notifications", style = MaterialTheme.typography.titleMedium)

            // Toggles til notifikationer
            ProfileSwitch(
                text = "Alert on new payment",
                checked = uiState.alertOnNewPayment,
                onCheckedChange = viewModel::onNewPaymentToggle,
                enabled = uiState.isEditing
            )
            ProfileSwitch(
                text = "Alert on missing payment",
                checked = uiState.alertOnMissingPayment,
                onCheckedChange = viewModel::onMissingPaymentToggle,
                enabled = uiState.isEditing
            )

            Spacer(modifier = Modifier.weight(1f)) // skubber knappen til bunden

            // Knap til at redigere/bekræfte
            Button(
                onClick = viewModel::onToggleEditMode,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                Text(if (uiState.isEditing) "Confirm" else "Edit Profile")
            }
        }

        // Viser en loading-spinner, mens data gemmes eller hentes
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

// Hjælpe-composable for at undgå gentagelse
@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None

    )
}

@Composable
private fun ProfileSwitch(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}
