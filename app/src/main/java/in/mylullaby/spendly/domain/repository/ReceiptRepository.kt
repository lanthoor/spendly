package `in`.mylullaby.spendly.domain.repository

import `in`.mylullaby.spendly.domain.model.Receipt
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for receipt operations.
 * Provides CRUD operations and queries for receipt data.
 */
interface ReceiptRepository {

    /**
     * Inserts a new receipt into the database.
     * @param receipt The receipt to insert
     * @return The ID of the inserted receipt
     */
    suspend fun insertReceipt(receipt: Receipt): Long

    /**
     * Deletes a receipt from the database.
     * @param receipt The receipt to delete
     */
    suspend fun deleteReceipt(receipt: Receipt)

    /**
     * Deletes a receipt by its ID.
     * @param receiptId The receipt ID
     */
    suspend fun deleteReceiptById(receiptId: Long)

    /**
     * Retrieves all receipts for a specific expense.
     * @param expenseId The expense ID
     * @return Flow emitting list of receipts
     */
    fun getReceiptsByExpense(expenseId: Long): Flow<List<Receipt>>

    /**
     * Retrieves a receipt by its ID.
     * @param receiptId The receipt ID
     * @return Flow emitting the receipt or null if not found
     */
    fun getReceiptById(receiptId: Long): Flow<Receipt?>

    /**
     * Gets the count of receipts for an expense.
     * @param expenseId The expense ID
     * @return Flow emitting the count
     */
    fun getReceiptCount(expenseId: Long): Flow<Int>

    /**
     * Calculates total file size of all receipts for an expense.
     * @param expenseId The expense ID
     * @return Total size in bytes
     */
    suspend fun getTotalSizeByExpense(expenseId: Long): Long
}
