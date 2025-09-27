package com.yourname.expenso.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val amount: Double,
    val type: String, // "Income" or "Expense"
    val categoryId: Int,
    val accountId: Int,
    val date: Long,
    val isDeleted: Boolean = false
)