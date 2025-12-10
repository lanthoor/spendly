package `in`.mylullaby.spendly.domain.repository

import `in`.mylullaby.spendly.domain.model.Tag
import `in`.mylullaby.spendly.utils.TransactionType
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for tag operations.
 * Handles tag management and tag-to-transaction associations.
 */
interface TagRepository {

    // Tag CRUD

    /**
     * Inserts a new tag into the database.
     * @param tag The tag to insert
     * @return The ID of the inserted tag
     */
    suspend fun insertTag(tag: Tag): Long

    /**
     * Updates an existing tag in the database.
     * @param tag The tag to update
     */
    suspend fun updateTag(tag: Tag)

    /**
     * Deletes a tag from the database.
     * @param tag The tag to delete
     */
    suspend fun deleteTag(tag: Tag)

    /**
     * Retrieves a tag by its ID.
     * @param id The tag ID
     * @return Flow emitting the tag or null if not found
     */
    fun getTagById(id: Long): Flow<Tag?>

    /**
     * Retrieves all tags.
     * @return Flow emitting list of all tags
     */
    fun getAllTags(): Flow<List<Tag>>

    // Transaction-Tag associations

    /**
     * Associates a tag with a transaction.
     * @param transactionType The transaction type (EXPENSE or INCOME)
     * @param transactionId The transaction ID
     * @param tagId The tag ID
     */
    suspend fun addTagToTransaction(transactionType: TransactionType, transactionId: Long, tagId: Long)

    /**
     * Removes a tag association from a transaction.
     * @param transactionType The transaction type (EXPENSE or INCOME)
     * @param transactionId The transaction ID
     * @param tagId The tag ID
     */
    suspend fun removeTagFromTransaction(transactionType: TransactionType, transactionId: Long, tagId: Long)

    /**
     * Retrieves all tags associated with a transaction.
     * @param transactionType The transaction type (EXPENSE or INCOME)
     * @param transactionId The transaction ID
     * @return Flow emitting list of tags for the transaction
     */
    fun getTagsForTransaction(transactionType: TransactionType, transactionId: Long): Flow<List<Tag>>

    /**
     * Retrieves all transaction IDs that have a specific tag.
     * @param tagId The tag ID
     * @return Flow emitting list of transaction IDs
     */
    fun getTransactionsByTag(tagId: Long): Flow<List<Long>>
}
