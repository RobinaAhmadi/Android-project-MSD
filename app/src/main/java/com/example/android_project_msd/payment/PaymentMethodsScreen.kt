package com.example.android_project_msd.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodsScreen(
    vm: PaymentMethodsViewModel,
    onBack: () -> Unit
) {
    val ui by vm.ui.collectAsState()

    val gradient = Brush.verticalGradient(
        listOf(
            Color(0xFF131B63),
            Color(0xFF481162)
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Payment Methods", color = Color.White, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {

            Column(modifier = Modifier.fillMaxWidth()) {

                // Wallet Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Wallet, contentDescription = null, tint = Color.White)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Your Wallet", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Add or remove payment methods",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Glass Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    color = Color.White.copy(alpha = 0.12f),
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {

                        if (ui.methods.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Outlined.CreditCard, contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "No payment methods yet",
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "Add a card or PayPal to get started",
                                    color = Color.White.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        } else {
                            Text("Saved methods", color = Color.White, fontWeight = FontWeight.SemiBold)

                            ui.methods.forEach { method ->
                                PaymentMethodRow(
                                    method = method,
                                    onClick = { vm.selectForOptions(method) }
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Text("Add new", color = Color.White, fontWeight = FontWeight.SemiBold)

                        GlassAddButton("Add card", Icons.Outlined.CreditCard) {
                            vm.showCardDialog(true)
                        }

                        GlassAddButton("Add PayPal", Icons.Outlined.Payment) {
                            vm.showPayPalDialog(true)
                        }
                    }
                }
            }

            // Dialogs
            if (ui.showAddCardDialog) {
                AddCardDialog(
                    onDismiss = { vm.showCardDialog(false) },
                    onSave = { number, holder ->
                        vm.addCard(number, holder)
                        vm.showCardDialog(false)
                    }
                )
            }

            if (ui.showAddPayPalDialog) {
                AddPayPalDialog(
                    onDismiss = { vm.showPayPalDialog(false) },
                    onSave = { email ->
                        vm.addPayPal(email)
                        vm.showPayPalDialog(false)
                    }
                )
            }

            ui.selectedForOptions?.let { selected ->
                AlertDialog(
                    onDismissRequest = { vm.selectForOptions(null) },
                    title = { Text("Payment method") },
                    text = { Text(selected.primaryText) },
                    confirmButton = {
                        TextButton(onClick = {
                            vm.removeMethod(selected)
                        }) {
                            Text("Remove")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { vm.selectForOptions(null) }) {
                            Text("Close")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun PaymentMethodRow(
    method: PaymentMethod,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.10f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    when (method.type) {
                        PaymentType.CARD -> Brush.horizontalGradient(
                            listOf(Color(0xFF1A237E), Color(0xFF3949AB))
                        )
                        else -> Brush.horizontalGradient(
                            listOf(Color(0xFF003087), Color(0xFF009CDE))
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (method.type == PaymentType.CARD) "💳" else "🅿️",
                color = Color.White, fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.width(12.dp))

        Column {
            Text(method.primaryText, color = Color.White, fontWeight = FontWeight.Medium)
            Text(
                method.secondaryText,
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun GlassAddButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.10f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color.White)
        Spacer(Modifier.width(10.dp))
        Text(text, color = Color.White, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        Icon(Icons.Outlined.Add, null, tint = Color.White)
    }
}

@Composable
private fun AddCardDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var number by remember { mutableStateOf("") }
    var holder by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add card") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = number,
                    onValueChange = { number = it.filter { c -> c.isDigit() || c == ' ' }.take(19) },
                    label = { Text("Card number") }
                )
                OutlinedTextField(
                    value = holder,
                    onValueChange = { holder = it },
                    label = { Text("Card holder name") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(number, holder) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddPayPalDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add PayPal") },
        text = {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("PayPal email") }
            )
        },
        confirmButton = {
            TextButton(
                enabled = email.contains("@"),
                onClick = { onSave(email) }
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
