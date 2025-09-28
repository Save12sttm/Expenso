package com.yourname.expenso.ui.add_transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.expenso.data.TransactionRepository
import com.yourname.expenso.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    val accounts = repository.getAllAccounts()
    val categories = repository.getAllCategories()

    fun addTransaction(transaction: Transaction, selectedAccountName: String) {
        viewModelScope.launch {
            // Insert the transaction
            repository.insertTransaction(transaction)
            
            // Update account balance
            repository.updateAccountBalance(selectedAccountName, transaction.amount, transaction.type)
        }
    }
    
    fun initializeData() {
        viewModelScope.launch {
            repository.initializeDefaultData()
        }
    }
}