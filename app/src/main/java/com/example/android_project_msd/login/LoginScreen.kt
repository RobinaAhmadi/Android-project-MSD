package com.example.android_project_msd.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.android_project_msd.R

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
                            Color(0xFF1F1F7A).copy(alpha = 0.96f),
                            Color(0xFF4C1E78).copy(alpha = 0.98f)
                        ),
                        startY = offset,
                        endY = offset + 800f
                    )
                )
        )


        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = (-40).dp, y = (-90).dp)
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
                .size(220.dp)
                .offset(x = 180.dp, y = 460.dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0x33D975BB), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(200.dp)
                )
                .blur(90.dp)
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.statusBars.asPaddingValues())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            AnimatedVisibility(visible = true) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Welcome back",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Sign in to continue managing your groups",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 24.dp)
                    )
                }
            }


            AnimatedVisibility(visible = true) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color.White.copy(alpha = 0.10f))
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    // Email
                    OutlinedTextField(
                        value = ui.email,
                        onValueChange = { vm.updateEmail(it) },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Email,
                                contentDescription = null,
                                tint = Color(0xFFD975BB)
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD975BB),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                            focusedLabelColor = Color(0xFFD975BB),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
                            cursorColor = Color(0xFFD975BB),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            disabledBorderColor = Color.White.copy(alpha = 0.2f),
                            disabledTextColor = Color.White.copy(alpha = 0.7f)
                        )
                    )

                    Spacer(Modifier.height(14.dp))

                    // Password
                    OutlinedTextField(
                        value = ui.password,
                        onValueChange = { vm.updatePassword(it) },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = Color(0xFFD975BB)
                            )
                        },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD975BB),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                            focusedLabelColor = Color(0xFFD975BB),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
                            cursorColor = Color(0xFFD975BB),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            disabledBorderColor = Color.White.copy(alpha = 0.2f),
                            disabledTextColor = Color.White.copy(alpha = 0.7f)
                        )
                    )

                    Spacer(Modifier.height(22.dp))

                    // Sign in button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF9C27B0),
                                        Color(0xFFE91E63)
                                    )
                                )
                            )
                            .clickable(enabled = !ui.isLoading) {
                                vm.signIn(onSuccess = onSignIn, onError = {})
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
                                "Sign in",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Error text
                    ui.error?.let { error ->
                        Spacer(Modifier.height(8.dp))
                        Text(
                            error,
                            color = Color(0xFFFFCDD2),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            fontSize = 13.sp
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Create account
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
