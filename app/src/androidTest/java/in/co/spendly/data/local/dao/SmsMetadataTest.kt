package `in`.co.spendly.data.local.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import `in`.co.spendly.data.local.SpendlyDatabase
import `in`.co.spendly.utils.TestDataBuilders
import `in`.co.spendly.utils.createTestDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for SMS metadata fields in Expense and Income entities.
 *
 * Tests verify that SMS metadata (smsSourceId, smsBody, smsConfidence, smsTimestamp)
 * is properly persisted, retrieved, and preserved across CRUD operations.
 *
 * Pattern: methodName_inputCondition_expectedResult
 */
@RunWith(AndroidJUnit4::class)
class SmsMetadataTest {

    private lateinit var database: SpendlyDatabase
    private lateinit var expenseDao: ExpenseDao
    private lateinit var incomeDao: IncomeDao

    @Before
    fun setUp() {
        database = createTestDatabase()
        expenseDao = database.expenseDao()
        incomeDao = database.incomeDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ==== Expense SMS Metadata Tests ====

    @Test
    fun insertExpenseWithSmsMetadata_allFieldsPersisted() = runTest {
        // Arrange
        val testSmsBody = "Spent Rs.123.45 at Test Merchant on 14-Dec-2025"
        val testConfidence = 0.85f
        val testTimestamp = System.currentTimeMillis()
        val testSourceId = 999L

        val expense = TestDataBuilders.createTestExpenseEntity(
            amount = 12345L,
            description = "SMS Auto-detected",
            smsSourceId = testSourceId,
            smsBody = testSmsBody,
            smsConfidence = testConfidence,
            smsTimestamp = testTimestamp
        )

        // Act
        val id = expenseDao.insert(expense)

        // Assert
        expenseDao.getExpenseById(id).test {
            val retrieved = awaitItem()
            assertEquals("SMS source ID should match", testSourceId, retrieved?.smsSourceId)
            assertEquals("SMS body should match", testSmsBody, retrieved?.smsBody)
            assertEquals("SMS confidence should match", testConfidence, retrieved?.smsConfidence)
            assertEquals("SMS timestamp should match", testTimestamp, retrieved?.smsTimestamp)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateExpenseWithSmsMetadata_fieldsPreserved() = runTest {
        // Arrange - Create expense with SMS metadata
        val originalSmsBody = "Original SMS: Spent Rs.100.00"
        val originalConfidence = 0.90f
        val originalTimestamp = System.currentTimeMillis()
        val originalSourceId = 888L

        val expense = TestDataBuilders.createTestExpenseEntity(
            amount = 10000L,
            description = "Original",
            smsSourceId = originalSourceId,
            smsBody = originalSmsBody,
            smsConfidence = originalConfidence,
            smsTimestamp = originalTimestamp
        )
        val id = expenseDao.insert(expense)

        // Act - Update only amount and description, SMS fields should remain unchanged
        val updated = expense.copy(
            id = id,
            amount = 20000L,
            description = "Updated",
            smsSourceId = originalSourceId,  // Explicitly preserve
            smsBody = originalSmsBody,      // Explicitly preserve
            smsConfidence = originalConfidence,  // Explicitly preserve
            smsTimestamp = originalTimestamp     // Explicitly preserve
        )
        expenseDao.update(updated)

        // Assert - SMS metadata should be preserved
        expenseDao.getExpenseById(id).test {
            val retrieved = awaitItem()
            assertEquals("Amount should be updated", 20000L, retrieved?.amount)
            assertEquals("Description should be updated", "Updated", retrieved?.description)
            assertEquals(
                "SMS source ID should be preserved",
                originalSourceId,
                retrieved?.smsSourceId
            )
            assertEquals("SMS body should be preserved", originalSmsBody, retrieved?.smsBody)
            assertEquals(
                "SMS confidence should be preserved",
                originalConfidence,
                retrieved?.smsConfidence
            )
            assertEquals(
                "SMS timestamp should be preserved",
                originalTimestamp,
                retrieved?.smsTimestamp
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertExpenseWithoutSmsMetadata_nullFieldsStored() = runTest {
        // Arrange - Create normal expense without SMS metadata
        val expense = TestDataBuilders.createTestExpenseEntity(
            amount = 50000L,
            description = "Manual Entry"
            // SMS fields default to null
        )

        // Act
        val id = expenseDao.insert(expense)

        // Assert - All SMS fields should be null
        expenseDao.getExpenseById(id).test {
            val retrieved = awaitItem()
            assertEquals("Amount should match", 50000L, retrieved?.amount)
            assertEquals("Description should match", "Manual Entry", retrieved?.description)
            assertNull("SMS source ID should be null", retrieved?.smsSourceId)
            assertNull("SMS body should be null", retrieved?.smsBody)
            assertNull("SMS confidence should be null", retrieved?.smsConfidence)
            assertNull("SMS timestamp should be null", retrieved?.smsTimestamp)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllExpenses_includesSmsMetadata() = runTest {
        // Arrange - Insert mixed expenses (with and without SMS metadata)
        val smsExpense = TestDataBuilders.createTestExpenseEntity(
            amount = 12000L,
            description = "SMS Expense",
            smsSourceId = 111L,
            smsBody = "Test SMS",
            smsConfidence = 0.75f,
            smsTimestamp = System.currentTimeMillis()
        )
        val manualExpense = TestDataBuilders.createTestExpenseEntity(
            amount = 15000L,
            description = "Manual Expense"
        )

        expenseDao.insert(smsExpense)
        expenseDao.insert(manualExpense)

        // Act & Assert
        expenseDao.getAllExpenses().test {
            val expenses = awaitItem()
            assertEquals("Should have 2 expenses", 2, expenses.size)

            val smsResult = expenses.find { it.description == "SMS Expense" }
            val manualResult = expenses.find { it.description == "Manual Expense" }

            assertEquals("SMS expense should have source ID", 111L, smsResult?.smsSourceId)
            assertEquals("SMS expense should have body", "Test SMS", smsResult?.smsBody)
            assertNull("Manual expense should have null SMS fields", manualResult?.smsSourceId)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==== Income SMS Metadata Tests ====

    @Test
    fun insertIncomeWithSmsMetadata_allFieldsPersisted() = runTest {
        // Arrange
        val testSmsBody = "Received Rs.500.00 salary credit"
        val testConfidence = 0.92f
        val testTimestamp = System.currentTimeMillis()
        val testSourceId = 777L

        val income = TestDataBuilders.createTestIncomeEntity(
            amount = 50000L,
            description = "SMS Auto-detected Income",
            smsSourceId = testSourceId,
            smsBody = testSmsBody,
            smsConfidence = testConfidence,
            smsTimestamp = testTimestamp
        )

        // Act
        val id = incomeDao.insert(income)

        // Assert
        incomeDao.getIncomeById(id).test {
            val retrieved = awaitItem()
            assertEquals("SMS source ID should match", testSourceId, retrieved?.smsSourceId)
            assertEquals("SMS body should match", testSmsBody, retrieved?.smsBody)
            assertEquals("SMS confidence should match", testConfidence, retrieved?.smsConfidence)
            assertEquals("SMS timestamp should match", testTimestamp, retrieved?.smsTimestamp)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateIncomeWithSmsMetadata_fieldsPreserved() = runTest {
        // Arrange
        val originalSmsBody = "Original SMS: Received Rs.300.00"
        val originalConfidence = 0.88f
        val originalTimestamp = System.currentTimeMillis()
        val originalSourceId = 666L

        val income = TestDataBuilders.createTestIncomeEntity(
            amount = 30000L,
            description = "Original Income",
            smsSourceId = originalSourceId,
            smsBody = originalSmsBody,
            smsConfidence = originalConfidence,
            smsTimestamp = originalTimestamp
        )
        val id = incomeDao.insert(income)

        // Act - Update only amount, SMS fields should remain
        val updated = income.copy(
            id = id,
            amount = 40000L,
            smsSourceId = originalSourceId,
            smsBody = originalSmsBody,
            smsConfidence = originalConfidence,
            smsTimestamp = originalTimestamp
        )
        incomeDao.update(updated)

        // Assert
        incomeDao.getIncomeById(id).test {
            val retrieved = awaitItem()
            assertEquals("Amount should be updated", 40000L, retrieved?.amount)
            assertEquals(
                "SMS source ID should be preserved",
                originalSourceId,
                retrieved?.smsSourceId
            )
            assertEquals("SMS body should be preserved", originalSmsBody, retrieved?.smsBody)
            assertEquals(
                "SMS confidence should be preserved",
                originalConfidence,
                retrieved?.smsConfidence
            )
            assertEquals(
                "SMS timestamp should be preserved",
                originalTimestamp,
                retrieved?.smsTimestamp
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertIncomeWithoutSmsMetadata_nullFieldsStored() = runTest {
        // Arrange
        val income = TestDataBuilders.createTestIncomeEntity(
            amount = 60000L,
            description = "Manual Income Entry"
        )

        // Act
        val id = incomeDao.insert(income)

        // Assert
        incomeDao.getIncomeById(id).test {
            val retrieved = awaitItem()
            assertEquals("Amount should match", 60000L, retrieved?.amount)
            assertNull("SMS source ID should be null", retrieved?.smsSourceId)
            assertNull("SMS body should be null", retrieved?.smsBody)
            assertNull("SMS confidence should be null", retrieved?.smsConfidence)
            assertNull("SMS timestamp should be null", retrieved?.smsTimestamp)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllIncomes_includesSmsMetadata() = runTest {
        // Arrange
        val smsIncome = TestDataBuilders.createTestIncomeEntity(
            amount = 100000L,
            description = "SMS Income",
            smsSourceId = 555L,
            smsBody = "Test Income SMS",
            smsConfidence = 0.80f,
            smsTimestamp = System.currentTimeMillis()
        )
        val manualIncome = TestDataBuilders.createTestIncomeEntity(
            amount = 75000L,
            description = "Manual Income"
        )

        incomeDao.insert(smsIncome)
        incomeDao.insert(manualIncome)

        // Act & Assert
        incomeDao.getAllIncome().test {
            val incomes = awaitItem()
            assertEquals("Should have 2 incomes", 2, incomes.size)

            val smsResult = incomes.find { it.description == "SMS Income" }
            val manualResult = incomes.find { it.description == "Manual Income" }

            assertEquals("SMS income should have source ID", 555L, smsResult?.smsSourceId)
            assertEquals("SMS income should have body", "Test Income SMS", smsResult?.smsBody)
            assertNull("Manual income should have null SMS fields", manualResult?.smsSourceId)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==== Edge Cases ====

    @Test
    fun insertExpenseWithPartialSmsMetadata_allowsNull() = runTest {
        // Arrange - Only some SMS fields populated (body but no confidence/timestamp)
        val expense = TestDataBuilders.createTestExpenseEntity(
            amount = 5000L,
            description = "Partial SMS",
            smsBody = "Only body field"
            // smsSourceId, smsConfidence, smsTimestamp remain null
        )

        // Act
        val id = expenseDao.insert(expense)

        // Assert
        expenseDao.getExpenseById(id).test {
            val retrieved = awaitItem()
            assertNull("SMS source ID should be null", retrieved?.smsSourceId)
            assertEquals("SMS body should be present", "Only body field", retrieved?.smsBody)
            assertNull("SMS confidence should be null", retrieved?.smsConfidence)
            assertNull("SMS timestamp should be null", retrieved?.smsTimestamp)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertIncomeWithPartialSmsMetadata_allowsNull() = runTest {
        // Arrange - Only confidence field populated
        val income = TestDataBuilders.createTestIncomeEntity(
            amount = 25000L,
            description = "Partial Income SMS",
            smsConfidence = 0.65f
            // Other SMS fields remain null
        )

        // Act
        val id = incomeDao.insert(income)

        // Assert
        incomeDao.getIncomeById(id).test {
            val retrieved = awaitItem()
            assertNull("SMS source ID should be null", retrieved?.smsSourceId)
            assertNull("SMS body should be null", retrieved?.smsBody)
            assertEquals("SMS confidence should be present", 0.65f, retrieved?.smsConfidence)
            assertNull("SMS timestamp should be null", retrieved?.smsTimestamp)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
