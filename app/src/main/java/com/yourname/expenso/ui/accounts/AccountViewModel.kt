package com.yourname.expenso.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.expenso.data.TransactionRepository
import com.yourname.expenso.model.Account
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {
    
    val accounts: StateFlow<List<Account>> = repository.getAllAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addAccount(name: String, balance: Double, type: String) {
        viewModelScope.launch {
            repository.insertAccount(Account(name = name, balance = balance, type = type))
        }
    }

    fun transferMoney(fromAccount: Account, toAccount: Account, amount: Double) {
        viewModelScope.launch {
            repository.transferMoney(fromAccount.id, toAccount.id, amount)
        }
    }
    
    fun updateAccountBalance(account: Account, newBalance: Double) {
        viewModelScope.launch {
            repository.updateAccount(account.copy(balance = newBalance))
        }
    }
    
    fun refreshData() {
        viewModelScope.launch {
            repository.initializeDefaultData()
        }
    }
    
    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            repository.deleteAccount(account)
        }
    }
}