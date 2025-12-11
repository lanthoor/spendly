package `in`.mylullaby.spendly.data.repository

import `in`.mylullaby.spendly.data.local.dao.AccountDao
import `in`.mylullaby.spendly.data.local.entities.AccountEntity
import `in`.mylullaby.spendly.domain.model.Account
import `in`.mylullaby.spendly.domain.repository.AccountRepository
import `in`.mylullaby.spendly.utils.AccountType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of AccountRepository.
 * Handles entity-to-model mapping, account seeding, and transaction reassignment.
 */
@Singleton
class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao
) : AccountRepository {

    // CRUD operations

    override suspend fun insertAccount(account: Account): Long {
        val timestamp = System.currentTimeMillis()
        return accountDao.insert(account.toEntity(timestamp))
    }

    override suspend fun updateAccount(account: Account) {
        val timestamp = System.currentTimeMillis()
        accountDao.update(account.toEntity(timestamp))
    }

    override suspend fun deleteAccount(accountId: Long, replacementAccountId: Long) {
        // Prevent deletion of default account
        if (accountId == Account.DEFAULT_ACCOUNT_ID) {
            throw IllegalArgumentException("Cannot delete the default account")
        }

        // Reassign all transactions to the replacement account
        val timestamp = System.currentTimeMillis()
        accountDao.reassignExpensesToAccount(accountId, replacementAccountId, timestamp)
        accountDao.reassignIncomesToAccount(accountId, replacementAccountId, timestamp)

        // Delete the account
        val account = accountDao.getAccountById(accountId).firstOrNull()
        if (account != null) {
            accountDao.delete(account)
        }
    }

    override fun getAccountById(id: Long): Flow<Account?> {
        return accountDao.getAccountById(id).map { it?.toDomainModel() }
    }

    override fun getAllAccounts(): Flow<List<Account>> {
        return accountDao.getAllAccounts().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    // Predefined vs custom

    override fun getPredefinedAccounts(): Flow<List<Account>> {
        return accountDao.getPredefinedAccounts().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getCustomAccounts(): Flow<List<Account>> {
        return accountDao.getCustomAccounts().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    // Filter by type

    override fun getAccountsByType(type: AccountType): Flow<List<Account>> {
        return accountDao.getAccountsByType(type.name).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    // Seeding

    override suspend fun seedPredefinedAccounts() {
        // Check if already seeded to avoid duplicates
        if (isPredefinedSeeded()) {
            return
        }

        // Insert all predefined accounts from domain model
        val timestamp = System.currentTimeMillis()
        val entities = Account.PREDEFINED.map { it.toEntity(timestamp) }
        accountDao.insertAll(entities)
    }

    override suspend fun isPredefinedSeeded(): Boolean {
        // Check if default account exists (ID 1)
        return accountDao.exists(Account.DEFAULT_ACCOUNT_ID) > 0
    }

    // Validation

    override suspend fun isAccountNameUnique(name: String, excludeId: Long?): Boolean {
        val existing = accountDao.getAccountByName(name)
        // If no existing account with this name, it's unique
        if (existing == null) return true
        // If editing and the existing account is the one being edited, it's still unique
        if (excludeId != null && existing.id == excludeId) return true
        // Otherwise, name is taken by a different account
        return false
    }

    override suspend fun getTransactionCount(accountId: Long): Pair<Int, Int> {
        val expenseCount = accountDao.getExpenseCountByAccount(accountId)
        val incomeCount = accountDao.getIncomeCountByAccount(accountId)
        return Pair(expenseCount, incomeCount)
    }

    // Entity to Domain Model mapping

    private fun AccountEntity.toDomainModel(): Account {
        return Account(
            id = id,
            name = name,
            type = AccountType.fromStringOrDefault(type, AccountType.BANK),
            icon = icon,
            color = color,
            isCustom = isCustom,
            sortOrder = sortOrder,
            createdAt = createdAt,
            modifiedAt = modifiedAt
        )
    }

    private fun Account.toEntity(timestamp: Long = System.currentTimeMillis()): AccountEntity {
        return AccountEntity(
            id = id,
            name = name,
            type = type.name,
            icon = icon,
            color = color,
            isCustom = isCustom,
            sortOrder = sortOrder,
            createdAt = if (createdAt == 0L) timestamp else createdAt,
            modifiedAt = timestamp
        )
    }
}
