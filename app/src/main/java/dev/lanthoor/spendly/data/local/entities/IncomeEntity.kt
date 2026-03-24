package dev.lanthoor.spendly.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Income entity representing a single income transaction.
 *
 * Tracks income from various sources including salary, freelance work,
 * investments, and refunds. Can be linked to an expense for refund tracking.
 *
 * @property id Unique identifier (auto-generated)
 * @property amount Amount in paise (₹1.00 = 100 paise)
 * @property categoryId Foreign key to CategoryEntity (nullable for backwards compatibility)
 * @property source Income source (Salary/Freelance/Investments/Refund/Other) - DEPRECATED, use categoryId
 * @property date Transaction date (Unix timestamp in milliseconds)
 * @property description Income description
 * @property accountId Foreign key to AccountEntity (required)
 * @property isRecurring Whether this income is from a recurring setup
 * @property linkedExpenseId Foreign key to ExpenseEntity (for refunds)
 * @property createdAt Record creation timestamp
 * @property modifiedAt Last modification timestamp
 */
@Entity(
    tableName = "income",
    indices = [
        Index(value = ["date"]),
        Index(value = ["category_id"]),
        Index(value = ["account_id"]),
        Index(value = ["source"]),
        Index(value = ["linked_expense_id"]),
        Index(value = ["created_at"]),
        Index(value = ["sms_timestamp"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL // Keep income, set category to null
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["account_id"],
            onDelete = ForeignKey.RESTRICT // Prevent account deletion with income
        ),
        ForeignKey(
            entity = ExpenseEntity::class,
            parentColumns = ["id"],
            childColumns = ["linked_expense_id"],
            onDelete = ForeignKey.SET_NULL // Keep income, unlink from expense
        )
    ]
)
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "amount")
    val amount: Long, // Paise

    @ColumnInfo(name = "category_id")
    val categoryId: Long?, // Foreign key to income category (nullable for backwards compatibility)

    @ColumnInfo(name = "source")
    val source: String, // Salary/Freelance/Investments/Refund/Other - DEPRECATED

    @ColumnInfo(name = "date")
    val date: Long,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "account_id")
    val accountId: Long, // Foreign key to AccountEntity (defaults to 1 = "My Account")

    @ColumnInfo(name = "is_recurring")
    val isRecurring: Boolean = false,

    @ColumnInfo(name = "linked_expense_id")
    val linkedExpenseId: Long?, // For refunds - links back to original expense

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "modified_at")
    val modifiedAt: Long,

    @ColumnInfo(name = "sms_source_id")
    val smsSourceId: Long? = null, // Link to SMS that created this income (for audit trail)

    @ColumnInfo(name = "sms_body")
    val smsBody: String? = null, // Original SMS text for reference

    @ColumnInfo(name = "sms_confidence")
    val smsConfidence: Float? = null, // Parsing confidence score (0.0-1.0)

    @ColumnInfo(name = "sms_timestamp")
    val smsTimestamp: Long? = null // When SMS was received (Unix timestamp)
)
