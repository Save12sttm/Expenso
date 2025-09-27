package com.yourname.expenso.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import com.yourname.expenso.model.Transaction
import com.yourname.expenso.model.Category
import com.yourname.expenso.model.Account

class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val accountDao: AccountDao
) {
    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()

    fun getDeletedTransactions(): Flow<List<Transaction>> = transactionDao.getDeletedTransactions()

    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun softDeleteTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction.copy(isDeleted = true))
    }

    suspend fun restoreTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction.copy(isDeleted = false))
    }

    // Category Methods
    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()
    suspend fun insertCategory(category: Category) = categoryDao.insertCategory(category)
    suspend fun deleteCategory(category: Category) = categoryDao.deleteCategory(category)
    suspend fun getCategoryById(categoryId: Int): Category? = categoryDao.getCategoryById(categoryId)

    // Account Methods
    fun getAllAccounts(): Flow<List<Account>> = accountDao.getAllAccounts()
    suspend fun insertAccount(account: Account) = accountDao.insertAccount(account)
    suspend fun deleteAccount(account: Account) = accountDao.deleteAccount(account)
    suspend fun getAccountById(accountId: Int): Account? = accountDao.getAccountById(accountId)
    suspend fun updateAccount(account: Account) = accountDao.updateAccount(account)
    suspend fun transferMoney(fromAccountId: Int, toAccountId: Int, amount: Double) = 
        accountDao.transfer(fromAccountId, toAccountId, amount)

    // Initialize default data
    suspend fun initializeDefaultData() {
        val defaultCategories = listOf(
            Category(name = "Food", icon = "🍽️"),
            Category(name = "Transport", icon = "🚗"),
            Category(name = "Shopping", icon = "🛒"),
            Category(name = "Bills", icon = "💡"),
            Category(name = "Entertainment", icon = "🎬"),
            Category(name = "Health", icon = "🏥"),
            Category(name = "General", icon = "📝")
        )
        defaultCategories.forEach { categoryDao.insertCategory(it) }
        
        val defaultAccounts = listOf(
            Account(name = "Cash", balance = 5000.0, type = "Cash"),
            Account(name = "Bank", balance = 25000.0, type = "Bank"),
            Account(name = "Credit Card", balance = -2500.0, type = "Credit")
        )
        defaultAccounts.forEach { accountDao.insertAccount(it) }
    }

    // Backup & Restore Methods
    suspend fun clearAllData() {
        transactionDao.deleteAllTransactions()
        categoryDao.deleteAllCategories()
        accountDao.deleteAllAccounts()
        initializeDefaultData()
    }

    suspend fun restoreFromBackup(
        transactions: List<Transaction>,
        categories: List<Category>,
        accounts: List<Account>
    ) {
        // Clear existing data and restore in a single transaction
        clearAllData()
        categories.forEach { categoryDao.insertCategory(it) }
        accounts.forEach { accountDao.insertAccount(it) }
        transactions.forEach { transactionDao.insertTransaction(it) }
    }
}