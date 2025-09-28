package com.yourname.expenso.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yourname.expenso.model.Transaction
import com.yourname.expenso.model.Account
import com.yourname.expenso.model.Category
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EditTransactionDialog(
    transaction: Transaction,
    accounts: List<Account>,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onUpdate: (Transaction) -> Unit
) {
    var title by remember { mutableStateOf(transaction.title) }
    var amount by remember { mutableStateOf(transaction.amount.toString()) }
    var selectedAccount by remember { mutableStateOf(accounts.find { it.id == transaction.accountId }?.name ?: "") }
    var selectedCategory by remember { mutableStateOf(categories.find { it.id == transaction.categoryId }?.name ?: "General") }
    var transactionType by remember { mutableStateOf(transaction.type) }
    var date by remember { mutableStateOf(transaction.date) }
    var expandedAccount by remember { mutableStateOf(false) }
    var expandedCategory by remember { mutableStateOf(false) }
    
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Transaction") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Amount
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Account Selection
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Box {
                        OutlinedTextField(
                            value = selectedAccount.ifEmpty { "Select Account" },
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
                                .clickable { expandedAccount = !expandedAccount }
                        )
                        DropdownMenu(
                            expanded = expandedAccount,
                            onDismissRequest = { expandedAccount = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            accounts.forEach { account ->
                                DropdownMenuItem(
                                    text = { Text(account.name) },
                                    onClick = {
                                        selectedAccount = account.name
                                        expandedAccount = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Category Selection (for expenses)
                if (transactionType == "Expense") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Box {
                            OutlinedTextField(
                                value = selectedCategory,
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
                                    .clickable { expandedCategory = !expandedCategory }
                            )
                            DropdownMenu(
                                expanded = expandedCategory,
                                onDismissRequest = { expandedCategory = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category.name) },
                                        onClick = {
                                            selectedCategory = category.name
                                            expandedCategory = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Transaction Type
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Transaction Type",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            FilterChip(
                                onClick = { transactionType = "Expense" },
                                label = { Text("Expense") },
                                selected = transactionType == "Expense"
                            )
                            FilterChip(
                                onClick = { transactionType = "Income" },
                                label = { Text("Income") },
                                selected = transactionType == "Income"
                            )
                        }
                    }
                }
                
                // Date
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    OutlinedTextField(
                        value = dateFormat.format(Date(date)),
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Date") },
                        trailingIcon = {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true }
                    )
                }
                
                // Date Picker Dialog
                if (showDatePicker) {
                    AlertDialog(
                        onDismissRequest = { showDatePicker = false },
                        title = { Text("Select Date") },
                        text = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Current date: ${dateFormat.format(Date(date))}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        date = System.currentTimeMillis()
                                        showDatePicker = false
                                    }
                                ) {
                                    Text("Set to Today")
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text("Close")
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedTransaction = transaction.copy(
                        title = title,
                        amount = amount.toDoubleOrNull() ?: transaction.amount,
                        type = transactionType,
                        categoryId = categories.find { it.name == selectedCategory }?.id ?: transaction.categoryId,
                        accountId = accounts.find { it.name == selectedAccount }?.id ?: transaction.accountId,
                        date = date
                    )
                    onUpdate(updatedTransaction)
                },
                enabled = title.isNotBlank() && amount.toDoubleOrNull()?.let { it > 0 } == true && selectedAccount.isNotEmpty()
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