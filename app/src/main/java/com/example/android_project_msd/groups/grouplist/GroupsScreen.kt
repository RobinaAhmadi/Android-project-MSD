package com.example.android_project_msd.groups.grouplist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.ui.text.style.TextAlign


private val GroupsBgStart = Color(0xFF1F1F7A)
private val GroupsBgEnd = Color(0xFF4C1E78)

@Composable
fun GroupsRoute(
    vm: GroupsViewModel = viewModel(),
    onBack: () -> Unit = {},
    onOpenGroup: (String) -> Unit = {},
    onCreateGroup: () -> Unit = {}
) {
    val ui by vm.ui.collectAsState()

    val totalBalance = ui.groups.sumOf { it.balance }
    val youAreOwed = ui.groups.filter { it.balance > 0 }.sumOf { it.balance }
    val youOwe = ui.groups.filter { it.balance < 0 }.sumOf { it.balance }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(GroupsBgStart, GroupsBgEnd))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.statusBars.asPaddingValues())     // FIXED TOP SPACING
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {

            // TOP BAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "My groups",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${ui.groups.size} active group${if (ui.groups.size == 1) "" else "s"}",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 14.sp
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.Group,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.height(18.dp))

            // OVERVIEW CARD
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.12f)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        "Overview",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        // LEFT SIDE
                        Column {
                            Text(
                                "Net balance",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 13.sp
                            )
                            Text(
                                String.format("%.2f DKK", totalBalance),
                                color = if (totalBalance >= 0) Color(0xFFB2FF59) else Color(0xFFFF8A80),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        // RIGHT SIDE
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "You’re owed",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 13.sp
                            )
                            Text(
                                String.format("%.2f DKK", youAreOwed),
                                color = Color(0xFFB2FF59),
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(Modifier.height(6.dp))

                            Text(
                                "You owe",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 13.sp
                            )
                            Text(
                                String.format("%.2f DKK", -youOwe),
                                color = Color(0xFFFF8A80),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // BOTTOM SHEET (white rounded)
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = Color.White,
                shadowElevation = 12.dp
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Your groups",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = Color(0xFF222222)
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            "${ui.groups.size}",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    when {
                        ui.isLoading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFF673AB7))
                            }
                        }

                        ui.groups.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Outlined.Group,
                                        tint = Color.LightGray,
                                        contentDescription = null,
                                        modifier = Modifier.size(70.dp)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        "No groups yet",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 18.sp
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        "Create your first group to start sharing expenses.",
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 32.dp)
                                    )
                                }
                            }
                        }

                        else -> {
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
                    }

                    Spacer(Modifier.height(20.dp))

                    // CREATE BUTTON
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF9C27B0), Color(0xFFE91E63))
                                )
                            )
                            .clickable(onClick = onCreateGroup),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.Add,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Create new group",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // ERROR POPUP
        ui.error?.let { error ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFB00020)
                ) {
                    Text(
                        error,
                        color = Color.White,
                        modifier = Modifier.padding(14.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
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
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 4.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // CIRCLE ICON
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF5C6BC0), Color(0xFFAB47BC))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Group,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    group.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF212121),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (group.description.isNotEmpty()) {
                    Text(
                        group.description,
                        color = Color.Gray,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${group.memberCount} members",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            val isPositive = group.balance >= 0
            val balanceText = (if (isPositive) "+" else "") +
                    String.format("%.2f DKK", group.balance)

            val col = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336)

            Surface(
                shape = RoundedCornerShape(50),
                color = col.copy(alpha = 0.12f)
            ) {
                Text(
                    balanceText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = col,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
