package `in`.mylullaby.spendly.domain.repository

import `in`.mylullaby.spendly.domain.model.Income
import `in`.mylullaby.spendly.utils.IncomeSource
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for income operations.
 * Provides CRUD operations, queries, and aggregations for income data.
 */
interface IncomeRepository {

    // CRUD operations

    /**
     * Inserts a new income record into the database.
     * @param income The income to insert
     * @return The ID of the inserted income
     */
    suspend fun insertIncome(income: Income): Long

    /**
     * Updates an existing income record in the database.
     * @param income The income to update
     */
    suspend fun updateIncome(income: Income)

    /**
     * Deletes an income record from the database.
     * @param income The income to delete
     */
    suspend fun deleteIncome(income: Income)

    /**
     * Retrieves an income record by its ID.
     * @param id The income ID
     * @return Flow emitting the income or null if not found
     */
    fun getIncomeById(id: Long): Flow<Income?>

    /**
     * Retrieves all income records.
     * @return Flow emitting list of all income records
     */
    fun getAllIncome(): Flow<List<Income>>

    // Queries

    /**
     * Retrieves income within a date range.
     * @param startDate Start date timestamp (inclusive)
     * @param endDate End date timestamp (inclusive)
     * @return Flow emitting list of income in the date range
     */
    fun getIncomeByDateRange(startDate: Long, endDate: Long): Flow<List<Income>>

    /**
     * Retrieves income filtered by source.
     * @param source The income source
     * @return Flow emitting list of income from the source
     */
    fun getIncomeBySource(source: IncomeSource): Flow<List<Income>>

    /**
     * Retrieves income filtered by account.
     * @param accountId The account ID
     * @return Flow emitting list of income associated with the account
     */
    fun getIncomeByAccount(accountId: Long): Flow<List<Income>>

    /**
     * Retrieves all refund income (income linked to expenses).
     * @return Flow emitting list of refund income
     */
    fun getRefunds(): Flow<List<Income>>

    /**
     * Retrieves all recurring income records.
     * @return Flow emitting list of recurring income
     */
    fun getRecurringIncome(): Flow<List<Income>>

    // Aggregations (returns amounts in paise)

    /**
     * Calculates total income within a date range.
     * @param startDate Start date timestamp (inclusive)
     * @param endDate End date timestamp (inclusive)
     * @return Flow emitting total income amount in paise
     */
    fun getTotalIncomeInRange(startDate: Long, endDate: Long): Flow<Long>

    /**
     * Calculates total income from a specific source within a date range.
     * @param source The income source
     * @param startDate Start date timestamp (inclusive)
     * @param endDate End date timestamp (inclusive)
     * @return Flow emitting total income amount in paise
     */
    fun getTotalIncomeBySource(source: IncomeSource, startDate: Long, endDate: Long): Flow<Long>

    // Recent transactions

    /**
     * Retrieves the most recent income records.
     * @param limit Maximum number of records to retrieve (default: 5)
     * @return Flow emitting list of recent income
     */
    fun getRecentIncome(limit: Int = 5): Flow<List<Income>>
}
