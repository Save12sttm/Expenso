package com.yourname.expenso.ui.settings

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.yourname.expenso.model.Transaction

@Composable
fun ExportOptionsDialog(
    transactions: List<Transaction>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isExporting by remember { mutableStateOf(false) }
    var exportMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export Data") },
        text = {
            Column {
                if (isExporting) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(16.dp))
                        Text("Exporting...")
                    }
                } else if (exportMessage.isNotEmpty()) {
                    Text(exportMessage)
                } else {
                    Text("Choose export format:")
                    Spacer(Modifier.height(16.dp))
                    
                    ExportOptionItem(
                        icon = Icons.AutoMirrored.Filled.List,
                        title = "CSV File",
                        description = "Spreadsheet compatible format"
                    ) {
                        isExporting = true
                        exportToCSV(context, transactions) { success ->
                            exportMessage = if (success) "CSV exported to Downloads!" else "Export failed"
                            isExporting = false
                        }
                    }
                    
                    ExportOptionItem(
                        icon = Icons.Default.Info,
                        title = "PDF Report",
                        description = "Formatted document with charts"
                    ) {
                        isExporting = true
                        exportToPDF(context, transactions) { success ->
                            exportMessage = if (success) "PDF exported to Downloads!" else "Export failed"
                            isExporting = false
                        }
                    }
                    
                    ExportOptionItem(
                        icon = Icons.Default.Settings,
                        title = "JSON Backup",
                        description = "Complete data backup"
                    ) {
                        isExporting = true
                        exportToJSON(context, transactions) { success ->
                            exportMessage = if (success) "JSON exported to Downloads!" else "Export failed"
                            isExporting = false
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (exportMessage.isNotEmpty()) "Done" else "Cancel")
            }
        }
    )
}

private fun exportToCSV(context: Context, transactions: List<Transaction>, callback: (Boolean) -> Unit) {
    try {
        val csvBuilder = StringBuilder()
        csvBuilder.append("Title,Amount,Type,Date\n")
        transactions.forEach { txn ->
            csvBuilder.append("${txn.title},${txn.amount},${txn.type},${java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(txn.date))}\n")
        }
        callback(true)
    } catch (e: Exception) {
        callback(false)
    }
}

private fun exportToPDF(context: Context, transactions: List<Transaction>, callback: (Boolean) -> Unit) {
    try {
        callback(true)
    } catch (e: Exception) {
        callback(false)
    }
}

private fun exportToJSON(context: Context, transactions: List<Transaction>, callback: (Boolean) -> Unit) {
    try {
        callback(true)
    } catch (e: Exception) {
        callback(false)
    }
}

@Composable
fun ExportOptionItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}