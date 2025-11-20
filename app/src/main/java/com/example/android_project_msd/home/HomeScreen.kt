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


        IconButton(
            onClick = onProfile,
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f))
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
                .background(Color.White.copy(alpha = 0.15f))
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notifications",
                tint = Color.White
            )
        }


        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = (-80).dp, y = 80.dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0x44D975BB), Color.Transparent)
                    ),
                    shape = CircleShape
                )
                .alpha(0.45f)
        )


        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Text(
                text = "Evenly makes it easy",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 0.5.sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Split, manage and stay updated.",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.82f)
            )

            Spacer(Modifier.height(34.dp))



            GlassButton(
                icon = Icons.Outlined.Group,
                text = "Create Group",
                onClick = onCreateGroup
            )

            Spacer(Modifier.height(20.dp))

            GlassButton(
                icon = Icons.Outlined.Group,
                text = "My Groups",
                onClick = onMyGroups
            )
        }
    }
}

@Composable
private fun GlassButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {

    val glassColor = Color.White.copy(alpha = 0.13f)

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(68.dp),
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(containerColor = glassColor),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
