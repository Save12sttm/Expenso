package com.yourname.expenso.ui.add_transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    navController: NavController,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf("Expense") }
    var category by remember { mutableStateOf("General") }
    var expandedCategory by remember { mutableStateOf(false) }
    var selectedAccount by remember { mutableStateOf("Cash") }
    var expandedAccount by remember { mutableStateOf(false) }
    
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
    val accounts = listOf("Cash", "Bank", "Credit Card")
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
            ExposedDropdownMenuBox(
                expanded = expandedAccount,
                onExpandedChange = { expandedAccount = !expandedAccount }
            ) {
                OutlinedTextField(
                    value = selectedAccount,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Account") },
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedAccount,
                    onDismissRequest = { expandedAccount = false }
                ) {
                    accounts.forEach { accountOption ->
                        DropdownMenuItem(
                            text = { Text(accountOption) },
                            onClick = {
                                selectedAccount = accountOption
                                expandedAccount = false
                            }
                        )
                    }
                }
            }
            
            // 4. Category Selection (only for expenses)
            if (transactionType == "Expense") {
                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        categories.forEach { categoryOption ->
                            DropdownMenuItem(
                                text = { Text(categoryOption) },
                                onClick = {
                                    category = categoryOption
                                    expandedCategory = false
                                }
                            )
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
                    viewModel.addTransaction(newTransaction)
                    navController.popBackStack()
                },
                enabled = isAmountValid(amount),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Transaction")
            }
        }
    }
}