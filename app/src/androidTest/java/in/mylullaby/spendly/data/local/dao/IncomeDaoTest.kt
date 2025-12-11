package `in`.mylullaby.spendly.data.local.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import `in`.mylullaby.spendly.data.local.SpendlyDatabase
import `in`.mylullaby.spendly.utils.TestDataBuilders
import `in`.mylullaby.spendly.utils.createTestDatabase
import `in`.mylullaby.spendly.utils.daysAgo
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
 * Instrumented tests for IncomeDao.
 *
 * Tests all CRUD operations, complex queries, aggregations, refund tracking, and Flow reactivity.
 * Validates amount precision (paise stored as Long) and source-based filtering.
 *
 * Pattern follows CurrencyUtilsTest: methodName_inputCondition_expectedResult
 */
@RunWith(AndroidJUnit4::class)
class IncomeDaoTest {

    private lateinit var database: SpendlyDatabase
    private lateinit var dao: IncomeDao
    private lateinit var expenseDao: ExpenseDao

    @Before
    fun setUp() {
        database = createTestDatabase()
        dao = database.incomeDao()
        expenseDao = database.expenseDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // CRUD Operations Tests

    @Test
    fun insertIncome_withValidData_returnsInsertedId() = runTest {
        // Arrange
        val income = TestDataBuilders.createTestIncomeEntity()

        // Act
        val id = dao.insert(income)

        // Assert
        assertTrue("Inserted ID should be positive", id > 0)
        dao.getIncomeById(id).test {
            val retrieved = awaitItem()
            assertEquals(income.amount, retrieved?.amount)
            assertEquals(income.description, retrieved?.description)
            assertEquals(income.source, retrieved?.source)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertIncome_withPaiseAmount_exactMatch() = runTest {
        // Test ZERO tolerance for precision loss
        val testAmounts = listOf(1L, 50L, 100L, 12345L, 123456789L)

        testAmounts.forEach { amount ->
            // Arrange
            val income = TestDataBuilders.createTestIncomeEntity(
                amount = amount,
                description = "Test amount $amount"
            )

            // Act
            val id = dao.insert(income)

            // Assert
            dao.getIncomeById(id).test {
                val retrieved = awaitItem()
                assertEquals("Exact match for $amount paise", amount, retrieved?.amount)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun updateIncome_changesData_andModifiedTimestamp() = runTest {
        // Arrange
        val originalIncome = TestDataBuilders.createTestIncomeEntity(
            description = "Original",
            amount = 10000L
        )
        val id = dao.insert(originalIncome)

        dao.getIncomeById(id).test {
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
    fun deleteIncome_removesFromDatabase() = runTest {
        // Arrange
        val income = TestDataBuilders.createTestIncomeEntity()
        val id = dao.insert(income)

        // Act
        dao.getIncomeById(id).test {
            val retrieved = awaitItem()!!
            dao.delete(retrieved)

            // Assert
            val afterDelete = awaitItem()
            assertNull("Income should be null after deletion", afterDelete)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Query Tests

    @Test
    fun getAllIncome_orderedByDateDesc() = runTest {
        // Arrange
        val now = System.currentTimeMillis()
        val income1 = TestDataBuilders.createTestIncomeEntity(
            description = "Oldest",
            date = now.daysAgo(3)
        )
        val income2 = TestDataBuilders.createTestIncomeEntity(
            description = "Middle",
            date = now.daysAgo(2)
        )
        val income3 = TestDataBuilders.createTestIncomeEntity(
            description = "Newest",
            date = now.daysAgo(1)
        )

        dao.insert(income1)
        dao.insert(income2)
        dao.insert(income3)

        // Act & Assert
        dao.getAllIncome().test {
            val incomes = awaitItem()
            assertEquals(3, incomes.size)
            // Newest first
            assertEquals("Newest", incomes[0].description)
            assertEquals("Middle", incomes[1].description)
            assertEquals("Oldest", incomes[2].description)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getIncomeById_returnsCorrectIncome() = runTest {
        // Arrange
        val income1 = TestDataBuilders.createTestIncomeEntity(description = "First")
        val income2 = TestDataBuilders.createTestIncomeEntity(description = "Second")
        val id1 = dao.insert(income1)
        val id2 = dao.insert(income2)

        // Act & Assert
        dao.getIncomeById(id2).test {
            val retrieved = awaitItem()
            assertEquals("Second", retrieved?.description)
            assertEquals(id2, retrieved?.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getIncomeById_withNonexistentId_returnsNull() = runTest {
        // Act & Assert
        dao.getIncomeById(999L).test {
            val retrieved = awaitItem()
            assertNull("Should return null for non-existent ID", retrieved)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getIncomeByDateRange_returnsOnlyWithinRange() = runTest {
        // Arrange
        val now = System.currentTimeMillis()
        val startDate = now.daysAgo(7)
        val endDate = now.daysAgo(1)

        val incomeBeforeRange = TestDataBuilders.createTestIncomeEntity(
            description = "Before",
            date = now.daysAgo(10)
        )
        val incomeInRange1 = TestDataBuilders.createTestIncomeEntity(
            description = "In Range 1",
            date = now.daysAgo(5)
        )
        val incomeInRange2 = TestDataBuilders.createTestIncomeEntity(
            description = "In Range 2",
            date = now.daysAgo(3)
        )
        val incomeAfterRange = TestDataBuilders.createTestIncomeEntity(
            description = "After",
            date = now
        )

        dao.insert(incomeBeforeRange)
        dao.insert(incomeInRange1)
        dao.insert(incomeInRange2)
        dao.insert(incomeAfterRange)

        // Act & Assert
        dao.getIncomeByDateRange(startDate, endDate).test {
            val incomes = awaitItem()
            assertEquals(2, incomes.size)
            assertTrue(incomes.any { it.description == "In Range 1" })
            assertTrue(incomes.any { it.description == "In Range 2" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getIncomeBySource_returnsOnlyMatchingSource() = runTest {
        // Arrange
        val salaryIncome1 = TestDataBuilders.createTestIncomeEntity(
            description = "Monthly Salary",
            source = "SALARY"
        )
        val salaryIncome2 = TestDataBuilders.createTestIncomeEntity(
            description = "Bonus",
            source = "SALARY"
        )
        val freelanceIncome = TestDataBuilders.createTestIncomeEntity(
            description = "Freelance Project",
            source = "FREELANCE"
        )

        dao.insert(salaryIncome1)
        dao.insert(salaryIncome2)
        dao.insert(freelanceIncome)

        // Act & Assert
        dao.getIncomeBySource("SALARY").test {
            val incomes = awaitItem()
            assertEquals(2, incomes.size)
            assertTrue(incomes.all { it.source == "SALARY" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getIncomeByLinkedExpense_returnsRefunds() = runTest {
        // Arrange
        val expense = TestDataBuilders.createTestExpenseEntity(description = "Original Purchase")
        val expenseId = expenseDao.insert(expense)

        val refund1 = TestDataBuilders.createTestIncomeEntity(
            description = "Partial Refund",
            amount = 5000L,
            linkedExpenseId = expenseId
        )
        val refund2 = TestDataBuilders.createTestIncomeEntity(
            description = "Additional Refund",
            amount = 2000L,
            linkedExpenseId = expenseId
        )
        val regularIncome = TestDataBuilders.createTestIncomeEntity(
            description = "Salary",
            linkedExpenseId = null
        )

        dao.insert(refund1)
        dao.insert(refund2)
        dao.insert(regularIncome)

        // Act & Assert
        dao.getIncomeByLinkedExpense(expenseId).test {
            val refunds = awaitItem()
            assertEquals(2, refunds.size)
            assertTrue(refunds.all { it.linkedExpenseId == expenseId })
            assertTrue(refunds.any { it.description == "Partial Refund" })
            assertTrue(refunds.any { it.description == "Additional Refund" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getRecentIncome_limitsResults() = runTest {
        // Arrange - insert 10 incomes
        repeat(10) { index ->
            val income = TestDataBuilders.createTestIncomeEntity(
                description = "Income $index",
                date = System.currentTimeMillis().daysAgo(index)
            )
            dao.insert(income)
        }

        // Act & Assert
        dao.getRecentIncome(limit = 5).test {
            val incomes = awaitItem()
            assertEquals(5, incomes.size)
            // Should get the 5 most recent (lowest daysAgo)
            assertEquals("Income 0", incomes[0].description)
            assertEquals("Income 4", incomes[4].description)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Aggregation Tests

    @Test
    fun getTotalIncomeByDateRange_returnsExactSum() = runTest {
        // Arrange
        val now = System.currentTimeMillis()
        val startDate = now.daysAgo(7)
        val endDate = now

        val income1 = TestDataBuilders.createTestIncomeEntity(
            amount = 12345L,
            date = now.daysAgo(5)
        )
        val income2 = TestDataBuilders.createTestIncomeEntity(
            amount = 67890L,
            date = now.daysAgo(3)
        )
        val income3 = TestDataBuilders.createTestIncomeEntity(
            amount = 1L,
            date = now.daysAgo(1)
        )

        dao.insert(income1)
        dao.insert(income2)
        dao.insert(income3)

        // Act & Assert
        dao.getTotalIncomeByDateRange(startDate, endDate).test {
            val total = awaitItem()
            assertEquals("Exact sum with ZERO precision loss", 80236L, total)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getTotalIncomeByDateRange_withNoIncome_returnsNull() = runTest {
        // Arrange
        val now = System.currentTimeMillis()
        val startDate = now.daysAgo(7)
        val endDate = now

        // Act & Assert
        dao.getTotalIncomeByDateRange(startDate, endDate).test {
            val total = awaitItem()
            assertNull("Should return null when no income", total)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getTotalIncomeBySource_returnsExactSourceSum() = runTest {
        // Arrange
        val now = System.currentTimeMillis()
        val startDate = now.daysAgo(7)
        val endDate = now

        val salaryIncome1 = TestDataBuilders.createTestIncomeEntity(
            amount = 10000L,
            source = "SALARY",
            date = now.daysAgo(5)
        )
        val salaryIncome2 = TestDataBuilders.createTestIncomeEntity(
            amount = 25000L,
            source = "SALARY",
            date = now.daysAgo(3)
        )
        val freelanceIncome = TestDataBuilders.createTestIncomeEntity(
            amount = 15000L,
            source = "FREELANCE",
            date = now.daysAgo(4)
        )

        dao.insert(salaryIncome1)
        dao.insert(salaryIncome2)
        dao.insert(freelanceIncome)

        // Act & Assert
        dao.getTotalIncomeBySource("SALARY", startDate, endDate).test {
            val total = awaitItem()
            assertEquals(35000L, total)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getIncomeBySourceGrouped_returnsCorrectSummaries() = runTest {
        // Arrange
        val now = System.currentTimeMillis()
        val startDate = now.daysAgo(7)
        val endDate = now

        // SALARY: 10000 + 20000 = 30000
        dao.insert(TestDataBuilders.createTestIncomeEntity(
            amount = 10000L,
            source = "SALARY",
            date = now.daysAgo(5)
        ))
        dao.insert(TestDataBuilders.createTestIncomeEntity(
            amount = 20000L,
            source = "SALARY",
            date = now.daysAgo(3)
        ))

        // FREELANCE: 50000
        dao.insert(TestDataBuilders.createTestIncomeEntity(
            amount = 50000L,
            source = "FREELANCE",
            date = now.daysAgo(4)
        ))

        // Act & Assert
        dao.getIncomeBySourceGrouped(startDate, endDate).test {
            val summaries = awaitItem()
            assertEquals(2, summaries.size)
            // Ordered by total DESC, so FREELANCE first
            assertEquals("FREELANCE", summaries[0].source)
            assertEquals(50000L, summaries[0].total)
            assertEquals("SALARY", summaries[1].source)
            assertEquals(30000L, summaries[1].total)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Search Tests

    @Test
    fun searchIncome_findsMatchingDescriptions() = runTest {
        // Arrange
        val income1 = TestDataBuilders.createTestIncomeEntity(description = "Monthly Salary")
        val income2 = TestDataBuilders.createTestIncomeEntity(description = "Freelance Project")
        val income3 = TestDataBuilders.createTestIncomeEntity(description = "Salary Bonus")

        dao.insert(income1)
        dao.insert(income2)
        dao.insert(income3)

        // Act & Assert
        dao.searchIncome("salary").test {
            val incomes = awaitItem()
            assertEquals(2, incomes.size)
            assertTrue(incomes.any { it.description.contains("Salary", ignoreCase = true) })
            assertTrue(incomes.any { it.description.contains("Bonus", ignoreCase = true) })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchIncome_caseInsensitive() = runTest {
        // Arrange
        val income = TestDataBuilders.createTestIncomeEntity(description = "UPPERCASE TEXT")
        dao.insert(income)

        // Act & Assert
        dao.searchIncome("uppercase").test {
            val incomes = awaitItem()
            assertEquals(1, incomes.size)
            assertEquals("UPPERCASE TEXT", incomes[0].description)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Join Tests

    @Test
    fun getIncomeByTag_returnsIncomesWithTag() = runTest {
        // Arrange
        val tag = TestDataBuilders.createTestTagEntity(name = "Business")
        val tagId = database.tagDao().insert(tag)

        val income1 = TestDataBuilders.createTestIncomeEntity(description = "Business Revenue")
        val income2 = TestDataBuilders.createTestIncomeEntity(description = "Personal Salary")
        val income3 = TestDataBuilders.createTestIncomeEntity(description = "Business Bonus")

        val incomeId1 = dao.insert(income1)
        val incomeId2 = dao.insert(income2)
        val incomeId3 = dao.insert(income3)

        // Tag income1 and income3 with "Business"
        val transactionTagDao = database.transactionTagDao()
        transactionTagDao.insert(TestDataBuilders.createTestTransactionTagEntity(
            transactionId = incomeId1,
            tagId = tagId,
            transactionType = "INCOME"
        ))
        transactionTagDao.insert(TestDataBuilders.createTestTransactionTagEntity(
            transactionId = incomeId3,
            tagId = tagId,
            transactionType = "INCOME"
        ))

        // Act & Assert
        dao.getIncomeByTag(tagId).test {
            val incomes = awaitItem()
            assertEquals(2, incomes.size)
            assertTrue(incomes.any { it.id == incomeId1 })
            assertTrue(incomes.any { it.id == incomeId3 })
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Foreign Key Tests

    @Test
    fun deleteExpense_setsIncomeLinkedExpenseIdToNull() = runTest {
        // Arrange
        val expense = TestDataBuilders.createTestExpenseEntity(description = "Original Purchase")
        val expenseId = expenseDao.insert(expense)

        val refund = TestDataBuilders.createTestIncomeEntity(
            description = "Refund",
            linkedExpenseId = expenseId
        )
        val incomeId = dao.insert(refund)

        // Verify income has linkedExpenseId initially
        dao.getIncomeById(incomeId).test {
            assertEquals(expenseId, awaitItem()?.linkedExpenseId)
            cancelAndIgnoreRemainingEvents()
        }

        // Act - Delete expense
        expenseDao.getExpenseById(expenseId).test {
            val retrievedExpense = awaitItem()!!
            expenseDao.delete(retrievedExpense)
            cancelAndIgnoreRemainingEvents()
        }

        // Assert - Income should still exist but with null linkedExpenseId (SET_NULL)
        dao.getIncomeById(incomeId).test {
            val retrievedIncome = awaitItem()
            assertNull("linkedExpenseId should be null after expense deletion", retrievedIncome?.linkedExpenseId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Flow Reactivity Tests

    @Test
    fun getAllIncome_emitsUpdatesOnInsert() = runTest {
        // Act & Assert
        dao.getAllIncome().test {
            // Initial emission (empty)
            assertEquals(0, awaitItem().size)

            // Insert income
            dao.insert(TestDataBuilders.createTestIncomeEntity())

            // Verify Flow emits updated list
            assertEquals(1, awaitItem().size)

            // Insert another
            dao.insert(TestDataBuilders.createTestIncomeEntity())
            assertEquals(2, awaitItem().size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllIncome_emitsUpdatesOnDelete() = runTest {
        // Arrange
        val income1 = TestDataBuilders.createTestIncomeEntity(description = "First")
        val income2 = TestDataBuilders.createTestIncomeEntity(description = "Second")
        dao.insert(income1)
        dao.insert(income2)

        // Act & Assert
        dao.getAllIncome().test {
            // Initial emission (2 incomes)
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
    fun getIncomeById_emitsUpdatesOnUpdate() = runTest {
        // Arrange
        val income = TestDataBuilders.createTestIncomeEntity(description = "Original")
        val id = dao.insert(income)

        // Act & Assert
        dao.getIncomeById(id).test {
            val original = awaitItem()!!
            assertEquals("Original", original.description)

            // Update
            val updated = original.copy(
                description = "Updated",
                modifiedAt = System.currentTimeMillis()
            )
            dao.update(updated)

            // Verify Flow emits updated income
            val afterUpdate = awaitItem()
            assertEquals("Updated", afterUpdate?.description)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
