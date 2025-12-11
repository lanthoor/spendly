package `in`.mylullaby.spendly.data.local.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import `in`.mylullaby.spendly.data.local.SpendlyDatabase
import `in`.mylullaby.spendly.utils.TestDataBuilders
import `in`.mylullaby.spendly.utils.createTestDatabase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for TransactionTagDao.
 *
 * Tests many-to-many relationship operations between transactions and tags,
 * composite key uniqueness, JOIN queries, and CASCADE delete behavior.
 *
 * Pattern follows CurrencyUtilsTest: methodName_inputCondition_expectedResult
 */
@RunWith(AndroidJUnit4::class)
class TransactionTagDaoTest {

    private lateinit var database: SpendlyDatabase
    private lateinit var dao: TransactionTagDao
    private lateinit var expenseDao: ExpenseDao
    private lateinit var incomeDao: IncomeDao
    private lateinit var tagDao: TagDao

    @Before
    fun setUp() {
        database = createTestDatabase()
        dao = database.transactionTagDao()
        expenseDao = database.expenseDao()
        incomeDao = database.incomeDao()
        tagDao = database.tagDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // CRUD Operations Tests

    @Test
    fun insertTransactionTag_withValidData_success() = runTest {
        // Arrange
        val expense = TestDataBuilders.createTestExpenseEntity()
        val expenseId = expenseDao.insert(expense)

        val tag = TestDataBuilders.createTestTagEntity(name = "Business")
        val tagId = tagDao.insert(tag)

        val transactionTag = TestDataBuilders.createTestTransactionTagEntity(
            transactionId = expenseId,
            tagId = tagId,
            transactionType = "EXPENSE"
        )

        // Act
        dao.insert(transactionTag)

        // Assert
        dao.getTagsForTransaction(expenseId, "EXPENSE").test {
            val tags = awaitItem()
            assertEquals(1, tags.size)
            assertEquals("Business", tags[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertAll_withMultipleTransactionTags_insertsAllSuccessfully() = runTest {
        // Arrange
        val expense = TestDataBuilders.createTestExpenseEntity()
        val expenseId = expenseDao.insert(expense)

        val tag1 = TestDataBuilders.createTestTagEntity(name = "Business")
        val tag2 = TestDataBuilders.createTestTagEntity(name = "Personal")
        val tag3 = TestDataBuilders.createTestTagEntity(name = "Important")
        val tagId1 = tagDao.insert(tag1)
        val tagId2 = tagDao.insert(tag2)
        val tagId3 = tagDao.insert(tag3)

        val transactionTags = listOf(
            TestDataBuilders.createTestTransactionTagEntity(expenseId, tagId1, "EXPENSE"),
            TestDataBuilders.createTestTransactionTagEntity(expenseId, tagId2, "EXPENSE"),
            TestDataBuilders.createTestTransactionTagEntity(expenseId, tagId3, "EXPENSE")
        )

        // Act
        dao.insertAll(transactionTags)

        // Assert
        dao.getTagsForTransaction(expenseId, "EXPENSE").test {
            val tags = awaitItem()
            assertEquals(3, tags.size)
            assertTrue(tags.any { it.name == "Business" })
            assertTrue(tags.any { it.name == "Important" })
            assertTrue(tags.any { it.name == "Personal" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertTransactionTag_withDuplicateCompositeKey_replaces() = runTest {
        // Arrange
        val expense = TestDataBuilders.createTestExpenseEntity()
        val expenseId = expenseDao.insert(expense)

        val tag = TestDataBuilders.createTestTagEntity(name = "Business")
        val tagId = tagDao.insert(tag)

        val transactionTag = TestDataBuilders.createTestTransactionTagEntity(
            transactionId = expenseId,
            tagId = tagId,
            transactionType = "EXPENSE"
        )

        // Insert first time
        dao.insert(transactionTag)

        // Act - Insert again (should replace due to OnConflictStrategy.REPLACE)
        dao.insert(transactionTag)

        // Assert - Should still have only 1 association
        dao.getTagsForTransaction(expenseId, "EXPENSE").test {
            val tags = awaitItem()
            assertEquals(1, tags.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteTransactionTag_removesAssociation() = runTest {
        // Arrange
        val expense = TestDataBuilders.createTestExpenseEntity()
        val expenseId = expenseDao.insert(expense)

        val tag = TestDataBuilders.createTestTagEntity(name = "Business")
        val tagId = tagDao.insert(tag)

        val transactionTag = TestDataBuilders.createTestTransactionTagEntity(
            transactionId = expenseId,
            tagId = tagId,
            transactionType = "EXPENSE"
        )
        dao.insert(transactionTag)

        // Act
        dao.delete(transactionTag)

        // Assert
        dao.getTagsForTransaction(expenseId, "EXPENSE").test {
            val tags = awaitItem()
            assertEquals(0, tags.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteAllTagsForTransaction_removesAllAssociations() = runTest {
        // Arrange
        val expense = TestDataBuilders.createTestExpenseEntity()
        val expenseId = expenseDao.insert(expense)

        val tag1 = TestDataBuilders.createTestTagEntity(name = "Business")
        val tag2 = TestDataBuilders.createTestTagEntity(name = "Personal")
        val tagId1 = tagDao.insert(tag1)
        val tagId2 = tagDao.insert(tag2)

        dao.insert(TestDataBuilders.createTestTransactionTagEntity(expenseId, tagId1, "EXPENSE"))
        dao.insert(TestDataBuilders.createTestTransactionTagEntity(expenseId, tagId2, "EXPENSE"))

        // Act
        dao.deleteAllTagsForTransaction(expenseId, "EXPENSE")

        // Assert
        dao.getTagsForTransaction(expenseId, "EXPENSE").test {
            val tags = awaitItem()
            assertEquals(0, tags.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Query Tests

    @Test
    fun getTagsForTransaction_returnsTagsOrderedAlphabetically() = runTest {
        // Arrange
        val expense = TestDataBuilders.createTestExpenseEntity()
        val expenseId = expenseDao.insert(expense)

        val tag1 = TestDataBuilders.createTestTagEntity(name = "Zebra")
        val tag2 = TestDataBuilders.createTestTagEntity(name = "Apple")
        val tag3 = TestDataBuilders.createTestTagEntity(name = "Mango")
        val tagId1 = tagDao.insert(tag1)
        val tagId2 = tagDao.insert(tag2)
        val tagId3 = tagDao.insert(tag3)

        dao.insert(TestDataBuilders.createTestTransactionTagEntity(expenseId, tagId1, "EXPENSE"))
        dao.insert(TestDataBuilders.createTestTransactionTagEntity(expenseId, tagId2, "EXPENSE"))
        dao.insert(TestDataBuilders.createTestTransactionTagEntity(expenseId, tagId3, "EXPENSE"))

        // Act & Assert
        dao.getTagsForTransaction(expenseId, "EXPENSE").test {
            val tags = awaitItem()
            assertEquals(3, tags.size)
            // Should be ordered alphabetically
            assertEquals("Apple", tags[0].name)
            assertEquals("Mango", tags[1].name)
            assertEquals("Zebra", tags[2].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getTagsForTransaction_withDifferentTransactionTypes_separatesExpenseAndIncome() = runTest {
        // Arrange
        val expense = TestDataBuilders.createTestExpenseEntity()
        val income = TestDataBuilders.createTestIncomeEntity()
        val expenseId = expenseDao.insert(expense)
        val incomeId = incomeDao.insert(income)

        val businessTag = TestDataBuilders.createTestTagEntity(name = "Business")
        val personalTag = TestDataBuilders.createTestTagEntity(name = "Personal")
        val businessTagId = tagDao.insert(businessTag)
        val personalTagId = tagDao.insert(personalTag)

        // Tag expense with Business
        dao.insert(TestDataBuilders.createTestTransactionTagEntity(expenseId, businessTagId, "EXPENSE"))

        // Tag income with Personal
        dao.insert(TestDataBuilders.createTestTransactionTagEntity(incomeId, personalTagId, "INCOME"))

        // Act & Assert
        dao.getTagsForTransaction(expenseId, "EXPENSE").test {
            val tags = awaitItem()
            assertEquals(1, tags.size)
            assertEquals("Business", tags[0].name)
            cancelAndIgnoreRemainingEvents()
        }

        dao.getTagsForTransaction(incomeId, "INCOME").test {
            val tags = awaitItem()
            assertEquals(1, tags.size)
            assertEquals("Personal", tags[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getTransactionsForTag_returnsAllAssociatedTransactions() = runTest {
        // Arrange
        val expense1 = TestDataBuilders.createTestExpenseEntity(description = "Expense 1")
        val expense2 = TestDataBuilders.createTestExpenseEntity(description = "Expense 2")
        val income = TestDataBuilders.createTestIncomeEntity(description = "Income 1")
        val expenseId1 = expenseDao.insert(expense1)
        val expenseId2 = expenseDao.insert(expense2)
        val incomeId = incomeDao.insert(income)

        val businessTag = TestDataBuilders.createTestTagEntity(name = "Business")
        val businessTagId = tagDao.insert(businessTag)

        // Tag all with Business
        dao.insert(TestDataBuilders.createTestTransactionTagEntity(expenseId1, businessTagId, "EXPENSE"))
        dao.insert(TestDataBuilders.createTestTransactionTagEntity(expenseId2, businessTagId, "EXPENSE"))
        dao.insert(TestDataBuilders.createTestTransactionTagEntity(incomeId, businessTagId, "INCOME"))

        // Act & Assert
        dao.getTransactionsForTag(businessTagId).test {
            val transactions = awaitItem()
            assertEquals(3, transactions.size)
            assertTrue(transactions.any { it.transactionId == expenseId1 && it.transactionType == "EXPENSE" })
            assertTrue(transactions.any { it.transactionId == expenseId2 && it.transactionType == "EXPENSE" })
            assertTrue(transactions.any { it.transactionId == incomeId && it.transactionType == "INCOME" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Aggregation Tests

    @Test
    fun getTransactionCountForTag_returnsCorrectCount() = runBlocking {
        // Arrange
        val expense1 = TestDataBuilders.createTestExpenseEntity()
        val expense2 = TestDataBuilders.createTestExpenseEntity()
        val expenseId1 = expenseDao.insert(expense1)
        val expenseId2 = expenseDao.insert(expense2)

        val businessTag = TestDataBuilders.createTestTagEntity(name = "Business")
        val businessTagId = tagDao.insert(businessTag)

        dao.insert(TestDataBuilders.createTestTransactionTagEntity(expenseId1, businessTagId, "EXPENSE"))
        dao.insert(TestDataBuilders.createTestTransactionTagEntity(expenseId2, businessTagId, "EXPENSE"))

        // Act
        val count = dao.getTransactionCountForTag(businessTagId)

        // Assert
        assertEquals(2, count)
    }

    // Foreign Key Tests
    // Note: No FK from TransactionTag to Expense/Income (they're in different tables)
    // So CASCADE delete doesn't apply. Manual cleanup would be needed in repository layer.

    @Test
    fun deleteTag_cascadesDeleteToTransactionTags() = runTest {
        // Arrange
        val expense = TestDataBuilders.createTestExpenseEntity()
        val expenseId = expenseDao.insert(expense)

        val tag = TestDataBuilders.createTestTagEntity(name = "Business")
        val tagId = tagDao.insert(tag)

        dao.insert(TestDataBuilders.createTestTransactionTagEntity(expenseId, tagId, "EXPENSE"))

        // Verify association exists
        val countBefore = dao.getTransactionCountForTag(tagId)
        assertEquals(1, countBefore)

        // Act - Delete tag
        tagDao.getTagById(tagId).test {
            val retrievedTag = awaitItem()!!
            tagDao.delete(retrievedTag)
            cancelAndIgnoreRemainingEvents()
        }

        // Assert - TransactionTag should be CASCADE deleted
        val countAfter = dao.getTransactionCountForTag(tagId)
        assertEquals(0, countAfter)
    }

    // Flow Reactivity Tests

    @Test
    fun getTagsForTransaction_emitsUpdatesOnInsert() = runTest {
        // Arrange
        val expense = TestDataBuilders.createTestExpenseEntity()
        val expenseId = expenseDao.insert(expense)

        val tag1 = TestDataBuilders.createTestTagEntity(name = "Business")
        val tag2 = TestDataBuilders.createTestTagEntity(name = "Personal")
        val tagId1 = tagDao.insert(tag1)
        val tagId2 = tagDao.insert(tag2)

        // Act & Assert
        dao.getTagsForTransaction(expenseId, "EXPENSE").test {
            // Initial emission (empty)
            assertEquals(0, awaitItem().size)

            // Insert first association
            dao.insert(TestDataBuilders.createTestTransactionTagEntity(expenseId, tagId1, "EXPENSE"))

            // Verify Flow emits updated list
            assertEquals(1, awaitItem().size)

            // Insert another association
            dao.insert(TestDataBuilders.createTestTransactionTagEntity(expenseId, tagId2, "EXPENSE"))
            assertEquals(2, awaitItem().size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getTransactionsForTag_emitsUpdatesOnInsert() = runTest {
        // Arrange
        val businessTag = TestDataBuilders.createTestTagEntity(name = "Business")
        val businessTagId = tagDao.insert(businessTag)

        val expense1 = TestDataBuilders.createTestExpenseEntity()
        val expense2 = TestDataBuilders.createTestExpenseEntity()
        val expenseId1 = expenseDao.insert(expense1)
        val expenseId2 = expenseDao.insert(expense2)

        // Act & Assert
        dao.getTransactionsForTag(businessTagId).test {
            // Initial emission (empty)
            assertEquals(0, awaitItem().size)

            // Tag first expense
            dao.insert(TestDataBuilders.createTestTransactionTagEntity(expenseId1, businessTagId, "EXPENSE"))

            // Verify Flow emits updated list
            assertEquals(1, awaitItem().size)

            // Tag second expense
            dao.insert(TestDataBuilders.createTestTransactionTagEntity(expenseId2, businessTagId, "EXPENSE"))
            assertEquals(2, awaitItem().size)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
