package `in`.mylullaby.spendly.data.local.entities

import androidx.room.*

/**
 * Budget entity representing a spending budget.
 *
 * Budgets can be set per-category or overall (null category).
 * Tracks notification status for 75% and 100% threshold alerts.
 *
 * @property id Unique identifier (auto-generated)
 * @property categoryId Foreign key to CategoryEntity (null = overall budget)
 * @property amount Budget amount in paise
 * @property month Budget month (1-12)
 * @property year Budget year (e.g., 2024)
 * @property notification75Sent Whether 75% threshold notification was sent
 * @property notification100Sent Whether 100% threshold notification was sent
 * @property createdAt Record creation timestamp
 * @property modifiedAt Last modification timestamp
 */
@Entity(
    tableName = "budgets",
    indices = [
        Index(value = ["category_id"]),
        Index(value = ["month", "year"]),
        Index(value = ["category_id", "month", "year"], unique = true)
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
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "category_id")
    val categoryId: Long?, // null = overall budget

    @ColumnInfo(name = "amount")
    val amount: Long, // Paise

    @ColumnInfo(name = "month")
    val month: Int, // 1-12

    @ColumnInfo(name = "year")
    val year: Int,

    @ColumnInfo(name = "notification_75_sent")
    val notification75Sent: Boolean = false,

    @ColumnInfo(name = "notification_100_sent")
    val notification100Sent: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "modified_at")
    val modifiedAt: Long
)
