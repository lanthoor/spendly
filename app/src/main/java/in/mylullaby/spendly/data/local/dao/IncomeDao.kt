package `in`.mylullaby.spendly.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import `in`.mylullaby.spendly.data.local.entities.IncomeEntity

/**
 * Data Access Object for Income operations.
 *
 * Provides methods for CRUD operations and complex queries on income,
 * including aggregations, filtering, and grouping for analytics.
 */
@Dao
interface IncomeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(income: IncomeEntity): Long

    @Update
    suspend fun update(income: IncomeEntity)

    @Delete
    suspend fun delete(income: IncomeEntity)

    /**
     * Get all income ordered by date (newest first).
     */
    @Query("SELECT * FROM income ORDER BY date DESC, created_at DESC")
    fun getAllIncome(): Flow<List<IncomeEntity>>

    /**
     * Get an income by ID.
     */
    @Query("SELECT * FROM income WHERE id = :incomeId")
    fun getIncomeById(incomeId: Long): Flow<IncomeEntity?>

    /**
     * Get income within a date range.
     */
    @Query("SELECT * FROM income WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getIncomeByDateRange(startDate: Long, endDate: Long): Flow<List<IncomeEntity>>

    /**
     * Get all income for a specific source.
     */
    @Query("SELECT * FROM income WHERE source = :source ORDER BY date DESC")
    fun getIncomeBySource(source: String): Flow<List<IncomeEntity>>

    /**
     * Get all income for a specific account.
     */
    @Query("SELECT * FROM income WHERE account_id = :accountId ORDER BY date DESC")
    fun getIncomeByAccount(accountId: Long): Flow<List<IncomeEntity>>

    /**
     * Get income linked to a specific expense (refunds).
     */
    @Query("SELECT * FROM income WHERE linked_expense_id = :expenseId")
    fun getIncomeByLinkedExpense(expenseId: Long): Flow<List<IncomeEntity>>

    /**
     * Get the most recent income.
     */
    @Query("SELECT * FROM income ORDER BY date DESC, created_at DESC LIMIT :limit")
    fun getRecentIncome(limit: Int): Flow<List<IncomeEntity>>

    /**
     * Get total income amount for a date range.
     */
    @Query("SELECT SUM(amount) FROM income WHERE date BETWEEN :startDate AND :endDate")
    fun getTotalIncomeByDateRange(startDate: Long, endDate: Long): Flow<Long?>

    /**
     * Get total income amount for a specific source within a date range.
     */
    @Query("SELECT SUM(amount) FROM income WHERE source = :source AND date BETWEEN :startDate AND :endDate")
    fun getTotalIncomeBySource(source: String, startDate: Long, endDate: Long): Flow<Long?>

    /**
     * Get income grouped by source for a date range.
     * Used for analytics and charts.
     */
    @Query("SELECT source, SUM(amount) as total FROM income WHERE date BETWEEN :startDate AND :endDate GROUP BY source ORDER BY total DESC")
    fun getIncomeBySourceGrouped(startDate: Long, endDate: Long): Flow<List<SourceIncomeSummary>>

    /**
     * Search income by description.
     */
    @Query("SELECT * FROM income WHERE description LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchIncome(query: String): Flow<List<IncomeEntity>>

    /**
     * Get income with a specific tag.
     * Uses JOIN with transaction_tags table.
     */
    @Query("""
        SELECT income.*
        FROM income
        INNER JOIN transaction_tags ON income.id = transaction_tags.transaction_id
        WHERE transaction_tags.tag_id = :tagId
        AND transaction_tags.transaction_type = 'INCOME'
        ORDER BY income.date DESC
    """)
    fun getIncomeByTag(tagId: Long): Flow<List<IncomeEntity>>
}

/**
 * Data class for grouped income results.
 */
data class SourceIncomeSummary(
    @ColumnInfo(name = "source")
    val source: String,

    @ColumnInfo(name = "total")
    val total: Long
)
