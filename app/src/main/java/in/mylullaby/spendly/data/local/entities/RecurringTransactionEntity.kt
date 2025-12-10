package `in`.mylullaby.spendly.data.local.entities

import androidx.room.*

/**
 * Recurring transaction entity for automatic transaction creation.
 *
 * Stores configuration for transactions that repeat on a schedule
 * (daily, weekly, or monthly). App processes these at startup to
 * create actual expense/income records.
 *
 * @property id Unique identifier (auto-generated)
 * @property transactionType Type of transaction ("EXPENSE" or "INCOME")
 * @property amount Amount in paise
 * @property categoryId Foreign key to CategoryEntity
 * @property description Transaction description
 * @property frequency Recurrence frequency ("DAILY"/"WEEKLY"/"MONTHLY")
 * @property nextDate When to process next (Unix timestamp)
 * @property lastProcessed When last transaction was created (Unix timestamp)
 * @property createdAt Record creation timestamp
 * @property modifiedAt Last modification timestamp
 */
@Entity(
    tableName = "recurring_transactions",
    indices = [
        Index(value = ["transaction_type"]),
        Index(value = ["category_id"]),
        Index(value = ["next_date"]),
        Index(value = ["frequency"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "transaction_type")
    val transactionType: String, // "EXPENSE" or "INCOME"

    @ColumnInfo(name = "amount")
    val amount: Long,

    @ColumnInfo(name = "category_id")
    val categoryId: Long,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "frequency")
    val frequency: String, // "DAILY", "WEEKLY", "MONTHLY"

    @ColumnInfo(name = "next_date")
    val nextDate: Long,

    @ColumnInfo(name = "last_processed")
    val lastProcessed: Long?,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "modified_at")
    val modifiedAt: Long
)
