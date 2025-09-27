package com.yourname.expenso.ui.accounts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.yourname.expenso.model.Account

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageAccountsScreen(
    navController: NavController,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val accounts by viewModel.accounts.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<Account?>(null) }
    var accountToDelete by remember { mutableStateOf<Account?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Accounts") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Account")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Button(
                onClick = { showTransferDialog = true },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                enabled = accounts.size >= 2
            ) {
                Text("Transfer Money")
            }
            
            LazyColumn {
                items(accounts) { account ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { accountToEdit = account }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(account.name, style = MaterialTheme.typography.bodyLarge)
                                Text(account.type, style = MaterialTheme.typography.bodySmall)
                            }
                            Row {
                                Text(
                                    String.format("₹%.2f", account.balance),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                IconButton(
                                    onClick = { accountToDelete = account }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete Account",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (showAddDialog) {
            AddAccountDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { name, balance, type ->
                    viewModel.addAccount(name, balance, type)
                    showAddDialog = false
                }
            )
        }
        
        if (showTransferDialog) {
            TransferMoneyDialog(
                accounts = accounts,
                onDismiss = { showTransferDialog = false },
                onConfirm = { from, to, amount ->
                    viewModel.transferMoney(from, to, amount)
                    showTransferDialog = false
                }
            )
        }
        
        accountToEdit?.let { account ->
            EditAccountBalanceDialog(
                account = account,
                onDismiss = { accountToEdit = null },
                onConfirm = { newBalance ->
                    viewModel.updateAccountBalance(account, newBalance)
                    accountToEdit = null
                }
            )
        }
        
        accountToDelete?.let { account ->
            AlertDialog(
                onDismissRequest = { accountToDelete = null },
                title = { Text("Delete Account") },
                text = { Text("Are you sure you want to delete '${account.name}'? This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteAccount(account)
                            accountToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { accountToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun EditAccountBalanceDialog(
    account: Account,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var balance by remember { mutableStateOf(account.balance.toString()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Balance: ${account.name}") },
        text = {
            OutlinedTextField(
                value = balance,
                onValueChange = { balance = it },
                label = { Text("New Balance (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val newBalance = balance.toDoubleOrNull()
                    if (newBalance != null) {
                        onConfirm(newBalance)
                    }
                },
                enabled = balance.toDoubleOrNull() != null
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddAccountDialog(onDismiss: () -> Unit, onConfirm: (String, Double, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Bank") }
    val types = listOf("Bank", "Cash", "UPI")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Account") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name") }
                )
                OutlinedTextField(
                    value = balance,
                    onValueChange = { balance = it },
                    label = { Text("Initial Balance") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Row {
                    types.forEach { accountType ->
                        Row {
                            RadioButton(
                                selected = type == accountType,
                                onClick = { type = accountType }
                            )
                            Text(accountType)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, balance.toDoubleOrNull() ?: 0.0, type) },
                enabled = name.isNotBlank() && balance.toDoubleOrNull() != null
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TransferMoneyDialog(
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onConfirm: (Account, Account, Double) -> Unit
) {
    var fromAccount by remember { mutableStateOf<Account?>(null) }
    var toAccount by remember { mutableStateOf<Account?>(null) }
    var amount by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Transfer Money") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("From Account:")
                accounts.forEach { account ->
                    Row {
                        RadioButton(
                            selected = fromAccount == account,
                            onClick = { fromAccount = account }
                        )
                        Text("${account.name} (₹${String.format("%.2f", account.balance)})")
                    }
                }
                Text("To Account:")
                accounts.forEach { account ->
                    if (account != fromAccount) {
                        Row {
                            RadioButton(
                                selected = toAccount == account,
                                onClick = { toAccount = account }
                            )
                            Text(account.name)
                        }
                    }
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val from = fromAccount
                    val to = toAccount
                    val amt = amount.toDoubleOrNull()
                    if (from != null && to != null && amt != null && amt > 0) {
                        onConfirm(from, to, amt)
                    }
                },
                enabled = fromAccount != null && toAccount != null && 
                         amount.toDoubleOrNull()?.let { it > 0 } == true
            ) {
                Text("Transfer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}