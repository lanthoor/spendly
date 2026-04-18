package dev.lanthoor.spendly.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dev.lanthoor.spendly.data.local.entities.TransactionAiEnrichmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionAiEnrichmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TransactionAiEnrichmentEntity): Long

    @Update
    suspend fun update(entity: TransactionAiEnrichmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<TransactionAiEnrichmentEntity>)

    @Query(
        """
        SELECT * FROM transaction_ai_enrichment
        WHERE transaction_type = :transactionType
          AND transaction_id = :transactionId
        LIMIT 1
        """
    )
    suspend fun getByTransaction(
        transactionType: String,
        transactionId: Long
    ): TransactionAiEnrichmentEntity?

    @Query(
        """
        SELECT * FROM transaction_ai_enrichment
        WHERE transaction_type = :transactionType
          AND transaction_id = :transactionId
        LIMIT 1
        """
    )
    fun observeByTransaction(
        transactionType: String,
        transactionId: Long
    ): Flow<TransactionAiEnrichmentEntity?>

    @Query("SELECT * FROM transaction_ai_enrichment ORDER BY modified_at DESC")
    fun observeAll(): Flow<List<TransactionAiEnrichmentEntity>>

    @Query(
        """
        SELECT * FROM transaction_ai_enrichment
        WHERE status IN (:statuses)
        ORDER BY modified_at ASC
        LIMIT :limit
        """
    )
    suspend fun getByStatuses(
        statuses: List<String>,
        limit: Int
    ): List<TransactionAiEnrichmentEntity>

    @Query(
        """
        SELECT * FROM transaction_ai_enrichment
        WHERE status IN (:statuses)
          AND transaction_type = :transactionType
          AND transaction_id IN (:transactionIds)
        ORDER BY modified_at ASC
        LIMIT :limit
        """
    )
    suspend fun getByStatusesForTransactions(
        statuses: List<String>,
        transactionType: String,
        transactionIds: List<Long>,
        limit: Int
    ): List<TransactionAiEnrichmentEntity>

    @Query(
        """
        SELECT * FROM transaction_ai_enrichment
        WHERE status IN (:statuses)
          AND (
            (transaction_type = 'EXPENSE' AND transaction_id IN (:expenseIds))
            OR
            (transaction_type = 'INCOME' AND transaction_id IN (:incomeIds))
          )
        ORDER BY modified_at ASC
        LIMIT :limit
        """
    )
    suspend fun getByStatusesForMixedTransactions(
        statuses: List<String>,
        expenseIds: List<Long>,
        incomeIds: List<Long>,
        limit: Int
    ): List<TransactionAiEnrichmentEntity>

    @Query("SELECT COUNT(*) FROM transaction_ai_enrichment WHERE status = :status")
    suspend fun countByStatus(status: String): Int

    @Query("SELECT * FROM transaction_ai_enrichment ORDER BY modified_at DESC")
    suspend fun getAllSnapshot(): List<TransactionAiEnrichmentEntity>

    @Query("DELETE FROM transaction_ai_enrichment")
    suspend fun deleteAll()
}
