package `in`.mylullaby.spendly.data.local.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import `in`.mylullaby.spendly.data.local.SpendlyDatabase
import `in`.mylullaby.spendly.utils.TestDataBuilders
import `in`.mylullaby.spendly.utils.createTestDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for ReceiptDao.
 *
 * Tests CRUD operations, one-to-many relationship with expenses, CASCADE delete,
 * file size validation, and Flow reactivity.
 *
 * Pattern follows CurrencyUtilsTest: methodName_inputCondition_expectedResult
 */
@RunWith(AndroidJUnit4::class)
class ReceiptDaoTest {

    private lateinit var database: SpendlyDatabase
    private lateinit var dao: ReceiptDao
    private lateinit var expenseDao: ExpenseDao

    @Before
    fun setUp() {
        database = createTestDatabase()
        dao = database.receiptDao()
        expenseDao = database.expenseDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // CRUD Operations Tests

    @Test
    fun insertReceipt_withValidData_returnsInsertedId() = runTest {
        // Arrange
        val expense = TestDataBuilders.createTestExpenseEntity()
        val expenseId = expenseDao.insert(expense)

        val receipt = TestDataBuilders.createTestReceiptEntity(expenseId = expenseId)

        // Act
        val id = dao.insert(receipt)

        // Assert
        assertTrue("Inserted ID should be positive", id > 0)
        dao.getReceiptById(id).test {
            val retrieved = awaitItem()
            assertEquals(expenseId, retrieved?.expenseId)
            assertEquals(receipt.filePath, retrieved?.filePath)
            assertEquals(receipt.fileType, retrieved?.fileType)
            assertEquals(receipt.fileSizeBytes, retrieved?.fileSizeBytes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertAll_withMultipleReceipts_insertsAllSuccessfully() = runTest {
        // Arrange
        val expense = TestDataBuilders.createTestExpenseEntity()
        val expenseId = expenseDao.insert(expense)

        val receipts = listOf(
            TestDataBuilders.createTestReceiptEntity(
                expenseId = expenseId,
                filePath = "/path/to/receipt1.jpg"
            ),
            TestDataBuilders.createTestReceiptEntity(
                expenseId = expenseId,
                filePath = "/path/to/receipt2.png"
            ),
            TestDataBuilders.createTestReceiptEntity(
                expenseId = expenseId,
                filePath = "/path/to/receipt3.pdf"
            )
        )

        // Act
        dao.insertAll(receipts)

        // Assert
        dao.getReceiptsByExpense(expenseId).test {
            val retrieved = awaitItem()
            assertEquals(3, retrieved.size)
            assertTrue(retrieved.any { it.filePath.contains("receipt1") })
            assertTrue(retrieved.any { it.filePath.contains("receipt2") })
            assertTrue(retrieved.any { it.filePath.contains("receipt3") })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateReceipt_changesData() = runTest {
        // Arrange
        val expense = TestDataBuilders.createTestExpenseEntity()
        val expenseId = expenseDao.insert(expense)

        val receipt = TestDataBuilders.createTestReceiptEntity(
            expenseId = expenseId,
            compressed = false
        )
        val id = dao.insert(receipt)

        // Act
        dao.getReceiptById(id).test {
            val inserted = awaitItem()!!
            val updated = inserted.copy(
                filePath = "/new/path/receipt.jpg",
                compressed = true
            )
            dao.update(updated)

            // Assert
            val retrieved = awaitItem()
            assertEquals("/new/path/receipt.jpg", retrieved?.filePath)
            assertTrue("Receipt should be marked as compressed", retrieved?.compressed == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteReceipt_removesFromDatabase() = runTest {
        // Arrange
        val expense = TestDataBuilders.createTestExpenseEntity()
        val expenseId = expenseDao.insert(expense)

        val receipt = TestDataBuilders.createTestReceiptEntity(expenseId = expenseId)
        val id = dao.insert(receipt)

        // Act
        dao.getReceiptById(id).test {
            val retrieved = awaitItem()!!
            dao.delete(retrieved)

            // Assert
            val afterDelete = awaitItem()
            assertNull("Receipt should be null after deletion", afterDelete)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteById_removesFromDatabase() = runTest {
        // Arrange
        val expense = TestDataBuilders.createTestExpenseEntity()
        val expenseId = expenseDao.insert(expense)

        val receipt = TestDataBuilders.createTestReceiptEntity(expenseId = expenseId)
        val id = dao.insert(receipt)

        // Act
        dao.deleteById(id)

        // Assert
        dao.getReceiptById(id).test {
            val afterDelete = awaitItem()
            assertNull("Receipt should be null after deletion by ID", afterDelete)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Query Tests

    @Test
    fun getReceiptsByExpense_orderedByCreatedAtAsc() = runTest {
        // Arrange
        val expense = TestDataBuilders.createTestExpenseEntity()
        val expenseId = expenseDao.insert(expense)

        val now = System.currentTimeMillis()
        val receipt1 = TestDataBuilders.createTestReceiptEntity(
            expenseId = expenseId,
            filePath = "/path/to/first.jpg",
            createdAt = now - 1000
        )
        val receipt2 = TestDataBuilders.createTestReceiptEntity(
            expenseId = expenseId,
            filePath = "/path/to/second.jpg",
            createdAt = now
        )
        val receipt3 = TestDataBuilders.createTestReceiptEntity(
            expenseId = expenseId,
            filePath = "/path/to/third.jpg",
            createdAt = now + 1000
        )

        dao.insert(receipt3)
        dao.insert(receipt1)
        dao.insert(receipt2)

        // Act & Assert
        dao.getReceiptsByExpense(expenseId).test {
            val receipts = awaitItem()
            assertEquals(3, receipts.size)
            // Should be ordered by createdAt ASC (oldest first)
            assertTrue(receipts[0].filePath.contains("first"))
            assertTrue(receipts[1].filePath.contains("second"))
            assertTrue(receipts[2].filePath.contains("third"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getReceiptsByExpense_withMultipleExpenses_returnsOnlyForSpecificExpense() = runTest {
        // Arrange
        val expense1 = TestDataBuilders.createTestExpenseEntity(description = "First")
        val expense2 = TestDataBuilders.createTestExpenseEntity(description = "Second")
        val expenseId1 = expenseDao.insert(expense1)
        val expenseId2 = expenseDao.insert(expense2)

        dao.insert(TestDataBuilders.createTestReceiptEntity(
            expenseId = expenseId1,
            filePath = "/path/to/expense1_receipt1.jpg"
        ))
        dao.insert(TestDataBuilders.createTestReceiptEntity(
            expenseId = expenseId1,
            filePath = "/path/to/expense1_receipt2.jpg"
        ))
        dao.insert(TestDataBuilders.createTestReceiptEntity(
            expenseId = expenseId2,
            filePath = "/path/to/expense2_receipt1.jpg"
        ))

        // Act & Assert
        dao.getReceiptsByExpense(expenseId1).test {
            val receipts = awaitItem()
            assertEquals(2, receipts.size)
            assertTrue(receipts.all { it.expenseId == expenseId1 })
            assertTrue(receipts.any { it.filePath.contains("expense1_receipt1") })
            assertTrue(receipts.any { it.filePath.contains("expense1_receipt2") })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getReceiptById_returnsCorrectReceipt() = runTest {
        // Arrange
        val expense = TestDataBuilders.createTestExpenseEntity()
        val expenseId = expenseDao.insert(expense)

        val receipt1 = TestDataBuilders.createTestReceiptEntity(
            expenseId = expenseId,
            filePath = "/path/to/first.jpg"
        )
        val receipt2 = TestDataBuilders.createTestReceiptEntity(
            expenseId = expenseId,
            filePath = "/path/to/second.jpg"
        )
        val id1 = dao.insert(receipt1)
        val id2 = dao.insert(receipt2)

        // Act & Assert
        dao.getReceiptById(id2).test {
            val retrieved = awaitItem()
            assertEquals(id2, retrieved?.id)
            assertTrue(retrieved?.filePath?.contains("second") == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getReceiptById_withNonexistentId_returnsNull() = runTest {
        // Act & Assert
        dao.getReceiptById(999L).test {
            val retrieved = awaitItem()
            assertNull("Should return null for non-existent ID", retrieved)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Aggregation Tests

    @Test
    fun getReceiptCount_returnsCorrectCount() = runTest {
        // Arrange
        val expense = TestDataBuilders.createTestExpenseEntity()
        val expenseId = expenseDao.insert(expense)

        dao.insert(TestDataBuilders.createTestReceiptEntity(expenseId = expenseId))
        dao.insert(TestDataBuilders.createTestReceiptEntity(expenseId = expenseId))
        dao.insert(TestDataBuilders.createTestReceiptEntity(expenseId = expenseId))

        // Act & Assert
        dao.getReceiptCount(expenseId).test {
            assertEquals(3, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getTotalSizeByExpense_returnsCorrectSum() = runTest {
        // Arrange
        val expense = TestDataBuilders.createTestExpenseEntity()
        val expenseId = expenseDao.insert(expense)

        dao.insert(TestDataBuilders.createTestReceiptEntity(
            expenseId = expenseId,
            fileSizeBytes = 1_000_000L // 1MB
        ))
        dao.insert(TestDataBuilders.createTestReceiptEntity(
            expenseId = expenseId,
            fileSizeBytes = 2_500_000L // 2.5MB
        ))
        dao.insert(TestDataBuilders.createTestReceiptEntity(
            expenseId = expenseId,
            fileSizeBytes = 500_000L // 0.5MB
        ))

        // Act
        val totalSize = dao.getTotalSizeByExpense(expenseId)

        // Assert
        assertEquals(4_000_000L, totalSize) // 4MB total
    }

    @Test
    fun getTotalSizeByExpense_withNoReceipts_returnsNull() = runTest {
        // Arrange
        val expense = TestDataBuilders.createTestExpenseEntity()
        val expenseId = expenseDao.insert(expense)

        // Act
        val totalSize = dao.getTotalSizeByExpense(expenseId)

        // Assert
        assertNull("Should return null when no receipts", totalSize)
    }

    // Foreign Key Tests

    @Test
    fun deleteExpense_cascadesDeleteToReceipts() = runTest {
        // Arrange
        val expense = TestDataBuilders.createTestExpenseEntity()
        val expenseId = expenseDao.insert(expense)

        val receipt1 = TestDataBuilders.createTestReceiptEntity(expenseId = expenseId)
        val receipt2 = TestDataBuilders.createTestReceiptEntity(expenseId = expenseId)
        dao.insert(receipt1)
        dao.insert(receipt2)

        // Verify receipts exist
        dao.getReceiptsByExpense(expenseId).test {
            assertEquals(2, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }

        // Act - Delete expense
        expenseDao.getExpenseById(expenseId).test {
            val retrievedExpense = awaitItem()!!
            expenseDao.delete(retrievedExpense)
            cancelAndIgnoreRemainingEvents()
        }

        // Assert - Receipts should be CASCADE deleted
        dao.getReceiptsByExpense(expenseId).test {
            val receipts = awaitItem()
            assertEquals(0, receipts.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Flow Reactivity Tests

    @Test
    fun getReceiptsByExpense_emitsUpdatesOnInsert() = runTest {
        // Arrange
        val expense = TestDataBuilders.createTestExpenseEntity()
        val expenseId = expenseDao.insert(expense)

        // Act & Assert
        dao.getReceiptsByExpense(expenseId).test {
            // Initial emission (empty)
            assertEquals(0, awaitItem().size)

            // Insert receipt
            dao.insert(TestDataBuilders.createTestReceiptEntity(expenseId = expenseId))

            // Verify Flow emits updated list
            assertEquals(1, awaitItem().size)

            // Insert another
            dao.insert(TestDataBuilders.createTestReceiptEntity(expenseId = expenseId))
            assertEquals(2, awaitItem().size)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
