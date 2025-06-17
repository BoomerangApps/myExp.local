package com.expense.smsextractor

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.expense.smsextractor.data.AppDatabase
import com.expense.smsextractor.data.ExpenseEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseViewModel(private val database: AppDatabase) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _searchQuery = MutableLiveData("")

    val draftExpenses: Flow<PagingData<ExpenseEntity>> = _searchQuery.asFlow().flatMapLatest { query ->
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = {
                if (query.isBlank()) {
                    database.expenseDao().getDraftExpenses()
                } else {
                    database.expenseDao().searchDraftExpenses("%${query}%")
                }
            }
        ).flow
    }.cachedIn(viewModelScope)

    val draftCount: LiveData<Int> = database.expenseDao().getDraftCount().asLiveData()
    val addedCount: LiveData<Int> = database.expenseDao().getAddedCount().asLiveData()

    val addedExpenses: Flow<PagingData<ExpenseEntity>> = _searchQuery.asFlow().flatMapLatest { query ->
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = {
                if (query.isBlank()) {
                    database.expenseDao().getAddedExpenses()
                } else {
                    database.expenseDao().searchAddedExpenses("%${query}%")
                }
            }
        ).flow
    }.cachedIn(viewModelScope)

    fun loadExpenses(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                try {
                    database.expenseDao().clearDrafts()
                    val extractedExpenses = ExpenseExtractor.extractFromSms(context)
                    val expenseEntities = extractedExpenses.map {
                        ExpenseEntity(
                            id = 0,
                            amount = it.amount,
                            description = it.description,
                            date = it.date,
                            timestamp = it.timestamp,
                            sender = it.sender,
                            isDraft = true
                        )
                    }.toTypedArray()

                    database.expenseDao().insertAll(*expenseEntities)
                } finally {
                    withContext(Dispatchers.Main) {
                        _isLoading.value = false
                    }
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addExpense(expenseId: Long) {
        viewModelScope.launch {
            val expense = database.expenseDao().getExpenseById(expenseId)
            if (expense != null) {
                database.expenseDao().updateExpense(
                    expense.copy(isDraft = false)
                )
            }
        }
    }
}