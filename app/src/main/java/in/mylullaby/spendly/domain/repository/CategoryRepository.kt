package `in`.mylullaby.spendly.domain.repository

import `in`.mylullaby.spendly.domain.model.Category
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for category operations.
 * Handles both predefined and custom categories.
 */
interface CategoryRepository {

    // CRUD operations

    /**
     * Inserts a new category into the database.
     * @param category The category to insert
     * @return The ID of the inserted category
     */
    suspend fun insertCategory(category: Category): Long

    /**
     * Updates an existing category in the database.
     * @param category The category to update
     */
    suspend fun updateCategory(category: Category)

    /**
     * Deletes a category and reassigns all transactions to a replacement category.
     * @param categoryId The ID of the category to delete
     * @param replacementCategoryId The ID of the category to reassign transactions to
     */
    suspend fun deleteCategory(categoryId: Long, replacementCategoryId: Long)

    /**
     * Retrieves a category by its ID.
     * @param id The category ID
     * @return Flow emitting the category or null if not found
     */
    fun getCategoryById(id: Long): Flow<Category?>

    /**
     * Retrieves all categories (both predefined and custom).
     * @return Flow emitting list of all categories
     */
    fun getAllCategories(): Flow<List<Category>>

    // Predefined vs custom

    /**
     * Retrieves only predefined categories.
     * @return Flow emitting list of predefined categories
     */
    fun getPredefinedCategories(): Flow<List<Category>>

    /**
     * Retrieves only custom (user-created) categories.
     * @return Flow emitting list of custom categories
     */
    fun getCustomCategories(): Flow<List<Category>>

    // Seeding

    /**
     * Seeds the database with predefined categories.
     * Should be called on first app launch.
     */
    suspend fun seedPredefinedCategories()

    /**
     * Checks if predefined categories have already been seeded.
     * @return true if predefined categories exist in the database
     */
    suspend fun isPredefinedSeeded(): Boolean

    // Validation

    /**
     * Checks if a category name is unique (not already in use).
     * @param name The category name to check
     * @return true if the name is unique
     */
    suspend fun isCategoryNameUnique(name: String): Boolean
}
