package `in`.mylullaby.spendly.data.repository

import `in`.mylullaby.spendly.data.local.dao.IncomeDao
import `in`.mylullaby.spendly.data.local.entities.IncomeEntity
import `in`.mylullaby.spendly.domain.model.Income
import `in`.mylullaby.spendly.domain.repository.IncomeRepository
import `in`.mylullaby.spendly.utils.IncomeSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of IncomeRepository.
 * Handles entity-to-model mapping and delegates database operations to DAO.
 */
@Singleton
class IncomeRepositoryImpl @Inject constructor(
    private val incomeDao: IncomeDao
) : IncomeRepository {

    // CRUD operations

    override suspend fun insertIncome(income: Income): Long {
        return incomeDao.insert(incomeEntityFrom(income))
    }

    override suspend fun updateIncome(income: Income) {
        incomeDao.update(incomeEntityFrom(income))
    }

    override suspend fun deleteIncome(income: Income) {
        incomeDao.delete(incomeEntityFrom(income))
    }

    override fun getIncomeById(id: Long): Flow<Income?> {
        return incomeDao.getIncomeById(id).map { it?.let { entity -> incomeFrom(entity) } }
    }

    override fun getAllIncome(): Flow<List<Income>> {
        return incomeDao.getAllIncome().map { entities ->
            entities.map { incomeFrom(it) }
        }
    }

    // Queries

    override fun getIncomeByDateRange(startDate: Long, endDate: Long): Flow<List<Income>> {
        return incomeDao.getIncomeByDateRange(startDate, endDate).map { entities ->
            entities.map { incomeFrom(it) }
        }
    }

    override fun getIncomeBySource(source: IncomeSource): Flow<List<Income>> {
        return incomeDao.getIncomeBySource(source.name).map { entities ->
            entities.map { incomeFrom(it) }
        }
    }

    override fun getRefunds(): Flow<List<Income>> {
        // Refunds are income with linkedExpenseId not null
        return incomeDao.getAllIncome().map { entities ->
            entities.filter { it.linkedExpenseId != null }.map { incomeFrom(it) }
        }
    }

    override fun getRecurringIncome(): Flow<List<Income>> {
        return incomeDao.getAllIncome().map { entities ->
            entities.filter { it.isRecurring }.map { incomeFrom(it) }
        }
    }

    // Aggregations (returns amounts in paise)

    override fun getTotalIncomeInRange(startDate: Long, endDate: Long): Flow<Long> {
        return incomeDao.getTotalIncomeByDateRange(startDate, endDate).map { it ?: 0L }
    }

    override fun getTotalIncomeBySource(
        source: IncomeSource,
        startDate: Long,
        endDate: Long
    ): Flow<Long> {
        return incomeDao.getTotalIncomeBySource(source.name, startDate, endDate).map { it ?: 0L }
    }

    // Recent transactions

    override fun getRecentIncome(limit: Int): Flow<List<Income>> {
        return incomeDao.getRecentIncome(limit).map { entities ->
            entities.map { incomeFrom(it) }
        }
    }

    // Entity to Domain Model mapping

    private fun incomeFrom(entity: IncomeEntity): Income {
        return Income(
            id = entity.id,
            amount = entity.amount,
            categoryId = entity.categoryId,
            source = IncomeSource.valueOf(entity.source),
            date = entity.date,
            description = entity.description,
            isRecurring = entity.isRecurring,
            linkedExpenseId = entity.linkedExpenseId,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt
        )
    }

    private fun incomeEntityFrom(income: Income): IncomeEntity {
        return IncomeEntity(
            id = income.id,
            amount = income.amount,
            categoryId = income.categoryId,
            source = income.source.name,
            date = income.date,
            description = income.description,
            isRecurring = income.isRecurring,
            linkedExpenseId = income.linkedExpenseId,
            createdAt = income.createdAt,
            modifiedAt = income.modifiedAt
        )
    }
}
