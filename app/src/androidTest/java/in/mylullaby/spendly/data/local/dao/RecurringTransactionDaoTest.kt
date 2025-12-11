package `in`.mylullaby.spendly.data.local.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import `in`.mylullaby.spendly.data.local.SpendlyDatabase
import `in`.mylullaby.spendly.utils.TestDataBuilders
import `in`.mylullaby.spendly.utils.createTestDatabase
import `in`.mylullaby.spendly.utils.daysAgo
import `in`.mylullaby.spendly.utils.daysFromNow
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
 * Instrumented tests for RecurringTransactionDao.
 *
 * Tests CRUD operations, frequency handling, due date queries, paise precision,
 * and Flow reactivity.
 *
 * Pattern follows CurrencyUtilsTest: methodName_inputCondition_expectedResult
 */
@RunWith(AndroidJUnit4::class)
class RecurringTransactionDaoTest {

    private lateinit var database: SpendlyDatabase
    private lateinit var dao: RecurringTransactionDao
    private lateinit var categoryDao: CategoryDao

    @Before
    fun setUp() {
        database = createTestDatabase()
        dao = database.recurringTransactionDao()
        categoryDao = database.categoryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // CRUD Operations Tests

    @Test
    fun insertRecurringTransaction_withValidData_returnsInsertedId() = runTest {
        // Arrange
        val category = TestDataBuilders.createTestCategoryEntity(name = "Rent-${System.currentTimeMillis()}")
        val categoryId = categoryDao.insert(category)

        val recurring = TestDataBuilders.createTestRecurringTransactionEntity(
            categoryId = categoryId,
            description = "Monthly Rent",
            frequency = "MONTHLY"
        )

        // Act
        val id = dao.insert(recurring)

        // Assert
        assertTrue("Inserted ID should be positive", id > 0)
        dao.getRecurringTransactionById(id).test {
            val retrieved = awaitItem()
            assertEquals("Monthly Rent", retrieved?.description)
            assertEquals("MONTHLY", retrieved?.frequency)
            assertEquals(categoryId, retrieved?.categoryId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertRecurringTransaction_withPaiseAmount_exactMatch() = runTest {
        // Test ZERO tolerance for precision loss
        val testAmounts = listOf(1L, 50L, 100L, 12345L, 123456789L)

        val category = TestDataBuilders.createTestCategoryEntity(name = "Test-${System.currentTimeMillis()}")
        val categoryId = categoryDao.insert(category)

        testAmounts.forEach { amount ->
            // Arrange
            val recurring = TestDataBuilders.createTestRecurringTransactionEntity(
                categoryId = categoryId,
                amount = amount,
                description = "Test amount $amount"
            )

            // Act
            val id = dao.insert(recurring)

            // Assert
            dao.getRecurringTransactionById(id).test {
                val retrieved = awaitItem()
                assertEquals("Exact match for $amount paise", amount, retrieved?.amount)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun updateRecurringTransaction_changesData_andModifiedTimestamp() = runTest {
        // Arrange
        val category = TestDataBuilders.createTestCategoryEntity(name = "Rent-${System.currentTimeMillis()}")
        val categoryId = categoryDao.insert(category)

        val recurring = TestDataBuilders.createTestRecurringTransactionEntity(
            categoryId = categoryId,
            description = "Original",
            amount = 10000L
        )
        val id = dao.insert(recurring)

        dao.getRecurringTransactionById(id).test {
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
    fun deleteRecurringTransaction_removesFromDatabase() = runTest {
        // Arrange
        val category = TestDataBuilders.createTestCategoryEntity(name = "Rent-${System.currentTimeMillis()}")
        val categoryId = categoryDao.insert(category)

        val recurring = TestDataBuilders.createTestRecurringTransactionEntity(categoryId = categoryId)
        val id = dao.insert(recurring)

        // Act
        dao.getRecurringTransactionById(id).test {
            val retrieved = awaitItem()!!
            dao.delete(retrieved)

            // Assert
            val afterDelete = awaitItem()
            assertNull("Recurring transaction should be null after deletion", afterDelete)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Query Tests

    @Test
    fun getAllRecurringTransactions_orderedByNextDateAsc() = runTest {
        // Arrange
        val category = TestDataBuilders.createTestCategoryEntity(name = "Test-${System.currentTimeMillis()}")
        val categoryId = categoryDao.insert(category)

        val now = System.currentTimeMillis()
        val recurring1 = TestDataBuilders.createTestRecurringTransactionEntity(
            categoryId = categoryId,
            description = "Furthest",
            nextDate = now.daysFromNow(10)
        )
        val recurring2 = TestDataBuilders.createTestRecurringTransactionEntity(
            categoryId = categoryId,
            description = "Middle",
            nextDate = now.daysFromNow(5)
        )
        val recurring3 = TestDataBuilders.createTestRecurringTransactionEntity(
            categoryId = categoryId,
            description = "Soonest",
            nextDate = now.daysFromNow(1)
        )

        dao.insert(recurring1)
        dao.insert(recurring2)
        dao.insert(recurring3)

        // Act & Assert
        dao.getAllRecurringTransactions().test {
            val transactions = awaitItem()
            assertEquals(3, transactions.size)
            // Soonest first
            assertEquals("Soonest", transactions[0].description)
            assertEquals("Middle", transactions[1].description)
            assertEquals("Furthest", transactions[2].description)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getRecurringTransactionById_returnsCorrectTransaction() = runTest {
        // Arrange
        val category = TestDataBuilders.createTestCategoryEntity(name = "Test-${System.currentTimeMillis()}")
        val categoryId = categoryDao.insert(category)

        val recurring1 = TestDataBuilders.createTestRecurringTransactionEntity(
            categoryId = categoryId,
            description = "First"
        )
        val recurring2 = TestDataBuilders.createTestRecurringTransactionEntity(
            categoryId = categoryId,
            description = "Second"
        )
        val id1 = dao.insert(recurring1)
        val id2 = dao.insert(recurring2)

        // Act & Assert
        dao.getRecurringTransactionById(id2).test {
            val retrieved = awaitItem()
            assertEquals("Second", retrieved?.description)
            assertEquals(id2, retrieved?.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getRecurringTransactionsByType_returnsOnlyMatchingType() = runTest {
        // Arrange
        val category = TestDataBuilders.createTestCategoryEntity(name = "Test-${System.currentTimeMillis()}")
        val categoryId = categoryDao.insert(category)

        val expense1 = TestDataBuilders.createTestRecurringTransactionEntity(
            categoryId = categoryId,
            transactionType = "EXPENSE",
            description = "Expense 1"
        )
        val expense2 = TestDataBuilders.createTestRecurringTransactionEntity(
            categoryId = categoryId,
            transactionType = "EXPENSE",
            description = "Expense 2"
        )
        val income = TestDataBuilders.createTestRecurringTransactionEntity(
            categoryId = categoryId,
            transactionType = "INCOME",
            description = "Income 1"
        )

        dao.insert(expense1)
        dao.insert(expense2)
        dao.insert(income)

        // Act & Assert
        dao.getRecurringTransactionsByType("EXPENSE").test {
            val transactions = awaitItem()
            assertEquals(2, transactions.size)
            assertTrue(transactions.all { it.transactionType == "EXPENSE" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getDueRecurringTransactions_returnsOnlyDue() = runTest {
        // Arrange
        val category = TestDataBuilders.createTestCategoryEntity(name = "Test-${System.currentTimeMillis()}")
        val categoryId = categoryDao.insert(category)

        val now = System.currentTimeMillis()

        // Past due
        val overdue = TestDataBuilders.createTestRecurringTransactionEntity(
            categoryId = categoryId,
            description = "Overdue",
            nextDate = now.daysAgo(5)
        )

        // Due today
        val dueToday = TestDataBuilders.createTestRecurringTransactionEntity(
            categoryId = categoryId,
            description = "Due Today",
            nextDate = now
        )

        // Future
        val future = TestDataBuilders.createTestRecurringTransactionEntity(
            categoryId = categoryId,
            description = "Future",
            nextDate = now.daysFromNow(5)
        )

        dao.insert(overdue)
        dao.insert(dueToday)
        dao.insert(future)

        // Act
        val dueTransactions = dao.getDueRecurringTransactions(now)

        // Assert
        assertEquals(2, dueTransactions.size)
        assertTrue(dueTransactions.any { it.description == "Overdue" })
        assertTrue(dueTransactions.any { it.description == "Due Today" })
    }

    @Test
    fun getRecurringTransactionsByCategory_returnsOnlyMatchingCategory() = runTest {
        // Arrange
        val timestamp = System.currentTimeMillis()
        val category1 = TestDataBuilders.createTestCategoryEntity(name = "Rent-$timestamp")
        val category2 = TestDataBuilders.createTestCategoryEntity(name = "Utilities-$timestamp")
        val categoryId1 = categoryDao.insert(category1)
        val categoryId2 = categoryDao.insert(category2)

        val rent1 = TestDataBuilders.createTestRecurringTransactionEntity(
            categoryId = categoryId1,
            description = "Monthly Rent"
        )
        val rent2 = TestDataBuilders.createTestRecurringTransactionEntity(
            categoryId = categoryId1,
            description = "Quarterly Rent"
        )
        val utilities = TestDataBuilders.createTestRecurringTransactionEntity(
            categoryId = categoryId2,
            description = "Electricity"
        )

        dao.insert(rent1)
        dao.insert(rent2)
        dao.insert(utilities)

        // Act & Assert
        dao.getRecurringTransactionsByCategory(categoryId1).test {
            val transactions = awaitItem()
            assertEquals(2, transactions.size)
            assertTrue(transactions.all { it.categoryId == categoryId1 })
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Aggregation Tests

    @Test
    fun getRecurringTransactionCountByCategory_returnsCorrectCount() = runBlocking {
        // Arrange
        val category = TestDataBuilders.createTestCategoryEntity(name = "Test-${System.currentTimeMillis()}")
        val categoryId = categoryDao.insert(category)

        dao.insert(TestDataBuilders.createTestRecurringTransactionEntity(categoryId = categoryId))
        dao.insert(TestDataBuilders.createTestRecurringTransactionEntity(categoryId = categoryId))
        dao.insert(TestDataBuilders.createTestRecurringTransactionEntity(categoryId = categoryId))

        // Act
        val count = dao.getRecurringTransactionCountByCategory(categoryId)

        // Assert
        assertEquals(3, count)
    }

    // Frequency Tests

    @Test
    fun insertRecurringTransaction_withDifferentFrequencies_success() = runTest {
        // Arrange
        val category = TestDataBuilders.createTestCategoryEntity(name = "Test-${System.currentTimeMillis()}")
        val categoryId = categoryDao.insert(category)

        val frequencies = listOf("DAILY", "WEEKLY", "MONTHLY")

        frequencies.forEach { frequency ->
            // Act
            val recurring = TestDataBuilders.createTestRecurringTransactionEntity(
                categoryId = categoryId,
                description = "$frequency Transaction",
                frequency = frequency
            )
            val id = dao.insert(recurring)

            // Assert
            dao.getRecurringTransactionById(id).test {
                val retrieved = awaitItem()
                assertEquals(frequency, retrieved?.frequency)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    // Foreign Key Tests

    @Test
    fun deleteCategory_setsRecurringTransactionCategoryIdToNull() = runTest {
        // Arrange
        val category = TestDataBuilders.createTestCategoryEntity(name = "Rent-${System.currentTimeMillis()}")
        val categoryId = categoryDao.insert(category)

        val recurring = TestDataBuilders.createTestRecurringTransactionEntity(
            categoryId = categoryId,
            description = "Monthly Rent"
        )
        val recurringId = dao.insert(recurring)

        // Verify recurring has category initially
        dao.getRecurringTransactionById(recurringId).test {
            assertEquals(categoryId, awaitItem()?.categoryId)
            cancelAndIgnoreRemainingEvents()
        }

        // Act - Delete category
        categoryDao.getCategoryById(categoryId).test {
            val retrievedCategory = awaitItem()!!
            categoryDao.delete(retrievedCategory)
            cancelAndIgnoreRemainingEvents()
        }

        // Assert - Recurring transaction should still exist but with null categoryId (SET_NULL)
        dao.getRecurringTransactionById(recurringId).test {
            val retrievedRecurring = awaitItem()
            assertNull("Category ID should be null after category deletion", retrievedRecurring?.categoryId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Flow Reactivity Tests

    @Test
    fun getAllRecurringTransactions_emitsUpdatesOnInsert() = runTest {
        // Arrange
        val category = TestDataBuilders.createTestCategoryEntity(name = "Test-${System.currentTimeMillis()}")
        val categoryId = categoryDao.insert(category)

        // Act & Assert
        dao.getAllRecurringTransactions().test {
            // Initial emission (empty)
            assertEquals(0, awaitItem().size)

            // Insert recurring transaction
            dao.insert(TestDataBuilders.createTestRecurringTransactionEntity(categoryId = categoryId))

            // Verify Flow emits updated list
            assertEquals(1, awaitItem().size)

            // Insert another
            dao.insert(TestDataBuilders.createTestRecurringTransactionEntity(categoryId = categoryId))
            assertEquals(2, awaitItem().size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getRecurringTransactionById_emitsUpdatesOnUpdate() = runTest {
        // Arrange
        val category = TestDataBuilders.createTestCategoryEntity(name = "Test-${System.currentTimeMillis()}")
        val categoryId = categoryDao.insert(category)

        val recurring = TestDataBuilders.createTestRecurringTransactionEntity(
            categoryId = categoryId,
            description = "Original"
        )
        val id = dao.insert(recurring)

        // Act & Assert
        dao.getRecurringTransactionById(id).test {
            val original = awaitItem()!!
            assertEquals("Original", original.description)

            // Update
            val updated = original.copy(
                description = "Updated",
                modifiedAt = System.currentTimeMillis()
            )
            dao.update(updated)

            // Verify Flow emits updated recurring transaction
            val afterUpdate = awaitItem()
            assertEquals("Updated", afterUpdate?.description)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
