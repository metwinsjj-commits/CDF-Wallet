package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallet_accounts")
data class WalletAccount(
    @PrimaryKey val currency: String, // "CDF" or "USD"
    val balance: Double,
    val accountName: String,
    val accountNumber: String,
    val updatedAt: Long = System.currentTimeMillis()
)
