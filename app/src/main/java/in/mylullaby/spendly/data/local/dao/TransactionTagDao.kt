package `in`.mylullaby.spendly.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import `in`.mylullaby.spendly.data.local.entities.TagEntity
import `in`.mylullaby.spendly.data.local.entities.TransactionTagEntity

/**
 * Data Access Object for TransactionTag operations.
 *
 * Provides methods for managing many-to-many relationships between
 * transactions (expenses/income) and tags.
 */
@Dao
interface TransactionTagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transactionTag: TransactionTagEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactionTags: List<TransactionTagEntity>)

    @Delete
    suspend fun delete(transactionTag: TransactionTagEntity)

    /**
     * Delete all tags for a specific transaction.
     * Used when reassigning tags.
     */
    @Query("DELETE FROM transaction_tags WHERE transaction_id = :transactionId AND transaction_type = :type")
    suspend fun deleteAllTagsForTransaction(transactionId: Long, type: String)

    /**
     * Get all tags for a specific transaction.
     * Uses JOIN to return full tag entities.
     */
    @Query("""
        SELECT tags.*
        FROM tags
        INNER JOIN transaction_tags ON tags.id = transaction_tags.tag_id
        WHERE transaction_tags.transaction_id = :transactionId
        AND transaction_tags.transaction_type = :type
        ORDER BY tags.name ASC
    """)
    fun getTagsForTransaction(transactionId: Long, type: String): Flow<List<TagEntity>>

    /**
     * Get all transactions (IDs and types) that have a specific tag.
     * Used for filtering transactions by tag.
     */
    @Query("SELECT transaction_id, transaction_type FROM transaction_tags WHERE tag_id = :tagId")
    fun getTransactionsForTag(tagId: Long): Flow<List<TransactionReference>>

    /**
     * Get count of transactions using a specific tag.
     * Used before tag deletion to warn user.
     */
    @Query("SELECT COUNT(*) FROM transaction_tags WHERE tag_id = :tagId")
    suspend fun getTransactionCountForTag(tagId: Long): Int
}

/**
 * Data class representing a reference to a transaction.
 * Used to identify transactions with specific tags.
 */
data class TransactionReference(
    @ColumnInfo(name = "transaction_id")
    val transactionId: Long,

    @ColumnInfo(name = "transaction_type")
    val transactionType: String
)
