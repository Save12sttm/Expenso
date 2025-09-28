package com.yourname.expenso.ui.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.yourname.expenso.model.Transaction

import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    windowSizeClass: WindowSizeClass,
    viewModel: DashboardViewModel = hiltViewModel(),

) {
    val uiState by viewModel.uiState.collectAsState()
    val isExpandedScreen = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    var selectedMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    MonthSelector(
                        selectedMonth = selectedMonth,
                        selectedYear = selectedYear,
                        onMonthChanged = { month, year ->
                            selectedMonth = month
                            selectedYear = year
                        }
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate("reports") }) {
                        Icon(Icons.Default.Info, contentDescription = "Reports")
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_transaction/expense") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // Enhanced Summary Cards Row
                item {
                    if (isExpandedScreen) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            BalanceCard(uiState, Modifier.weight(1f))
                            SpendingCard(uiState, Modifier.weight(1f))
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BalanceCard(uiState, Modifier.fillMaxWidth())
                            SpendingCard(uiState, Modifier.fillMaxWidth())
                        }
                    }
                }
                
                // Category Spending Chart
                item {
                    CategorySpendingChart(uiState.transactions, selectedMonth, selectedYear)
                }
                
                // Quick Actions
                item {
                    if (isExpandedScreen) {
                        QuickActionsRowTablet(navController)
                    } else {
                        QuickActionsRow(navController)
                    }
                }
                
                // Recent Transactions Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Recent Activity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { navController.navigate("transactions") }) {
                            Text("View All")
                        }
                    }
                }
                
                // Recent Transactions
                items(uiState.transactions.take(5)) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onDelete = viewModel::softDeleteTransaction,
                        onEdit = viewModel::updateTransaction
                    )
                }
            }
            

        }
        
    }
}

@Composable
fun BalanceCard(uiState: DashboardUiState, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Balance", style = MaterialTheme.typography.bodyMedium)
            Text(
                "₹${String.format("%.0f", uiState.balance)}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF008000)
            )
        }
    }
}

@Composable
fun SpendingCard(uiState: DashboardUiState, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("This Month", style = MaterialTheme.typography.bodyMedium)
            Text(
                "₹${String.format("%.0f", uiState.totalExpense)}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Red
            )
            Text(
                "Spent",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}



@Composable
fun CategorySpendingChart(transactions: List<Transaction>, selectedMonth: Int, selectedYear: Int) {
    val monthlyTransactions = transactions.filter { transaction ->
        val cal = Calendar.getInstance().apply { timeInMillis = transaction.date }
        cal.get(Calendar.MONTH) == selectedMonth && cal.get(Calendar.YEAR) == selectedYear && transaction.type == "Expense"
    }
    
    val categorySpending = monthlyTransactions
        .groupBy { it.title.split(" ").firstOrNull() ?: "Other" }
        .mapValues { it.value.sumOf { transaction -> transaction.amount } }
        .toList()
        .sortedByDescending { it.second }
        .take(3)
    
    if (categorySpending.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Top Spending Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                
                categorySpending.forEach { (category, amount) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "▓▓▓▓▓",
                                color = Color.Red,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(category)
                        }
                        Text(
                            "₹${String.format("%.0f", amount)}",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionsRow(navController: androidx.navigation.NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickActionButton("💸", "Expense") { navController.navigate("add_transaction/expense") }
            QuickActionButton("💰", "Income") { navController.navigate("add_transaction/income") }
            QuickActionButton("📊", "Reports") { navController.navigate("reports") }
            QuickActionButton("⚙️", "Settings") { navController.navigate("settings") }
        }
    }
}

@Composable
fun QuickActionButton(icon: String, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            icon,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun MonthSelector(
    selectedMonth: Int,
    selectedYear: Int,
    onMonthChanged: (Int, Int) -> Unit
) {
    val months = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextButton(
            onClick = {
                if (selectedMonth > 0) {
                    onMonthChanged(selectedMonth - 1, selectedYear)
                } else {
                    onMonthChanged(11, selectedYear - 1)
                }
            }
        ) { Text("◀") }
        
        Text(
            "${months[selectedMonth]} $selectedYear",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        TextButton(
            onClick = {
                if (selectedMonth < 11) {
                    onMonthChanged(selectedMonth + 1, selectedYear)
                } else {
                    onMonthChanged(0, selectedYear + 1)
                }
            }
        ) { Text("▶") }
    }
}



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionItem(
    transaction: Transaction,
    onDelete: (Transaction) -> Unit,
    onEdit: (Transaction) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    val amountColor = if (transaction.type == "Income") Color(0xFF008000) else Color.Red
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = { showEditDialog = true },
                onLongClick = { showDeleteDialog = true }
            ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = dateFormat.format(Date(transaction.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format("₹%.0f", transaction.amount),
                    color = amountColor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit transaction",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Transaction") },
            text = { Text("Move '${transaction.title}' to recycle bin?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(transaction)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showEditDialog) {
        // We'll implement this properly in the Transactions screen where we have access to accounts and categories
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Transaction") },
            text = { Text("Editing is available in the Transactions screen. Tap on a transaction in the Transactions screen to edit it.") },
            confirmButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun QuickActionsRowTablet(navController: androidx.navigation.NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickActionButtonTablet("💸", "Add Expense") { navController.navigate("add_transaction/expense") }
            QuickActionButtonTablet("💰", "Add Income") { navController.navigate("add_transaction/income") }
            QuickActionButtonTablet("📆", "View Reports") { navController.navigate("reports") }
            QuickActionButtonTablet("⚙️", "Settings") { navController.navigate("settings") }
        }
    }
}

@Composable
fun QuickActionButtonTablet(icon: String, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            icon,
            style = MaterialTheme.typography.displaySmall
        )
        Spacer(Modifier.height(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

private fun getCurrentMonthYear(): String {
    val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    return dateFormat.format(Date())
}