package `in`.mylullaby.spendly.data.local.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import `in`.mylullaby.spendly.data.local.SpendlyDatabase
import `in`.mylullaby.spendly.utils.TestDataBuilders
import `in`.mylullaby.spendly.utils.createTestDatabase
import `in`.mylullaby.spendly.utils.daysAgo
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for ExpenseDao.
 *
 * Tests all CRUD operations, complex queries, aggregations, and Flow reactivity.
 * Validates amount precision (paise stored as Long), foreign key behavior, and search functionality.
 *
 * Pattern follows CurrencyUtilsTest: methodName_inputCondition_expectedResult
 */
@RunWith(AndroidJUnit4::class)
class ExpenseDaoTest {

    private lateinit var database: SpendlyDatabase
    private lateinit var dao: ExpenseDao
    private lateinit var categoryDao: CategoryDao

    @Before
    fun setUp() {
        database = createTestDatabase()
        dao = database.expenseDao()
        categoryDao = database.categoryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // CRUD Operations Tests

    @Test
    fun insertExpense_withValidData_returnsInsertedId() = runTest {
        // Arrange
        val expense = TestDataBuilders.createTestExpenseEntity()

        // Act
        val id = dao.insert(expense)

        // Assert
        assertTrue("Inserted ID should be positive", id > 0)
        dao.getExpenseById(id).test {
            val retrieved = awaitItem()
            assertEquals(expense.amount, retrieved?.amount)
            assertEquals(expense.description, retrieved?.description)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertExpense_withPaiseAmount_exactMatch() = runTest {
        // Test ZERO tolerance for precision loss
        val testAmounts = listOf(1L, 50L, 100L, 12345L, 123456789L)

        testAmounts.forEach { amount ->
            // Arrange
            val expense = TestDataBuilders.createTestExpenseEntity(
                amount = amount,
                description = "Test amount $amount"
            )

            // Act
            val id = dao.insert(expense)

            // Assert
            dao.getExpenseById(id).test {
                val retrieved = awaitItem()
                assertEquals("Exact match for $amount paise", amount, retrieved?.amount)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun updateExpense_changesData_andModifiedTimestamp() = runTest {
        // Arrange
        val originalExpense = TestDataBuilders.createTestExpenseEntity(
            description = "Original",
            amount = 10000L
        )
        val id = dao.insert(originalExpense)

        dao.getExpenseById(id).test {
            val inserted = awaitItem()!!
            val originalModifiedAt = inserted.modifiedAt

            // Wait briefly to ensure timestamp changes
            Thread.sleep(10)

            // Act
            val updated = inserted.copy(
                description = "Updated",
                amount = 20000L,
                modifiedAt = System.currentTimeMillis()
            )
            dao.update(updated)

            // Assert
            val retrieved = awaitItem()
            assertEquals("Updated", retrieved?.description)
            assertEquals(20000L, retrieved?.amount)
            assertNotEquals(
                "modifiedAt should change on update",
                originalModifiedAt,
                retrieved?.modifiedAt
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteExpense_removesFromDatabase() = runTest {
        // Arrange
        val expense = TestDataBuilders.createTestExpenseEntity()
        val id = dao.insert(expense)

        // Act
        dao.getExpenseById(id).test {
            val retrieved = awaitItem()!!
            dao.delete(retrieved)

            // Assert
            val afterDelete = awaitItem()
            assertNull("Expense should be null after deletion", afterDelete)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Query Tests

    @Test
    fun getAllExpenses_orderedByDateDesc() = runTest {
        // Arrange
        val now = System.currentTimeMillis()
        val expense1 = TestDataBuilders.createTestExpenseEntity(
            description = "Oldest",
            date = now.daysAgo(3)
        )
        val expense2 = TestDataBuilders.createTestExpenseEntity(
            description = "Middle",
            date = now.daysAgo(2)
        )
        val expense3 = TestDataBuilders.createTestExpenseEntity(
            description = "Newest",
            date = now.daysAgo(1)
        )

        dao.insert(expense1)
        dao.insert(expense2)
        dao.insert(expense3)

        // Act & Assert
        dao.getAllExpenses().test {
            val expenses = awaitItem()
            assertEquals(3, expenses.size)
            // Newest first
            assertEquals("Newest", expenses[0].description)
            assertEquals("Middle", expenses[1].description)
            assertEquals("Oldest", expenses[2].description)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getExpenseById_returnsCorrectExpense() = runTest {
        // Arrange
        val expense1 = TestDataBuilders.createTestExpenseEntity(description = "First")
        val expense2 = TestDataBuilders.createTestExpenseEntity(description = "Second")
        val id1 = dao.insert(expense1)
        val id2 = dao.insert(expense2)

        // Act & Assert
        dao.getExpenseById(id2).test {
            val retrieved = awaitItem()
            assertEquals("Second", retrieved?.description)
            assertEquals(id2, retrieved?.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getExpenseById_withNonexistentId_returnsNull() = runTest {
        // Act & Assert
        dao.getExpenseById(999L).test {
            val retrieved = awaitItem()
            assertNull("Should return null for non-existent ID", retrieved)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getExpensesByDateRange_returnsOnlyWithinRange() = runTest {
        // Arrange
        val now = System.currentTimeMillis()
        val startDate = now.daysAgo(7)
        val endDate = now.daysAgo(1)

        val expenseBeforeRange = TestDataBuilders.createTestExpenseEntity(
            description = "Before",
            date = now.daysAgo(10)
        )
        val expenseInRange1 = TestDataBuilders.createTestExpenseEntity(
            description = "In Range 1",
            date = now.daysAgo(5)
        )
        val expenseInRange2 = TestDataBuilders.createTestExpenseEntity(
            description = "In Range 2",
            date = now.daysAgo(3)
        )
        val expenseAfterRange = TestDataBuilders.createTestExpenseEntity(
            description = "After",
            date = now
        )

        dao.insert(expenseBeforeRange)
        dao.insert(expenseInRange1)
        dao.insert(expenseInRange2)
        dao.insert(expenseAfterRange)

        // Act & Assert
        dao.getExpensesByDateRange(startDate, endDate).test {
            val expenses = awaitItem()
            assertEquals(2, expenses.size)
            assertTrue(expenses.any { it.description == "In Range 1" })
            assertTrue(expenses.any { it.description == "In Range 2" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getExpensesByCategory_returnsOnlyMatchingCategory() = runTest {
        // Arrange
        val category1 = TestDataBuilders.createTestCategoryEntity(name = "Food")
        val category2 = TestDataBuilders.createTestCategoryEntity(name = "Travel")
        val categoryId1 = categoryDao.insert(category1)
        val categoryId2 = categoryDao.insert(category2)

        val foodExpense1 = TestDataBuilders.createTestExpenseEntity(
            description = "Lunch",
            categoryId = categoryId1
        )
        val foodExpense2 = TestDataBuilders.createTestExpenseEntity(
            description = "Dinner",
            categoryId = categoryId1
        )
        val travelExpense = TestDataBuilders.createTestExpenseEntity(
            description = "Bus",
            categoryId = categoryId2
        )

        dao.insert(foodExpense1)
        dao.insert(foodExpense2)
        dao.insert(travelExpense)

        // Act & Assert
        dao.getExpensesByCategory(categoryId1).test {
            val expenses = awaitItem()
            assertEquals(2, expenses.size)
            assertTrue(expenses.all { it.categoryId == categoryId1 })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getExpensesByPaymentMethod_returnsOnlyMatchingMethod() = runTest {
        // Arrange
        val cashExpense = TestDataBuilders.createTestExpenseEntity(
            description = "Cash Payment",
            paymentMethod = "CASH"
        )
        val upiExpense = TestDataBuilders.createTestExpenseEntity(
            description = "UPI Payment",
            paymentMethod = "UPI"
        )
        val cardExpense = TestDataBuilders.createTestExpenseEntity(
            description = "Card Payment",
            paymentMethod = "DEBIT_CARD"
        )

        dao.insert(cashExpense)
        dao.insert(upiExpense)
        dao.insert(cardExpense)

        // Act & Assert
        dao.getExpensesByPaymentMethod("UPI").test {
            val expenses = awaitItem()
            assertEquals(1, expenses.size)
            assertEquals("UPI Payment", expenses[0].description)
            assertEquals("UPI", expenses[0].paymentMethod)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getRecentExpenses_limitsResults() = runTest {
        // Arrange - insert 10 expenses
        repeat(10) { index ->
            val expense = TestDataBuilders.createTestExpenseEntity(
                description = "Expense $index",
                date = System.currentTimeMillis().daysAgo(index)
            )
            dao.insert(expense)
        }

        // Act & Assert
        dao.getRecentExpenses(limit = 5).test {
            val expenses = awaitItem()
            assertEquals(5, expenses.size)
            // Should get the 5 most recent (lowest daysAgo)
            assertEquals("Expense 0", expenses[0].description)
            assertEquals("Expense 4", expenses[4].description)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Aggregation Tests

    @Test
    fun getTotalExpensesByDateRange_returnsExactSum() = runTest {
        // Arrange
        val now = System.currentTimeMillis()
        val startDate = now.daysAgo(7)
        val endDate = now

        val expense1 = TestDataBuilders.createTestExpenseEntity(
            amount = 12345L,
            date = now.daysAgo(5)
        )
        val expense2 = TestDataBuilders.createTestExpenseEntity(
            amount = 67890L,
            date = now.daysAgo(3)
        )
        val expense3 = TestDataBuilders.createTestExpenseEntity(
            amount = 1L,
            date = now.daysAgo(1)
        )

        dao.insert(expense1)
        dao.insert(expense2)
        dao.insert(expense3)

        // Act & Assert
        dao.getTotalExpensesByDateRange(startDate, endDate).test {
            val total = awaitItem()
            assertEquals("Exact sum with ZERO precision loss", 80236L, total)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getTotalExpensesByDateRange_withNoExpenses_returnsNull() = runTest {
        // Arrange
        val now = System.currentTimeMillis()
        val startDate = now.daysAgo(7)
        val endDate = now

        // Act & Assert
        dao.getTotalExpensesByDateRange(startDate, endDate).test {
            val total = awaitItem()
            assertNull("Should return null when no expenses", total)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getTotalExpensesByCategory_returnsExactCategorySum() = runTest {
        // Arrange
        val category = TestDataBuilders.createTestCategoryEntity(name = "Food")
        val categoryId = categoryDao.insert(category)

        val now = System.currentTimeMillis()
        val startDate = now.daysAgo(7)
        val endDate = now

        val foodExpense1 = TestDataBuilders.createTestExpenseEntity(
            amount = 10000L,
            categoryId = categoryId,
            date = now.daysAgo(5)
        )
        val foodExpense2 = TestDataBuilders.createTestExpenseEntity(
            amount = 25000L,
            categoryId = categoryId,
            date = now.daysAgo(3)
        )

        dao.insert(foodExpense1)
        dao.insert(foodExpense2)

        // Act & Assert
        dao.getTotalExpensesByCategory(categoryId, startDate, endDate).test {
            val total = awaitItem()
            assertEquals(35000L, total)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getExpensesByCategoryGrouped_returnsCorrectSummaries() = runTest {
        // Arrange
        val category1 = TestDataBuilders.createTestCategoryEntity(name = "Food")
        val category2 = TestDataBuilders.createTestCategoryEntity(name = "Travel")
        val categoryId1 = categoryDao.insert(category1)
        val categoryId2 = categoryDao.insert(category2)

        val now = System.currentTimeMillis()
        val startDate = now.daysAgo(7)
        val endDate = now

        // Food expenses: 10000 + 20000 = 30000
        dao.insert(TestDataBuilders.createTestExpenseEntity(
            amount = 10000L,
            categoryId = categoryId1,
            date = now.daysAgo(5)
        ))
        dao.insert(TestDataBuilders.createTestExpenseEntity(
            amount = 20000L,
            categoryId = categoryId1,
            date = now.daysAgo(3)
        ))

        // Travel expenses: 50000
        dao.insert(TestDataBuilders.createTestExpenseEntity(
            amount = 50000L,
            categoryId = categoryId2,
            date = now.daysAgo(4)
        ))

        // Act & Assert
        dao.getExpensesByCategoryGrouped(startDate, endDate).test {
            val summaries = awaitItem()
            assertEquals(2, summaries.size)
            // Ordered by total DESC, so Travel first
            assertEquals(categoryId2, summaries[0].categoryId)
            assertEquals(50000L, summaries[0].total)
            assertEquals(categoryId1, summaries[1].categoryId)
            assertEquals(30000L, summaries[1].total)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getExpenseCountByCategory_returnsCorrectCount() = runBlocking {
        // Arrange
        val category = TestDataBuilders.createTestCategoryEntity(name = "Food")
        val categoryId = categoryDao.insert(category)

        val expense1 = TestDataBuilders.createTestExpenseEntity(categoryId = categoryId)
        val expense2 = TestDataBuilders.createTestExpenseEntity(categoryId = categoryId)
        val expense3 = TestDataBuilders.createTestExpenseEntity(categoryId = categoryId)

        dao.insert(expense1)
        dao.insert(expense2)
        dao.insert(expense3)

        // Act
        val count = dao.getExpenseCountByCategory(categoryId)

        // Assert
        assertEquals(3, count)
    }

    // Search Tests

    @Test
    fun searchExpenses_findsMatchingDescriptions() = runTest {
        // Arrange
        val expense1 = TestDataBuilders.createTestExpenseEntity(description = "Grocery shopping")
        val expense2 = TestDataBuilders.createTestExpenseEntity(description = "Restaurant dinner")
        val expense3 = TestDataBuilders.createTestExpenseEntity(description = "Shopping mall")

        dao.insert(expense1)
        dao.insert(expense2)
        dao.insert(expense3)

        // Act & Assert
        dao.searchExpenses("shop").test {
            val expenses = awaitItem()
            assertEquals(2, expenses.size)
            assertTrue(expenses.any { it.description.contains("shopping", ignoreCase = true) })
            assertTrue(expenses.any { it.description.contains("Shopping", ignoreCase = true) })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchExpenses_caseInsensitive() = runTest {
        // Arrange
        val expense = TestDataBuilders.createTestExpenseEntity(description = "UPPERCASE TEXT")
        dao.insert(expense)

        // Act & Assert
        dao.searchExpenses("uppercase").test {
            val expenses = awaitItem()
            assertEquals(1, expenses.size)
            assertEquals("UPPERCASE TEXT", expenses[0].description)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Join Tests

    @Test
    fun getExpensesByTag_returnsExpensesWithTag() = runTest {
        // Arrange
        val tag = TestDataBuilders.createTestTagEntity(name = "Business")
        val tagId = database.tagDao().insert(tag)

        val expense1 = TestDataBuilders.createTestExpenseEntity(description = "Business Lunch")
        val expense2 = TestDataBuilders.createTestExpenseEntity(description = "Personal Dinner")
        val expense3 = TestDataBuilders.createTestExpenseEntity(description = "Business Travel")

        val expenseId1 = dao.insert(expense1)
        val expenseId2 = dao.insert(expense2)
        val expenseId3 = dao.insert(expense3)

        // Tag expense1 and expense3 with "Business"
        val transactionTagDao = database.transactionTagDao()
        transactionTagDao.insert(TestDataBuilders.createTestTransactionTagEntity(
            transactionId = expenseId1,
            tagId = tagId,
            transactionType = "EXPENSE"
        ))
        transactionTagDao.insert(TestDataBuilders.createTestTransactionTagEntity(
            transactionId = expenseId3,
            tagId = tagId,
            transactionType = "EXPENSE"
        ))

        // Act & Assert
        dao.getExpensesByTag(tagId).test {
            val expenses = awaitItem()
            assertEquals(2, expenses.size)
            assertTrue(expenses.any { it.id == expenseId1 })
            assertTrue(expenses.any { it.id == expenseId3 })
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Foreign Key Tests

    @Test
    fun deleteCategory_setsExpenseCategoryIdToNull() = runTest {
        // Arrange
        val category = TestDataBuilders.createTestCategoryEntity(name = "Food")
        val categoryId = categoryDao.insert(category)

        val expense = TestDataBuilders.createTestExpenseEntity(categoryId = categoryId)
        val expenseId = dao.insert(expense)

        // Act
        categoryDao.getCategoryById(categoryId).test {
            val retrievedCategory = awaitItem()!!
            categoryDao.delete(retrievedCategory)

            // Assert - expense should still exist but with null categoryId (SET_NULL)
            cancelAndIgnoreRemainingEvents()
        }

        dao.getExpenseById(expenseId).test {
            val retrievedExpense = awaitItem()
            assertNull("Category ID should be null after category deletion", retrievedExpense?.categoryId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Flow Reactivity Tests

    @Test
    fun getAllExpenses_emitsUpdatesOnInsert() = runTest {
        // Act & Assert
        dao.getAllExpenses().test {
            // Initial emission (empty)
            assertEquals(0, awaitItem().size)

            // Insert expense
            dao.insert(TestDataBuilders.createTestExpenseEntity())

            // Verify Flow emits updated list
            assertEquals(1, awaitItem().size)

            // Insert another
            dao.insert(TestDataBuilders.createTestExpenseEntity())
            assertEquals(2, awaitItem().size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllExpenses_emitsUpdatesOnDelete() = runTest {
        // Arrange
        val expense1 = TestDataBuilders.createTestExpenseEntity(description = "First")
        val expense2 = TestDataBuilders.createTestExpenseEntity(description = "Second")
        dao.insert(expense1)
        dao.insert(expense2)

        // Act & Assert
        dao.getAllExpenses().test {
            // Initial emission (2 expenses)
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
    fun getExpenseById_emitsUpdatesOnUpdate() = runTest {
        // Arrange
        val expense = TestDataBuilders.createTestExpenseEntity(description = "Original")
        val id = dao.insert(expense)

        // Act & Assert
        dao.getExpenseById(id).test {
            val original = awaitItem()!!
            assertEquals("Original", original.description)

            // Update
            val updated = original.copy(
                description = "Updated",
                modifiedAt = System.currentTimeMillis()
            )
            dao.update(updated)

            // Verify Flow emits updated expense
            val afterUpdate = awaitItem()
            assertEquals("Updated", afterUpdate?.description)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
