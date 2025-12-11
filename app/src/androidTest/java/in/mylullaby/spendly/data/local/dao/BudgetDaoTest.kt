package `in`.mylullaby.spendly.data.local.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import `in`.mylullaby.spendly.data.local.SpendlyDatabase
import `in`.mylullaby.spendly.utils.TestDataBuilders
import `in`.mylullaby.spendly.utils.createTestDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for BudgetDao.
 *
 * Tests CRUD operations, composite unique key (category_id, month, year),
 * overall budget (null category_id), notification flags, and Flow reactivity.
 *
 * Pattern follows CurrencyUtilsTest: methodName_inputCondition_expectedResult
 */
@RunWith(AndroidJUnit4::class)
class BudgetDaoTest {

    private lateinit var database: SpendlyDatabase
    private lateinit var dao: BudgetDao
    private lateinit var categoryDao: CategoryDao

    @Before
    fun setUp() {
        database = createTestDatabase()
        dao = database.budgetDao()
        categoryDao = database.categoryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // CRUD Operations Tests

    @Test
    fun insertBudget_withValidData_returnsInsertedId() = runTest {
        // Arrange
        val category = TestDataBuilders.createTestCategoryEntity(name = "Food")
        val categoryId = categoryDao.insert(category)

        val budget = TestDataBuilders.createTestBudgetEntity(
            categoryId = categoryId,
            amount = 50000L,
            month = 1,
            year = 2025
        )

        // Act
        val id = dao.insert(budget)

        // Assert
        assertTrue("Inserted ID should be positive", id > 0)
        dao.getBudgetById(id).test {
            val retrieved = awaitItem()
            assertEquals(50000L, retrieved?.amount)
            assertEquals(categoryId, retrieved?.categoryId)
            assertEquals(1, retrieved?.month)
            assertEquals(2025, retrieved?.year)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertBudget_withPaiseAmount_exactMatch() = runTest {
        // Test ZERO tolerance for precision loss
        val testAmounts = listOf(1L, 50L, 100L, 12345L, 123456789L)

        testAmounts.forEach { amount ->
            // Arrange
            val budget = TestDataBuilders.createTestBudgetEntity(
                amount = amount,
                month = 1,
                year = 2025
            )

            // Act
            val id = dao.insert(budget)

            // Assert
            dao.getBudgetById(id).test {
                val retrieved = awaitItem()
                assertEquals("Exact match for $amount paise", amount, retrieved?.amount)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun insertBudget_withDuplicateCategoryAndMonth_replaces() = runTest {
        // Arrange
        val category = TestDataBuilders.createTestCategoryEntity(name = "Food")
        val categoryId = categoryDao.insert(category)

        val budget1 = TestDataBuilders.createTestBudgetEntity(
            categoryId = categoryId,
            amount = 50000L,
            month = 1,
            year = 2025
        )
        dao.insert(budget1)

        // Act - Insert with same category and month but different amount
        val budget2 = TestDataBuilders.createTestBudgetEntity(
            categoryId = categoryId,
            amount = 75000L,
            month = 1,
            year = 2025
        )
        dao.insert(budget2)

        // Assert - Should replace due to OnConflictStrategy.REPLACE
        dao.getBudgetsByMonth(1, 2025).test {
            val budgets = awaitItem()
            val foodBudgets = budgets.filter { it.categoryId == categoryId }
            assertEquals(1, foodBudgets.size)
            assertEquals(75000L, foodBudgets[0].amount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateBudget_changesData_andModifiedTimestamp() = runTest {
        // Arrange
        val budget = TestDataBuilders.createTestBudgetEntity(
            amount = 50000L,
            month = 1,
            year = 2025
        )
        val id = dao.insert(budget)

        dao.getBudgetById(id).test {
            val inserted = awaitItem()!!
            val originalModifiedAt = inserted.modifiedAt

            // Wait briefly to ensure timestamp changes
            Thread.sleep(10)

            // Act
            val updated = inserted.copy(
                amount = 75000L,
                modifiedAt = System.currentTimeMillis()
            )
            dao.update(updated)

            // Assert
            val retrieved = awaitItem()
            assertEquals(75000L, retrieved?.amount)
            assertNotEquals(
                "modifiedAt should change on update",
                originalModifiedAt,
                retrieved?.modifiedAt
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteBudget_removesFromDatabase() = runTest {
        // Arrange
        val budget = TestDataBuilders.createTestBudgetEntity()
        val id = dao.insert(budget)

        // Act
        dao.getBudgetById(id).test {
            val retrieved = awaitItem()!!
            dao.delete(retrieved)

            // Assert
            val afterDelete = awaitItem()
            assertNull("Budget should be null after deletion", afterDelete)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Query Tests

    @Test
    fun getBudgetsByMonth_returnsOnlyForSpecificMonth() = runTest {
        // Arrange
        val category1 = TestDataBuilders.createTestCategoryEntity(name = "Food")
        val category2 = TestDataBuilders.createTestCategoryEntity(name = "Travel")
        val categoryId1 = categoryDao.insert(category1)
        val categoryId2 = categoryDao.insert(category2)

        // January 2025 budgets
        dao.insert(TestDataBuilders.createTestBudgetEntity(
            categoryId = categoryId1,
            month = 1,
            year = 2025
        ))
        dao.insert(TestDataBuilders.createTestBudgetEntity(
            categoryId = categoryId2,
            month = 1,
            year = 2025
        ))

        // February 2025 budget
        dao.insert(TestDataBuilders.createTestBudgetEntity(
            categoryId = categoryId1,
            month = 2,
            year = 2025
        ))

        // Act & Assert
        dao.getBudgetsByMonth(1, 2025).test {
            val budgets = awaitItem()
            assertEquals(2, budgets.size)
            assertTrue(budgets.all { it.month == 1 && it.year == 2025 })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getBudgetByCategoryAndMonth_returnsCorrectBudget() = runTest {
        // Arrange
        val category = TestDataBuilders.createTestCategoryEntity(name = "Food")
        val categoryId = categoryDao.insert(category)

        dao.insert(TestDataBuilders.createTestBudgetEntity(
            categoryId = categoryId,
            amount = 50000L,
            month = 1,
            year = 2025
        ))

        // Act & Assert
        dao.getBudgetByCategoryAndMonth(categoryId, 1, 2025).test {
            val budget = awaitItem()
            assertNotNull(budget)
            assertEquals(categoryId, budget?.categoryId)
            assertEquals(50000L, budget?.amount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getBudgetByCategoryAndMonth_withNonexistent_returnsNull() = runTest {
        // Act & Assert
        dao.getBudgetByCategoryAndMonth(999L, 1, 2025).test {
            val budget = awaitItem()
            assertNull("Should return null for non-existent budget", budget)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getOverallBudget_returnsOnlyNullCategory() = runTest {
        // Arrange
        val category = TestDataBuilders.createTestCategoryEntity(name = "Food")
        val categoryId = categoryDao.insert(category)

        // Category budget
        dao.insert(TestDataBuilders.createTestBudgetEntity(
            categoryId = categoryId,
            amount = 50000L,
            month = 1,
            year = 2025
        ))

        // Overall budget (null category)
        dao.insert(TestDataBuilders.createTestBudgetEntity(
            categoryId = null,
            amount = 200000L,
            month = 1,
            year = 2025
        ))

        // Act & Assert
        dao.getOverallBudget(1, 2025).test {
            val overallBudget = awaitItem()
            assertNotNull(overallBudget)
            assertNull("Overall budget should have null categoryId", overallBudget?.categoryId)
            assertEquals(200000L, overallBudget?.amount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getBudgetById_returnsCorrectBudget() = runTest {
        // Arrange
        val budget1 = TestDataBuilders.createTestBudgetEntity(month = 1, year = 2025)
        val budget2 = TestDataBuilders.createTestBudgetEntity(month = 2, year = 2025)
        val id1 = dao.insert(budget1)
        val id2 = dao.insert(budget2)

        // Act & Assert
        dao.getBudgetById(id2).test {
            val retrieved = awaitItem()
            assertEquals(id2, retrieved?.id)
            assertEquals(2, retrieved?.month)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllBudgets_orderedByYearDescMonthDesc() = runTest {
        // Arrange
        val budget1 = TestDataBuilders.createTestBudgetEntity(month = 1, year = 2024)
        val budget2 = TestDataBuilders.createTestBudgetEntity(month = 12, year = 2024)
        val budget3 = TestDataBuilders.createTestBudgetEntity(month = 2, year = 2025)
        val budget4 = TestDataBuilders.createTestBudgetEntity(month = 1, year = 2025)

        dao.insert(budget1)
        dao.insert(budget2)
        dao.insert(budget3)
        dao.insert(budget4)

        // Act & Assert
        dao.getAllBudgets().test {
            val budgets = awaitItem()
            assertEquals(4, budgets.size)
            // Newest first (2025/02, 2025/01, 2024/12, 2024/01)
            assertEquals(2025, budgets[0].year)
            assertEquals(2, budgets[0].month)
            assertEquals(2025, budgets[1].year)
            assertEquals(1, budgets[1].month)
            assertEquals(2024, budgets[2].year)
            assertEquals(12, budgets[2].month)
            assertEquals(2024, budgets[3].year)
            assertEquals(1, budgets[3].month)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Aggregation Tests

    @Test
    fun getBudgetCountByCategory_returnsCorrectCount() = runTest {
        // Arrange
        val category = TestDataBuilders.createTestCategoryEntity(name = "Food")
        val categoryId = categoryDao.insert(category)

        dao.insert(TestDataBuilders.createTestBudgetEntity(categoryId = categoryId, month = 1, year = 2025))
        dao.insert(TestDataBuilders.createTestBudgetEntity(categoryId = categoryId, month = 2, year = 2025))
        dao.insert(TestDataBuilders.createTestBudgetEntity(categoryId = categoryId, month = 3, year = 2025))

        // Act
        val count = dao.getBudgetCountByCategory(categoryId)

        // Assert
        assertEquals(3, count)
    }

    // Notification Flag Tests

    @Test
    fun updateNotification75Sent_changesFlag() = runTest {
        // Arrange
        val budget = TestDataBuilders.createTestBudgetEntity(
            notification75Sent = false,
            month = 1,
            year = 2025
        )
        val id = dao.insert(budget)

        // Act
        dao.updateNotification75Sent(id, true)

        // Assert
        dao.getBudgetById(id).test {
            val retrieved = awaitItem()
            assertTrue("notification75Sent should be true", retrieved?.notification75Sent == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateNotification100Sent_changesFlag() = runTest {
        // Arrange
        val budget = TestDataBuilders.createTestBudgetEntity(
            notification100Sent = false,
            month = 1,
            year = 2025
        )
        val id = dao.insert(budget)

        // Act
        dao.updateNotification100Sent(id, true)

        // Assert
        dao.getBudgetById(id).test {
            val retrieved = awaitItem()
            assertTrue("notification100Sent should be true", retrieved?.notification100Sent == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Foreign Key Tests

    @Test
    fun deleteCategory_cascadesDeleteToBudgets() = runTest {
        // Arrange
        val category = TestDataBuilders.createTestCategoryEntity(name = "Food")
        val categoryId = categoryDao.insert(category)

        val budget = TestDataBuilders.createTestBudgetEntity(
            categoryId = categoryId,
            month = 1,
            year = 2025
        )
        val budgetId = dao.insert(budget)

        // Verify budget exists initially
        dao.getBudgetById(budgetId).test {
            val initial = awaitItem()
            assertEquals(categoryId, initial?.categoryId)
            cancelAndIgnoreRemainingEvents()
        }

        // Act - Delete category
        categoryDao.getCategoryById(categoryId).test {
            val retrievedCategory = awaitItem()!!
            categoryDao.delete(retrievedCategory)
            cancelAndIgnoreRemainingEvents()
        }

        // Assert - Budget should be CASCADE deleted
        dao.getBudgetById(budgetId).test {
            val afterDelete = awaitItem()
            assertNull("Budget should be CASCADE deleted when category is deleted", afterDelete)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Flow Reactivity Tests

    @Test
    fun getBudgetsByMonth_emitsUpdatesOnInsert() = runTest {
        // Act & Assert
        dao.getBudgetsByMonth(1, 2025).test {
            // Initial emission (empty)
            assertEquals(0, awaitItem().size)

            // Insert budget
            dao.insert(TestDataBuilders.createTestBudgetEntity(month = 1, year = 2025))

            // Verify Flow emits updated list
            assertEquals(1, awaitItem().size)

            // Insert another
            dao.insert(TestDataBuilders.createTestBudgetEntity(month = 1, year = 2025))
            assertEquals(2, awaitItem().size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getBudgetById_emitsUpdatesOnUpdate() = runTest {
        // Arrange
        val budget = TestDataBuilders.createTestBudgetEntity(amount = 50000L)
        val id = dao.insert(budget)

        // Act & Assert
        dao.getBudgetById(id).test {
            val original = awaitItem()!!
            assertEquals(50000L, original.amount)
            assertFalse("notification75Sent should initially be false", original.notification75Sent)

            // Update notification flag
            dao.updateNotification75Sent(id, true)

            // Verify Flow emits updated budget
            val afterUpdate = awaitItem()
            assertTrue("notification75Sent should be true after update", afterUpdate?.notification75Sent == true)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
