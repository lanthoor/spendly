package `in`.mylullaby.spendly.data.repository

import `in`.mylullaby.spendly.data.local.dao.BudgetDao
import `in`.mylullaby.spendly.data.local.entities.BudgetEntity
import `in`.mylullaby.spendly.domain.model.Budget
import `in`.mylullaby.spendly.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of BudgetRepository.
 * Handles entity-to-model mapping and notification flag tracking.
 */
@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao
) : BudgetRepository {

    // CRUD operations

    override suspend fun insertBudget(budget: Budget): Long {
        return budgetDao.insert(budget.toEntity())
    }

    override suspend fun updateBudget(budget: Budget) {
        budgetDao.update(budget.toEntity())
    }

    override suspend fun deleteBudget(budget: Budget) {
        budgetDao.delete(budget.toEntity())
    }

    override fun getBudgetById(id: Long): Flow<Budget?> {
        return budgetDao.getBudgetById(id).map { it?.toDomainModel() }
    }

    override fun getAllBudgets(): Flow<List<Budget>> {
        return budgetDao.getAllBudgets().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    // Month-based queries

    override fun getBudgetsForMonth(month: Int, year: Int): Flow<List<Budget>> {
        return budgetDao.getBudgetsByMonth(month, year).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getOverallBudget(month: Int, year: Int): Flow<Budget?> {
        return budgetDao.getOverallBudget(month, year).map { it?.toDomainModel() }
    }

    override fun getCategoryBudget(categoryId: Long, month: Int, year: Int): Flow<Budget?> {
        return budgetDao.getBudgetByCategoryAndMonth(categoryId, month, year).map { it?.toDomainModel() }
    }

    // Notification tracking

    override suspend fun markNotification75Sent(budgetId: Long) {
        budgetDao.updateNotification75Sent(budgetId, true)
    }

    override suspend fun markNotification100Sent(budgetId: Long) {
        budgetDao.updateNotification100Sent(budgetId, true)
    }

    // Budget vs actual calculation

    override suspend fun getBudgetProgress(budgetId: Long, currentSpent: Long): Float {
        val budget = budgetDao.getBudgetById(budgetId).first()
        return budget?.let {
            it.toDomainModel().calculateProgress(currentSpent)
        } ?: 0f
    }

    // Entity to Domain Model mapping

    private fun BudgetEntity.toDomainModel(): Budget {
        return Budget(
            id = id,
            categoryId = categoryId,
            amount = amount,
            month = month,
            year = year,
            notification75Sent = notification75Sent,
            notification100Sent = notification100Sent
        )
    }

    private fun Budget.toEntity(): BudgetEntity {
        return BudgetEntity(
            id = id,
            categoryId = categoryId,
            amount = amount,
            month = month,
            year = year,
            notification75Sent = notification75Sent,
            notification100Sent = notification100Sent,
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis()
        )
    }
}
