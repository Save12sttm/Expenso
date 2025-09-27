package com.yourname.expenso.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yourname.expenso.model.Transaction
import com.yourname.expenso.model.Category
import com.yourname.expenso.model.Account

@Database(entities = [Transaction::class, Category::class, Account::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao
}