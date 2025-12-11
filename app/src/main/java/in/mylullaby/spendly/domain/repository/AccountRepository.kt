package `in`.mylullaby.spendly.domain.repository

import `in`.mylullaby.spendly.domain.model.Account
import `in`.mylullaby.spendly.utils.AccountType
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for account operations.
 * Handles both predefined and custom accounts.
 */
interface AccountRepository {

    // CRUD operations

    /**
     * Inserts a new account into the database.
     * @param account The account to insert
     * @return The ID of the inserted account
     */
    suspend fun insertAccount(account: Account): Long

    /**
     * Updates an existing account in the database.
     * @param account The account to update
     */
    suspend fun updateAccount(account: Account)

    /**
     * Deletes an account and reassigns all transactions to a replacement account.
     * Cannot delete the default account (ID 1).
     * @param accountId The ID of the account to delete
     * @param replacementAccountId The ID of the account to reassign transactions to
     * @throws IllegalArgumentException if trying to delete default account
     */
    suspend fun deleteAccount(accountId: Long, replacementAccountId: Long)

    /**
     * Retrieves an account by its ID.
     * @param id The account ID
     * @return Flow emitting the account or null if not found
     */
    fun getAccountById(id: Long): Flow<Account?>

    /**
     * Retrieves all accounts (both predefined and custom).
     * @return Flow emitting list of all accounts
     */
    fun getAllAccounts(): Flow<List<Account>>

    // Predefined vs custom

    /**
     * Retrieves only predefined accounts.
     * @return Flow emitting list of predefined accounts
     */
    fun getPredefinedAccounts(): Flow<List<Account>>

    /**
     * Retrieves only custom (user-created) accounts.
     * @return Flow emitting list of custom accounts
     */
    fun getCustomAccounts(): Flow<List<Account>>

    // Filter by type

    /**
     * Retrieves all accounts of a specific type.
     * @param type The account type to filter by
     * @return Flow emitting list of accounts of the specified type
     */
    fun getAccountsByType(type: AccountType): Flow<List<Account>>

    // Seeding

    /**
     * Seeds the database with predefined account ("My Account").
     * Should be called on first app launch.
     */
    suspend fun seedPredefinedAccounts()

    /**
     * Checks if predefined accounts have already been seeded.
     * @return true if predefined accounts exist in the database
     */
    suspend fun isPredefinedSeeded(): Boolean

    // Validation

    /**
     * Checks if an account name is unique (not already in use).
     * @param name The account name to check
     * @param excludeId Optional account ID to exclude from the check (for edit mode)
     * @return true if the name is unique
     */
    suspend fun isAccountNameUnique(name: String, excludeId: Long? = null): Boolean

    /**
     * Gets the count of transactions associated with an account.
     * @param accountId The account ID
     * @return Pair of (expense count, income count)
     */
    suspend fun getTransactionCount(accountId: Long): Pair<Int, Int>
}
