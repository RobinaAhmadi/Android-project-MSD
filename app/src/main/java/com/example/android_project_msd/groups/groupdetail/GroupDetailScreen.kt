package com.example.android_project_msd.groups.groupdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.tooling.preview.Preview
import com.example.android_project_msd.groups.groupdetail.GroupDetailViewModel
import com.example.android_project_msd.groups.data.Expense
import com.example.android_project_msd.groups.data.Settlement

@Composable
fun GroupDetailRoute(
    groupId: String,
    vm: GroupDetailViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val ui by vm.ui.collectAsState()
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showRecordPaymentDialog by remember {mutableStateOf(false)}

    LaunchedEffect(groupId) {
        vm.loadGroup(groupId)
    }

    Box(Modifier.fillMaxSize()) {
        // Top gradient header
        Box(
            Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF112A66), Color(0xFF0B1B3D))
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
                Column(Modifier.weight(1f)) {
                    Text(
                        ui.group?.name ?: "Group",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    if (ui.group?.description?.isNotEmpty() == true) {
                        Text(
                            ui.group!!.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                IconButton(onClick = { /* TODO: Group settings */ }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }
            }

            // Balance card in header
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 4.dp,
                color = Color.White
            ) {
                Column(
                    Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Your Balance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF757575)
                    )
                    Text(
                        if (ui.userBalance >= 0) "+${ui.userBalance} DKK" else "${ui.userBalance} DKK",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (ui.userBalance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                    )
                    Text(
                        if (ui.userBalance >= 0) "You are owed" else "You owe",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9E9E9E)
                    )

                    // For when all debts are settled
                    if (ui.isDebtsSettled && ui.expenses.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "All debts settled!",
                                color = Color(0xFF4CAF50),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // White rounded content area
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 44.dp, topEnd = 44.dp),
                tonalElevation = 2.dp,
                shadowElevation = 10.dp,
                color = Color.White
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Action buttons
                    item {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ActionButton(
                                text = "Add Expense",
                                icon = Icons.Default.Add,
                                onClick = { showAddExpenseDialog = true },
                                modifier = Modifier.weight(1f),
                                isPrimary = true
                            )
                            ActionButton(
                                text = "Add Member",
                                icon = Icons.Default.PersonAdd,
                                onClick = { showAddMemberDialog = true },
                                modifier = Modifier.weight(1f),
                                isPrimary = false
                            )
                        }
                        Spacer(Modifier.height(12.dp))

                        //Record/add payment button
                        ActionButton(
                            text = "Record Payment",
                            icon = Icons.Default.Payment,
                            onClick = { showRecordPaymentDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            isPrimary = false
                        )

                        Spacer(Modifier.height(24.dp))
                    }

                    //Section for settlement suggestion
                    if(ui.settlements.isNotEmpty() && !ui.isDebtsSettled) {
                        item {
                            SectionHeader("Settlement Suggestion")
                            Text(
                                "Optimal way to settle ALL debts",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF757575)
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                        items(ui.settlements) { settlement ->
                            SettlementCard(settlement = settlement)
                            Spacer(Modifier.height(8.dp))
                        }
                        item { Spacer(Modifier.height(22.dp))}
                    }

                    // Members section
                    item {
                        SectionHeader("Members")
                        Spacer(Modifier.height(12.dp))
                    }

                    items(ui.members) { member ->
                        MemberItem(member)
                        Spacer(Modifier.height(8.dp))
                    }

                    item {
                        Spacer(Modifier.height(24.dp))
                        SectionHeader("Expenses")
                        Spacer(Modifier.height(12.dp))
                    }

                    // Expenses section
                    if (ui.expenses.isEmpty()) {
                        item {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.Receipt,
                                        contentDescription = null,
                                        modifier = Modifier.size(60.dp),
                                        tint = Color(0xFFE0E0E0)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        "No expenses yet",
                                        color = Color(0xFF9E9E9E),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    } else {
                        items(ui.expenses) { expense ->
                            ExpenseItem(expense)
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showAddExpenseDialog) {
        AddExpenseDialog(
            members = ui.members,
            onDismiss = { showAddExpenseDialog = false },
            onAdd = { description, amount, paidBy, splitAmong ->
                vm.addExpense(description, amount, paidBy, splitAmong)
                showAddExpenseDialog = false
            }
        )
    }

    if (showAddMemberDialog) {
        AddMemberDialog(
            onDismiss = { showAddMemberDialog = false },
            onAdd = { name, email ->
                vm.addMember(name, email)
                showAddMemberDialog = false
            }
        )
    }

    if (showRecordPaymentDialog) {
        RecordPaymentDialog(
            members = ui.members,
            onDismiss = { showAddMemberDialog = false },
            onRecord = { from, to, amount ->
                vm.recordPayment(from, to, amount)
                showRecordPaymentDialog = false
            }
        )
    }
}

@Composable
fun SettlementCard(settlement: com.example.android_project_msd.groups.data.Settlement) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp,
        color = Color(0xFFFFF8E1)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.TrendingFlat,
                contentDescription = null,
                tint = Color(0xFFF57C00),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "${settlement.fromPerson} owe ${settlement.toPerson}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    "Suggested settlement",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575)
                )
            }
            Text(
                "${settlement.amount} DKK",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF57C00)
                )
            )
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isPrimary) {
                    Brush.horizontalGradient(
                        listOf(Color(0xFF163A96), Color(0xFF0B1A3A))
                    )
                } else {
                    Brush.horizontalGradient(
                        listOf(Color(0xFFF5F5F5), Color(0xFFEEEEEE))
                    )
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isPrimary) Color.White else Color(0xFF163A96),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text,
                color = if (isPrimary) Color.White else Color(0xFF163A96),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E1E1E)
        )
    )
}

@Composable
fun MemberItem(member: GroupMember) {
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
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    if (member.balance >= 0) "+${member.balance}" else "${member.balance}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (member.balance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                )
                Text(
                    "DKK",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9E9E9E)
                )
            }
        }
    }
}

@Composable
fun ExpenseItem(expense: Expense) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (expense.description.contains("Payment", ignoreCase = true))
                        Icons.Default.Payment
                    else
                        Icons.Default.Receipt,
                    contentDescription = null,
                    tint = Color(0xFF163A96),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    expense.description,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    "Paid by ${expense.paidBy}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575)
                )
                Text(
                    expense.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9E9E9E)
                )
            }
            Text(
                "${expense.amount} DKK",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E1E)
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    members: List<GroupMember>,
    onDismiss: () -> Unit,
    onAdd: (String, Double, String, List<String>) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedPayer by remember { mutableStateOf(members.firstOrNull()?.name ?: "") }
    var expandedPayer by remember { mutableStateOf(false) }
    val selectedSplitMembers =
        remember { mutableStateListOf<String>().apply { addAll(members.map { it.name }) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add Expense",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        },
        text = {
            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("e.g., Dinner at restaurant") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it.filter { char -> char.isDigit() || char == '.' }
                    },
                    label = { Text("Amount (DKK)") },
                    placeholder = { Text("0.00") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Paid by dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedPayer,
                    onExpandedChange = { expandedPayer = !expandedPayer }
                ) {
                    OutlinedTextField(
                        value = selectedPayer,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Paid by") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPayer) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPayer,
                        onDismissRequest = { expandedPayer = false }
                    ) {
                        members.forEach { member ->
                            DropdownMenuItem(
                                text = { Text(member.name) },
                                onClick = {
                                    selectedPayer = member.name
                                    expandedPayer = false
                                }
                            )
                        }
                    }
                }

                Text(
                    "Split among:",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                )

                members.forEach { member ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = selectedSplitMembers.contains(member.name),
                            onCheckedChange = { checked ->
                                if (checked) {
                                    selectedSplitMembers.add(member.name)
                                } else {
                                    selectedSplitMembers.remove(member.name)
                                }
                            }
                        )
                        Text(member.name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull() ?: 0.0
                    onAdd(
                        description,
                        amountDouble,
                        selectedPayer,
                        selectedSplitMembers.toList()
                    )
                },
                enabled = description.isNotBlank() && amount.isNotBlank() && selectedSplitMembers.isNotEmpty()
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

@Composable
fun AddMemberDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
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
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    placeholder = { Text("John Doe") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
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
                onClick = { onAdd(name, email) },
                enabled = name.isNotBlank() && email.isNotBlank()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordPaymentDialog(
    members: List<GroupMember>,
    onDismiss: () -> Unit,
    onRecord: (String, String, Double) -> Unit
) {
    var amount by remember { mutableStateOf("")}
    var selectedFrom by remember { mutableStateOf(members.firstOrNull()?.name ?: "")}
    var selectedTo by remember { mutableStateOf(members.getOrNull(1)?.name ?: "") }
    var expandedFrom by remember { mutableStateOf(false) }
    var expandedTo by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Record Payment",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        },
        text = {
            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Record when someone pays their debt",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575)
                )

                // From dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedFrom,
                    onExpandedChange = { expandedFrom = !expandedFrom }
                ) {
                    OutlinedTextField(
                        value = selectedFrom,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("From (payer)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFrom) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedFrom,
                        onDismissRequest = { expandedFrom = false }
                    ) {
                        members.forEach { member ->
                            DropdownMenuItem(
                                text = { Text(member.name) },
                                onClick = {
                                    selectedFrom = member.name
                                    expandedFrom = false
                                }
                            )
                        }
                    }
                }

                // To dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedTo,
                    onExpandedChange = { expandedTo = !expandedTo }
                ) {
                    OutlinedTextField(
                        value = selectedTo,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("To (receiver)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTo) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedTo,
                        onDismissRequest = { expandedTo = false }
                    ) {
                        members.forEach { member ->
                            DropdownMenuItem(
                                text = { Text(member.name) },
                                onClick = {
                                    selectedTo = member.name
                                    expandedTo = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it.filter { char -> char.isDigit() || char == '.' }
                    },
                    label = { Text("Amount (DKK)") },
                    placeholder = { Text("0.00") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull() ?: 0.0
                    onRecord(selectedFrom, selectedTo, amountDouble)
                },
                enabled = amount.isNotBlank() && selectedFrom != selectedTo
            ) {
                Text("RECORD")
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
fun GroupDetailPreview() {
    MaterialTheme {
        GroupDetailRoute(groupId = "1", onBack = {})
    }
}