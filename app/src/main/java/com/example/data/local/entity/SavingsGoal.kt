package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // "Tontine", "Projet Kinshasa", "Frais Scolaires", "Urgence"
    val targetAmount: Double,
    val currentAmount: Double,
    val currency: String, // "CDF" or "USD"
    val deadlineDays: Int = 30,
    val isLocked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
