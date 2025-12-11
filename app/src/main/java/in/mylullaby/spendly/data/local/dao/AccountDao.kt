package `in`.mylullaby.spendly.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import `in`.mylullaby.spendly.data.local.entities.AccountEntity

/**
 * Data Access Object for Account operations.
 *
 * Provides methods for CRUD operations and queries on accounts.
 * All query methods return Flow for reactive updates.
 *
 * Note: Accounts use ON DELETE RESTRICT for foreign keys, so accounts
 * with associated transactions must have their transactions reassigned
 * before deletion.
 */
@Dao
interface AccountDao {

    /**
     * Insert a new account.
     * Replaces existing account if conflict occurs (based on unique name).
     *
     * @param account Account to insert
     * @return Row ID of inserted account
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: AccountEntity): Long

    /**
     * Insert multiple accounts.
     * Used for pre-populating predefined accounts.
     *
     * @param accounts List of accounts to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(accounts: List<AccountEntity>)

    /**
     * Update an existing account.
     *
     * @param account Account with updated values
     */
    @Update
    suspend fun update(account: AccountEntity)

    /**
     * Delete an account.
     * Note: Will fail if account has associated transactions (FK RESTRICT).
     * Use reassignment methods before deletion.
     *
     * @param account Account to delete
     */
    @Delete
    suspend fun delete(account: AccountEntity)

    /**
     * Get all accounts ordered by sort order.
     * Includes both predefined and custom accounts.
     *
     * @return Flow of account list
     */
    @Query("SELECT * FROM accounts ORDER BY sort_order ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    /**
     * Get an account by ID.
     *
     * @param accountId Account ID
     * @return Flow of account (null if not found)
     */
    @Query("SELECT * FROM accounts WHERE id = :accountId")
    fun getAccountById(accountId: Long): Flow<AccountEntity?>

    /**
     * Get all custom (user-created) accounts.
     *
     * @return Flow of custom account list
     */
    @Query("SELECT * FROM accounts WHERE is_custom = 1 ORDER BY sort_order ASC")
    fun getCustomAccounts(): Flow<List<AccountEntity>>

    /**
     * Get all predefined accounts.
     *
     * @return Flow of predefined account list
     */
    @Query("SELECT * FROM accounts WHERE is_custom = 0 ORDER BY sort_order ASC")
    fun getPredefinedAccounts(): Flow<List<AccountEntity>>

    /**
     * Get all accounts of a specific type.
     *
     * @param type Account type (BANK/CARD/WALLET/CASH/LOAN/INVESTMENT)
     * @return Flow of account list filtered by type
     */
    @Query("SELECT * FROM accounts WHERE type = :type ORDER BY sort_order ASC")
    fun getAccountsByType(type: String): Flow<List<AccountEntity>>

    /**
     * Check if an account exists by ID.
     *
     * @param accountId Account ID to check
     * @return Count (1 if exists, 0 if not)
     */
    @Query("SELECT COUNT(*) FROM accounts WHERE id = :accountId")
    suspend fun exists(accountId: Long): Int

    /**
     * Get an account by name.
     * Used to check for duplicate names before insert.
     *
     * @param name Account name
     * @return Account entity (null if not found)
     */
    @Query("SELECT * FROM accounts WHERE name = :name LIMIT 1")
    suspend fun getAccountByName(name: String): AccountEntity?

    /**
     * Get count of expenses associated with an account.
     * Used before deletion to check if reassignment is needed.
     *
     * @param accountId Account ID
     * @return Count of expenses using this account
     */
    @Query("SELECT COUNT(*) FROM expenses WHERE account_id = :accountId")
    suspend fun getExpenseCountByAccount(accountId: Long): Int

    /**
     * Get count of income entries associated with an account.
     * Used before deletion to check if reassignment is needed.
     *
     * @param accountId Account ID
     * @return Count of income entries using this account
     */
    @Query("SELECT COUNT(*) FROM income WHERE account_id = :accountId")
    suspend fun getIncomeCountByAccount(accountId: Long): Int

    /**
     * Reassign all expenses from one account to another.
     * Used when deleting an account to preserve transaction data.
     *
     * @param oldAccountId Account ID being deleted
     * @param newAccountId Account ID to reassign transactions to
     * @param timestamp Current timestamp for modified_at field
     */
    @Transaction
    @Query("""
        UPDATE expenses
        SET account_id = :newAccountId, modified_at = :timestamp
        WHERE account_id = :oldAccountId
    """)
    suspend fun reassignExpensesToAccount(
        oldAccountId: Long,
        newAccountId: Long,
        timestamp: Long
    )

    /**
     * Reassign all income entries from one account to another.
     * Used when deleting an account to preserve transaction data.
     *
     * @param oldAccountId Account ID being deleted
     * @param newAccountId Account ID to reassign transactions to
     * @param timestamp Current timestamp for modified_at field
     */
    @Transaction
    @Query("""
        UPDATE income
        SET account_id = :newAccountId, modified_at = :timestamp
        WHERE account_id = :oldAccountId
    """)
    suspend fun reassignIncomesToAccount(
        oldAccountId: Long,
        newAccountId: Long,
        timestamp: Long
    )
}
