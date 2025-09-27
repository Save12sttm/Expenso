package com.yourname.expenso.util

import com.yourname.expenso.model.Account
import com.yourname.expenso.model.Category
import com.yourname.expenso.model.Transaction
import java.util.*
import kotlin.random.Random

object SampleDataGenerator {
    
    fun generateSampleAccounts(): List<Account> = listOf(
        Account(id = 1, name = "Cash", balance = 5000.0, type = "Cash"),
        Account(id = 2, name = "Bank", balance = 25000.0, type = "Bank"),
        Account(id = 3, name = "Credit Card", balance = -2500.0, type = "Credit")
    )
    
    fun generateSampleCategories(): List<Category> = listOf(
        Category(id = 1, name = "Food", icon = "🍽️"),
        Category(id = 2, name = "Transport", icon = "🚗"),
        Category(id = 3, name = "Shopping", icon = "🛒"),
        Category(id = 4, name = "Bills", icon = "💡"),
        Category(id = 5, name = "Entertainment", icon = "🎬"),
        Category(id = 6, name = "Health", icon = "🏥"),
        Category(id = 7, name = "General", icon = "📝")
    )
    
    fun generateSampleTransactions(): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val calendar = Calendar.getInstance()
        
        // Generate transactions for last 7 days
        repeat(7) { dayOffset ->
            calendar.timeInMillis = System.currentTimeMillis() - (dayOffset * 24 * 60 * 60 * 1000)
            val dayStart = calendar.timeInMillis
            
            // 2-4 transactions per day
            repeat(Random.nextInt(2, 5)) { 
                transactions.add(generateRandomTransaction(dayStart + Random.nextLong(0, 24 * 60 * 60 * 1000)))
            }
        }
        
        return transactions.sortedByDescending { it.date }
    }
    
    private fun generateRandomTransaction(timestamp: Long): Transaction {
        val expenseData = listOf(
            "Food Lunch" to 250.0,
            "Transport Bus" to 50.0,
            "Shopping Groceries" to 800.0,
            "Bills Electricity" to 1200.0,
            "Entertainment Movie" to 300.0,
            "Health Medicine" to 150.0,
            "Food Coffee" to 120.0,
            "Transport Uber" to 180.0,
            "Shopping Clothes" to 1500.0,
            "Bills Internet" to 600.0
        )
        
        val incomeData = listOf(
            "Salary Payment" to 5000.0,
            "Freelance Work" to 2000.0,
            "Investment Return" to 800.0,
            "Bonus Payment" to 1500.0
        )
        
        val isExpense = Random.nextBoolean()
        
        return if (isExpense) {
            val (title, baseAmount) = expenseData.random()
            Transaction(
                title = title,
                amount = baseAmount + Random.nextDouble(-50.0, 100.0),
                type = "Expense",
                categoryId = Random.nextInt(1, 8),
                accountId = Random.nextInt(1, 4),
                date = timestamp
            )
        } else {
            val (title, baseAmount) = incomeData.random()
            Transaction(
                title = title,
                amount = baseAmount + Random.nextDouble(-200.0, 500.0),
                type = "Income",
                categoryId = 7, // General category for income
                accountId = Random.nextInt(1, 4),
                date = timestamp
            )
        }
    }
}