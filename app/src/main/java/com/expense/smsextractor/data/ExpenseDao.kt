package com.expense.smsextractor.data

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE isDraft = 1 ORDER BY timestamp DESC")
    fun getDraftExpenses(): PagingSource<Int, ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE isDraft = 0 ORDER BY timestamp DESC")
    fun getAddedExpenses(): PagingSource<Int, ExpenseEntity>

    @Query("SELECT DISTINCT strftime('%Y', date) as year, strftime('%m', date) as month, strftime('%B', date) as monthName FROM expenses WHERE isDraft = 0 ORDER BY year DESC, month DESC")
    fun getUniqueMonths(): Flow<List<MonthYear>>

    @Query("SELECT * FROM expenses WHERE isDraft = 0 AND strftime('%Y', date) = :year AND strftime('%m', date) = :month ORDER BY timestamp DESC")
    fun getExpensesByMonth(year: String, month: String): PagingSource<Int, ExpenseEntity>

    @Query("DELETE FROM expenses WHERE isDraft = 1")
    suspend fun clearDrafts()

    @Insert
    suspend fun insert(expense: ExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg expenses: ExpenseEntity)

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): ExpenseEntity?

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM expenses WHERE isDraft = 1")
    fun getDraftCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM expenses WHERE isDraft = 0")
    fun getAddedCount(): Flow<Int>

    @Query("SELECT * FROM expenses WHERE isDraft = 1 AND (description LIKE :query OR CAST(amount AS TEXT) LIKE :query) ORDER BY timestamp DESC")
    fun searchDraftExpenses(query: String): PagingSource<Int, ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE isDraft = 0 AND (description LIKE :query OR CAST(amount AS TEXT) LIKE :query) ORDER BY timestamp DESC")
    fun searchAddedExpenses(query: String): PagingSource<Int, ExpenseEntity>
}
