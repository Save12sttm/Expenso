package com.yourname.expenso.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.yourname.expenso.model.Transaction
import com.yourname.expenso.ui.components.*
import com.yourname.expenso.ui.theme.FinancialColors
import kotlinx.coroutines.delay
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedDashboardScreen(
    navController: NavController,
    windowSizeClass: WindowSizeClass,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isExpandedScreen = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    var selectedMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var isRefreshing by remember { mutableStateOf(false) }
    

    
    LaunchedEffect(uiState) {
        if (isRefreshing) {
            delay(1000)
            isRefreshing = false
        }
    }
    


    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    EnhancedMonthSelector(
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
                onClick = { navController.navigate("add_transaction") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)

        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        EnhancedBalanceCard(
                            balance = uiState.balance
                        )
                        EnhancedSpendingCard(
                            totalExpense = uiState.totalExpense
                        )
                    }
                }
                

                

                
                item {
                    EnhancedCategorySpending(
                        transactions = uiState.transactions,
                        selectedMonth = selectedMonth,
                        selectedYear = selectedYear
                    )
                }
                
                item {
                    EnhancedQuickActions(navController, isExpandedScreen)
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Recent Activity",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { navController.navigate("transactions") }) {
                            Text("View All")
                        }
                    }
                }
                
                itemsIndexed(
                    items = uiState.transactions.take(5),
                    key = { _, transaction -> "${transaction.id}-${transaction.date}" }
                ) { index, transaction ->
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(200, delayMillis = index * 50)
                        ) + fadeIn(animationSpec = tween(200, delayMillis = index * 50))
                    ) {
                        SwipeableTransactionCard(
                            transaction = transaction,
                            onEdit = { },
                            onDelete = { viewModel.softDeleteTransaction(transaction) }
                        )
                    }
                }
            }
            

        }
    }
}

@Composable
fun EnhancedMonthSelector(
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
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(
            onClick = {
                if (selectedMonth > 0) {
                    onMonthChanged(selectedMonth - 1, selectedYear)
                } else {
                    onMonthChanged(11, selectedYear - 1)
                }
            }
        ) {
            Icon(
                Icons.Default.KeyboardArrowLeft,
                contentDescription = "Previous month",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Text(
            "${months[selectedMonth]} $selectedYear",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        IconButton(
            onClick = {
                if (selectedMonth < 11) {
                    onMonthChanged(selectedMonth + 1, selectedYear)
                } else {
                    onMonthChanged(0, selectedYear + 1)
                }
            }
        ) {
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = "Next month",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun EnhancedCategorySpending(
    transactions: List<Transaction>,
    selectedMonth: Int,
    selectedYear: Int
) {
    val monthlyTransactions = transactions.filter { transaction ->
        val cal = Calendar.getInstance().apply { timeInMillis = transaction.date }
        cal.get(Calendar.MONTH) == selectedMonth && 
        cal.get(Calendar.YEAR) == selectedYear && 
        transaction.type == "Expense"
    }
    
    val categorySpending = monthlyTransactions
        .groupBy { it.title.split(" ").firstOrNull() ?: "Other" }
        .mapValues { it.value.sumOf { transaction -> transaction.amount } }
        .toList()
        .sortedByDescending { it.second }
        .take(5)
    
    val totalSpending = categorySpending.sumOf { it.second }
    
    if (categorySpending.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Top Spending Categories",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            categorySpending.forEach { (category, amount) ->
                val percentage = if (totalSpending > 0) (amount / totalSpending).toFloat() else 0f
                CategorySpendingBar(
                    categoryName = category,
                    amount = amount,
                    percentage = percentage,
                    categoryIcon = getCategoryIcon(category)
                )
            }
        }
    }
}

@Composable
fun EnhancedQuickActions(
    navController: NavController,
    isExpandedScreen: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    EnhancedQuickActionButton(
                        icon = Icons.Default.KeyboardArrowDown,
                        label = "Expense",
                        color = FinancialColors.ExpenseRed
                    ) { navController.navigate("add_transaction/expense") }
                    
                    EnhancedQuickActionButton(
                        icon = Icons.Default.KeyboardArrowUp,
                        label = "Income",
                        color = FinancialColors.IncomeGreen
                    ) { navController.navigate("add_transaction/income") }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    EnhancedQuickActionButton(
                        icon = Icons.Default.Info,
                        label = "Reports",
                        color = FinancialColors.SavingsBlue
                    ) { navController.navigate("reports") }
                    
                    EnhancedQuickActionButton(
                        icon = Icons.Default.Settings,
                        label = "Settings",
                        color = MaterialTheme.colorScheme.outline
                    ) { navController.navigate("settings") }
                }
            }
        }
    }
}

private fun getCategoryIcon(category: String): String {
    return when {
        category.contains("food", ignoreCase = true) -> "🍽️"
        category.contains("transport", ignoreCase = true) -> "🚗"
        category.contains("shop", ignoreCase = true) -> "🛍️"
        category.contains("bill", ignoreCase = true) -> "💵"
        category.contains("enter", ignoreCase = true) -> "🎬"
        category.contains("health", ignoreCase = true) -> "🏥"
        else -> "💰"
    }
}