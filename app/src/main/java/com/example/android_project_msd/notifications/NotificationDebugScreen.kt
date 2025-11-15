package com.example.android_project_msd.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BgStart = Color(0xFF131B63)
private val BgEnd = Color(0xFF481162)
private val CardBackground = Color(0xFFF3EAF5)

@Composable
fun NotificationDebugScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allNotifications by NotificationCenter.notifications.collectAsState()
    val myNotifications = allNotifications.filter { it.recipients.contains("You") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgStart, BgEnd)))
            .padding(16.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            TextButton(onClick = onBack) {
                Text("Back", color = Color.White)
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Notifications",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "A list of all notifications for you",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(16.dp))

            if (myNotifications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No notifications yet",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(myNotifications) { notification ->
                        NotificationCard(notification)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(notification: AppNotification) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large),
        color = CardBackground,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = null,
                tint = Color(0xFF5C2B7F),
                modifier = Modifier
                    .padding(top = 4.dp)
            )

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp
                    )
                )
                Spacer(Modifier.height(4.dp))

                Text(
                    text = notification.line1,
                    style = MaterialTheme.typography.bodyMedium
                )

                notification.line2?.let {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                notification.youOweLine?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFFB83B7F)
                        )
                    )
                }
            }
        }
    }
}
