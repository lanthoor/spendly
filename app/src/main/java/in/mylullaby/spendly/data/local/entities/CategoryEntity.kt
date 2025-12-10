package `in`.mylullaby.spendly.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Category entity for expense tracking.
 *
 * Represents both predefined categories (e.g., Food & Dining, Travel) and
 * custom user-created categories.
 *
 * @property id Unique identifier (auto-generated)
 * @property name Category name (unique)
 * @property icon Material Icon name (e.g., "restaurant", "flight")
 * @property color ARGB color int
 * @property isCustom True for user-created categories, false for predefined
 * @property sortOrder Display order in category lists
 */
@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["sort_order"])
    ]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "icon")
    val icon: String, // Material Icon name as String

    @ColumnInfo(name = "color")
    val color: Int, // ARGB color int

    @ColumnInfo(name = "is_custom")
    val isCustom: Boolean = false,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int
)
