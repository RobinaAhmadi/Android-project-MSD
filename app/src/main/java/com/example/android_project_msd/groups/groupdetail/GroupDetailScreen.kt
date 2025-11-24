package com.example.android_project_msd.groups.groupdetail

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.android_project_msd.data.UserSession
import com.example.android_project_msd.groups.data.Expense
import com.example.android_project_msd.groups.data.Settlement


private val BgStart = Color(0xFF1F1F7A)
private val BgEnd = Color(0xFF4C1E78)



@Composable
fun GroupDetailRoute(
    groupId: String,
    vm: GroupDetailViewModel = viewModel(),
    onBack: () -> Unit = {},
    onSettings: () -> Unit = {},
    onOpenNotifications: () -> Unit = {}
) {
    val ui by vm.ui.collectAsState()
    val memberDisplayNames = remember(ui.members) {
        ui.members.associate { it.name to it.displayName.ifBlank { it.name } }
    }
    val currentUserActualName = remember(ui.members) {
        val userId = UserSession.currentUserId
        ui.members.find { it.id == userId }?.name
    }

    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showRecordPaymentDialog by remember { mutableStateOf(false) }

    LaunchedEffect(groupId) { vm.loadGroup(groupId) }

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
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        ui.group?.name ?: "Group",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.White
                    )

                    ui.group?.description?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            it,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                }

                IconButton(
                    onClick = onSettings,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }
            }



            Spacer(Modifier.height(20.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.12f)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        "Your balance",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )

                    val balance = ui.userBalance
                    val positive = balance >= 0
                    val color =
                        if (positive) Color(0xFFB2FF59) else Color(0xFFFF8A80)

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = (if (positive) "+" else "") +
                                String.format("%.2f DKK", balance),
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = color
                    )

                    Text(
                        text = if (positive) "You are owed" else "You owe",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )

                    if (ui.isDebtsSettled && ui.expenses.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "All debts settled!",
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }



            Spacer(Modifier.height(24.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = Color.White,
                shadowElevation = 12.dp
            ) {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {



                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ActionButton(
                                text = "Add expense",
                                icon = Icons.Default.Add,
                                onClick = { showAddExpenseDialog = true },
                                modifier = Modifier.weight(1f),
                                isPrimary = true
                            )
                            ActionButton(
                                text = "Add member",
                                icon = Icons.Default.PersonAdd,
                                onClick = { showAddMemberDialog = true },
                                modifier = Modifier.weight(1f),
                                isPrimary = false
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        ActionButton(
                            text = "Record payment",
                            icon = Icons.Default.Payment,
                            onClick = { showRecordPaymentDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            isPrimary = false
                        )

                        Spacer(Modifier.height(24.dp))
                    }



                    if (ui.settlements.isNotEmpty() && !ui.isDebtsSettled) {

                        item {
                            SectionHeader("Settlement suggestion")
                            Text(
                                "Optimal way to settle all debts",
                                color = Color(0xFF757575),
                                fontSize = 13.sp
                            )
                            Spacer(Modifier.height(12.dp))
                        }

                        items(ui.settlements) { settlement ->
                            val isYouCreditor = currentUserActualName != null &&
                                    settlement.toPerson == currentUserActualName

        SettlementCard(
            settlement = settlement,
            showReminder = isYouCreditor,
            onReminderClick = {
                vm.sendReminderForSettlement(settlement)
                onOpenNotifications()
            },
            nameLookup = memberDisplayNames
        )

                            Spacer(Modifier.height(10.dp))
                        }

                        item { Spacer(Modifier.height(24.dp)) }
                    }



                    item {
                        SectionHeader("Members")
                        Spacer(Modifier.height(12.dp))
                    }

                    items(ui.members) { member ->
                        MemberItem(member)
                        Spacer(Modifier.height(10.dp))
                    }



                    item {
                        Spacer(Modifier.height(24.dp))
                        SectionHeader("Expenses")
                        Spacer(Modifier.height(12.dp))
                    }

                    if (ui.expenses.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.Receipt,
                                        contentDescription = null,
                                        tint = Color(0xFFE0E0E0),
                                        modifier = Modifier.size(60.dp)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        "No expenses yet",
                                        color = Color(0xFF9E9E9E)
                                    )
                                }
                            }
                        }
                    } else {
                        items(ui.expenses) { expense ->
                            ExpenseItem(expense, memberDisplayNames)
                            Spacer(Modifier.height(10.dp))
                        }
                    }

                    item { Spacer(Modifier.height(12.dp)) }
                }
            }
        }
    }


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
            onAdd = { email ->
                vm.addMember(email)
                showAddMemberDialog = false
            }
        )
    }

    if (showRecordPaymentDialog) {
        RecordPaymentDialog(
            members = ui.members,
            onDismiss = { showRecordPaymentDialog = false },
            onRecord = { from, to, amount ->
                vm.recordPayment(from, to, amount)
                showRecordPaymentDialog = false
            }
        )
    }
}


@Composable
fun SettlementCard(
    settlement: Settlement,
    showReminder: Boolean,
    onReminderClick: () -> Unit,
    nameLookup: Map<String, String> = emptyMap()
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
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

            Column(
                modifier = Modifier.weight(1f)
            ) {
                val fromDisplay = nameLookup[settlement.fromPerson] ?: settlement.fromPerson
                val toDisplay = nameLookup[settlement.toPerson] ?: settlement.toPerson

                Text(
                    "$fromDisplay owes $toDisplay",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Text(
                    "Suggested settlement",
                    color = Color(0xFF757575),
                    fontSize = 13.sp
                )
            }

            Text(
                "${settlement.amount} DKK",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFFF57C00)
            )

            if (showReminder) {
                Spacer(Modifier.width(6.dp))
                TextButton(onClick = onReminderClick) {
                    Text(
                        "Remind",
                        color = Color(0xFFF57C00),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = Color(0xFF1E1E1E)
    )
}

@Composable
fun MemberItem(member: GroupMember) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 1.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF5C6BC0), Color(0xFFAB47BC))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                val label = member.displayName.ifBlank { member.name }
                val initial = label.firstOrNull()?.uppercase() ?: ""
                Text(
                    initial,
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    member.displayName.ifBlank { member.name },
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Text(
                    member.email,
                    color = Color(0xFF757575),
                    fontSize = 13.sp
                )
            }

            val pos = member.balance >= 0
            val color =
                if (pos) Color(0xFF4CAF50) else Color(0xFFF44336)

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    (if (pos) "+" else "") +
                            String.format("%.2f", member.balance),
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    "DKK",
                    color = Color(0xFF9E9E9E),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun ExpenseItem(expense: Expense, nameLookup: Map<String, String>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 1.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
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
                    if (expense.description.contains("Payment", true))
                        Icons.Default.Payment
                    else Icons.Default.Receipt,
                    contentDescription = null,
                    tint = Color(0xFF9C27B0),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    expense.description,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                val payerDisplay = nameLookup[expense.paidBy] ?: expense.paidBy

                Text(
                    "Paid by $payerDisplay",
                    color = Color(0xFF757575),
                    fontSize = 13.sp
                )
                Text(
                    expense.date,
                    color = Color(0xFF9E9E9E),
                    fontSize = 12.sp
                )
            }

            Text(
                "${expense.amount} DKK",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
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
    isPrimary: Boolean
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (isPrimary)
                    Brush.horizontalGradient(
                        listOf(Color(0xFF9C27B0), Color(0xFFE91E63))
                    )
                else
                    Brush.horizontalGradient(
                        listOf(Color(0xFFF5F5F5), Color(0xFFEEEEEE))
                    )
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {

            Icon(
                icon,
                contentDescription = null,
                tint = if (isPrimary) Color.White else Color(0xFF9C27B0),
                modifier = Modifier.size(20.dp)
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text,
                color = if (isPrimary) Color.White else Color(0xFF9C27B0),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
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
        remember(members) { mutableStateListOf<String>().apply { addAll(members.map { it.name }) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add expense",
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )
        },
        text = {

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("e.g. Dinner at restaurant") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it.filter { c -> c.isDigit() || c == '.' }
                    },
                    label = { Text("Amount (DKK)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )



                ExposedDropdownMenuBox(
                    expanded = expandedPayer,
                    onExpandedChange = { expandedPayer = !expandedPayer }
                ) {

                    val payerDisplay = members.firstOrNull { it.name == selectedPayer }?.displayName
                        ?: selectedPayer

                    OutlinedTextField(
                        value = payerDisplay,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Paid by") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPayer)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = expandedPayer,
                        onDismissRequest = { expandedPayer = false }
                    ) {
                        members.forEach { member ->
                            DropdownMenuItem(
                                text = { Text(member.displayName.ifBlank { member.name }) },
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
                    fontWeight = FontWeight.SemiBold
                )

                members.forEach { member ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedSplitMembers.contains(member.name),
                            onCheckedChange = { check ->
                                if (check) selectedSplitMembers.add(member.name)
                                else selectedSplitMembers.remove(member.name)
                            }
                        )
                        Text(member.displayName.ifBlank { member.name })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull() ?: 0.0
                    onAdd(description, amountDouble, selectedPayer, selectedSplitMembers)
                },
                enabled = description.isNotBlank() && amount.isNotBlank()
            ) {
                Text("ADD")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL") }
        }
    )
}

@Composable
fun AddMemberDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add member",
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )
        },
        text = {

            Column {

                Text(
                    "Enter email address of the member.",
                    color = Color(0xFF757575)
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = email.contains("@") && email.isNotBlank(),
                onClick = { onAdd(email) }
            ) {
                Text("ADD")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL") }
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

    var amount by remember { mutableStateOf("") }
    var selectedFrom by remember { mutableStateOf(members.firstOrNull()?.name ?: "") }
    var selectedTo by remember { mutableStateOf(members.getOrNull(1)?.name ?: "") }

    var expandedFrom by remember { mutableStateOf(false) }
    var expandedTo by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Record payment",
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )
        },
        text = {

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Text(
                    "Record when a member pays their debt.",
                    color = Color(0xFF757575)
                )



                ExposedDropdownMenuBox(
                    expanded = expandedFrom,
                    onExpandedChange = { expandedFrom = !expandedFrom }
                ) {

                    val fromDisplay = members.firstOrNull { it.name == selectedFrom }?.displayName
                        ?: selectedFrom

                    OutlinedTextField(
                        value = fromDisplay,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("From (payer)") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFrom)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = expandedFrom,
                        onDismissRequest = { expandedFrom = false }
                    ) {
                        members.forEach { member ->
                            DropdownMenuItem(
                                text = { Text(member.displayName.ifBlank { member.name }) },
                                onClick = {
                                    selectedFrom = member.name
                                    expandedFrom = false
                                }
                            )
                        }
                    }
                }


                ExposedDropdownMenuBox(
                    expanded = expandedTo,
                    onExpandedChange = { expandedTo = !expandedTo }
                ) {

                    val toDisplay = members.firstOrNull { it.name == selectedTo }?.displayName
                        ?: selectedTo

                    OutlinedTextField(
                        value = toDisplay,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("To (receiver)") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTo)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = expandedTo,
                        onDismissRequest = { expandedTo = false }
                    ) {
                        members.forEach { member ->
                            DropdownMenuItem(
                                text = { Text(member.displayName.ifBlank { member.name }) },
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
                        amount = it.filter { c -> c.isDigit() || c == '.' }
                    },
                    label = { Text("Amount (DKK)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = selectedFrom != selectedTo && amount.isNotBlank(),
                onClick = {
                    val value = amount.toDoubleOrNull() ?: 0.0
                    onRecord(selectedFrom, selectedTo, value)
                }
            ) { Text("RECORD") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL") }
        }
    )
}

@Preview(showBackground = true, heightDp = 900)
@Composable
fun PreviewDetail() {
    MaterialTheme {
        GroupDetailRoute(groupId = "1")
    }
}
