package com.example.new_app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses ORDER BY date DESC LIMIT :limit OFFSET :offset")
    fun getExpensesPaginated(limit: Int, offset: Int): Flow<List<ExpenseEntity>>

    @Query("SELECT COUNT(*) FROM expenses")
    fun getExpenseCount(): Flow<Int>

    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    suspend fun getExpenseById(id: Long): ExpenseEntity?

    @Query("SELECT * FROM expenses WHERE vendorName LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%' OR rawOcrText LIKE '%' || :query || '%' OR supplierPhoneOrContact LIKE '%' || :query || '%' ORDER BY date DESC LIMIT :limit OFFSET :offset")
    fun searchExpensesPaginated(query: String, limit: Int, offset: Int): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY date DESC LIMIT :limit OFFSET :offset")
    fun getExpensesByCategoryPaginated(category: String, limit: Int, offset: Int): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)

    @Query("SELECT SUM(totalAmount) FROM expenses")
    fun getTotalExpenseAmount(): Flow<Double?>

    @Query("SELECT category, SUM(totalAmount) as total FROM expenses GROUP BY category")
    fun getCategorySummaries(): Flow<List<CategorySummary>>
}
