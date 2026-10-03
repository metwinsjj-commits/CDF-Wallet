package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subtitle: String,
    val amount: Double,
    val currency: String, // "CDF" or "USD"
    val type: String, // "SEND", "RECEIVE", "EXCHANGE", "BILL", "AIRTIME", "TOPUP", "SAVINGS"
    val status: String = "SUCCESS", // "SUCCESS", "PENDING", "FAILED"
    val recipientOrSource: String,
    val fee: Double = 0.0,
    val referenceCode: String,
    val note: String = "",
    val category: String = "Général",
    val timestamp: Long = System.currentTimeMillis()
)
