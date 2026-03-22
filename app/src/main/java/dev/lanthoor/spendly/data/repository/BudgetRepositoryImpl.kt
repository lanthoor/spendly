package dev.lanthoor.spendly.data.repository

import dev.lanthoor.spendly.data.local.dao.BudgetDao
import dev.lanthoor.spendly.data.local.entities.BudgetEntity
import dev.lanthoor.spendly.domain.model.Budget
import dev.lanthoor.spendly.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao
) : BudgetRepository {

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

    override fun getBudgetsForMonth(month: Int, year: Int): Flow<List<Budget>> {
        return budgetDao.getBudgetsByMonth(month, year).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getOverallBudget(month: Int, year: Int): Flow<Budget?> {
        return budgetDao.getOverallBudget(month, year).map { it?.toDomainModel() }
    }

    override fun getCategoryBudget(categoryId: Long, month: Int, year: Int): Flow<Budget?> {
        return budgetDao.getBudgetByCategoryAndMonth(categoryId, month, year)
            .map { it?.toDomainModel() }
    }

    override suspend fun markNotification75Sent(budgetId: Long) {
        budgetDao.updateNotification75Sent(budgetId, true)
    }

    override suspend fun markNotification100Sent(budgetId: Long) {
        budgetDao.updateNotification100Sent(budgetId, true)
    }

    override suspend fun resetMonthlyNotificationFlags() {
        budgetDao.resetAllNotificationFlags()
    }

    override suspend fun getBudgetProgress(budgetId: Long, currentSpent: Long): Float {
        val budget = budgetDao.getBudgetById(budgetId).first()
        return budget?.let {
            it.toDomainModel().calculateProgress(currentSpent)
        } ?: 0f
    }

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
