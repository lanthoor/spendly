package `in`.mylullaby.spendly.domain.repository

import `in`.mylullaby.spendly.domain.model.RecurringTransaction
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for recurring transaction operations.
 * Provides CRUD operations for recurring transaction templates.
 */
interface RecurringTransactionRepository {

    /**
     * Inserts a new recurring transaction.
     * @param recurringTransaction The recurring transaction to insert
     * @return The ID of the inserted recurring transaction
     */
    suspend fun insertRecurringTransaction(recurringTransaction: RecurringTransaction): Long

    /**
     * Updates an existing recurring transaction.
     * @param recurringTransaction The recurring transaction to update
     */
    suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransaction)

    /**
     * Deletes a recurring transaction.
     * @param recurringTransaction The recurring transaction to delete
     */
    suspend fun deleteRecurringTransaction(recurringTransaction: RecurringTransaction)

    /**
     * Retrieves a recurring transaction by its ID.
     * @param id The recurring transaction ID
     * @return Flow emitting the recurring transaction or null if not found
     */
    fun getRecurringTransactionById(id: Long): Flow<RecurringTransaction?>

    /**
     * Retrieves all recurring transactions.
     * @return Flow emitting list of all recurring transactions
     */
    fun getAllRecurringTransactions(): Flow<List<RecurringTransaction>>

    /**
     * Retrieves recurring transactions by type (EXPENSE or INCOME).
     * @param type The transaction type
     * @return Flow emitting list of recurring transactions of the specified type
     */
    fun getRecurringTransactionsByType(type: String): Flow<List<RecurringTransaction>>

    /**
     * Retrieves recurring transactions by frequency.
     * @param frequency The frequency (DAILY, WEEKLY, MONTHLY)
     * @return Flow emitting list of recurring transactions with the specified frequency
     */
    fun getRecurringTransactionsByFrequency(frequency: String): Flow<List<RecurringTransaction>>
}
