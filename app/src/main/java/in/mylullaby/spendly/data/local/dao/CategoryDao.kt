package `in`.mylullaby.spendly.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import `in`.mylullaby.spendly.data.local.entities.CategoryEntity

/**
 * Data Access Object for Category operations.
 *
 * Provides methods for CRUD operations and queries on categories.
 * All query methods return Flow for reactive updates.
 */
@Dao
interface CategoryDao {

    /**
     * Insert a new category.
     * Replaces existing category if conflict occurs (based on unique name).
     *
     * @param category Category to insert
     * @return Row ID of inserted category
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity): Long

    /**
     * Insert multiple categories.
     * Used for pre-populating predefined categories.
     *
     * @param categories List of categories to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    /**
     * Update an existing category.
     *
     * @param category Category with updated values
     */
    @Update
    suspend fun update(category: CategoryEntity)

    /**
     * Delete a category.
     * Note: Related expenses will have categoryId set to null (SET_NULL FK).
     *
     * @param category Category to delete
     */
    @Delete
    suspend fun delete(category: CategoryEntity)

    /**
     * Get all categories ordered by sort order.
     * Includes both predefined and custom categories.
     *
     * @return Flow of category list
     */
    @Query("SELECT * FROM categories ORDER BY sort_order ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    /**
     * Get a category by ID.
     *
     * @param categoryId Category ID
     * @return Flow of category (null if not found)
     */
    @Query("SELECT * FROM categories WHERE id = :categoryId")
    fun getCategoryById(categoryId: Long): Flow<CategoryEntity?>

    /**
     * Get all custom (user-created) categories.
     *
     * @return Flow of custom category list
     */
    @Query("SELECT * FROM categories WHERE is_custom = 1 ORDER BY sort_order ASC")
    fun getCustomCategories(): Flow<List<CategoryEntity>>

    /**
     * Get all predefined categories.
     *
     * @return Flow of predefined category list
     */
    @Query("SELECT * FROM categories WHERE is_custom = 0 ORDER BY sort_order ASC")
    fun getPredefinedCategories(): Flow<List<CategoryEntity>>

    /**
     * Check if a category exists by ID.
     *
     * @param categoryId Category ID to check
     * @return Count (1 if exists, 0 if not)
     */
    @Query("SELECT COUNT(*) FROM categories WHERE id = :categoryId")
    suspend fun exists(categoryId: Long): Int

    /**
     * Get a category by name.
     * Used to check for duplicate names before insert.
     *
     * @param name Category name
     * @return Category entity (null if not found)
     */
    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): CategoryEntity?

    /**
     * Get all categories of a specific type.
     *
     * @param type Category type ("EXPENSE" or "INCOME")
     * @return Flow of category list filtered by type
     */
    @Query("SELECT * FROM categories WHERE type = :type ORDER BY sort_order ASC")
    fun getCategoriesByType(type: String): Flow<List<CategoryEntity>>

    /**
     * Get all expense categories (predefined + custom).
     *
     * @return Flow of expense category list
     */
    @Query("SELECT * FROM categories WHERE type = 'EXPENSE' ORDER BY sort_order ASC")
    fun getExpenseCategories(): Flow<List<CategoryEntity>>

    /**
     * Get all income categories (predefined + custom).
     *
     * @return Flow of income category list
     */
    @Query("SELECT * FROM categories WHERE type = 'INCOME' ORDER BY sort_order ASC")
    fun getIncomeCategories(): Flow<List<CategoryEntity>>
}
