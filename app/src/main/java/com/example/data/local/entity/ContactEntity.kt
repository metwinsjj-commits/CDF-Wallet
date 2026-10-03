package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val operator: String, // "M-Pesa (Vodacom)", "Airtel Money", "Orange Money", "Afrimoney"
    val isFavorite: Boolean = false,
    val avatarColorHex: String = "#0F4C81"
)
