package com.yourname.expenso.ui.add_transaction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.yourname.expenso.model.Transaction

private fun isAmountValid(amount: String): Boolean {
    return amount.toDoubleOrNull()?.let { it > 0 } ?: false
}

private fun getCategoryEmoji(category: String): String {
    return when (category) {
        "Food" -> "🍽️"
        "Transport" -> "🚗"
        "Shopping" -> "🛍️"
        "Bills" -> "💵"
        "Entertainment" -> "🎬"
        "General" -> "📝"
        else -> "💰"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    navController: NavController,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val accounts by viewModel.accounts.collectAsState(initial = emptyList())
    
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf("Expense") }
    var category by remember { mutableStateOf("General") }
    var expandedCategory by remember { mutableStateOf(false) }
    var selectedAccount by remember { mutableStateOf("") }
    var expandedAccount by remember { mutableStateOf(false) }
    
    // Set first account as default when accounts load
    LaunchedEffect(accounts) {
        if (accounts.isNotEmpty() && selectedAccount.isEmpty()) {
            selectedAccount = accounts.first().name
        }
    }
    
    // Auto-categorization
    LaunchedEffect(title) {
        if (title.isNotBlank() && transactionType == "Expense") {
            category = when {
                title.contains("food", ignoreCase = true) || 
                title.contains("restaurant", ignoreCase = true) -> "Food"
                title.contains("uber", ignoreCase = true) || 
                title.contains("taxi", ignoreCase = true) -> "Transport"
                title.contains("shop", ignoreCase = true) -> "Shopping"
                title.contains("movie", ignoreCase = true) -> "Entertainment"
                else -> "General"
            }
        }
    }
    
    val categories = listOf("Food", "Transport", "Shopping", "Bills", "Entertainment", "General")
    // Note: title is now optional, only amount is required

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        val configuration = androidx.compose.ui.platform.LocalConfiguration.current
        val isTablet = configuration.screenWidthDp >= 600
        
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(if (isTablet) 32.dp else 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(if (isTablet) 24.dp else 16.dp)
        ) {
            // 1. Amount - Primary field
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = amount.isNotEmpty() && !isAmountValid(amount),
                modifier = Modifier.fillMaxWidth()
            )
            
            // 2. Note - Optional with smart categorization
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Note (Optional)") },
                supportingText = {
                    if (title.isNotBlank() && transactionType == "Expense") {
                        Text(
                            "Auto-categorized as: $category",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            // 3. Account Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Box {
                    OutlinedTextField(
                        value = if (selectedAccount.isNotEmpty()) selectedAccount else "Select Account",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Account") },
                        trailingIcon = {
                            Icon(
                                if (expandedAccount) Icons.Default.ArrowDropDown else Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { expandedAccount = !expandedAccount }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedAccount = !expandedAccount },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    DropdownMenu(
                        expanded = expandedAccount,
                        onDismissRequest = { expandedAccount = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        accounts.forEach { account ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                account.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                account.type,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            "₹${String.format("%.0f", account.balance)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (account.balance >= 0) Color(0xFF008000) else Color.Red
                                        )
                                    }
                                },
                                onClick = {
                                    selectedAccount = account.name
                                    expandedAccount = false
                                }
                            )
                        }
                    }
                }
            }
            
            // 4. Category Selection (only for expenses)
            if (transactionType == "Expense") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Box {
                        OutlinedTextField(
                            value = category,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.clickable { expandedCategory = !expandedCategory }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedCategory = !expandedCategory },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        DropdownMenu(
                            expanded = expandedCategory,
                            onDismissRequest = { expandedCategory = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            categories.forEach { categoryOption ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                getCategoryEmoji(categoryOption),
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                categoryOption,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    },
                                    onClick = {
                                        category = categoryOption
                                        expandedCategory = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // 5. Type Selection
            Row {
                RadioButton(
                    selected = transactionType == "Expense",
                    onClick = { transactionType = "Expense" }
                )
                Text("Expense", modifier = Modifier.padding(start = 4.dp, end = 16.dp))
                RadioButton(
                    selected = transactionType == "Income",
                    onClick = { transactionType = "Income" }
                )
                Text("Income", modifier = Modifier.padding(start = 4.dp))
            }
            
            Button(
                onClick = {
                    val newTransaction = Transaction(
                        title = if (title.isBlank()) "${transactionType} - ${amount}" else title,
                        amount = amount.toDouble(),
                        type = transactionType,
                        categoryId = 1, // Default category ID
                        accountId = 1,  // Default account ID
                        date = System.currentTimeMillis()
                    )
                    viewModel.addTransaction(newTransaction, selectedAccount)
                    navController.popBackStack()
                },
                enabled = isAmountValid(amount) && selectedAccount.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Transaction")
            }
        }
    }
}