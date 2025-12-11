package `in`.mylullaby.spendly.data.local.entities

import androidx.room.*

/**
 * Expense entity representing a single expense transaction.
 *
 * Stores all expense-related data including amount (in paise), category,
 * account, and timestamps for auditing.
 *
 * @property id Unique identifier (auto-generated)
 * @property amount Amount in paise (₹1.00 = 100 paise)
 * @property categoryId Foreign key to CategoryEntity (nullable)
 * @property date Transaction date (Unix timestamp in milliseconds)
 * @property description Expense description
 * @property accountId Foreign key to AccountEntity (required)
 * @property createdAt Record creation timestamp
 * @property modifiedAt Last modification timestamp
 */
@Entity(
    tableName = "expenses",
    indices = [
        Index(value = ["category_id"]),
        Index(value = ["date"]),
        Index(value = ["account_id"]),
        Index(value = ["created_at"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL // Expense remains, category becomes null
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["account_id"],
            onDelete = ForeignKey.RESTRICT // Prevent account deletion with expenses
        )
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "amount")
    val amount: Long, // Paise (₹1 = 100 paise)

    @ColumnInfo(name = "category_id")
    val categoryId: Long?, // Nullable - defaults to Uncategorized

    @ColumnInfo(name = "date")
    val date: Long, // Unix timestamp in milliseconds

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "account_id")
    val accountId: Long, // Foreign key to AccountEntity (defaults to 1 = "My Account")

    @ColumnInfo(name = "created_at")
    val createdAt: Long, // Unix timestamp in milliseconds

    @ColumnInfo(name = "modified_at")
    val modifiedAt: Long // Unix timestamp in milliseconds
)
