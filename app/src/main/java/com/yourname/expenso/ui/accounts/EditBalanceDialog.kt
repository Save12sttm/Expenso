package com.yourname.expenso.ui.accounts

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.yourname.expenso.model.Account

@Composable
fun EditBalanceDialog(
    account: Account,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var balance by remember { mutableStateOf(account.balance.toString()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Balance - ${account.name}") },
        text = {
            OutlinedTextField(
                value = balance,
                onValueChange = { balance = it },
                label = { Text("Balance (₹)") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    balance.toDoubleOrNull()?.let { onConfirm(it) }
                },
                enabled = balance.toDoubleOrNull() != null
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