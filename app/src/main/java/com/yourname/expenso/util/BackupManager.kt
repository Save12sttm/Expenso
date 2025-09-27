package com.yourname.expenso.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import com.yourname.expenso.model.Account
import com.yourname.expenso.model.Category
import com.yourname.expenso.model.Transaction
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class AppBackup(
    val transactions: List<Transaction>,
    val categories: List<Category>,
    val accounts: List<Account>
)

@Singleton
class BackupManager @Inject constructor() {
    
    fun createBackupJson(
        transactions: List<Transaction>,
        categories: List<Category>,
        accounts: List<Account>
    ): String {
        val backupData = AppBackup(
            transactions = transactions,
            categories = categories,
            accounts = accounts
        )
        return Json.encodeToString(AppBackup.serializer(), backupData)
    }
    
    fun parseBackupJson(jsonString: String): AppBackup {
        return Json.decodeFromString(AppBackup.serializer(), jsonString)
    }
    
    fun createBackupIntent(): Intent {
        return Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "expenso_backup.json")
        }
    }
    
    fun createRestoreIntent(): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
    }
    
    fun writeBackupToUri(context: Context, uri: Uri, jsonContent: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonContent.toByteArray())
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun readBackupFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes().toString(Charsets.UTF_8)
            }
        } catch (e: Exception) {
            null
        }
    }
}