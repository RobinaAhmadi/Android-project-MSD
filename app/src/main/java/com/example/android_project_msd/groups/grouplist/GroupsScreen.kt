package com.example.android_project_msd.groups.grouplist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
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

import androidx.compose.ui.tooling.preview.Preview

@Composable
fun GroupsRoute(
    vm: GroupsViewModel = viewModel(),
    onBack: () -> Unit = {},
    onOpenGroup: (String) -> Unit = {},
    onCreateGroup: () -> Unit = {}
) {

    val ui by vm.ui.collectAsState()

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
            // Top bar with back button and title
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
                    "My Groups",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(Modifier.height(40.dp))

            // White rounded content area
            Surface(
                modifier = Modifier
                    .fillMaxSize(),
                shape = RoundedCornerShape(topStart = 44.dp, topEnd = 44.dp),
                tonalElevation = 2.dp,
                shadowElevation = 10.dp,
                color = Color.White
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    if (ui.groups.isEmpty()) {
                        // Empty state
                        Box(
                            Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Group,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = Color(0xFFE0E0E0)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "No groups yet",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color(0xFF757575)
                                )
                                Text(
                                    "Create your first group to start splitting expenses",
                                    color = Color(0xFF9E9E9E),
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    } else {
                        // Groups list
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(ui.groups) { group ->
                                GroupCard(
                                    group = group,
                                    onClick = { onOpenGroup(group.id) }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Create Group Button
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF9C27B0), Color(0xFFE91E63))
                                )
                            )
                            .clickable { onCreateGroup() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "CREATE NEW GROUP",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GroupCard(
    group: Group,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Group icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF9C27B0), Color(0xFFE91E63))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Group,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    group.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E1E1E)
                    )
                )
                if (group.description.isNotEmpty()) {
                    Text(
                        group.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF757575),
                        maxLines = 1
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF9E9E9E)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${group.memberCount} members",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }

            // Balance indicator
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    if (group.balance >= 0) "+${group.balance} DKK" else "${group.balance} DKK",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (group.balance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                )
                Text(
                    if (group.balance >= 0) "You're owed" else "You owe",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9E9E9E)
                )
            }
        }
    }
}

// Preview for Android Studio
@Preview(showBackground = true, heightDp = 800)
@Composable
fun GroupsScreenPreview() {
    MaterialTheme {
        GroupsRoute(onBack = {})
    }
}

