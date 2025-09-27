package com.yourname.expenso.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.expenso.data.TransactionRepository
import com.yourname.expenso.model.Transaction
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
    val transactions: List<Transaction> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> =
        transactionRepository.getAllTransactions().map { transactions ->
            val income = transactions.filter { it.type == "Income" }.sumOf { it.amount }
            val expense = transactions.filter { it.type == "Expense" }.sumOf { it.amount }
            DashboardUiState(
                totalIncome = income,
                totalExpense = expense,
                balance = income - expense,
                transactions = transactions
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = DashboardUiState()
        )

    fun softDeleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.softDeleteTransaction(transaction)
        }
    }
    

}