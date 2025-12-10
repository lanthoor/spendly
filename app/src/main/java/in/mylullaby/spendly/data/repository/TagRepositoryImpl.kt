package `in`.mylullaby.spendly.data.repository

import `in`.mylullaby.spendly.data.local.dao.TagDao
import `in`.mylullaby.spendly.data.local.dao.TransactionTagDao
import `in`.mylullaby.spendly.data.local.entities.TagEntity
import `in`.mylullaby.spendly.data.local.entities.TransactionTagEntity
import `in`.mylullaby.spendly.domain.model.Tag
import `in`.mylullaby.spendly.domain.repository.TagRepository
import `in`.mylullaby.spendly.utils.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of TagRepository.
 * Handles tag management and many-to-many transaction-tag associations.
 */
@Singleton
class TagRepositoryImpl @Inject constructor(
    private val tagDao: TagDao,
    private val transactionTagDao: TransactionTagDao
) : TagRepository {

    // Tag CRUD

    override suspend fun insertTag(tag: Tag): Long {
        return tagDao.insert(tagEntityFrom(tag))
    }

    override suspend fun updateTag(tag: Tag) {
        tagDao.update(tagEntityFrom(tag))
    }

    override suspend fun deleteTag(tag: Tag) {
        tagDao.delete(tagEntityFrom(tag))
    }

    override fun getTagById(id: Long): Flow<Tag?> {
        return tagDao.getTagById(id).map { it?.let { entity -> tagFrom(entity) } }
    }

    override fun getAllTags(): Flow<List<Tag>> {
        return tagDao.getAllTags().map { entities ->
            entities.map { tagFrom(it) }
        }
    }

    // Transaction-Tag associations

    override suspend fun addTagToTransaction(
        transactionType: TransactionType,
        transactionId: Long,
        tagId: Long
    ) {
        val entity = TransactionTagEntity(
            transactionType = transactionType.name,
            transactionId = transactionId,
            tagId = tagId
        )
        transactionTagDao.insert(entity)
    }

    override suspend fun removeTagFromTransaction(
        transactionType: TransactionType,
        transactionId: Long,
        tagId: Long
    ) {
        val entity = TransactionTagEntity(
            transactionType = transactionType.name,
            transactionId = transactionId,
            tagId = tagId
        )
        transactionTagDao.delete(entity)
    }

    override fun getTagsForTransaction(
        transactionType: TransactionType,
        transactionId: Long
    ): Flow<List<Tag>> {
        return transactionTagDao.getTagsForTransaction(transactionId, transactionType.name).map { entities ->
            entities.map { tagFrom(it) }
        }
    }

    override fun getTransactionsByTag(tagId: Long): Flow<List<Long>> {
        return transactionTagDao.getTransactionsForTag(tagId).map { references ->
            references.map { it.transactionId }
        }
    }

    // Entity to Domain Model mapping

    private fun tagFrom(entity: TagEntity): Tag {
        return Tag(
            id = entity.id,
            name = entity.name
        )
    }

    private fun tagEntityFrom(tag: Tag): TagEntity {
        return TagEntity(
            id = tag.id,
            name = tag.name,
            color = 0xFF000000.toInt(), // Default black color
            createdAt = System.currentTimeMillis()
        )
    }
}
