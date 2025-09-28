package com.yourname.expenso.ui.settings

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.expenso.data.AppTheme
import com.yourname.expenso.data.AppPreferencesManager
import com.yourname.expenso.data.TransactionRepository
import com.yourname.expenso.model.Transaction
import com.yourname.expenso.notification.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val transactions: List<Transaction> = emptyList()
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPreferencesManager: AppPreferencesManager,
    private val repository: TransactionRepository,
    private val notificationHelper: NotificationHelper
) : ViewModel() {
    
    val theme = appPreferencesManager.theme
    val notificationsEnabled = appPreferencesManager.notificationsEnabled
    val profileName = appPreferencesManager.profileName
    val profileEmail = appPreferencesManager.profileEmail
    val profileAvatar = appPreferencesManager.profileAvatar
    
    val uiState: StateFlow<SettingsUiState> = repository.getAllTransactions()
        .map { transactions -> SettingsUiState(transactions = transactions) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = SettingsUiState()
        )
    
    fun setTheme(theme: AppTheme) {
        viewModelScope.launch { appPreferencesManager.setTheme(theme) }
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            appPreferencesManager.setNotificationsEnabled(enabled)
            if (enabled) {
                notificationHelper.scheduleDailyReminder()
            } else {
                notificationHelper.cancelDailyReminder()
            }
        }
    }
    
    fun setProfileName(name: String) {
        viewModelScope.launch { appPreferencesManager.setProfileName(name) }
    }
    
    fun setProfileEmail(email: String) {
        viewModelScope.launch { appPreferencesManager.setProfileEmail(email) }
    }
    
    fun setProfileAvatar(avatar: String) {
        viewModelScope.launch { appPreferencesManager.setProfileAvatar(avatar) }
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
    
    fun resetAppData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

}