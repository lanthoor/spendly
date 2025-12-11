package `in`.mylullaby.spendly.data.local.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import `in`.mylullaby.spendly.data.local.SpendlyDatabase
import `in`.mylullaby.spendly.domain.model.Category
import `in`.mylullaby.spendly.utils.TestDataBuilders
import `in`.mylullaby.spendly.utils.createTestDatabase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for CategoryDao.
 *
 * Tests CRUD operations, unique constraints, sorting, custom vs predefined filtering,
 * and foreign key behavior (SET_NULL on delete).
 *
 * Pattern follows CurrencyUtilsTest: methodName_inputCondition_expectedResult
 */
@RunWith(AndroidJUnit4::class)
class CategoryDaoTest {

    private lateinit var database: SpendlyDatabase
    private lateinit var dao: CategoryDao
    private lateinit var expenseDao: ExpenseDao

    @Before
    fun setUp() {
        database = createTestDatabase()
        dao = database.categoryDao()
        expenseDao = database.expenseDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // CRUD Operations Tests

    @Test
    fun insertCategory_withValidData_returnsInsertedId() = runTest {
        // Arrange
        val category = TestDataBuilders.createTestCategoryEntity(name = "Food")

        // Act
        val id = dao.insert(category)

        // Assert
        assertTrue("Inserted ID should be positive", id > 0)
        dao.getCategoryById(id).test {
            val retrieved = awaitItem()
            assertEquals("Food", retrieved?.name)
            assertEquals(category.icon, retrieved?.icon)
            assertEquals(category.color, retrieved?.color)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertCategory_withDuplicateName_replacesExisting() = runTest {
        // Arrange
        val category1 = TestDataBuilders.createTestCategoryEntity(
            name = "Food",
            icon = "restaurant",
            color = 0xFFFF0000.toInt()
        )
        val id1 = dao.insert(category1)

        // Act - Insert with same name but different properties
        val category2 = TestDataBuilders.createTestCategoryEntity(
            name = "Food",
            icon = "fastfood",
            color = 0xFF00FF00.toInt()
        )
        dao.insert(category2)

        // Assert - Should replace due to OnConflictStrategy.REPLACE
        dao.getAllCategories().test {
            val categories = awaitItem()
            // Only 1 "Food" category should exist
            val foodCategories = categories.filter { it.name == "Food" }
            assertEquals(1, foodCategories.size)
            // Should have new properties
            assertEquals("fastfood", foodCategories[0].icon)
            assertEquals(0xFF00FF00.toInt(), foodCategories[0].color)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertAll_withMultipleCategories_insertsAllSuccessfully() = runTest {
        // Arrange
        val categories = listOf(
            TestDataBuilders.createTestCategoryEntity(name = "Food", sortOrder = 1),
            TestDataBuilders.createTestCategoryEntity(name = "Travel", sortOrder = 2),
            TestDataBuilders.createTestCategoryEntity(name = "Rent", sortOrder = 3)
        )

        // Act
        dao.insertAll(categories)

        // Assert
        dao.getAllCategories().test {
            val retrieved = awaitItem()
            assertEquals(3, retrieved.size)
            assertTrue(retrieved.any { it.name == "Food" })
            assertTrue(retrieved.any { it.name == "Travel" })
            assertTrue(retrieved.any { it.name == "Rent" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertAll_withPredefinedCategories_insertsAllThirteen() = runTest {
        // Arrange - Use actual predefined categories from domain model
        val predefinedCategories = Category.PREDEFINED.map { category ->
            TestDataBuilders.createTestCategoryEntity(
                id = category.id,
                name = category.name,
                icon = category.icon,
                color = category.color,
                isCustom = category.isCustom,
                sortOrder = category.sortOrder
            )
        }

        // Act
        dao.insertAll(predefinedCategories)

        // Assert
        dao.getAllCategories().test {
            val retrieved = awaitItem()
            assertEquals(13, retrieved.size)
            // Verify key categories exist
            assertTrue(retrieved.any { it.name == "Food & Dining" })
            assertTrue(retrieved.any { it.name == "Travel" })
            assertTrue(retrieved.any { it.name == "Misc" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateCategory_changesData() = runTest {
        // Arrange
        val category = TestDataBuilders.createTestCategoryEntity(
            name = "Original",
            icon = "category",
            color = 0xFFFF0000.toInt()
        )
        val id = dao.insert(category)

        // Act
        dao.getCategoryById(id).test {
            val inserted = awaitItem()!!
            val updated = inserted.copy(
                name = "Updated",
                icon = "new_icon",
                color = 0xFF00FF00.toInt()
            )
            dao.update(updated)

            // Assert
            val retrieved = awaitItem()
            assertEquals("Updated", retrieved?.name)
            assertEquals("new_icon", retrieved?.icon)
            assertEquals(0xFF00FF00.toInt(), retrieved?.color)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteCategory_removesFromDatabase() = runTest {
        // Arrange
        val category = TestDataBuilders.createTestCategoryEntity(name = "Test")
        val id = dao.insert(category)

        // Act
        dao.getCategoryById(id).test {
            val retrieved = awaitItem()!!
            dao.delete(retrieved)

            // Assert
            val afterDelete = awaitItem()
            assertNull("Category should be null after deletion", afterDelete)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Query Tests

    @Test
    fun getAllCategories_orderedBySortOrder() = runTest {
        // Arrange
        val category1 = TestDataBuilders.createTestCategoryEntity(name = "Third", sortOrder = 3)
        val category2 = TestDataBuilders.createTestCategoryEntity(name = "First", sortOrder = 1)
        val category3 = TestDataBuilders.createTestCategoryEntity(name = "Second", sortOrder = 2)

        dao.insert(category1)
        dao.insert(category2)
        dao.insert(category3)

        // Act & Assert
        dao.getAllCategories().test {
            val categories = awaitItem()
            assertEquals(3, categories.size)
            // Should be ordered by sortOrder ASC
            assertEquals("First", categories[0].name)
            assertEquals("Second", categories[1].name)
            assertEquals("Third", categories[2].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getCategoryById_returnsCorrectCategory() = runTest {
        // Arrange
        val category1 = TestDataBuilders.createTestCategoryEntity(name = "First")
        val category2 = TestDataBuilders.createTestCategoryEntity(name = "Second")
        val id1 = dao.insert(category1)
        val id2 = dao.insert(category2)

        // Act & Assert
        dao.getCategoryById(id2).test {
            val retrieved = awaitItem()
            assertEquals("Second", retrieved?.name)
            assertEquals(id2, retrieved?.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getCategoryById_withNonexistentId_returnsNull() = runTest {
        // Act & Assert
        dao.getCategoryById(999L).test {
            val retrieved = awaitItem()
            assertNull("Should return null for non-existent ID", retrieved)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getCustomCategories_returnsOnlyCustom() = runTest {
        // Arrange
        val predefined = TestDataBuilders.createTestCategoryEntity(
            name = "Predefined",
            isCustom = false
        )
        val custom1 = TestDataBuilders.createTestCategoryEntity(
            name = "Custom 1",
            isCustom = true
        )
        val custom2 = TestDataBuilders.createTestCategoryEntity(
            name = "Custom 2",
            isCustom = true
        )

        dao.insert(predefined)
        dao.insert(custom1)
        dao.insert(custom2)

        // Act & Assert
        dao.getCustomCategories().test {
            val categories = awaitItem()
            assertEquals(2, categories.size)
            assertTrue(categories.all { it.isCustom })
            assertTrue(categories.any { it.name == "Custom 1" })
            assertTrue(categories.any { it.name == "Custom 2" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getPredefinedCategories_returnsOnlyPredefined() = runTest {
        // Arrange
        val predefined1 = TestDataBuilders.createTestCategoryEntity(
            name = "Predefined 1",
            isCustom = false
        )
        val predefined2 = TestDataBuilders.createTestCategoryEntity(
            name = "Predefined 2",
            isCustom = false
        )
        val custom = TestDataBuilders.createTestCategoryEntity(
            name = "Custom",
            isCustom = true
        )

        dao.insert(predefined1)
        dao.insert(predefined2)
        dao.insert(custom)

        // Act & Assert
        dao.getPredefinedCategories().test {
            val categories = awaitItem()
            assertEquals(2, categories.size)
            assertFalse(categories.any { it.isCustom })
            assertTrue(categories.any { it.name == "Predefined 1" })
            assertTrue(categories.any { it.name == "Predefined 2" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Validation Tests

    @Test
    fun exists_withExistingCategory_returnsOne() = runBlocking {
        // Arrange
        val category = TestDataBuilders.createTestCategoryEntity(name = "Test")
        val id = dao.insert(category)

        // Act
        val count = dao.exists(id)

        // Assert
        assertEquals(1, count)
    }

    @Test
    fun exists_withNonexistentCategory_returnsZero() = runBlocking {
        // Act
        val count = dao.exists(999L)

        // Assert
        assertEquals(0, count)
    }

    @Test
    fun getCategoryByName_withExistingName_returnsCategory() = runBlocking {
        // Arrange
        val category = TestDataBuilders.createTestCategoryEntity(name = "Food")
        dao.insert(category)

        // Act
        val retrieved = dao.getCategoryByName("Food")

        // Assert
        assertNotNull(retrieved)
        assertEquals("Food", retrieved?.name)
    }

    @Test
    fun getCategoryByName_withNonexistentName_returnsNull() = runBlocking {
        // Act
        val retrieved = dao.getCategoryByName("Nonexistent")

        // Assert
        assertNull(retrieved)
    }

    // Foreign Key Tests

    @Test
    fun deleteCategory_setsExpenseCategoryIdToNull() = runTest {
        // Arrange
        val category = TestDataBuilders.createTestCategoryEntity(name = "Food")
        val categoryId = dao.insert(category)

        val expense = TestDataBuilders.createTestExpenseEntity(categoryId = categoryId)
        val expenseId = expenseDao.insert(expense)

        // Verify expense has category initially
        expenseDao.getExpenseById(expenseId).test {
            assertEquals(categoryId, awaitItem()?.categoryId)
            cancelAndIgnoreRemainingEvents()
        }

        // Act - Delete category
        dao.getCategoryById(categoryId).test {
            val retrievedCategory = awaitItem()!!
            dao.delete(retrievedCategory)
            cancelAndIgnoreRemainingEvents()
        }

        // Assert - Expense should still exist but with null categoryId (SET_NULL)
        expenseDao.getExpenseById(expenseId).test {
            val retrievedExpense = awaitItem()
            assertNotNull("Expense should still exist", retrievedExpense)
            assertNull("Category ID should be null after category deletion", retrievedExpense?.categoryId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Flow Reactivity Tests

    @Test
    fun getAllCategories_emitsUpdatesOnInsert() = runTest {
        // Act & Assert
        dao.getAllCategories().test {
            // Initial emission (empty)
            assertEquals(0, awaitItem().size)

            // Insert category
            dao.insert(TestDataBuilders.createTestCategoryEntity())

            // Verify Flow emits updated list
            assertEquals(1, awaitItem().size)

            // Insert another
            dao.insert(TestDataBuilders.createTestCategoryEntity(name = "Another"))
            assertEquals(2, awaitItem().size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllCategories_emitsUpdatesOnDelete() = runTest {
        // Arrange
        val category1 = TestDataBuilders.createTestCategoryEntity(name = "First")
        val category2 = TestDataBuilders.createTestCategoryEntity(name = "Second")
        dao.insert(category1)
        dao.insert(category2)

        // Act & Assert
        dao.getAllCategories().test {
            // Initial emission (2 categories)
            val initial = awaitItem()
            assertEquals(2, initial.size)

            // Delete one
            dao.delete(initial[0])

            // Verify Flow emits updated list
            val afterDelete = awaitItem()
            assertEquals(1, afterDelete.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getCategoryById_emitsUpdatesOnUpdate() = runTest {
        // Arrange
        val category = TestDataBuilders.createTestCategoryEntity(name = "Original")
        val id = dao.insert(category)

        // Act & Assert
        dao.getCategoryById(id).test {
            val original = awaitItem()!!
            assertEquals("Original", original.name)

            // Update
            val updated = original.copy(name = "Updated")
            dao.update(updated)

            // Verify Flow emits updated category
            val afterUpdate = awaitItem()
            assertEquals("Updated", afterUpdate?.name)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
