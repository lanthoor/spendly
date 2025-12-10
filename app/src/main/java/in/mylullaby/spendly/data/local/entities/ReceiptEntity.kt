package `in`.mylullaby.spendly.data.local.entities

import androidx.room.*

/**
 * Receipt entity representing an attached receipt file for an expense.
 *
 * Stores metadata about receipt files stored in internal storage.
 * Multiple receipts can be attached to a single expense.
 *
 * @property id Unique identifier (auto-generated)
 * @property expenseId Foreign key to ExpenseEntity
 * @property filePath Relative path in internal storage (e.g., "receipts/2024/12/expense_123_receipt_1.jpg")
 * @property fileType File type (JPG/PNG/WebP/PDF)
 * @property fileSizeBytes File size in bytes (max 5MB = 5242880)
 * @property compressed Whether the file has been compressed
 * @property createdAt Upload timestamp
 */
@Entity(
    tableName = "receipts",
    indices = [
        Index(value = ["expense_id"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = ExpenseEntity::class,
            parentColumns = ["id"],
            childColumns = ["expense_id"],
            onDelete = ForeignKey.CASCADE // Delete receipts when expense deleted
        )
    ]
)
data class ReceiptEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "expense_id")
    val expenseId: Long,

    @ColumnInfo(name = "file_path")
    val filePath: String, // Relative path: "receipts/2024/12/expense_123_receipt_1.jpg"

    @ColumnInfo(name = "file_type")
    val fileType: String, // JPG, PNG, WebP, PDF

    @ColumnInfo(name = "file_size_bytes")
    val fileSizeBytes: Long, // Max 5MB (5 * 1024 * 1024 = 5242880)

    @ColumnInfo(name = "compressed")
    val compressed: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long // Unix timestamp in milliseconds
)
