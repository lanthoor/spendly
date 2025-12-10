package `in`.mylullaby.spendly.data.local.entities

import androidx.room.*

/**
 * Junction table entity for many-to-many relationship between transactions and tags.
 *
 * Associates tags with both expense and income transactions.
 * Uses a composite primary key to prevent duplicate tag assignments.
 *
 * @property transactionId ID of the transaction (expense or income)
 * @property tagId Foreign key to TagEntity
 * @property transactionType Discriminator ("EXPENSE" or "INCOME")
 */
@Entity(
    tableName = "transaction_tags",
    primaryKeys = ["transaction_id", "tag_id", "transaction_type"],
    indices = [
        Index(value = ["tag_id"]),
        Index(value = ["transaction_id", "transaction_type"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tag_id"],
            onDelete = ForeignKey.CASCADE
        )
        // Note: Cannot add FK to transaction_id because it points to multiple tables (expenses/income)
    ]
)
data class TransactionTagEntity(
    @ColumnInfo(name = "transaction_id")
    val transactionId: Long,

    @ColumnInfo(name = "tag_id")
    val tagId: Long,

    @ColumnInfo(name = "transaction_type")
    val transactionType: String // "EXPENSE" or "INCOME"
)
