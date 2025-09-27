package com.yourname.expenso.ui.quickadd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.expenso.data.TransactionRepository
import com.yourname.expenso.model.Account
import com.yourname.expenso.model.Category
import com.yourname.expenso.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuickAddUiState(
    val amount: Double = 0.0,
    val displayAmount: String = "0.00",
    val selectedAccount: Account? = null,
    val selectedCategory: Category? = null,
    val note: String = "",
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList()
)

@HiltViewModel
class QuickAddViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuickAddUiState())
    val uiState: StateFlow<QuickAddUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                repository.getAllAccounts(),
                repository.getAllCategories()
            ) { accounts, categories ->
                _uiState.value = _uiState.value.copy(
                    accounts = accounts,
                    categories = categories,
                    selectedAccount = accounts.firstOrNull(),
                    selectedCategory = categories.firstOrNull { it.name == "General" }
                )
            }.collect()
        }
    }

    fun addDigit(digit: String) {
        val current = _uiState.value.displayAmount
        val new = if (current == "0.00") {
            digit
        } else {
            current + digit
        }
        updateDisplayAmount(new)
    }

    fun addDecimal() {
        val current = _uiState.value.displayAmount
        if (!current.contains(".")) {
            updateDisplayAmount("$current.")
        }
    }

    fun backspace() {
        val current = _uiState.value.displayAmount
        val new = if (current.length > 1) {
            current.dropLast(1)
        } else {
            "0.00"
        }
        updateDisplayAmount(new)
    }

    fun clear() {
        updateDisplayAmount("0.00")
    }

    fun toggleSign() {
        val current = _uiState.value.displayAmount
        val new = if (current.startsWith("-")) {
            current.drop(1)
        } else {
            "-$current"
        }
        updateDisplayAmount(new)
    }

    private fun updateDisplayAmount(display: String) {
        val amount = display.toDoubleOrNull() ?: 0.0
        _uiState.value = _uiState.value.copy(
            displayAmount = display,
            amount = amount
        )
    }

    fun updateAmount(amountStr: String) {
        val amount = amountStr.toDoubleOrNull() ?: 0.0
        _uiState.value = _uiState.value.copy(
            displayAmount = amountStr,
            amount = amount
        )
    }

    fun selectAccount(account: Account) {
        _uiState.value = _uiState.value.copy(selectedAccount = account)
    }

    fun selectCategory(category: Category) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun updateNote(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }

    fun saveTransaction(type: String) {
        val state = _uiState.value
        if (state.amount <= 0.0 || state.selectedAccount == null || state.selectedCategory == null) {
            return
        }

        val transaction = Transaction(
            title = if (state.note.isBlank()) "${state.selectedCategory?.name} $type" else state.note,
            amount = state.amount,
            type = type,
            categoryId = state.selectedCategory?.id ?: 1,
            accountId = state.selectedAccount?.id ?: 1,
            date = System.currentTimeMillis()
        )

        viewModelScope.launch {
            repository.insertTransaction(transaction)
            // Reset form
            _uiState.value = _uiState.value.copy(
                amount = 0.0,
                displayAmount = "0.00",
                note = ""
            )
        }
    }
}