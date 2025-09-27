package com.yourname.expenso.ui.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

data class BudgetItem(
    val id: Int = 0,
    val category: String,
    val budgetAmount: Double,
    val spentAmount: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetGoalsScreen(navController: NavController) {
    var monthlyBudget by remember { mutableStateOf("") }
    var savingsGoal by remember { mutableStateOf("") }
    var showAddBudgetDialog by remember { mutableStateOf(false) }
    
    val budgetItems = remember {
        mutableStateListOf(
            BudgetItem(1, "Food", 5000.0, 3200.0),
            BudgetItem(2, "Transport", 2000.0, 1800.0),
            BudgetItem(3, "Shopping", 3000.0, 4200.0)
        )
    }
    var editingItem by remember { mutableStateOf<BudgetItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget & Goals") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddBudgetDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Budget")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Monthly Budget",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = monthlyBudget,
                            onValueChange = { monthlyBudget = it },
                            label = { Text("Total Monthly Budget (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Savings Goal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = savingsGoal,
                            onValueChange = { savingsGoal = it },
                            label = { Text("Monthly Savings Target (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            item {
                Text(
                    "Category Budgets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(budgetItems) { budgetItem ->
                BudgetItemCard(
                    budgetItem = budgetItem,
                    onEdit = { editingItem = budgetItem },
                    onDelete = { budgetItems.remove(budgetItem) }
                )
            }
        }
        
        if (showAddBudgetDialog) {
            AddBudgetDialog(
                onDismiss = { showAddBudgetDialog = false },
                onConfirm = { category, amount ->
                    budgetItems.add(BudgetItem(budgetItems.size + 1, category, amount, 0.0))
                    showAddBudgetDialog = false
                }
            )
        }
        
        editingItem?.let { item ->
            EditBudgetDialog(
                budgetItem = item,
                onDismiss = { editingItem = null },
                onConfirm = { newAmount ->
                    val index = budgetItems.indexOfFirst { it.id == item.id }
                    if (index != -1) {
                        budgetItems[index] = item.copy(budgetAmount = newAmount)
                    }
                    editingItem = null
                }
            )
        }
    }
}

@Composable
fun BudgetItemCard(
    budgetItem: BudgetItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val percentage = (budgetItem.spentAmount / budgetItem.budgetAmount * 100).toInt()
    val isOverBudget = budgetItem.spentAmount > budgetItem.budgetAmount
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverBudget) 
                MaterialTheme.colorScheme.errorContainer 
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    budgetItem.category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Row {
                    Text(
                        "$percentage%",
                        color = if (isOverBudget) Color.Red else Color.Green,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { (budgetItem.spentAmount / budgetItem.budgetAmount).toFloat().coerceAtMost(1f) },
                modifier = Modifier.fillMaxWidth(),
                color = if (isOverBudget) Color.Red else MaterialTheme.colorScheme.primary
            )
            
            Spacer(Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Spent: ₹${String.format("%.0f", budgetItem.spentAmount)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Budget: ₹${String.format("%.0f", budgetItem.budgetAmount)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            if (isOverBudget) {
                Text(
                    "⚠️ Over budget by ₹${String.format("%.0f", budgetItem.spentAmount - budgetItem.budgetAmount)}",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun AddBudgetDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var category by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Category Budget") },
        text = {
            Column {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Budget Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val budgetAmount = amount.toDoubleOrNull()
                    if (category.isNotBlank() && budgetAmount != null && budgetAmount > 0) {
                        onConfirm(category, budgetAmount)
                    }
                },
                enabled = category.isNotBlank() && amount.toDoubleOrNull()?.let { it > 0 } == true
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
fun EditBudgetDialog(
    budgetItem: BudgetItem,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amount by remember { mutableStateOf(budgetItem.budgetAmount.toString()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Budget: ${budgetItem.category}") },
        text = {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Budget Amount (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { 
                    val budgetAmount = amount.toDoubleOrNull()
                    if (budgetAmount != null && budgetAmount > 0) {
                        onConfirm(budgetAmount)
                    }
                },
                enabled = amount.toDoubleOrNull()?.let { it > 0 } == true
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