package dev.lanthoor.spendly.data.local.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dev.lanthoor.spendly.data.local.entities.IncomeEntity
import kotlinx.coroutines.flow.Flow

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

    @Query("SELECT * FROM income WHERE id IN (:ids)")
    suspend fun getIncomeByIds(ids: List<Long>): List<IncomeEntity>

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
     * Get monthly income totals for analytics charts.
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
        FROM income
        WHERE date BETWEEN :startDate AND :endDate
        GROUP BY month
        ORDER BY month
    """
    )
    fun getMonthlyIncomeTotals(startDate: Long, endDate: Long): Flow<List<MonthlyIncomeSummary>>

    /**
     * Get daily income totals for trend line charts.
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
        FROM income
        WHERE date BETWEEN :startDate AND :endDate
        GROUP BY day
        ORDER BY day
    """
    )
    fun getDailyIncomeTotals(startDate: Long, endDate: Long): Flow<List<DailyIncomeSummary>>

    /**
     * Get all income as a snapshot (one-shot query for export).
     * Does not return Flow for reactive updates.
     *
     * @return List of all income
     */
    @Query("SELECT * FROM income ORDER BY date DESC")
    suspend fun getAllSnapshot(): List<IncomeEntity>

    @Query(
        """
        SELECT * FROM income
        WHERE sms_body IS NOT NULL
          AND sms_timestamp IS NOT NULL
          AND sms_timestamp >= :minSmsTimestamp
        ORDER BY sms_timestamp DESC
        """
    )
    suspend fun getSmsLinkedSnapshotSince(minSmsTimestamp: Long): List<IncomeEntity>

    /**
     * Delete all income.
     * Used during import to clear all income data before restoring.
     */
    @Query("DELETE FROM income")
    suspend fun deleteAll()
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

/**
 * Data class for monthly income aggregation.
 * Used by getMonthlyIncomeTotals query.
 */
data class MonthlyIncomeSummary(
    @ColumnInfo(name = "month")
    val month: String, // Format: YYYY-MM

    @ColumnInfo(name = "total")
    val total: Long // Total amount in paise
)

/**
 * Data class for daily income aggregation.
 * Used by getDailyIncomeTotals query.
 */
data class DailyIncomeSummary(
    @ColumnInfo(name = "day")
    val day: String, // Format: YYYY-MM-DD

    @ColumnInfo(name = "total")
    val total: Long // Total amount in paise
)
