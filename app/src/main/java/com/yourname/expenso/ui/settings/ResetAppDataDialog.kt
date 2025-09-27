package com.yourname.expenso.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ResetAppDataDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var confirmText by remember { mutableStateOf("") }
    val requiredText = "DELETE ALL DATA"
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                "Reset App Data",
                color = Color.Red,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "⚠️ WARNING: This action cannot be undone!",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.height(16.dp))
                
                Text("This will permanently delete:")
                Text("• All transactions")
                Text("• All categories")
                Text("• All accounts")
                Text("• All settings")
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    "Type \"$requiredText\" to confirm:",
                    fontWeight = FontWeight.Medium
                )
                
                OutlinedTextField(
                    value = confirmText,
                    onValueChange = { confirmText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(requiredText) },
                    isError = confirmText.isNotEmpty() && confirmText != requiredText
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = confirmText == requiredText,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    disabledContainerColor = Color.Gray
                )
            ) {
                Text("Reset All Data")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}