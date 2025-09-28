package com.yourname.expenso.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.yourname.expenso.ui.dashboard.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    
    val monthlyTransactions = uiState.transactions.filter { transaction ->
        val cal = Calendar.getInstance().apply { timeInMillis = transaction.date }
        cal.get(Calendar.MONTH) == selectedMonth && cal.get(Calendar.YEAR) == selectedYear
    }
    
    val monthlyIncome = monthlyTransactions.filter { it.type == "Income" }.sumOf { it.amount }
    val monthlyExpense = monthlyTransactions.filter { it.type == "Expense" }.sumOf { it.amount }
    val monthlyBalance = monthlyIncome - monthlyExpense
    
    val categoryBreakdown = monthlyTransactions
        .filter { it.type == "Expense" }
        .groupBy { it.title.split(" ").firstOrNull() ?: "Other" }
        .mapValues { it.value.sumOf { transaction -> transaction.amount } }
        .toList()
        .sortedByDescending { it.second }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monthly Reports") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                MonthSelector(
                    selectedMonth = selectedMonth,
                    selectedYear = selectedYear,
                    onMonthChanged = { month, year ->
                        selectedMonth = month
                        selectedYear = year
                    }
                )
            }
            
            item {
                MonthlySummaryCard(
                    income = monthlyIncome,
                    expense = monthlyExpense,
                    balance = monthlyBalance,
                    transactionCount = monthlyTransactions.size
                )
            }
            
            if (categoryBreakdown.isNotEmpty()) {
                item {
                    Text(
                        "Expense Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                items(categoryBreakdown.take(5)) { (category, amount) ->
                    CategoryBreakdownItem(
                        category = category,
                        amount = amount,
                        percentage = (amount / monthlyExpense * 100).toInt()
                    )
                }
            }
            
            item {
                DailySpendingCard(monthlyTransactions)
            }
        }
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
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.DateRange, contentDescription = null)
            Text(
                "${months[selectedMonth]} $selectedYear",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row {
                TextButton(
                    onClick = {
                        if (selectedMonth > 0) {
                            onMonthChanged(selectedMonth - 1, selectedYear)
                        } else {
                            onMonthChanged(11, selectedYear - 1)
                        }
                    }
                ) { Text("◀") }
                
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
    }
}

@Composable
fun MonthlySummaryCard(
    income: Double,
    expense: Double,
    balance: Double,
    transactionCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Monthly Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Income", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "₹${String.format("%.2f", income)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Green,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Expense", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "₹${String.format("%.2f", expense)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Net Balance", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "₹${String.format("%.2f", balance)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (balance >= 0) Color.Green else Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Transactions", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "$transactionCount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryBreakdownItem(
    category: String,
    amount: Double,
    percentage: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                    category,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "$percentage% of expenses",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "₹${String.format("%.2f", amount)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Red
            )
        }
    }
}

@Composable
fun DailySpendingCard(transactions: List<com.yourname.expenso.model.Transaction>) {
    val dailySpending = transactions
        .filter { it.type == "Expense" }
        .groupBy { 
            SimpleDateFormat("dd", Locale.getDefault()).format(Date(it.date))
        }
        .mapValues { it.value.sumOf { transaction -> transaction.amount } }
        .toList()
        .sortedBy { it.first.toInt() }
        .takeLast(7)
    
    if (dailySpending.isNotEmpty()) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Last 7 Days Spending",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.height(12.dp))
                
                dailySpending.forEach { (day, amount) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Day $day")
                        Text(
                            "₹${String.format("%.2f", amount)}",
                            color = Color.Red,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}