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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// Same gradient as Home / Groups / GroupDetail
private val BgStart = Color(0xFF1F1F7A)
private val BgEnd = Color(0xFF4C1E78)

@Composable
fun CreateGroupFullRoute(
    vm: CreateGroupFullViewModel = viewModel(),
    onDone: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val ui by vm.ui.collectAsState()
    val scroll = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(BgStart, BgEnd))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.statusBars.asPaddingValues())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "Create group",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Set up details and members",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF5C6BC0), Color(0xFFAB47BC))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            // White bottom sheet
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = Color.White,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scroll)
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    // Group info
                    Text(
                        "Group information",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1E1E)
                        )
                    )
                    Spacer(Modifier.height(16.dp))

                    LineInput(
                        label = "Group name",
                        value = ui.groupName,
                        placeholder = "Weekend trip, Apartment, etc.",
                        onChange = { newValue ->
                            vm.update { it.copy(groupName = newValue) }
                        }
                    )

                    LineInput(
                        label = "Description (optional)",
                        value = ui.description,
                        placeholder = "What is this group for?",
                        onChange = { newValue ->
                            vm.update { it.copy(description = newValue) }
                        }
                    )

                    Spacer(Modifier.height(20.dp))

                    // Members
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Members",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E1E1E)
                            )
                        )
                        TextButton(onClick = { vm.showAddMemberDialog() }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Add member")
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    if (ui.members.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 18.dp),
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
                                    color = Color(0xFF9E9E9E),
                                    fontSize = 14.sp
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

                    Spacer(Modifier.height(22.dp))

                    // Currency
                    Text(
                        "Currency",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1E1E)
                        )
                    )
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("DKK", "EUR", "USD", "GBP").forEach { currency ->
                            CurrencyChip(
                                currency = currency,
                                isSelected = ui.currency == currency,
                                onClick = {
                                    vm.update { it.copy(currency = currency) }
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // Create button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                Brush.horizontalGradient(
                                    if (ui.canCreate) {
                                        listOf(Color(0xFF9C27B0), Color(0xFFE91E63))
                                    } else {
                                        listOf(
                                            Color(0xFF9C27B0).copy(alpha = 0.5f),
                                            Color(0xFFE91E63).copy(alpha = 0.5f)
                                        )
                                    }
                                )
                            )
                            .clickable(enabled = ui.canCreate && !ui.isLoading) {
                                vm.createGroup(
                                    onSuccess = { _ ->
                                        onDone()
                                    },
                                    onError = { _ ->
                                        // ui.error already handled
                                    }
                                )
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
                                "Create group",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    // Error
                    ui.error?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            it,
                            color = Color(0xFFFF3B30),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            fontSize = 14.sp
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    // Cancel text
                    Text(
                        "Cancel",
                        color = Color(0xFF8E8E93),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clickable { onCancel() }
                            .padding(6.dp),
                        fontSize = 14.sp
                    )
                }
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
        style = MaterialTheme.typography.titleSmall.copy(
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
                        color = Color(0xFFB3B3B3),
                        fontSize = 14.sp
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
    Spacer(Modifier.height(14.dp))
}

@Composable
fun MemberCard(
    member: CreateGroupMember,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF5C6BC0), Color(0xFFAB47BC))
                        )
                    ),
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
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 14.sp
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
                "Add member",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        },
        text = {
            Column {
                Text(
                    "Enter the email of the person you want to add.",
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
