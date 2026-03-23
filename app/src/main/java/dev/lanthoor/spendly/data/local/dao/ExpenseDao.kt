package dev.lanthoor.spendly.data.local.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dev.lanthoor.spendly.data.local.entities.ExpenseEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Expense operations.
 *
 * Provides methods for CRUD operations and complex queries on expenses,
 * including aggregations, filtering, and grouping for analytics.
 */
@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: ExpenseEntity): Long

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Delete
    suspend fun delete(expense: ExpenseEntity)

    /**
     * Get all expenses ordered by date (newest first).
     */
    @Query("SELECT * FROM expenses ORDER BY date DESC, created_at DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    /**
     * Get an expense by ID.
     */
    @Query("SELECT * FROM expenses WHERE id = :expenseId")
    fun getExpenseById(expenseId: Long): Flow<ExpenseEntity?>

    /**
     * Get expenses within a date range.
     *
     * @param startDate Start of range (inclusive)
     * @param endDate End of range (inclusive)
     */
    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>

    /**
     * Get all expenses for a specific category.
     */
    @Query("SELECT * FROM expenses WHERE category_id = :categoryId ORDER BY date DESC")
    fun getExpensesByCategory(categoryId: Long): Flow<List<ExpenseEntity>>

    /**
     * Get all expenses for a specific account.
     */
    @Query("SELECT * FROM expenses WHERE account_id = :accountId ORDER BY date DESC")
    fun getExpensesByAccount(accountId: Long): Flow<List<ExpenseEntity>>

    /**
     * Get the most recent expenses.
     *
     * @param limit Maximum number of expenses to return
     */
    @Query("SELECT * FROM expenses ORDER BY date DESC, created_at DESC LIMIT :limit")
    fun getRecentExpenses(limit: Int): Flow<List<ExpenseEntity>>

    /**
     * Get total expense amount for a date range.
     *
     * @return Total amount in paise (null if no expenses)
     */
    @Query("SELECT SUM(amount) FROM expenses WHERE date BETWEEN :startDate AND :endDate")
    fun getTotalExpensesByDateRange(startDate: Long, endDate: Long): Flow<Long?>

    /**
     * Get total expense amount for a specific category within a date range.
     *
     * @return Total amount in paise (null if no expenses)
     */
    @Query("SELECT SUM(amount) FROM expenses WHERE category_id = :categoryId AND date BETWEEN :startDate AND :endDate")
    fun getTotalExpensesByCategory(categoryId: Long, startDate: Long, endDate: Long): Flow<Long?>

    /**
     * Get expenses grouped by category for a date range.
     * Used for analytics and charts.
     *
     * @return List of category summaries with totals
     */
    @Query("SELECT category_id, SUM(amount) as total FROM expenses WHERE date BETWEEN :startDate AND :endDate GROUP BY category_id ORDER BY total DESC")
    fun getExpensesByCategoryGrouped(
        startDate: Long,
        endDate: Long
    ): Flow<List<CategoryExpenseSummary>>

    /**
     * Get count of expenses for a specific category.
     * Used before category deletion to warn user.
     */
    @Query("SELECT COUNT(*) FROM expenses WHERE category_id = :categoryId")
    suspend fun getExpenseCountByCategory(categoryId: Long): Int

    /**
     * Search expenses by description.
     * Basic LIKE search - will be enhanced with FTS in future task.
     */
    @Query("SELECT * FROM expenses WHERE description LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchExpenses(query: String): Flow<List<ExpenseEntity>>

    /**
     * Get monthly expense totals for analytics charts.
     * Returns month-wise aggregation in YYYY-MM format.
     *
     * @param startDate Start of date range (inclusive)
     * @param endDate End of date range (inclusive)
     * @return List of monthly totals sorted by month
     */
    @Query(
        """
        SELECT strftime('%Y-%m', datetime(date/1000, 'unixepoch')) as month,
        SUM(amount) as total
        FROM expenses
        WHERE date BETWEEN :startDate AND :endDate
        GROUP BY month
        ORDER BY month
    """
    )
    fun getMonthlyExpenseTotals(startDate: Long, endDate: Long): Flow<List<MonthlyExpenseSummary>>

    /**
     * Get daily expense totals for trend line charts.
     * Returns day-wise aggregation in YYYY-MM-DD format.
     *
     * @param startDate Start of date range (inclusive)
     * @param endDate End of date range (inclusive)
     * @return List of daily totals sorted by date
     */
    @Query(
        """
        SELECT date(date/1000, 'unixepoch') as day,
        SUM(amount) as total
        FROM expenses
        WHERE date BETWEEN :startDate AND :endDate
        GROUP BY day
        ORDER BY day
    """
    )
    fun getDailyExpenseTotals(startDate: Long, endDate: Long): Flow<List<DailyExpenseSummary>>

    /**
     * Get all expenses as a snapshot (one-shot query for export).
     * Does not return Flow for reactive updates.
     *
     * @return List of all expenses
     */
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    suspend fun getAllSnapshot(): List<ExpenseEntity>

    @Query(
        """
        SELECT * FROM expenses
        WHERE sms_body IS NOT NULL
          AND sms_timestamp IS NOT NULL
          AND sms_timestamp >= :minSmsTimestamp
        ORDER BY sms_timestamp DESC
        """
    )
    suspend fun getSmsLinkedSnapshotSince(minSmsTimestamp: Long): List<ExpenseEntity>

    /**
     * Delete all expenses.
     * Used during import to clear all expense data before restoring.
     * Receipts will be cascaded deleted automatically (FK CASCADE).
     */
    @Query("DELETE FROM expenses")
    suspend fun deleteAll()
}

/**
 * Data class for grouped expense results.
 * Used by getExpensesByCategoryGrouped query.
 */
data class CategoryExpenseSummary(
    @ColumnInfo(name = "category_id")
    val categoryId: Long?,

    @ColumnInfo(name = "total")
    val total: Long
)

/**
 * Data class for monthly expense aggregation.
 * Used by getMonthlyExpenseTotals query.
 */
data class MonthlyExpenseSummary(
    @ColumnInfo(name = "month")
    val month: String, // Format: YYYY-MM

    @ColumnInfo(name = "total")
    val total: Long // Total amount in paise
)

/**
 * Data class for daily expense aggregation.
 * Used by getDailyExpenseTotals query.
 */
data class DailyExpenseSummary(
    @ColumnInfo(name = "day")
    val day: String, // Format: YYYY-MM-DD

    @ColumnInfo(name = "total")
    val total: Long // Total amount in paise
)
