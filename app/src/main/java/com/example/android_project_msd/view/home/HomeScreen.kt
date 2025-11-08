package com.example.android_project_msd.view.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onProfile: () -> Unit,
    onCreateGroup: () -> Unit,
    onMyGroups: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF131B63), Color(0xFF481162))))
    ) {
        // Profile button (larger, with shadow)
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(56.dp),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.15f),
            shadowElevation = 8.dp
        ) {
            IconButton(onClick = onProfile, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Centered actions
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(border = androidx.compose.foundation.BorderStroke(1.dp, Color.White), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                onClick = onCreateGroup,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.width(200.dp)
            ) { Text("Create group") }

            OutlinedButton(border = androidx.compose.foundation.BorderStroke(1.dp, Color.White), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                onClick = onMyGroups,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.width(200.dp)
            ) { Text("My Groups") }
        }

        Text(
            text = "Product Shot",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            color = Color.White.copy(alpha = 0.85f)
        )
    }
}




