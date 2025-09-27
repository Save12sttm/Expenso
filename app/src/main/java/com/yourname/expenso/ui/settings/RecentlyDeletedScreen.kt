package com.yourname.expenso.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.yourname.expenso.model.Transaction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentlyDeletedScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val deletedTransactions by viewModel.deletedTransactions.collectAsState()
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }
    var isMultiSelectMode by remember { mutableStateOf(false) }
    var selectedTransactions by remember { mutableStateOf(setOf<Int>()) }
    var showBulkDeleteDialog by remember { mutableStateOf(false) }
    var showBulkRestoreDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (isMultiSelectMode) 
                            "Selected: ${selectedTransactions.size}" 
                        else "Recently Deleted"
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (deletedTransactions.isNotEmpty()) {
                        if (isMultiSelectMode) {
                            if (selectedTransactions.isNotEmpty()) {
                                IconButton(onClick = { showBulkRestoreDialog = true }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Restore Selected")
                                }
                                IconButton(onClick = { showBulkDeleteDialog = true }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Selected")
                                }
                            }
                            TextButton(onClick = {
                                isMultiSelectMode = false
                                selectedTransactions = emptySet()
                            }) {
                                Text("Cancel")
                            }
                        } else {
                            TextButton(onClick = { isMultiSelectMode = true }) {
                                Text("Select")
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Multi-select controls
            if (isMultiSelectMode && deletedTransactions.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            selectedTransactions = deletedTransactions.map { it.id }.toSet()
                        }
                    ) {
                        Text("Select All")
                    }
                    TextButton(
                        onClick = { selectedTransactions = emptySet() }
                    ) {
                        Text("Deselect All")
                    }
                }
            }
            
            LazyColumn {
                if (deletedTransactions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Your recycle bin is empty.",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                } else {
                    items(deletedTransactions) { transaction ->
                        MultiSelectDeletedTransactionItem(
                            transaction = transaction,
                            isMultiSelectMode = isMultiSelectMode,
                            isSelected = selectedTransactions.contains(transaction.id),
                            onSelectionChanged = { isSelected ->
                                selectedTransactions = if (isSelected) {
                                    selectedTransactions + transaction.id
                                } else {
                                    selectedTransactions - transaction.id
                                }
                            },
                            onRestore = { viewModel.restoreTransaction(it) },
                            onDeletePermanent = { transactionToDelete = it }
                        )
                    }
                }
            }
        }
        
        transactionToDelete?.let { txn ->
            DeleteConfirmationDialog(
                transactionTitle = txn.title,
                onConfirm = {
                    viewModel.permanentlyDeleteTransaction(txn)
                    transactionToDelete = null
                },
                onDismiss = {
                    transactionToDelete = null
                }
            )
        }
        
        if (showBulkDeleteDialog) {
            BulkActionDialog(
                title = "Delete Permanently",
                message = "Are you sure you want to permanently delete ${selectedTransactions.size} transactions? This action cannot be undone.",
                confirmText = "Delete",
                onConfirm = {
                    selectedTransactions.forEach { transactionId ->
                        deletedTransactions.find { it.id == transactionId }?.let {
                            viewModel.permanentlyDeleteTransaction(it)
                        }
                    }
                    selectedTransactions = emptySet()
                    isMultiSelectMode = false
                    showBulkDeleteDialog = false
                },
                onDismiss = { showBulkDeleteDialog = false }
            )
        }
        
        if (showBulkRestoreDialog) {
            BulkActionDialog(
                title = "Restore Transactions",
                message = "Are you sure you want to restore ${selectedTransactions.size} transactions?",
                confirmText = "Restore",
                onConfirm = {
                    selectedTransactions.forEach { transactionId ->
                        deletedTransactions.find { it.id == transactionId }?.let {
                            viewModel.restoreTransaction(it)
                        }
                    }
                    selectedTransactions = emptySet()
                    isMultiSelectMode = false
                    showBulkRestoreDialog = false
                },
                onDismiss = { showBulkRestoreDialog = false }
            )
        }
    }
}

@Composable
fun MultiSelectDeletedTransactionItem(
    transaction: Transaction,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit,
    onRestore: (Transaction) -> Unit,
    onDeletePermanent: (Transaction) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isMultiSelectMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = onSelectionChanged,
                modifier = Modifier.padding(start = 16.dp, end = 8.dp)
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            DeletedTransactionItem(
                transaction = transaction,
                onRestore = onRestore,
                onDeletePermanent = onDeletePermanent
            )
        }
    }
}

@Composable
fun DeletedTransactionItem(
    transaction: Transaction,
    onRestore: (Transaction) -> Unit,
    onDeletePermanent: (Transaction) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(transaction.title, style = MaterialTheme.typography.bodyLarge)
            Text(String.format("₹%.2f", transaction.amount), style = MaterialTheme.typography.bodyMedium)
        }
        IconButton(onClick = { onRestore(transaction) }) {
            Icon(Icons.Default.Refresh, contentDescription = "Restore")
        }
        IconButton(onClick = { onDeletePermanent(transaction) }) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Permanently", tint = Color.Red)
        }
    }
}

@Composable
fun BulkActionDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = if (confirmText == "Delete") 
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                else ButtonDefaults.buttonColors()
            ) {
                Text(confirmText)
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
fun DeleteConfirmationDialog(
    transactionTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permanently Delete?") },
        text = { 
            Text("Are you sure you want to permanently delete the transaction \"$transactionTitle\"? This action cannot be undone.") 
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}