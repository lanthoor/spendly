package `in`.mylullaby.spendly.data.repository

import `in`.mylullaby.spendly.data.local.dao.ExpenseDao
import `in`.mylullaby.spendly.data.local.dao.ReceiptDao
import `in`.mylullaby.spendly.data.local.entities.ExpenseEntity
import `in`.mylullaby.spendly.data.local.entities.ReceiptEntity
import `in`.mylullaby.spendly.domain.model.Expense
import `in`.mylullaby.spendly.domain.model.Receipt
import `in`.mylullaby.spendly.domain.repository.ExpenseRepository
import `in`.mylullaby.spendly.utils.PaymentMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ExpenseRepository.
 * Handles entity-to-model mapping and delegates database operations to DAOs.
 */
@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val receiptDao: ReceiptDao
) : ExpenseRepository {

    // CRUD operations

    override suspend fun insertExpense(expense: Expense): Long {
        return expenseDao.insert(expenseEntityFrom(expense))
    }

    override suspend fun updateExpense(expense: Expense) {
        expenseDao.update(expenseEntityFrom(expense))
    }

    override suspend fun deleteExpense(expense: Expense) {
        expenseDao.delete(expenseEntityFrom(expense))
    }

    override fun getExpenseById(id: Long): Flow<Expense?> {
        return combine(
            expenseDao.getExpenseById(id),
            receiptDao.getReceiptsByExpense(id)
        ) { entity, receipts ->
            entity?.let { expenseFrom(it, receipts.map { r -> receiptFrom(r) }) }
        }
    }

    override fun getAllExpenses(): Flow<List<Expense>> {
        return expenseDao.getAllExpenses().map { entities ->
            entities.map { expenseFrom(it) }
        }
    }

    // Queries with default sort: date DESC (newest first)

    override fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>> {
        return expenseDao.getExpensesByDateRange(startDate, endDate).map { entities ->
            entities.map { expenseFrom(it) }
        }
    }

    override fun getExpensesByCategory(categoryId: Long): Flow<List<Expense>> {
        return expenseDao.getExpensesByCategory(categoryId).map { entities ->
            entities.map { expenseFrom(it) }
        }
    }

    override fun getExpensesByPaymentMethod(method: PaymentMethod): Flow<List<Expense>> {
        return expenseDao.getExpensesByPaymentMethod(method.name).map { entities ->
            entities.map { expenseFrom(it) }
        }
    }

    override fun getExpensesByTags(tagIds: List<Long>): Flow<List<Expense>> {
        // For now, return empty list - will be implemented with proper junction table queries
        return expenseDao.getAllExpenses().map { emptyList() }
    }

    // Aggregations (returns amounts in paise)

    override fun getTotalSpentInRange(startDate: Long, endDate: Long): Flow<Long> {
        return expenseDao.getTotalExpensesByDateRange(startDate, endDate).map { it ?: 0L }
    }

    override fun getTotalSpentByCategory(
        categoryId: Long,
        startDate: Long,
        endDate: Long
    ): Flow<Long> {
        return expenseDao.getTotalExpensesByCategory(categoryId, startDate, endDate).map { it ?: 0L }
    }

    override fun getCategorySpendingBreakdown(
        startDate: Long,
        endDate: Long
    ): Flow<Map<Long, Long>> {
        return expenseDao.getExpensesByCategoryGrouped(startDate, endDate).map { summaries ->
            summaries.associate { (it.categoryId ?: 0L) to it.total }
        }
    }

    // Recent transactions

    override fun getRecentExpenses(limit: Int): Flow<List<Expense>> {
        return expenseDao.getRecentExpenses(limit).map { entities ->
            entities.map { expenseFrom(it) }
        }
    }

    // Entity to Domain Model mapping

    private fun expenseFrom(entity: ExpenseEntity, receipts: List<Receipt> = emptyList()): Expense {
        return Expense(
            id = entity.id,
            amount = entity.amount,
            categoryId = entity.categoryId,
            date = entity.date,
            description = entity.description,
            paymentMethod = PaymentMethod.valueOf(entity.paymentMethod),
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt,
            receipts = receipts
        )
    }

    private fun expenseEntityFrom(expense: Expense): ExpenseEntity {
        return ExpenseEntity(
            id = expense.id,
            amount = expense.amount,
            categoryId = expense.categoryId,
            date = expense.date,
            description = expense.description,
            paymentMethod = expense.paymentMethod.name,
            createdAt = expense.createdAt,
            modifiedAt = expense.modifiedAt
        )
    }

    private fun receiptFrom(entity: ReceiptEntity): Receipt {
        return Receipt(
            id = entity.id,
            expenseId = entity.expenseId,
            filePath = entity.filePath,
            fileType = entity.fileType,
            fileSizeBytes = entity.fileSizeBytes,
            compressed = entity.compressed
        )
    }
}
