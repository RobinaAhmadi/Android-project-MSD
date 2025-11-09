package com.example.android_project_msd.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android_project_msd.R
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    onCreateAccountClick: () -> Unit,
    onSignIn: () -> Unit = {}
) {
    val vm: LoginViewModel = viewModel()
    val ui by vm.ui.collectAsState()


    val infiniteTransition = rememberInfiniteTransition()
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.background_pic),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )


        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF131B63).copy(alpha = 0.95f),
                            Color(0xFF481162).copy(alpha = 0.95f)
                        ),
                        startY = offset,
                        endY = offset + 800f
                    )
                )
        )


        Box(
            modifier = Modifier
                .size(250.dp)
                .offset(x = (-40).dp, y = (-100).dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0x33D975BB), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(200.dp)
                )
                .blur(80.dp)
        )

        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = 180.dp, y = 450.dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0x33D975BB), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(200.dp)
                )
                .blur(90.dp)
        )

        // Content column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(visible = true) {
                Text(
                    "Welcome back",
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 28.dp)
                )
            }

            // Glass card container
            AnimatedVisibility(visible = true) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(20.dp)
                ) {
                    // Email input
                    OutlinedTextField(
                        value = ui.email,
                        onValueChange = { vm.updateEmail(it) },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Email, contentDescription = null, tint = Color(0xFFD975BB))
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD975BB),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                            focusedLabelColor = Color(0xFFD975BB),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
                            cursorColor = Color(0xFFD975BB),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(Modifier.height(14.dp))

                    // Password input
                    OutlinedTextField(
                        value = ui.password,
                        onValueChange = { vm.updatePassword(it) },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = Color(0xFFD975BB))
                        },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD975BB),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                            focusedLabelColor = Color(0xFFD975BB),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
                            cursorColor = Color(0xFFD975BB),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(Modifier.height(24.dp))

                    // Gradient button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF261863),
                                        Color(0xFFD975BB),
                                        Color(0xFFEBA2C6)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (ui.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            TextButton(
                                onClick = {
                                    vm.signIn(onSuccess = onSignIn, onError = {})
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Sign In",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    if (ui.error != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            ui.error!!,
                            color = Color(0xFFFFCDD2),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    TextButton(
                        onClick = onCreateAccountClick,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            "Create account",
                            color = Color(0xFFD975BB),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
