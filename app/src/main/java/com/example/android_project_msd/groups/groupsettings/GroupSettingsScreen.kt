package com.example.android_project_msd.groups.groupsettings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun GroupSettingsRoute(
    groupId: String,
    vm: GroupSettingsViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val ui by vm.ui.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var memberToRemove by remember { mutableStateOf<GroupSettingsMember?>(null) }

    LaunchedEffect(groupId) {
        vm.loadGroup(groupId)
    }

    Box(Modifier.fillMaxSize()) {
        // Top gradient header
        Box(
            Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF131B63), Color(0xFF481162))
                    )
                )
        )

        Column(Modifier.fillMaxSize()) {
            // Top bar
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "Group Settings",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(Modifier.height(60.dp))

            // White rounded content area
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 44.dp, topEnd = 44.dp),
                tonalElevation = 2.dp,
                shadowElevation = 10.dp,
                color = Color.White
            ) {
                if (ui.isLoading) {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (ui.group == null) {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Group not found", color = Color(0xFF757575))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        // Group Info Section
                        item {
                            SectionHeader("Group Information")
                            Spacer(Modifier.height(12.dp))
                        }

                        item {
                            InfoCard(
                                label = "Group Name",
                                value = ui.group!!.name,
                                onEdit = if (ui.isOwner) {
                                    { showEditDialog = true }
                                } else null
                            )
                            Spacer(Modifier.height(8.dp))
                        }

                        item {
                            InfoCard(
                                label = "Description",
                                value = ui.group!!.description.ifEmpty { "No description" },
                                onEdit = if (ui.isOwner) {
                                    { showEditDialog = true }
                                } else null
                            )
                            Spacer(Modifier.height(24.dp))
                        }

                        // Members Section
                        item {
                            SectionHeader("Members (${ui.members.size})")
                            Spacer(Modifier.height(12.dp))
                        }

                        items(ui.members) { member ->
                            MemberCard(
                                member = member,
                                isOwner = member.id == ui.group!!.ownerId,
                                canRemove = ui.isOwner && member.id != ui.group!!.ownerId,
                                onRemove = {
                                    memberToRemove = member
                                }
                            )
                            Spacer(Modifier.height(8.dp))
                        }

                        // Delete Group Section (only for owner)
                        if (ui.isOwner) {
                            item {
                                Spacer(Modifier.height(32.dp))
                                SectionHeader("Danger Zone")
                                Spacer(Modifier.height(12.dp))
                            }

                            item {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { showDeleteConfirmation = true },
                                    color = Color(0xFFFFEBEE),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = Color(0xFFD32F2F)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                "Delete Group",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFFD32F2F)
                                                )
                                            )
                                            Text(
                                                "This action cannot be undone",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF757575)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(Modifier.height(40.dp))
                        }
                    }
                }
            }
        }
    }

    // Edit Group Dialog
    if (showEditDialog && ui.group != null) {
        EditGroupDialog(
            currentName = ui.group!!.name,
            currentDescription = ui.group!!.description,
            onDismiss = { showEditDialog = false },
            onSave = { newName, newDescription ->
                vm.updateGroup(newName, newDescription)
                showEditDialog = false
            }
        )
    }

    // Remove Member Confirmation
    if (memberToRemove != null) {
        RemoveMemberDialog(
            memberName = memberToRemove!!.name,
            onDismiss = { memberToRemove = null },
            onConfirm = {
                vm.removeMember(memberToRemove!!.id)
                memberToRemove = null
            }
        )
    }

    // Delete Group Confirmation
    if (showDeleteConfirmation) {
        DeleteGroupDialog(
            groupName = ui.group?.name ?: "",
            onDismiss = { showDeleteConfirmation = false },
            onConfirm = {
                vm.deleteGroup(
                    onSuccess = {
                        showDeleteConfirmation = false
                        onBack()
                    }
                )
            }
        )
    }

    // Show error if any
    if (ui.error != null) {
        LaunchedEffect(ui.error) {
            // You could show a snackbar here
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E1E1E)
        )
    )
}

@Composable
private fun InfoCard(
    label: String,
    value: String,
    onEdit: (() -> Unit)?
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF757575)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    value,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            if (onEdit != null) {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color(0xFF163A96)
                    )
                }
            }
        }
    }
}

@Composable
private fun MemberCard(
    member: GroupSettingsMember,
    isOwner: Boolean,
    canRemove: Boolean,
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
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF163A96)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    member.name.first().uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        member.name,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                    if (isOwner) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = Color(0xFFE3F2FD),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "Owner",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF1976D2),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Text(
                    member.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575)
                )
            }
            if (canRemove) {
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove member",
                        tint = Color(0xFFD32F2F)
                    )
                }
            }
        }
    }
}

@Composable
private fun EditGroupDialog(
    currentName: String,
    currentDescription: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var description by remember { mutableStateOf(currentDescription) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Edit Group",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Group Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, description) },
                enabled = name.isNotBlank()
            ) {
                Text("SAVE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}

@Composable
private fun RemoveMemberDialog(
    memberName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFF57C00)
            )
        },
        title = {
            Text("Remove Member?")
        },
        text = {
            Text("Are you sure you want to remove $memberName from this group?")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFFD32F2F)
                )
            ) {
                Text("REMOVE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}

@Composable
private fun DeleteGroupDialog(
    groupName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Delete,
                contentDescription = null,
                tint = Color(0xFFD32F2F)
            )
        },
        title = {
            Text(
                "Delete Group?",
                color = Color(0xFFD32F2F)
            )
        },
        text = {
            Column {
                Text("Are you sure you want to delete \"$groupName\"?")
                Spacer(Modifier.height(8.dp))
                Text(
                    "This will permanently delete:",
                    fontWeight = FontWeight.SemiBold
                )
                Text("• All expenses")
                Text("• All member data")
                Text("• All group history")
                Spacer(Modifier.height(8.dp))
                Text(
                    "This action cannot be undone.",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFFD32F2F)
                )
            ) {
                Text("DELETE GROUP")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}