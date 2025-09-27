package com.yourname.expenso.ui.quickadd

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourname.expenso.model.Account
import com.yourname.expenso.model.Category

@Composable
fun QuickAddBottomSheet(
    onDismiss: () -> Unit,
    viewModel: QuickAddViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isDetailMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isDetailMode) "Transaction Details" else "Quick Add",
                style = MaterialTheme.typography.titleLarge
            )
            
            TextButton(
                onClick = { isDetailMode = !isDetailMode }
            ) {
                Icon(
                    if (isDetailMode) Icons.Default.ArrowBack else Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(if (isDetailMode) "Quick" else "More")
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        if (isDetailMode) {
            DetailedForm(
                uiState = uiState,
                onAccountSelected = viewModel::selectAccount,
                onAmountChanged = viewModel::updateAmount,
                onCategorySelected = viewModel::selectCategory,
                onNoteChanged = viewModel::updateNote,
                onSaveExpense = { viewModel.saveTransaction("Expense"); onDismiss() },
                onSaveIncome = { viewModel.saveTransaction("Income"); onDismiss() }
            )
        } else {
            QuickCalculator(
                uiState = uiState,
                onNumberClick = viewModel::addDigit,
                onClear = viewModel::clear,
                onBackspace = viewModel::backspace,
                onDecimal = viewModel::addDecimal,
                onToggleSign = viewModel::toggleSign,
                onCategorySelected = viewModel::selectCategory,
                onAccountSelected = viewModel::selectAccount,
                onSaveExpense = { viewModel.saveTransaction("Expense"); onDismiss() },
                onSaveIncome = { viewModel.saveTransaction("Income"); onDismiss() }
            )
        }
    }
}

@Composable
fun QuickCalculator(
    uiState: QuickAddUiState,
    onNumberClick: (String) -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onDecimal: () -> Unit,
    onToggleSign: () -> Unit,
    onCategorySelected: (Category) -> Unit,
    onAccountSelected: (Account) -> Unit,
    onSaveExpense: () -> Unit,
    onSaveIncome: () -> Unit
) {
    Column {
        // Amount Display
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                text = "₹ ${uiState.displayAmount}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Calculator Grid
        val buttons = listOf(
            listOf("7", "8", "9", "C"),
            listOf("4", "5", "6", "⌫"),
            listOf("1", "2", "3", "00"),
            listOf(".", "0", "=", "+/-")
        )
        
        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { button ->
                    CalculatorButton(
                        text = button,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            when (button) {
                                "C" -> onClear()
                                "⌫" -> onBackspace()
                                "." -> onDecimal()
                                "+/-" -> onToggleSign()
                                "00" -> { onNumberClick("0"); onNumberClick("0") }
                                "=" -> { /* Could add calculation logic */ }
                                else -> onNumberClick(button)
                            }
                        }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Expense Section with Categories
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("💸 EXPENSE", style = MaterialTheme.typography.titleSmall, color = Color.Red)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    items(uiState.categories.take(4)) { category ->
                        CategoryChip(
                            category = category,
                            isSelected = uiState.selectedCategory?.id == category.id,
                            onClick = { onCategorySelected(category) }
                        )
                    }
                }
                Button(
                    onClick = onSaveExpense,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    enabled = uiState.amount > 0.0
                ) {
                    Text("SAVE EXPENSE", color = Color.White)
                }
            }
        }
        
        Spacer(Modifier.height(12.dp))
        
        // Income Section with Accounts
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.Green.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("💰 INCOME", style = MaterialTheme.typography.titleSmall, color = Color.Green)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    items(uiState.accounts.take(3)) { account ->
                        AccountChip(
                            account = account,
                            isSelected = uiState.selectedAccount?.id == account.id,
                            onClick = { onAccountSelected(account) }
                        )
                    }
                }
                Button(
                    onClick = onSaveIncome,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                    enabled = uiState.amount > 0.0
                ) {
                    Text("SAVE INCOME", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun DetailedForm(
    uiState: QuickAddUiState,
    onAccountSelected: (Account) -> Unit,
    onAmountChanged: (String) -> Unit,
    onCategorySelected: (Category) -> Unit,
    onNoteChanged: (String) -> Unit,
    onSaveExpense: () -> Unit,
    onSaveIncome: () -> Unit
) {
    Column {
        // Account Selection
        Text("Account", style = MaterialTheme.typography.titleSmall)
        DropdownMenuBox(
            items = uiState.accounts,
            selectedItem = uiState.selectedAccount,
            onItemSelected = onAccountSelected,
            itemText = { "${it.name} (₹${String.format("%.2f", it.balance)})" }
        )
        
        Spacer(Modifier.height(16.dp))
        
        // Amount Field
        Text("Amount (₹)", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(
            value = uiState.displayAmount,
            onValueChange = onAmountChanged,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            placeholder = { Text("0.00") }
        )
        
        Spacer(Modifier.height(16.dp))
        
        // Category Selection
        Text("Category", style = MaterialTheme.typography.titleSmall)
        DropdownMenuBox(
            items = uiState.categories,
            selectedItem = uiState.selectedCategory,
            onItemSelected = onCategorySelected,
            itemText = { "${it.icon} ${it.name}" }
        )
        
        Spacer(Modifier.height(16.dp))
        
        // Note Field
        Text("Note (Optional)", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(
            value = uiState.note,
            onValueChange = onNoteChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Add description...") },
            maxLines = 3
        )
        
        Spacer(Modifier.height(24.dp))
        
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onSaveExpense,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                enabled = uiState.amount > 0.0
            ) {
                Text("EXPENSE", color = Color.White)
            }
            
            Button(
                onClick = onSaveIncome,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                enabled = uiState.amount > 0.0
            ) {
                Text("INCOME", color = Color.White)
            }
        }
    }
}

@Composable
fun CalculatorButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun CategoryChip(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Text(
            text = "${category.icon} ${category.name}",
            modifier = Modifier.padding(8.dp),
            fontSize = 12.sp
        )
    }
}

@Composable
fun AccountChip(
    account: Account,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Text(
            text = account.name,
            modifier = Modifier.padding(8.dp),
            fontSize = 12.sp
        )
    }
}

@Composable
fun <T> DropdownMenuBox(
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    itemText: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        OutlinedTextField(
            value = selectedItem?.let { itemText(it) } ?: "",
            onValueChange = { },
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            readOnly = true,
            trailingIcon = {
                Text("▼", modifier = Modifier.clickable { expanded = true })
            }
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(itemText(item)) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}