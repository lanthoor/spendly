package `in`.mylullaby.spendly.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tag entity for categorizing transactions with custom labels.
 *
 * Tags can be applied to both expenses and income transactions,
 * enabling flexible organization and filtering.
 *
 * @property id Unique identifier (auto-generated)
 * @property name Tag name (unique)
 * @property color ARGB color int for UI display
 * @property createdAt Record creation timestamp
 */
@Entity(
    tableName = "tags",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "color")
    val color: Int,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
