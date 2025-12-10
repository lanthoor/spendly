package `in`.mylullaby.spendly.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import `in`.mylullaby.spendly.data.local.entities.ReceiptEntity

/**
 * Data Access Object for Receipt operations.
 *
 * Provides methods for managing receipt attachments for expenses.
 */
@Dao
interface ReceiptDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(receipt: ReceiptEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(receipts: List<ReceiptEntity>)

    @Update
    suspend fun update(receipt: ReceiptEntity)

    @Delete
    suspend fun delete(receipt: ReceiptEntity)

    /**
     * Delete a receipt by ID.
     */
    @Query("DELETE FROM receipts WHERE id = :receiptId")
    suspend fun deleteById(receiptId: Long)

    /**
     * Get all receipts for an expense, ordered by upload time.
     */
    @Query("SELECT * FROM receipts WHERE expense_id = :expenseId ORDER BY created_at ASC")
    fun getReceiptsByExpense(expenseId: Long): Flow<List<ReceiptEntity>>

    /**
     * Get a receipt by ID.
     */
    @Query("SELECT * FROM receipts WHERE id = :receiptId")
    fun getReceiptById(receiptId: Long): Flow<ReceiptEntity?>

    /**
     * Get count of receipts for an expense.
     * Used for UI display (e.g., "3 receipts").
     */
    @Query("SELECT COUNT(*) FROM receipts WHERE expense_id = :expenseId")
    fun getReceiptCount(expenseId: Long): Flow<Int>

    /**
     * Get total file size of all receipts for an expense.
     * Used for storage management.
     */
    @Query("SELECT SUM(file_size_bytes) FROM receipts WHERE expense_id = :expenseId")
    suspend fun getTotalSizeByExpense(expenseId: Long): Long?
}
