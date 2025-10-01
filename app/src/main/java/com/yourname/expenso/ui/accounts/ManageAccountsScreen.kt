package com.yourname.expenso.ui.accounts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.yourname.expenso.model.Account
import com.yourname.expenso.ui.dashboard.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageAccountsScreen(
    navController: NavController,
    viewModel: AccountViewModel = hiltViewModel(),
    dashboardViewModel: DashboardViewModel = hiltViewModel()
) {
    val accounts by viewModel.accounts.collectAsState()
    val uiState by dashboardViewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<Account?>(null) }
    var accountToDelete by remember { mutableStateOf<Account?>(null) }
    var isMultiSelectMode by remember { mutableStateOf(false) }
    var selectedAccounts by remember { mutableStateOf(setOf<Int>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (isMultiSelectMode) 
                            "Selected: ${selectedAccounts.size}" 
                        else "Manage Accounts"
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isMultiSelectMode) {
                        if (selectedAccounts.isNotEmpty()) {
                            IconButton(onClick = {
                                selectedAccounts.forEach { accountId ->
                                    accounts.find { it.id == accountId }?.let {
                                        viewModel.deleteAccount(it)
                                    }
                                }
                                selectedAccounts = emptySet()
                                isMultiSelectMode = false
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Selected")
                            }
                        }
                        TextButton(onClick = {
                            isMultiSelectMode = false
                            selectedAccounts = emptySet()
                        }) {
                            Text("Cancel")
                        }
                    } else {
                        TextButton(onClick = { isMultiSelectMode = true }) {
                            Text("Select")
                        }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = accounts.size >= 2,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Transfer Money")
            }
            
            if (accounts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No accounts yet",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Add your first account to get started",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn {
                    item {
                        Text(
                            "Tap account to edit balance.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    
                    items(accounts) { account ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isMultiSelectMode) {
                                Checkbox(
                                    checked = selectedAccounts.contains(account.id),
                                    onCheckedChange = { isSelected ->
                                        selectedAccounts = if (isSelected) {
                                            selectedAccounts + account.id
                                        } else {
                                            selectedAccounts - account.id
                                        }
                                    },
                                    modifier = Modifier.padding(start = 16.dp, end = 8.dp)
                                )
                            }
                            
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = if (isMultiSelectMode) 0.dp else 16.dp, vertical = 4.dp)
                                    .clickable { 
                                        if (isMultiSelectMode) {
                                            selectedAccounts = if (selectedAccounts.contains(account.id)) {
                                                selectedAccounts - account.id
                                            } else {
                                                selectedAccounts + account.id
                                            }
                                        } else {
                                            accountToEdit = account
                                        }
                                    },
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            account.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                        Text(
                                            account.type,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Calculate account balance from transactions
                                        val accountTransactions = remember(uiState.transactions, account.id) {
                                            uiState.transactions.filter { it.accountId == account.id }
                                        }
                                        val accountIncome = accountTransactions.filter { it.type == "Income" }.sumOf { it.amount }
                                        val accountExpense = accountTransactions.filter { it.type == "Expense" }.sumOf { it.amount }
                                        val calculatedAccountBalance = accountIncome - accountExpense
                                        
                                        Text(
                                            String.format("₹%.2f", calculatedAccountBalance),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = if (calculatedAccountBalance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                        )
                                        if (!isMultiSelectMode) {
                                            Spacer(Modifier.width(8.dp))
                                            IconButton(
                                                onClick = { navController.navigate("account_transactions/${account.id}") }
                                            ) {
                                                Text(
                                                    "📋",
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                            }
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
                text = { 
                    Column {
                        Text("Are you sure you want to delete '${account.name}'?") 
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Note: Existing transactions for this account will not be affected.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
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
            Column {
                OutlinedTextField(
                    value = balance,
                    onValueChange = { balance = it },
                    label = { Text("New Balance (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Current balance: ₹${String.format("%.2f", account.balance)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
    var type by remember { mutableStateOf("") }
    var expandedType by remember { mutableStateOf(false) }
    val types = listOf("Bank", "Cash", "UPI", "Credit Card", "Investment", "Other")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Account") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = balance,
                    onValueChange = { balance = it },
                    label = { Text("Initial Balance") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                // Type selection with dropdown
                Box {
                    OutlinedTextField(
                        value = if (type.isNotEmpty()) type else "Select Account Type",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Account Type") },
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { expandedType = !expandedType }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedType = !expandedType }
                    )
                    DropdownMenu(
                        expanded = expandedType,
                        onDismissRequest = { expandedType = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        types.forEach { accountType ->
                            DropdownMenuItem(
                                text = { Text(accountType) },
                                onClick = {
                                    type = accountType
                                    expandedType = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (name.isNotBlank() && balance.toDoubleOrNull() != null && type.isNotEmpty()) {
                        onConfirm(name, balance.toDoubleOrNull() ?: 0.0, type)
                    }
                },
                enabled = name.isNotBlank() && balance.toDoubleOrNull() != null && type.isNotEmpty()
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
                Text(
                    "From Account:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                accounts.forEach { account ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { fromAccount = account }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = fromAccount == account,
                            onClick = { fromAccount = account }
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                account.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "₹${String.format("%.2f", account.balance)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    "To Account:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                accounts.forEach { account ->
                    if (account != fromAccount) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { toAccount = account }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = toAccount == account,
                                onClick = { toAccount = account }
                            )
                            Text(
                                account.name,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
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