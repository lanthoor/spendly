package dev.lanthoor.spendly.data.repository

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.lanthoor.spendly.data.local.dao.ExpenseDao
import dev.lanthoor.spendly.data.local.dao.ReceiptDao
import dev.lanthoor.spendly.data.local.entities.ExpenseEntity
import dev.lanthoor.spendly.data.local.entities.ReceiptEntity
import dev.lanthoor.spendly.domain.model.Expense
import dev.lanthoor.spendly.domain.model.Receipt
import dev.lanthoor.spendly.domain.repository.ExpenseRepository
import dev.lanthoor.spendly.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val receiptDao: ReceiptDao,
    @ApplicationContext private val context: Context
) : ExpenseRepository {

    override suspend fun insertExpense(expense: Expense): Long {
        return expenseDao.insert(expenseEntityFrom(expense))
    }

    override suspend fun updateExpense(expense: Expense) {
        expenseDao.update(expenseEntityFrom(expense))
    }

    override suspend fun deleteExpense(expense: Expense) = withContext(Dispatchers.IO) {
        try {
            val receipts = receiptDao.getReceiptsByExpense(expense.id).firstOrNull() ?: emptyList()
            receipts.forEach { receiptEntity ->
                try {
                    FileUtils.deleteReceiptFile(context, receiptEntity.filePath)
                } catch (e: Exception) {
                    Log.w(
                        "ExpenseRepository",
                        "Failed to delete receipt file: ${receiptEntity.filePath}",
                        e
                    )
                }
            }

            expenseDao.delete(expenseEntityFrom(expense))
        } catch (e: Exception) {
            Log.e("ExpenseRepository", "Failed to delete expense: ${expense.id}", e)
            throw e
        }
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

    override fun getExpensesByAccount(accountId: Long): Flow<List<Expense>> {
        return expenseDao.getExpensesByAccount(accountId).map { entities ->
            entities.map { expenseFrom(it) }
        }
    }

    override fun getTotalSpentInRange(startDate: Long, endDate: Long): Flow<Long> {
        return expenseDao.getTotalExpensesByDateRange(startDate, endDate).map { it ?: 0L }
    }

    override fun getTotalSpentByCategory(
        categoryId: Long,
        startDate: Long,
        endDate: Long
    ): Flow<Long> {
        return expenseDao.getTotalExpensesByCategory(categoryId, startDate, endDate)
            .map { it ?: 0L }
    }

    override fun getCategorySpendingBreakdown(
        startDate: Long,
        endDate: Long
    ): Flow<Map<Long, Long>> {
        return expenseDao.getExpensesByCategoryGrouped(startDate, endDate).map { summaries ->
            summaries.associate { (it.categoryId ?: 0L) to it.total }
        }
    }

    override fun getRecentExpenses(limit: Int): Flow<List<Expense>> {
        return expenseDao.getRecentExpenses(limit).map { entities ->
            entities.map { expenseFrom(it) }
        }
    }

    override suspend fun getSmsLinkedExpensesSince(minSmsTimestamp: Long): List<Expense> {
        return expenseDao.getSmsLinkedSnapshotSince(minSmsTimestamp)
            .map { expenseFrom(it) }
    }

    private fun expenseFrom(entity: ExpenseEntity, receipts: List<Receipt> = emptyList()): Expense {
        return Expense(
            id = entity.id,
            amount = entity.amount,
            categoryId = entity.categoryId,
            date = entity.date,
            description = entity.description,
            accountId = entity.accountId,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt,
            receipts = receipts,
            smsSourceId = entity.smsSourceId,
            smsBody = entity.smsBody,
            smsConfidence = entity.smsConfidence,
            smsTimestamp = entity.smsTimestamp
        )
    }

    private fun expenseEntityFrom(expense: Expense): ExpenseEntity {
        return ExpenseEntity(
            id = expense.id,
            amount = expense.amount,
            categoryId = expense.categoryId,
            date = expense.date,
            description = expense.description,
            accountId = expense.accountId,
            createdAt = expense.createdAt,
            modifiedAt = expense.modifiedAt,
            smsSourceId = expense.smsSourceId,
            smsBody = expense.smsBody,
            smsConfidence = expense.smsConfidence,
            smsTimestamp = expense.smsTimestamp
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
