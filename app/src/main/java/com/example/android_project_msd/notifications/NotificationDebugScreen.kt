package com.example.android_project_msd.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.android_project_msd.data.GroupInvitation

private val BgStart = Color(0xFF131B63)
private val BgEnd = Color(0xFF481162)
private val CardBackground = Color(0xFFF6F1FA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDebugScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationsViewModel = viewModel()
) {
    val invitations by viewModel.invitations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Notifications",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(BgStart, BgEnd)))
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {

            Column {
                Text(
                    text = "Group Invitations",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Pending invitations",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f)
                )

                Spacer(Modifier.height(16.dp))

                when {
                    isLoading -> LoadingState()
                    invitations.isEmpty() -> EmptyState()
                    else -> InvitationList(
                        invitations = invitations,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color.White)
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.Group,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "No pending invitations",
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun InvitationList(
    invitations: List<GroupInvitation>,
    viewModel: NotificationsViewModel
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(invitations) { invitation ->
            GroupInvitationCard(
                invitation = invitation,
                onAccept = { viewModel.acceptInvitation(invitation.id) },
                onDecline = { viewModel.declineInvitation(invitation.id) }
            )
        }
    }
}

@Composable
private fun GroupInvitationCard(
    invitation: GroupInvitation,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    var isProcessing by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp)),
        color = CardBackground,
        shadowElevation = 6.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            // Header row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Group,
                    contentDescription = null,
                    tint = Color(0xFF6C3EA8),
                    modifier = Modifier.size(32.dp)
                )

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(
                        "Group Invitation",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF6C3EA8),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        invitation.groupName,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF1D0F2A),
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Message
            Text(
                invitation.message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF4A4A4A)
            )

            if (invitation.groupDescription.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    invitation.groupDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF777777),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                "From: ${invitation.fromUserName} (${invitation.fromUserEmail})",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF777777)
            )

            Spacer(Modifier.height(18.dp))

            // Accept / Decline buttons
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                OutlinedButton(
                    onClick = {
                        isProcessing = true
                        onDecline()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isProcessing,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFD7266B)
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFFD7266B), Color(0xFFB81A55))
                        )
                    )
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Decline")
                }

                Button(
                    onClick = {
                        isProcessing = true
                        onAccept()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Accept")
                    }
                }
            }
        }
    }
}
