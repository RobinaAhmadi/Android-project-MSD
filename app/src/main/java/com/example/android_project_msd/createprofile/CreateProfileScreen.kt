package com.example.android_project_msd.createprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CreateProfileRoute(
    vm: CreateProfileViewModel = viewModel(),
    onDone: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val ui by vm.ui.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF131B63), Color(0xFF481162))
                )
            )
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Title
            Text(
                text = "Create Profile",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Let's set up your account",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            // Avatar
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.AccountCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize(0.75f)
                )
            }

            Spacer(Modifier.height(28.dp))

            // Glass card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                color = Color.White.copy(alpha = 0.10f),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 22.dp)
                ) {

                    IconLineInput(
                        icon = Icons.Outlined.Person,
                        label = "User Name",
                        value = ui.username,
                        placeholder = "John Smith",
                        onChange = { newValue ->
                            vm.update { state -> state.copy(username = newValue) }
                        }
                    )

                    IconLineInput(
                        icon = Icons.Outlined.Email,
                        label = "Email",
                        value = ui.email,
                        placeholder = "john@email.com",
                        keyboardType = KeyboardType.Email,
                        onChange = { newValue ->
                            vm.update { state -> state.copy(email = newValue) }
                        }
                    )

                    PhoneInput(
                        countryCode = ui.countryCode,
                        phone = ui.phone,
                        onCountryChange = { code ->
                            vm.update { state -> state.copy(countryCode = code) }
                        },
                        onPhoneChange = { digits ->
                            vm.update { state -> state.copy(phone = digits) }
                        }
                    )

                    IconLineInput(
                        icon = Icons.Outlined.Lock,
                        label = "Password",
                        value = ui.password,
                        placeholder = "••••••",
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                        onChange = { newValue ->
                            vm.update { state -> state.copy(password = newValue) }
                        }
                    )

                    Spacer(Modifier.height(18.dp))

                    // Sign Up button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(
                                if (ui.canSubmit && !ui.isLoading)
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF9C27B0), Color(0xFFE91E63))
                                    )
                                else
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFF9C27B0).copy(alpha = 0.4f),
                                            Color(0xFFE91E63).copy(alpha = 0.4f)
                                        )
                                    )
                            )
                            .clickable(enabled = ui.canSubmit && !ui.isLoading) {
                                vm.submit(onSuccess = onDone, onError = {})
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (ui.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Sign Up",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Global error text (from ViewModel)
                    ui.error?.let { error ->
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = error,
                            color = Color(0xFFFFCDD2),
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Cancel",
                        color = Color(0xFFE0E0FF),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCancel() }
                            .padding(vertical = 4.dp),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Already have an account? ",
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            text = "Sign In",
                            color = Color(0xFFF48FB1),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { onCancel() }
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun IconLineInput(
    icon: ImageVector,
    label: String,
    value: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    onChange: (String) -> Unit
) {
    Text(
        text = label,
        style = fieldLabelStyle()
    )
    Spacer(Modifier.height(6.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color(0xFFE1D9FF)
        )
        Spacer(Modifier.width(10.dp))
        LineInputCore(
            value = value,
            placeholder = placeholder,
            keyboardType = keyboardType,
            isPassword = isPassword,
            onChange = onChange,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PhoneInput(
    countryCode: String,
    phone: String,
    onCountryChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit
) {
    Text(
        text = "Phone",
        style = fieldLabelStyle()
    )
    Spacer(Modifier.height(6.dp))

    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(
                text = countryCode,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                listOf("+45", "+44", "+1", "+49", "+33", "+34").forEach { code ->
                    DropdownMenuItem(
                        text = { Text(code) },
                        onClick = {
                            onCountryChange(code)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.width(10.dp))

        LineInputCore(
            value = phone,
            placeholder = "20 15 00 01",
            keyboardType = KeyboardType.Number,
            isPassword = false,
            onChange = { digits ->
                onPhoneChange(digits)
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun LineInputCore(
    value: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    onChange: (String) -> Unit
) {
    var internal by remember(value) { mutableStateOf(value) }

    Column(modifier) {
        BasicTextField(
            value = internal,
            onValueChange = { newValue ->
                // Enforce digits-only for numeric fields
                val filtered =
                    if (keyboardType == KeyboardType.Number ||
                        keyboardType == KeyboardType.Phone ||
                        keyboardType == KeyboardType.NumberPassword
                    ) {
                        newValue.filter { it.isDigit() }.take(15)
                    } else {
                        newValue
                    }

                internal = filtered
                onChange(filtered)
            },
            singleLine = true,
            cursorBrush = SolidColor(Color(0xFFF48FB1)),
            textStyle = LocalTextStyle.current.copy(
                fontSize = 16.sp,
                color = Color.White
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation =
                if (isPassword) PasswordVisualTransformation()
                else VisualTransformation.None,
            decorationBox = { inner ->
                Box(Modifier.fillMaxWidth()) {
                    if (internal.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = Color.White.copy(alpha = 0.4f),
                            overflow = TextOverflow.Clip
                        )
                    }
                    inner()
                }
            }
        )
        Divider(
            thickness = 1.dp,
            color = Color.White.copy(alpha = 0.22f)
        )
    }
}

@Composable
private fun fieldLabelStyle() =
    MaterialTheme.typography.titleMedium.copy(
        color = Color.White,
        fontWeight = FontWeight.Medium
    )
