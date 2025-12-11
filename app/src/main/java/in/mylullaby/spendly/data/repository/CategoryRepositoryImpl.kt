package `in`.mylullaby.spendly.data.repository

import `in`.mylullaby.spendly.data.local.dao.CategoryDao
import `in`.mylullaby.spendly.data.local.entities.CategoryEntity
import `in`.mylullaby.spendly.domain.model.Category
import `in`.mylullaby.spendly.domain.model.CategoryType
import `in`.mylullaby.spendly.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of CategoryRepository.
 * Handles entity-to-model mapping, category seeding, and transaction reassignment.
 */
@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    // CRUD operations

    override suspend fun insertCategory(category: Category): Long {
        return categoryDao.insert(category.toEntity())
    }

    override suspend fun updateCategory(category: Category) {
        categoryDao.update(category.toEntity())
    }

    override suspend fun deleteCategory(categoryId: Long, replacementCategoryId: Long) {
        // Note: Transaction reassignment will be handled in Phase 4 when implementing UI
        // For now, just delete the category (FK SET_NULL will handle expenses)
        val category = categoryDao.getCategoryById(categoryId).firstOrNull()
        if (category != null) {
            categoryDao.delete(category)
        }
    }

    override fun getCategoryById(id: Long): Flow<Category?> {
        return categoryDao.getCategoryById(id).map { it?.toDomainModel() }
    }

    override fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    // Predefined vs custom

    override fun getPredefinedCategories(): Flow<List<Category>> {
        return categoryDao.getPredefinedCategories().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getCustomCategories(): Flow<List<Category>> {
        return categoryDao.getCustomCategories().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    // Seeding

    override suspend fun seedPredefinedCategories() {
        // Check if already seeded to avoid duplicates
        if (isPredefinedSeeded()) {
            return
        }

        // Insert all predefined categories from domain model
        val entities = Category.PREDEFINED.map { it.toEntity() }
        categoryDao.insertAll(entities)
    }

    override suspend fun isPredefinedSeeded(): Boolean {
        // Check if any predefined categories exist (check for "Misc" by name)
        val misc = categoryDao.getCategoryByName("Misc")
        return misc != null
    }

    // Filter by type

    override fun getCategoriesByType(type: CategoryType): Flow<List<Category>> {
        val typeString = when (type) {
            CategoryType.INCOME -> "INCOME"
            CategoryType.EXPENSE -> "EXPENSE"
        }
        return categoryDao.getCategoriesByType(typeString).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getExpenseCategories(): Flow<List<Category>> {
        return categoryDao.getExpenseCategories().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getIncomeCategories(): Flow<List<Category>> {
        return categoryDao.getIncomeCategories().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    // Validation

    override suspend fun isCategoryNameUnique(name: String): Boolean {
        val existing = categoryDao.getCategoryByName(name)
        return existing == null
    }

    // Entity to Domain Model mapping

    private fun CategoryEntity.toDomainModel(): Category {
        return Category(
            id = id,
            name = name,
            icon = icon,
            color = color,
            isCustom = isCustom,
            sortOrder = sortOrder,
            type = when (type) {
                "INCOME" -> CategoryType.INCOME
                else -> CategoryType.EXPENSE
            }
        )
    }

    private fun Category.toEntity(): CategoryEntity {
        return CategoryEntity(
            id = id,
            name = name,
            icon = icon,
            color = color,
            isCustom = isCustom,
            sortOrder = sortOrder,
            type = when (type) {
                CategoryType.INCOME -> "INCOME"
                CategoryType.EXPENSE -> "EXPENSE"
            }
        )
    }
}
