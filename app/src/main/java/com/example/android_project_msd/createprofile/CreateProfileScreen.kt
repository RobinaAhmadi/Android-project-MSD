package com.example.android_project_msd.createprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.LocalTextStyle


@Composable
fun CreateProfileRoute(
    vm: CreateProfileViewModel = viewModel(),
    onDone: () -> Unit = {}
) {
    val ui by vm.ui.collectAsState()
    val scroll = rememberScrollState()
    var countryCode by remember { mutableStateOf("+45") }

    Box(Modifier.fillMaxSize()) {

        // Top gradient
        Box(
            Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF112A66), Color(0xFF0B1B3D))
                    )
                )
        )

        // Title and profile icon
        Box(
            Modifier
                .fillMaxWidth()
                .height(260.dp)
                .padding(horizontal = 20.dp, vertical = 28.dp)
        ) {
            Text(
                "Create Your\nAccount",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.align(Alignment.TopStart)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = null,
                    tint = Color(0xFF1E1E1E),
                    modifier = Modifier.fillMaxSize(0.8f)
                )
            }
        }

        // White rounded sheet
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 160.dp),
            shape = RoundedCornerShape(topStart = 44.dp, topEnd = 44.dp),
            tonalElevation = 2.dp,
            shadowElevation = 10.dp,
            color = Color.White
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(horizontal = 20.dp)
                    .padding(top = 24.dp, bottom = 20.dp)
            ) {
                LineInput(
                    label = "User Name",
                    value = ui.username,
                    placeholder = "John Smith",
                    onChange = { v -> vm.update { it.copy(username = v) } }
                )

                LineInput(
                    label = "Email",
                    value = ui.email,
                    placeholder = "john@email.com",
                    onChange = { v -> vm.update { it.copy(email = v) } },
                    keyboardType = KeyboardType.Email
                )

                LineInput(
                    label = "Phone",
                    value = ui.phone,
                    placeholder = "+45 20 15 00 01",
                    onChange = { s ->
                        val noSpaces = s.replace(" ", "")
                        val hasPlus = noSpaces.startsWith("+")
                        val digits = noSpaces.dropWhile { it == '+' }.filter { it.isDigit() }.take(15)
                        val grouped = digits.chunked(2).joinToString(" ")
                        vm.update { it.copy(phone = (if (hasPlus) "+ " else "") + grouped) }
                    },
                    keyboardType = KeyboardType.Phone
                )

                LineInput(
                    label = "Password",
                    value = ui.password,
                    placeholder = "••••••",
                    onChange = { v -> vm.update { it.copy(password = v) } },
                    keyboardType = KeyboardType.Password,
                    isPassword = true
                )

                LineInput(
                    label = "Card Holder Name",
                    value = ui.cardHolder,
                    placeholder = "John Smith",
                    onChange = { v -> vm.update { it.copy(cardHolder = v) } }
                )

                LineInput(
                    label = "Card Number",
                    value = ui.cardNumber,
                    placeholder = "4111 1111 1111 1111",
                    onChange = { s ->
                        vm.update {
                            it.copy(
                                cardNumber = s.filter(Char::isDigit)
                                    .take(16)
                                    .chunked(4)
                                    .joinToString(" ")
                            )
                        }
                    },
                    keyboardType = KeyboardType.Number
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text("Expiry Date", style = fieldLabelStyle())
                        Spacer(Modifier.height(6.dp))
                        LineInputCore(
                            value = ui.expiry,
                            onChange = { s ->
                                val d = s.filter(Char::isDigit).take(4)
                                val v = if (d.length <= 2) d else d.substring(0, 2) + "/" + d.substring(2)
                                vm.update { it.copy(expiry = v) }
                            },
                            placeholder = "MM/YY",
                            modifier = Modifier.fillMaxWidth(),
                            keyboardType = KeyboardType.Number
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text("CVV", style = fieldLabelStyle())
                        Spacer(Modifier.height(6.dp))
                        LineInputCore(
                            value = ui.cvv,
                            onChange = { s -> vm.update { it.copy(cvv = s.filter(Char::isDigit).take(4)) } },
                            placeholder = "123",
                            modifier = Modifier.fillMaxWidth(),
                            keyboardType = KeyboardType.NumberPassword,
                            isPassword = true
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                // SIGN UP
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF163A96), Color(0xFF0B1A3A))
                            )
                        )
                        .clickable(enabled = ui.canSubmit) {
                            vm.submit(onSuccess = onDone, onError = {})
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("SIGN UP", color = Color.White, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    "Cancel",
                    color = Color(0xFF8E8E93),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable { onDone() }
                        .padding(6.dp)
                )

                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text("Already have an account? ", color = Color(0xFF8E8E93))
                    Text("Sign In", fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { onDone() })
                }
            }
        }
    }
}

@Composable
private fun LineInput(
    label: String,
    value: String,
    placeholder: String,
    onChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    Text(label, style = fieldLabelStyle())
    Spacer(Modifier.height(6.dp))
    LineInputCore(
        value = value,
        onChange = onChange,
        placeholder = placeholder,
        modifier = Modifier.fillMaxWidth(),
        keyboardType = keyboardType,
        isPassword = isPassword
    )
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun LineInputCore(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    trailing: @Composable (() -> Unit)? = null
) {
    var internal by remember(value) { mutableStateOf(value) }

    BasicTextField(
        value = internal,
        onValueChange = {
            internal = it
            onChange(it)
        },
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(
            fontSize = 16.sp,
            color = Color(0xFF1E1E1E),
            textAlign = TextAlign.Start
        ),
        cursorBrush = SolidColor(Color(0xFF3D5AFE)),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = modifier.padding(vertical = 6.dp),
        decorationBox = { inner ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (internal.isEmpty()) {
                        Text(
                            placeholder,
                            color = Color(0xFFB3B3B3),
                            overflow = TextOverflow.Clip
                        )
                    }
                    inner()
                }
                if (trailing != null) {
                    Spacer(Modifier.width(6.dp))
                    trailing()
                }
            }
        }
    )

    HorizontalDivider(
        modifier = Modifier.padding(top = 2.dp),
        thickness = 1.dp,
        color = Color(0x1A000000)
    )
}

@Composable
private fun fieldLabelStyle() =
    MaterialTheme.typography.titleMedium.copy(
        color = Color(0xFF111111),
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Start
    )
