package com.expense.smsextractor

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.expense.smsextractor.data.AppDatabase
import com.expense.smsextractor.data.ExpenseEntity
import com.expense.smsextractor.data.MonthYear
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import androidx.lifecycle.*
import androidx.paging.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class ExpenseViewModel(private val database: AppDatabase) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _searchQuery = MutableLiveData("")

    private val _selectedMonth = MutableLiveData<MonthYear>(MonthYear.DEFAULT)
    val selectedMonth: LiveData<MonthYear> = _selectedMonth

    private val _draftCount = MutableLiveData<Int>(0)
    val draftCount: LiveData<Int> = _draftCount

    private val _addedCount = MutableLiveData<Int>(0)
    val addedCount: LiveData<Int> = _addedCount

    init {
        viewModelScope.launch {
            database.expenseDao().getDraftCount().collect { count ->
                _draftCount.value = count
            }
            database.expenseDao().getAddedCount().collect { count ->
                _addedCount.value = count
            }
        }
    }

    val draftExpenses: Flow<PagingData<ExpenseEntity>> = _searchQuery.asFlow()
        .flatMapLatest { query ->
            Pager(
                config = PagingConfig(pageSize = 20, enablePlaceholders = false),
                pagingSourceFactory = { 
                    if (query.isNotBlank()) database.expenseDao().searchDraftExpenses(query)
                    else database.expenseDao().getDraftExpenses()
                }
            ).flow
        }
        .cachedIn(viewModelScope)

    val addedExpenses: Flow<PagingData<ExpenseEntity>> = _searchQuery.asFlow()
        .flatMapLatest { query ->
            Pager(
                config = PagingConfig(pageSize = 20, enablePlaceholders = false),
                pagingSourceFactory = { 
                    if (query.isNotBlank()) database.expenseDao().searchAddedExpenses(query)
                    else database.expenseDao().getAddedExpenses()
                }
            ).flow
        }
        .debounce(300L)
        .cachedIn(viewModelScope)

    val uniqueMonths: LiveData<List<MonthYear>> = database.expenseDao().getUniqueMonths()
        .asLiveData()
        .map { 
            val mutableMonths = it.toMutableList()
            mutableMonths.add(0, MonthYear.DEFAULT)
            mutableMonths
        }

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

    fun selectMonth(month: MonthYear?) {
        _selectedMonth.value = month ?: MonthYear.DEFAULT
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

    fun sendBackToDraft(expenseId: Long) {
        viewModelScope.launch {
            val expense = database.expenseDao().getExpenseById(expenseId)
            if (expense != null) {
                database.expenseDao().updateExpense(
                    expense.copy(isDraft = true)
                )
            }
        }
    }
}