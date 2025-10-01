package com.yourname.expenso.ui.categories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCategoriesScreen(
    navController: NavController,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<com.yourname.expenso.model.Category?>(null) }
    var categoryToEdit by remember { mutableStateOf<com.yourname.expenso.model.Category?>(null) }
    var isMultiSelectMode by remember { mutableStateOf(false) }
    var selectedCategories by remember { mutableStateOf(setOf<Int>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (isMultiSelectMode) 
                            "Selected: ${selectedCategories.size}" 
                        else "Manage Categories"
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isMultiSelectMode) {
                        if (selectedCategories.isNotEmpty()) {
                            IconButton(onClick = {
                                selectedCategories.forEach { categoryId ->
                                    categories.find { it.id == categoryId }?.let {
                                        viewModel.deleteCategory(it)
                                    }
                                }
                                selectedCategories = emptySet()
                                isMultiSelectMode = false
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Selected")
                            }
                        }
                        TextButton(onClick = {
                            isMultiSelectMode = false
                            selectedCategories = emptySet()
                        }) {
                            Text("Cancel")
                        }
                    } else {
                        TextButton(onClick = { isMultiSelectMode = true }) {
                            Text("Select")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            item {
                Text(
                    "Tap on a category to edit it. Swipe to delete.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            items(categories) { category ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isMultiSelectMode) {
                        Checkbox(
                            checked = selectedCategories.contains(category.id),
                            onCheckedChange = { isSelected ->
                                selectedCategories = if (isSelected) {
                                    selectedCategories + category.id
                                } else {
                                    selectedCategories - category.id
                                }
                            },
                            modifier = Modifier.padding(start = 16.dp, end = 8.dp)
                        )
                    }
                    
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = if (isMultiSelectMode) 0.dp else 16.dp, vertical = 4.dp)
                            .clickable { 
                                if (isMultiSelectMode) {
                                    selectedCategories = if (selectedCategories.contains(category.id)) {
                                        selectedCategories - category.id
                                    } else {
                                        selectedCategories + category.id
                                    }
                                } else {
                                    categoryToEdit = category
                                }
                            },
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = MaterialTheme.shapes.medium
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    getCategoryEmoji(category.name),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            
                            if (!isMultiSelectMode) {
                                IconButton(
                                    onClick = { categoryToDelete = category }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete Category",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (showAddDialog) {
            AddCategoryDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { name, icon ->
                    viewModel.addCategory(name, icon)
                    showAddDialog = false
                }
            )
        }
        
        categoryToDelete?.let { category ->
            AlertDialog(
                onDismissRequest = { categoryToDelete = null },
                title = { Text("Delete Category") },
                text = { 
                    Column {
                        Text("Are you sure you want to delete '${category.name}'?") 
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Note: Existing transactions with this category will not be affected.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteCategory(category)
                            categoryToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { categoryToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        categoryToEdit?.let { category ->
            EditCategoryDialog(
                category = category,
                onDismiss = { categoryToEdit = null },
                onConfirm = { name, icon ->
                    viewModel.updateCategory(category.copy(name = name, icon = icon))
                    categoryToEdit = null
                }
            )
        }
    }
}

@Composable
fun EditCategoryDialog(
    category: com.yourname.expenso.model.Category,
    onDismiss: () -> Unit, 
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(category.name) }
    var selectedIcon by remember { mutableStateOf(category.icon) }
    
    val iconOptions = listOf(
        "⭐" to "Star"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Category") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Select Icon:", style = MaterialTheme.typography.bodyMedium)
                LazyRow(modifier = Modifier.padding(top = 8.dp)) {
                    items(iconOptions) { (icon, label) ->
                        Card(
                            modifier = Modifier
                                .padding(4.dp)
                                .clickable { selectedIcon = icon },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedIcon == icon) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    icon,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    label,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (name.isNotBlank()) {
                        onConfirm(name, selectedIcon)
                    }
                },
                enabled = name.isNotBlank()
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

@Composable
fun AddCategoryDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("⭐") }
    
    val iconOptions = listOf(
        "⭐" to "Star"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Category") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Select Icon:", style = MaterialTheme.typography.bodyMedium)
                LazyRow(modifier = Modifier.padding(top = 8.dp)) {
                    items(iconOptions) { (icon, label) ->
                        Card(
                            modifier = Modifier
                                .padding(4.dp)
                                .clickable { selectedIcon = icon },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedIcon == icon) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    icon,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    label,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (name.isNotBlank()) {
                        onConfirm(name, selectedIcon)
                    }
                },
                enabled = name.isNotBlank()
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

private fun getCategoryEmoji(categoryName: String): String {
    return when {
        categoryName.contains("food", ignoreCase = true) -> "🍽️"
        categoryName.contains("transport", ignoreCase = true) -> "🚗"
        categoryName.contains("shop", ignoreCase = true) -> "🛍️"
        categoryName.contains("bill", ignoreCase = true) -> "💡"
        categoryName.contains("enter", ignoreCase = true) -> "🎬"
        categoryName.contains("health", ignoreCase = true) -> "🏥"
        categoryName.contains("home", ignoreCase = true) -> "🏠"
        categoryName.contains("work", ignoreCase = true) -> "💼"
        categoryName.contains("edu", ignoreCase = true) -> "🎓"
        else -> "💰"
    }
}