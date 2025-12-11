package `in`.mylullaby.spendly.data.repository

import `in`.mylullaby.spendly.data.local.dao.RecurringTransactionDao
import `in`.mylullaby.spendly.data.local.entities.RecurringTransactionEntity
import `in`.mylullaby.spendly.domain.model.RecurringTransaction
import `in`.mylullaby.spendly.domain.repository.RecurringTransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of RecurringTransactionRepository.
 * Handles entity-to-model mapping and delegates database operations to DAO.
 */
@Singleton
class RecurringTransactionRepositoryImpl @Inject constructor(
    private val recurringTransactionDao: RecurringTransactionDao
) : RecurringTransactionRepository {

    override suspend fun insertRecurringTransaction(recurringTransaction: RecurringTransaction): Long {
        return recurringTransactionDao.insert(entityFrom(recurringTransaction))
    }

    override suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransaction) {
        recurringTransactionDao.update(entityFrom(recurringTransaction))
    }

    override suspend fun deleteRecurringTransaction(recurringTransaction: RecurringTransaction) {
        recurringTransactionDao.delete(entityFrom(recurringTransaction))
    }

    override fun getRecurringTransactionById(id: Long): Flow<RecurringTransaction?> {
        return recurringTransactionDao.getRecurringTransactionById(id).map { entity ->
            entity?.let { modelFrom(it) }
        }
    }

    override fun getAllRecurringTransactions(): Flow<List<RecurringTransaction>> {
        return recurringTransactionDao.getAllRecurringTransactions().map { entities ->
            entities.map { modelFrom(it) }
        }
    }

    override fun getRecurringTransactionsByType(type: String): Flow<List<RecurringTransaction>> {
        return recurringTransactionDao.getRecurringTransactionsByType(type).map { entities ->
            entities.map { modelFrom(it) }
        }
    }

    override fun getRecurringTransactionsByFrequency(frequency: String): Flow<List<RecurringTransaction>> {
        // Filter in memory since DAO doesn't have this query
        return getAllRecurringTransactions().map { transactions ->
            transactions.filter { it.frequency.equals(frequency, ignoreCase = true) }
        }
    }

    // Entity to Domain Model mapping

    private fun modelFrom(entity: RecurringTransactionEntity): RecurringTransaction {
        return RecurringTransaction(
            id = entity.id,
            transactionType = entity.transactionType,
            amount = entity.amount,
            categoryId = entity.categoryId,
            description = entity.description,
            frequency = entity.frequency,
            nextDate = entity.nextDate,
            lastProcessed = entity.lastProcessed,
            paymentMethod = null, // Entity doesn't have this field
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt
        )
    }

    private fun entityFrom(model: RecurringTransaction): RecurringTransactionEntity {
        return RecurringTransactionEntity(
            id = model.id,
            transactionType = model.transactionType,
            amount = model.amount,
            categoryId = model.categoryId,
            description = model.description,
            frequency = model.frequency,
            nextDate = model.nextDate,
            lastProcessed = model.lastProcessed,
            createdAt = model.createdAt,
            modifiedAt = model.modifiedAt
        )
    }
}
