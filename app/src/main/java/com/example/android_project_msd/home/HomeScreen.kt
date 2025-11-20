package com.example.android_project_msd.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BgStart = Color(0xFF131B63)
private val BgEnd = Color(0xFF481162)
private val ButtonStart = Color(0xFFD975BB)
private val ButtonEnd = Color(0xFFB858A1)

@Composable
fun HomeScreen(
    onProfile: () -> Unit,
    onCreateGroup: () -> Unit,
    onMyGroups: () -> Unit,
    onNotificationsDebug: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(BgStart, BgEnd))
            )
            .padding(20.dp)
    ) {

        // Top icons
        IconButton(
            onClick = onProfile,
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.18f))
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = "Profile",
                tint = Color.White
            )
        }

        IconButton(
            onClick = onNotificationsDebug,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.18f))
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notifications",
                tint = Color.White
            )
        }

        Box(
            modifier = Modifier
                .size(250.dp)
                .offset(x = (-60).dp, y = 40.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x55D975BB), Color.Transparent)
                    ),
                    shape = CircleShape
                )
                .alpha(0.5f)
        )


        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(26.dp)
        ) {


            Text(
                text = "Evenly makes it easy",
                fontSize = 26.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Create, join, and manage your groups",
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.8f)

            )

            Spacer(Modifier.height(20.dp))


            HomeButton(
                icon = Icons.Outlined.Group,
                text = "Create Group",
                onClick = onCreateGroup
            )

            HomeButton(
                icon = Icons.Outlined.Group,
                text = "My Groups",
                onClick = onMyGroups
            )
        }
    }
}

@Composable
private fun HomeButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(65.dp),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(listOf(ButtonStart, ButtonEnd)),
                    RoundedCornerShape(22.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = text,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp
                )
            }
        }
    }
}
