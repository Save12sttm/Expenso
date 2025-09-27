package com.yourname.expenso.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.expenso.data.AppTheme
import com.yourname.expenso.data.ThemeManager
import com.yourname.expenso.data.TransactionRepository
import com.yourname.expenso.model.Transaction
import com.yourname.expenso.notification.NotificationHelper
import com.yourname.expenso.util.BackupManager
import com.yourname.expenso.util.ReportGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class SettingsUiState(
    val transactions: List<Transaction> = emptyList()
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeManager: ThemeManager,
    private val repository: TransactionRepository,
    private val reportGenerator: ReportGenerator,
    private val backupManager: BackupManager,
    private val notificationHelper: NotificationHelper
) : ViewModel() {
    
    val theme = themeManager.theme
    val notificationsEnabled = themeManager.notificationsEnabled
    
    val uiState: StateFlow<SettingsUiState> = repository.getAllTransactions()
        .map { transactions -> SettingsUiState(transactions = transactions) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = SettingsUiState()
        )
    
    fun setTheme(theme: AppTheme) {
        viewModelScope.launch { themeManager.setTheme(theme) }
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            themeManager.setNotificationsEnabled(enabled)
            if (enabled) {
                notificationHelper.scheduleDailyReminder()
            } else {
                notificationHelper.cancelDailyReminder()
            }
        }
    }

    val deletedTransactions: StateFlow<List<Transaction>> =
        repository.getDeletedTransactions().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    fun restoreTransaction(transaction: Transaction) {
        viewModelScope.launch { repository.restoreTransaction(transaction) }
    }

    fun permanentlyDeleteTransaction(transaction: Transaction) {
        viewModelScope.launch { repository.deleteTransaction(transaction) }
    }
    
    fun exportAllData() {
        viewModelScope.launch {
            val transactions = repository.getAllTransactions().first()
            val startDate = 0L // All time
            val endDate = System.currentTimeMillis()
            reportGenerator.generateCSVReport(transactions, startDate, endDate)
        }
    }
    
    fun exportTransactionsToCsv(context: Context) {
        viewModelScope.launch {
            val transactions = repository.getAllTransactions().first()
            
            if (transactions.isEmpty()) {
                Toast.makeText(context, "No transactions to export.", Toast.LENGTH_SHORT).show()
                return@launch
            }
            
            val csvBuilder = StringBuilder()
            csvBuilder.append("ID,Title,Amount,Type,Category,Date\n")
            
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            transactions.forEach { txn ->
                csvBuilder.append("${txn.id},\"${txn.title}\",${txn.amount},${txn.type},General,${dateFormat.format(Date(txn.date))}\n")
            }
            
            val csvContent = csvBuilder.toString()
            saveAsFile(context, "expenso_transactions.csv", csvContent, "text/csv")
        }
    }
    
    private fun saveAsFile(context: Context, fileName: String, content: String, mimeType: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_TEXT, content)
            putExtra(Intent.EXTRA_SUBJECT, "Expenso Export - $fileName")
        }
        context.startActivity(Intent.createChooser(shareIntent, "Save $fileName"))
    }
    
    fun resetAppData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

}