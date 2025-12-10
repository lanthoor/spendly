package `in`.mylullaby.spendly.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import `in`.mylullaby.spendly.data.local.entities.RecurringTransactionEntity

/**
 * Data Access Object for RecurringTransaction operations.
 *
 * Provides methods for managing recurring transaction configurations
 * and processing due transactions.
 */
@Dao
interface RecurringTransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recurringTransaction: RecurringTransactionEntity): Long

    @Update
    suspend fun update(recurringTransaction: RecurringTransactionEntity)

    @Delete
    suspend fun delete(recurringTransaction: RecurringTransactionEntity)

    /**
     * Get all recurring transactions ordered by next date.
     */
    @Query("SELECT * FROM recurring_transactions ORDER BY next_date ASC")
    fun getAllRecurringTransactions(): Flow<List<RecurringTransactionEntity>>

    /**
     * Get a recurring transaction by ID.
     */
    @Query("SELECT * FROM recurring_transactions WHERE id = :id")
    fun getRecurringTransactionById(id: Long): Flow<RecurringTransactionEntity?>

    /**
     * Get recurring transactions by type (EXPENSE or INCOME).
     */
    @Query("SELECT * FROM recurring_transactions WHERE transaction_type = :type ORDER BY next_date ASC")
    fun getRecurringTransactionsByType(type: String): Flow<List<RecurringTransactionEntity>>

    /**
     * Get recurring transactions that are due for processing.
     * Used at app startup to create pending transactions.
     *
     * Note: Returns List (not Flow) for synchronous processing.
     */
    @Query("SELECT * FROM recurring_transactions WHERE next_date <= :currentDate ORDER BY next_date ASC")
    suspend fun getDueRecurringTransactions(currentDate: Long): List<RecurringTransactionEntity>

    /**
     * Get recurring transactions for a specific category.
     */
    @Query("SELECT * FROM recurring_transactions WHERE category_id = :categoryId")
    fun getRecurringTransactionsByCategory(categoryId: Long): Flow<List<RecurringTransactionEntity>>

    /**
     * Get count of recurring transactions for a specific category.
     * Used before category deletion.
     */
    @Query("SELECT COUNT(*) FROM recurring_transactions WHERE category_id = :categoryId")
    suspend fun getRecurringTransactionCountByCategory(categoryId: Long): Int
}
