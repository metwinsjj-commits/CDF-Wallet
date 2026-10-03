package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.WalletAccount
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet_accounts")
    fun getAllAccounts(): Flow<List<WalletAccount>>

    @Query("SELECT * FROM wallet_accounts WHERE currency = :currency LIMIT 1")
    fun getAccount(currency: String): Flow<WalletAccount?>

    @Query("SELECT * FROM wallet_accounts WHERE currency = :currency LIMIT 1")
    suspend fun getAccountSync(currency: String): WalletAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(account: WalletAccount)

    @Query("UPDATE wallet_accounts SET balance = :balance, updatedAt = :updatedAt WHERE currency = :currency")
    suspend fun updateBalance(currency: String, balance: Double, updatedAt: Long = System.currentTimeMillis())
}
