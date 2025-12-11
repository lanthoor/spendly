package `in`.mylullaby.spendly.data.repository

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.mylullaby.spendly.data.local.dao.ReceiptDao
import `in`.mylullaby.spendly.data.local.entities.ReceiptEntity
import `in`.mylullaby.spendly.domain.model.Receipt
import `in`.mylullaby.spendly.domain.repository.ReceiptRepository
import `in`.mylullaby.spendly.utils.FileUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
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
        try {
            // Delete from database FIRST (can be rolled back if transaction fails)
            val entity = receipt.toEntity()
            receiptDao.delete(entity)

            // Then delete physical file (best effort)
            if (!FileUtils.deleteReceiptFile(context, receipt.filePath)) {
                Log.w("ReceiptRepository", "Receipt deleted from DB but file removal failed: ${receipt.filePath}")
                // File deletion failed, but database record is already gone
                // This is acceptable - orphaned files can be cleaned up later
            }
        } catch (e: Exception) {
            Log.e("ReceiptRepository", "Failed to delete receipt: ${receipt.id}", e)
            throw e // Re-throw to let caller handle
        }
    }

    override suspend fun deleteReceiptById(receiptId: Long) {
        try {
            // Use firstOrNull() instead of collect to get single emission
            // This prevents race condition from multiple Flow emissions
            val entity = receiptDao.getReceiptById(receiptId).firstOrNull()

            entity?.let {
                // Delete database record FIRST (atomic operation)
                receiptDao.deleteById(receiptId)

                // Then delete physical file (best effort)
                if (!FileUtils.deleteReceiptFile(context, it.filePath)) {
                    Log.w("ReceiptRepository", "Receipt deleted from DB but file removal failed: ${it.filePath}")
                    // File deletion failed, but database record is already gone
                    // Consider scheduling cleanup job for orphaned files
                }
            }
        } catch (e: Exception) {
            Log.e("ReceiptRepository", "Failed to delete receipt by ID: $receiptId", e)
            throw e // Re-throw to let caller handle
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
