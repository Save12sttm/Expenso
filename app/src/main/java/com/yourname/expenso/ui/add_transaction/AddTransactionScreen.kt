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
    return when {
        category.contains("food", ignoreCase = true) -> "🍽️"
        category.contains("sabji", ignoreCase = true) -> "🥬"
        category.contains("grocery", ignoreCase = true) -> "🛒"
        category.contains("transport", ignoreCase = true) -> "🚗"
        category.contains("shop", ignoreCase = true) -> "🛍️"
        category.contains("bill", ignoreCase = true) -> "💵"
        category.contains("enter", ignoreCase = true) -> "🎬"
        category.contains("health", ignoreCase = true) -> "🏥"
        else -> "💰"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    navController: NavController,
    initialType: String = "expense",
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val accounts by viewModel.accounts.collectAsState(initial = emptyList())
    val categories by viewModel.categories.collectAsState(initial = emptyList())
    
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf(if (initialType == "income") "Income" else "Expense") }
    var category by remember { mutableStateOf("General") }
    var expandedCategory by remember { mutableStateOf(false) }
    var selectedAccount by remember { mutableStateOf("") }
    var expandedAccount by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    
    // Set UPI account as default when accounts load
    LaunchedEffect(accounts) {
        if (accounts.isNotEmpty() && selectedAccount.isEmpty()) {
            val upiAccount = accounts.find { it.name.equals("UPI", ignoreCase = true) }
            selectedAccount = upiAccount?.name ?: accounts.first().name
        }
    }
    
    // Initialize default data and set first category
    LaunchedEffect(Unit) {
        viewModel.initializeData()
    }
    
    LaunchedEffect(categories) {
        if (categories.isNotEmpty() && category == "General") {
            if (transactionType == "Expense") {
                val sabjiCategory = categories.find { it.name.equals("Sabji", ignoreCase = true) }
                category = sabjiCategory?.name ?: categories.first().name
            } else {
                category = categories.first().name
            }
        }
    }
    
    // Auto-categorization
    LaunchedEffect(title) {
        if (title.isNotBlank() && transactionType == "Expense" && categories.isNotEmpty()) {
            category = when {
                title.contains("food", ignoreCase = true) || 
                title.contains("restaurant", ignoreCase = true) -> 
                    categories.find { it.name.contains("Food", ignoreCase = true) }?.name ?: categories.first().name
                title.contains("uber", ignoreCase = true) || 
                title.contains("taxi", ignoreCase = true) -> 
                    categories.find { it.name.contains("Transport", ignoreCase = true) }?.name ?: categories.first().name
                title.contains("shop", ignoreCase = true) -> 
                    categories.find { it.name.contains("Shopping", ignoreCase = true) }?.name ?: categories.first().name
                title.contains("movie", ignoreCase = true) -> 
                    categories.find { it.name.contains("Entertainment", ignoreCase = true) }?.name ?: categories.first().name
                else -> categories.first().name
            }
        }
    }
    
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
            // 1. Amount - Primary field with enhanced styling
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                )
            ) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (₹)", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = amount.isNotEmpty() && !isAmountValid(amount),
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    supportingText = {
                        if (amount.isNotEmpty() && !isAmountValid(amount)) {
                            Text(
                                "Please enter a valid amount",
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text("Enter the transaction amount", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                )
            }
            
            // 2. Note - Optional with smart categorization
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Note (Optional)") },
                supportingText = {
                    if (title.isNotBlank() && transactionType == "Expense") {
                        Text(
                            "Auto-categorized as: ${getCategoryEmoji(category)} $category",
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
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Box {
                    OutlinedTextField(
                        value = if (selectedAccount.isNotEmpty()) selectedAccount else "Select Account",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Account") },
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
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
                                            color = if (account.balance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
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
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Box {
                        OutlinedTextField(
                            value = if (categories.isNotEmpty()) {
                                val selectedCategory = categories.find { it.name == category }
                                "${selectedCategory?.icon ?: getCategoryEmoji(category)} $category"
                            } else "Loading categories...",
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
                            if (categories.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No categories available") },
                                    onClick = { }
                                )
                            } else {
                                categories.forEach { categoryOption ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    categoryOption.icon ?: getCategoryEmoji(categoryOption.name),
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    categoryOption.name,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (category == categoryOption.name) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        },
                                        onClick = {
                                            category = categoryOption.name
                                            expandedCategory = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // 5. Type Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Transaction Type",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FilterChip(
                            onClick = { transactionType = "Expense" },
                            label = { Text("Expense") },
                            selected = transactionType == "Expense",
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = if (transactionType == "Expense") 
                                    MaterialTheme.colorScheme.errorContainer 
                                else MaterialTheme.colorScheme.surface,
                                labelColor = if (transactionType == "Expense") 
                                    MaterialTheme.colorScheme.onErrorContainer 
                                else MaterialTheme.colorScheme.onSurface
                            )
                        )
                        FilterChip(
                            onClick = { transactionType = "Income" },
                            label = { Text("Income") },
                            selected = transactionType == "Income",
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = if (transactionType == "Income") 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else MaterialTheme.colorScheme.surface,
                                labelColor = if (transactionType == "Income") 
                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
            
            Button(
                onClick = {
                    if (!isSaving) {
                        isSaving = true
                        val newTransaction = Transaction(
                            title = if (title.isBlank()) "${transactionType} - ₹${amount}" else title,
                            amount = amount.toDouble(),
                            type = transactionType,
                            categoryId = categories.find { it.name == category }?.id ?: 1,
                            accountId = accounts.find { it.name == selectedAccount }?.id ?: 1,
                            date = System.currentTimeMillis()
                        )
                        viewModel.addTransaction(newTransaction, selectedAccount)
                        navController.popBackStack()
                    }
                },
                enabled = isAmountValid(amount) && selectedAccount.isNotEmpty() && categories.isNotEmpty() && !isSaving,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (transactionType == "Expense") 
                        MaterialTheme.colorScheme.error 
                    else MaterialTheme.colorScheme.primary,
                    contentColor = if (transactionType == "Expense") 
                        MaterialTheme.colorScheme.onError 
                    else MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    if (isSaving) "Saving..." else "Save ${transactionType} ₹${amount.ifBlank { "0" }}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}