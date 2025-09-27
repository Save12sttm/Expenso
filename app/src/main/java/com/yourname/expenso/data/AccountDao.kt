package com.yourname.expenso.data

import androidx.room.*
import com.yourname.expenso.model.Account
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY name ASC")
    fun getAllAccounts(): Flow<List<Account>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: Account)

    @Update
    suspend fun updateAccount(account: Account)

    @Delete
    suspend fun deleteAccount(account: Account)

    @Query("SELECT * FROM accounts WHERE id = :accountId")
    suspend fun getAccountById(accountId: Int): Account?

    @androidx.room.Transaction
    suspend fun transfer(fromAccountId: Int, toAccountId: Int, amount: Double) {
        val fromAccount = getAccountById(fromAccountId)
        val toAccount = getAccountById(toAccountId)
        if (fromAccount != null && toAccount != null) {
            updateAccount(fromAccount.copy(balance = fromAccount.balance - amount))
            updateAccount(toAccount.copy(balance = toAccount.balance + amount))
        }
    }

    @Query("DELETE FROM accounts")
    suspend fun deleteAllAccounts()
    
    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun getAccountCount(): Int
    
    @Query("SELECT * FROM accounts WHERE name = :accountName LIMIT 1")
    suspend fun getAccountByName(accountName: String): Account?
}