package com.expense.smsextractor.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val description: String,
    val date: String,
    val timestamp: Long,
    val sender: String,
    val isDraft: Boolean = true,
    val originalSms: String = ""
)
