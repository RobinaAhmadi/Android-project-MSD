package com.example.android_project_msd.groups.creategroup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun CreateGroupFullRoute(
    vm: CreateGroupFullViewModel = viewModel(),
    onDone: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val ui by vm.ui.collectAsState()
    val scroll = rememberScrollState()

    Box(Modifier.fillMaxSize()) {
        // Top gradient
        Box(
            Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF131B63), Color(0xFF481162))
                    )
                )
        )

        // Title and group icon
        Box(
            Modifier
                .fillMaxWidth()
                .height(260.dp)
                .padding(horizontal = 20.dp, vertical = 28.dp)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCancel) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "Create New\nGroup",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = Color(0xFF1E1E1E),
                    modifier = Modifier.fillMaxSize(0.6f)
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
                // Group Info Section
                Text(
                    "Group Information",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E1E)
                    )
                )
                Spacer(Modifier.height(16.dp))

                LineInput(
                    label = "Group Name",
                    value = ui.groupName,
                    placeholder = "Weekend Trip, Apartment, etc.",
                    onChange = { newValue -> vm.update { it.copy(groupName = newValue) } }
                )

                LineInput(
                    label = "Description (Optional)",
                    value = ui.description,
                    placeholder = "What is this group for?",
                    onChange = { newValue -> vm.update { it.copy(description = newValue) } }
                )

                Spacer(Modifier.height(24.dp))

                // Members Section
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Members",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1E1E)
                        )
                    )
                    TextButton(onClick = { vm.showAddMemberDialog() }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Add Member")
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (ui.members.isEmpty()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color(0xFFE0E0E0)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "No members added yet",
                                color = Color(0xFF9E9E9E)
                            )
                        }
                    }
                } else {
                    ui.members.forEach { member ->
                        MemberCard(
                            member = member,
                            onRemove = { vm.removeMember(member.id) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Currency Selection (Optional feature)
                Text(
                    "Currency",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E1E)
                    )
                )
                Spacer(Modifier.height(12.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("DKK", "EUR", "USD", "GBP").forEach { currency ->
                        CurrencyChip(
                            currency = currency,
                            isSelected = ui.currency == currency,
                            onClick = { vm.update { it.copy(currency = currency) } }
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                // CREATE BUTTON
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            if (ui.canCreate) {
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF9C27B0), Color(0xFFE91E63))
                                )
                            } else {
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF9C27B0).copy(alpha = 0.5f), Color(0xFFE91E63).copy(alpha = 0.5f))
                                )
                            }
                        )
                        .clickable(enabled = ui.canCreate) {
                            vm.createGroup(
                                onSuccess = { groupId ->
                                    onDone()
                                },
                                onError = { error ->
                                    // Error is shown in UI via ui.error
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (ui.isLoading) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = androidx.compose.ui.Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "CREATE GROUP",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Error message
                if (ui.error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        ui.error!!,
                        color = Color(0xFFFF3B30),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 14.sp
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    "Cancel",
                    color = Color(0xFF8E8E93),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable { onCancel() }
                        .padding(6.dp)
                )
            }
        }
    }

    // Add member dialog
    if (ui.showAddMemberDialog) {
        AddMemberToGroupDialog(
            onDismiss = { vm.hideAddMemberDialog() },
            onAdd = { email ->
                vm.addMember(email)
                vm.hideAddMemberDialog()
            }
        )
    }
}

@Composable
private fun LineInput(
    label: String,
    value: String,
    placeholder: String,
    onChange: (String) -> Unit
) {
    Text(
        label,
        style = MaterialTheme.typography.titleMedium.copy(
            color = Color(0xFF111111),
            fontWeight = FontWeight.Medium
        )
    )
    Spacer(Modifier.height(6.dp))

    var internal by remember(value) { mutableStateOf(value) }

    BasicTextField(
        value = internal,
        onValueChange = {
            internal = it
            onChange(it)
        },
        textStyle = LocalTextStyle.current.copy(
            fontSize = 16.sp,
            color = Color(0xFF1E1E1E)
        ),
        cursorBrush = SolidColor(Color(0xFF3D5AFE)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        decorationBox = { inner ->
            Box {
                if (internal.isEmpty()) {
                    Text(
                        placeholder,
                        color = Color(0xFFB3B3B3)
                    )
                }
                inner()
            }
        }
    )

    HorizontalDivider(
        modifier = Modifier.padding(top = 2.dp),
        thickness = 1.dp,
        color = Color(0x1A000000)
    )
    Spacer(Modifier.height(16.dp))
}

@Composable
fun MemberCard(
    member: CreateGroupMember,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF9C27B0)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    member.name.first().uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    member.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    member.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575)
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = Color(0xFF757575)
                )
            }
        }
    }
}

@Composable
fun CurrencyChip(
    currency: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) {
                    Color(0xFF9C27B0)
                } else {
                    Color(0xFFF5F5F5)
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            currency,
            color = if (isSelected) Color.White else Color(0xFF757575),
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun AddMemberToGroupDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add Member",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        },
        text = {
            Column {
                Text(
                    "Enter the email address of the person you want to add to this group.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF757575)
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    placeholder = { Text("john@email.com") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(email) },
                enabled = email.isNotBlank() && email.contains("@")
            ) {
                Text("ADD")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun CreateGroupFullPreview() {
    MaterialTheme {
        CreateGroupFullRoute()
    }
}