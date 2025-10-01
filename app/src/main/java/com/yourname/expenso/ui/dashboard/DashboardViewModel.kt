package com.yourname.expenso.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.expenso.data.TransactionRepository
import com.yourname.expenso.model.Transaction
import com.yourname.expenso.model.Account
import com.yourname.expenso.model.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val transactions: List<Transaction> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> =
        kotlinx.coroutines.flow.combine(
            transactionRepository.getAllTransactions(),
            transactionRepository.getAllAccounts(),
            transactionRepository.getAllCategories()
        ) { transactions, accounts, categories ->
            val activeTransactions = transactions.filter { !it.isDeleted }
            val income = activeTransactions.filter { it.type == "Income" }.sumOf { it.amount }
            val expense = activeTransactions.filter { it.type == "Expense" }.sumOf { it.amount }
            val calculatedBalance = income - expense
            
            DashboardUiState(
                totalIncome = income,
                totalExpense = expense,
                balance = calculatedBalance,
                transactions = activeTransactions,
                accounts = accounts,
                categories = categories
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = DashboardUiState()
        )

    fun softDeleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.softDeleteTransaction(transaction)
        }
    }
    
    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.updateTransaction(transaction)
        }
    }
    
    fun refreshData() {
        viewModelScope.launch {
            transactionRepository.initializeDefaultData()
        }
    }
}