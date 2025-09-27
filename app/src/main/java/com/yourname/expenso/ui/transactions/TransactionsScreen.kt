package com.yourname.expenso.ui.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.yourname.expenso.ui.dashboard.DashboardViewModel
import com.yourname.expenso.ui.dashboard.TransactionItem
import com.yourname.expenso.model.Transaction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var filterType by remember { mutableStateOf("All") }
    var isMultiSelectMode by remember { mutableStateOf(false) }
    var selectedTransactions by remember { mutableStateOf(setOf<Int>()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    val filteredTransactions = uiState.transactions.filter { transaction ->
        val matchesSearch = transaction.title.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (filterType) {
            "Income" -> transaction.type == "Income"
            "Expense" -> transaction.type == "Expense"
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (isMultiSelectMode) 
                            "Selected: ${selectedTransactions.size}" 
                        else "Transactions (${filteredTransactions.size})"
                    ) 
                },
                actions = {
                    if (isMultiSelectMode) {
                        if (selectedTransactions.isNotEmpty()) {
                            IconButton(onClick = { showDeleteDialog = true }) {
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
                        IconButton(onClick = { showFilters = !showFilters }) {
                            Text("⚙")
                        }
                        IconButton(onClick = { navController.navigate("settings") }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add_transaction") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search transactions...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )
            
            // Filter Chips
            if (showFilters) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Income", "Expense").forEach { filter ->
                        FilterChip(
                            onClick = { filterType = filter },
                            label = { Text(filter) },
                            selected = filterType == filter
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            
            // Multi-select controls
            if (isMultiSelectMode && filteredTransactions.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            selectedTransactions = filteredTransactions.map { it.id }.toSet()
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
            
            // Transactions List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(filteredTransactions) { transaction ->
                    MultiSelectTransactionItem(
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
                        onDelete = { viewModel.softDeleteTransaction(it) }
                    )
                }
                
                if (filteredTransactions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No transactions found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        
        if (showDeleteDialog) {
            BulkDeleteDialog(
                count = selectedTransactions.size,
                onConfirm = {
                    selectedTransactions.forEach { transactionId ->
                        filteredTransactions.find { it.id == transactionId }?.let {
                            viewModel.softDeleteTransaction(it)
                        }
                    }
                    selectedTransactions = emptySet()
                    isMultiSelectMode = false
                    showDeleteDialog = false
                },
                onDismiss = { showDeleteDialog = false }
            )
        }
    }
}

@Composable
fun MultiSelectTransactionItem(
    transaction: Transaction,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit,
    onDelete: (Transaction) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isMultiSelectMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = onSelectionChanged,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            TransactionItem(transaction, onDelete)
        }
    }
}

@Composable
fun BulkDeleteDialog(
    count: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Transactions") },
        text = { Text("Are you sure you want to delete $count selected transactions?") },
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