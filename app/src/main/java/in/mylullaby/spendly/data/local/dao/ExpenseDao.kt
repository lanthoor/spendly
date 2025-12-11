package `in`.mylullaby.spendly.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import `in`.mylullaby.spendly.data.local.entities.ExpenseEntity

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
    fun getExpensesByCategoryGrouped(startDate: Long, endDate: Long): Flow<List<CategoryExpenseSummary>>

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
     * Get expenses with a specific tag.
     * Uses JOIN with transaction_tags table.
     */
    @Query("""
        SELECT expenses.*
        FROM expenses
        INNER JOIN transaction_tags ON expenses.id = transaction_tags.transaction_id
        WHERE transaction_tags.tag_id = :tagId
        AND transaction_tags.transaction_type = 'EXPENSE'
        ORDER BY expenses.date DESC
    """)
    fun getExpensesByTag(tagId: Long): Flow<List<ExpenseEntity>>
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
