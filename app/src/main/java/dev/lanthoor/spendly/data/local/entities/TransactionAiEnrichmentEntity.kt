package dev.lanthoor.spendly.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transaction_ai_enrichment",
    indices = [
        Index(value = ["transaction_type", "transaction_id"], unique = true),
        Index(value = ["status"]),
        Index(value = ["modified_at"])
    ]
)
data class TransactionAiEnrichmentEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "transaction_type")
    val transactionType: String,
    @ColumnInfo(name = "transaction_id")
    val transactionId: Long,
    @ColumnInfo(name = "status")
    val status: String,
    @ColumnInfo(name = "display_description")
    val displayDescription: String? = null,
    @ColumnInfo(name = "counterparty_name")
    val counterpartyName: String? = null,
    @ColumnInfo(name = "counterparty_role")
    val counterpartyRole: String,
    @ColumnInfo(name = "counterparty_type")
    val counterpartyType: String,
    @ColumnInfo(name = "identifier_type")
    val identifierType: String,
    @ColumnInfo(name = "identifier_value")
    val identifierValue: String? = null,
    @ColumnInfo(name = "payment_rail")
    val paymentRail: String,
    @ColumnInfo(name = "confidence")
    val confidence: Float? = null,
    @ColumnInfo(name = "reason")
    val reason: String? = null,
    @ColumnInfo(name = "model_name")
    val modelName: String? = null,
    @ColumnInfo(name = "prompt_version")
    val promptVersion: Int,
    @ColumnInfo(name = "enriched_at")
    val enrichedAt: Long? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "modified_at")
    val modifiedAt: Long
)
