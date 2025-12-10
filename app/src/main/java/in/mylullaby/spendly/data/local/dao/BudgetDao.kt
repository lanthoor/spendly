package `in`.mylullaby.spendly.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import `in`.mylullaby.spendly.data.local.entities.BudgetEntity

/**
 * Data Access Object for Budget operations.
 *
 * Provides methods for managing spending budgets and tracking notifications.
 */
@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity): Long

    @Update
    suspend fun update(budget: BudgetEntity)

    @Delete
    suspend fun delete(budget: BudgetEntity)

    /**
     * Get all budgets for a specific month and year.
     */
    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year ORDER BY category_id ASC")
    fun getBudgetsByMonth(month: Int, year: Int): Flow<List<BudgetEntity>>

    /**
     * Get a budget for a specific category and month.
     */
    @Query("SELECT * FROM budgets WHERE category_id = :categoryId AND month = :month AND year = :year LIMIT 1")
    fun getBudgetByCategoryAndMonth(categoryId: Long?, month: Int, year: Int): Flow<BudgetEntity?>

    /**
     * Get the overall budget (null category) for a specific month.
     */
    @Query("SELECT * FROM budgets WHERE category_id IS NULL AND month = :month AND year = :year LIMIT 1")
    fun getOverallBudget(month: Int, year: Int): Flow<BudgetEntity?>

    /**
     * Get a budget by ID.
     */
    @Query("SELECT * FROM budgets WHERE id = :budgetId")
    fun getBudgetById(budgetId: Long): Flow<BudgetEntity?>

    /**
     * Get all budgets ordered by newest first.
     */
    @Query("SELECT * FROM budgets ORDER BY year DESC, month DESC")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    /**
     * Get count of budgets for a specific category.
     * Used before category deletion.
     */
    @Query("SELECT COUNT(*) FROM budgets WHERE category_id = :categoryId")
    suspend fun getBudgetCountByCategory(categoryId: Long): Int

    /**
     * Update 75% notification status.
     */
    @Query("UPDATE budgets SET notification_75_sent = :sent WHERE id = :budgetId")
    suspend fun updateNotification75Sent(budgetId: Long, sent: Boolean)

    /**
     * Update 100% notification status.
     */
    @Query("UPDATE budgets SET notification_100_sent = :sent WHERE id = :budgetId")
    suspend fun updateNotification100Sent(budgetId: Long, sent: Boolean)
}
