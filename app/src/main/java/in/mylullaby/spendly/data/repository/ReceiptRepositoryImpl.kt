package `in`.mylullaby.spendly.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.mylullaby.spendly.data.local.dao.ReceiptDao
import `in`.mylullaby.spendly.data.local.entities.ReceiptEntity
import `in`.mylullaby.spendly.domain.model.Receipt
import `in`.mylullaby.spendly.domain.repository.ReceiptRepository
import `in`.mylullaby.spendly.utils.FileUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ReceiptRepository.
 * Delegates to ReceiptDao and handles entity-to-model mapping.
 * Also manages physical file deletion when receipts are deleted.
 */
@Singleton
class ReceiptRepositoryImpl @Inject constructor(
    private val receiptDao: ReceiptDao,
    @ApplicationContext private val context: Context
) : ReceiptRepository {

    override suspend fun insertReceipt(receipt: Receipt): Long {
        val entity = receipt.toEntity()
        return receiptDao.insert(entity)
    }

    override suspend fun deleteReceipt(receipt: Receipt) {
        // Delete physical file first
        FileUtils.deleteReceiptFile(context, receipt.filePath)

        // Then delete from database
        val entity = receipt.toEntity()
        receiptDao.delete(entity)
    }

    override suspend fun deleteReceiptById(receiptId: Long) {
        // Get receipt to find file path
        receiptDao.getReceiptById(receiptId).collect { entity ->
            entity?.let {
                // Delete physical file
                FileUtils.deleteReceiptFile(context, it.filePath)
                // Delete from database
                receiptDao.deleteById(receiptId)
            }
        }
    }

    override fun getReceiptsByExpense(expenseId: Long): Flow<List<Receipt>> {
        return receiptDao.getReceiptsByExpense(expenseId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getReceiptById(receiptId: Long): Flow<Receipt?> {
        return receiptDao.getReceiptById(receiptId).map { entity ->
            entity?.toDomainModel()
        }
    }

    override fun getReceiptCount(expenseId: Long): Flow<Int> {
        return receiptDao.getReceiptCount(expenseId)
    }

    override suspend fun getTotalSizeByExpense(expenseId: Long): Long {
        return receiptDao.getTotalSizeByExpense(expenseId) ?: 0L
    }

    // Entity <-> Domain model mappers

    private fun Receipt.toEntity(): ReceiptEntity {
        return ReceiptEntity(
            id = id,
            expenseId = expenseId,
            filePath = filePath,
            fileType = fileType,
            fileSizeBytes = fileSizeBytes,
            compressed = compressed,
            createdAt = System.currentTimeMillis()
        )
    }

    private fun ReceiptEntity.toDomainModel(): Receipt {
        return Receipt(
            id = id,
            expenseId = expenseId,
            filePath = filePath,
            fileType = fileType,
            fileSizeBytes = fileSizeBytes,
            compressed = compressed
        )
    }
}
