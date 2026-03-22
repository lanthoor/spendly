package dev.lanthoor.spendly.data.repository

import dev.lanthoor.spendly.data.local.dao.CategoryDao
import dev.lanthoor.spendly.data.local.entities.CategoryEntity
import dev.lanthoor.spendly.domain.model.Category
import dev.lanthoor.spendly.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override suspend fun insertCategory(category: Category): Long {
        return categoryDao.insert(category.toEntity())
    }

    override suspend fun updateCategory(category: Category) {
        categoryDao.update(category.toEntity())
    }

    override suspend fun deleteCategory(categoryId: Long, replacementCategoryId: Long) {
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

    override suspend fun seedPredefinedCategories() {
        if (isPredefinedSeeded()) {
            return
        }

        val entities = Category.PREDEFINED.map { it.toEntity() }
        categoryDao.insertAll(entities)
    }

    override suspend fun isPredefinedSeeded(): Boolean {
        val others = categoryDao.getCategoryByName("Others")
        return others != null
    }

    override suspend fun isCategoryNameUnique(name: String): Boolean {
        val existing = categoryDao.getCategoryByName(name)
        return existing == null
    }

    private fun CategoryEntity.toDomainModel(): Category {
        return Category(
            id = id,
            name = name,
            icon = icon,
            color = color,
            isCustom = isCustom,
            sortOrder = sortOrder
        )
    }

    private fun Category.toEntity(): CategoryEntity {
        return CategoryEntity(
            id = id,
            name = name,
            icon = icon,
            color = color,
            isCustom = isCustom,
            sortOrder = sortOrder
        )
    }
}
