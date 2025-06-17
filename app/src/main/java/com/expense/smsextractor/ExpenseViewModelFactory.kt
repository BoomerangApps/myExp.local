package com.expense.smsextractor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.expense.smsextractor.data.AppDatabase

class ExpenseViewModelFactory(private val database: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}