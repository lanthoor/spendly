package dev.lanthoor.spendly.data.local.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import dev.lanthoor.spendly.data.local.SpendlyDatabase
import dev.lanthoor.spendly.utils.TestDataBuilders
import dev.lanthoor.spendly.utils.createTestDatabase
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
 * Instrumented tests for AccountDao.
 *
 * Tests CRUD operations, unique name constraints, sorting, custom vs predefined filtering,
 * type filtering, reassignment operations, and transaction counting.
 *
 * Pattern: methodName_inputCondition_expectedResult
 */
@RunWith(AndroidJUnit4::class)
class AccountDaoTest {

    private lateinit var database: SpendlyDatabase
    private lateinit var dao: AccountDao
    private lateinit var expenseDao: ExpenseDao
    private lateinit var incomeDao: IncomeDao

    @Before
    fun setUp() {
        database = createTestDatabase()
        dao = database.accountDao()
        expenseDao = database.expenseDao()
        incomeDao = database.incomeDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // CRUD Operations Tests

    @Test
    fun insertAccount_withValidData_returnsInsertedId() = runTest {
        // Arrange
        val account = TestDataBuilders.createTestAccountEntity(name = "Savings Account")

        // Act
        val id = dao.insert(account)

        // Assert
        assertTrue("Inserted ID should be positive", id > 0)
        dao.getAccountById(id).test {
            val retrieved = awaitItem()
            assertEquals("Savings Account", retrieved?.name)
            assertEquals(account.type, retrieved?.type)
            assertEquals(account.icon, retrieved?.icon)
            assertEquals(account.color, retrieved?.color)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertAccount_withDuplicateName_replacesExisting() = runTest {
        // Arrange
        val account1 = TestDataBuilders.createTestAccountEntity(
            name = "Chase Card",
            type = "CARD",
            color = 0xFFFF0000.toInt()
        )
        dao.insert(account1)

        // Act - Insert with same name but different properties
        val account2 = TestDataBuilders.createTestAccountEntity(
            name = "Chase Card",
            type = "BANK",
            color = 0xFF00FF00.toInt()
        )
        dao.insert(account2)

        // Assert - Should replace due to OnConflictStrategy.REPLACE
        dao.getAllAccounts().test {
            val accounts = awaitItem()
            // Only 1 "Chase Card" should exist (plus default "My Account")
            val chaseAccounts = accounts.filter { it.name == "Chase Card" }
            assertEquals(1, chaseAccounts.size)
            // Should have new properties
            assertEquals("BANK", chaseAccounts[0].type)
            assertEquals(0xFF00FF00.toInt(), chaseAccounts[0].color)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertAll_withMultipleAccounts_insertsAllSuccessfully() = runTest {
        // Arrange
        val accounts = listOf(
            TestDataBuilders.createTestAccountEntity(name = "Checking", sortOrder = 1),
            TestDataBuilders.createTestAccountEntity(name = "Savings", sortOrder = 2),
            TestDataBuilders.createTestAccountEntity(name = "Credit Card", sortOrder = 3)
        )

        // Act
        dao.insertAll(accounts)

        // Assert
        dao.getAllAccounts().test {
            val retrieved = awaitItem()
            // Should have 3 new + 1 default = 4 total
            assertEquals(4, retrieved.size)
            assertTrue(retrieved.any { it.name == "Checking" })
            assertTrue(retrieved.any { it.name == "Savings" })
            assertTrue(retrieved.any { it.name == "Credit Card" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateAccount_changesData() = runTest {
        // Arrange
        val account = TestDataBuilders.createTestAccountEntity(
            name = "Original",
            type = "BANK",
            icon = "bank",
            color = 0xFFFF0000.toInt()
        )
        val id = dao.insert(account)

        // Act
        dao.getAccountById(id).test {
            val inserted = awaitItem()!!
            val updated = inserted.copy(
                name = "Updated Account",
                type = "CARD",
                icon = "credit_card",
                color = 0xFF00FF00.toInt()
            )
            dao.update(updated)

            // Assert
            val retrieved = awaitItem()
            assertEquals("Updated Account", retrieved?.name)
            assertEquals("CARD", retrieved?.type)
            assertEquals("credit_card", retrieved?.icon)
            assertEquals(0xFF00FF00.toInt(), retrieved?.color)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteAccount_removesFromDatabase() = runTest {
        // Arrange
        val account = TestDataBuilders.createTestAccountEntity(name = "To Delete")
        val id = dao.insert(account)

        // Act
        dao.getAccountById(id).test {
            val retrieved = awaitItem()!!
            dao.delete(retrieved)

            // Assert
            val afterDelete = awaitItem()
            assertNull("Account should be null after deletion", afterDelete)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Query Tests

    @Test
    fun getAllAccounts_orderedBySortOrder() = runTest {
        // Arrange
        val account1 = TestDataBuilders.createTestAccountEntity(name = "Third", sortOrder = 3)
        val account2 = TestDataBuilders.createTestAccountEntity(name = "First", sortOrder = 1)
        val account3 = TestDataBuilders.createTestAccountEntity(name = "Second", sortOrder = 2)

        dao.insert(account1)
        dao.insert(account2)
        dao.insert(account3)

        // Act & Assert
        dao.getAllAccounts().test {
            val accounts = awaitItem()
            // Should have 4 accounts (3 new + 1 default at sortOrder 0)
            assertEquals(4, accounts.size)
            // First should be default "My Account" (sortOrder 0)
            assertEquals("My Account", accounts[0].name)
            // Then ordered by sortOrder ASC
            assertEquals("First", accounts[1].name)
            assertEquals("Second", accounts[2].name)
            assertEquals("Third", accounts[3].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAccountById_returnsCorrectAccount() = runTest {
        // Arrange
        val account1 = TestDataBuilders.createTestAccountEntity(name = "First")
        val account2 = TestDataBuilders.createTestAccountEntity(name = "Second")
        dao.insert(account1)
        val id2 = dao.insert(account2)

        // Act & Assert
        dao.getAccountById(id2).test {
            val retrieved = awaitItem()
            assertEquals("Second", retrieved?.name)
            assertEquals(id2, retrieved?.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAccountById_withNonexistentId_returnsNull() = runTest {
        // Act & Assert
        dao.getAccountById(999L).test {
            val retrieved = awaitItem()
            assertNull("Should return null for non-existent ID", retrieved)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getCustomAccounts_returnsOnlyCustom() = runTest {
        // Arrange
        val predefined = TestDataBuilders.createTestAccountEntity(
            name = "Predefined",
            isCustom = false
        )
        val custom1 = TestDataBuilders.createTestAccountEntity(
            name = "Custom 1",
            isCustom = true
        )
        val custom2 = TestDataBuilders.createTestAccountEntity(
            name = "Custom 2",
            isCustom = true
        )

        dao.insert(predefined)
        dao.insert(custom1)
        dao.insert(custom2)

        // Act & Assert
        dao.getCustomAccounts().test {
            val accounts = awaitItem()
            assertEquals(2, accounts.size)
            assertTrue(accounts.all { it.isCustom })
            assertTrue(accounts.any { it.name == "Custom 1" })
            assertTrue(accounts.any { it.name == "Custom 2" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getPredefinedAccounts_returnsOnlyPredefined() = runTest {
        // Arrange
        val predefined1 = TestDataBuilders.createTestAccountEntity(
            name = "Predefined 1",
            isCustom = false
        )
        val predefined2 = TestDataBuilders.createTestAccountEntity(
            name = "Predefined 2",
            isCustom = false
        )
        val custom = TestDataBuilders.createTestAccountEntity(
            name = "Custom",
            isCustom = true
        )

        dao.insert(predefined1)
        dao.insert(predefined2)
        dao.insert(custom)

        // Act & Assert
        dao.getPredefinedAccounts().test {
            val accounts = awaitItem()
            // Should have 2 new + 1 default = 3 predefined
            assertEquals(3, accounts.size)
            assertFalse(accounts.any { it.isCustom })
            assertTrue(accounts.any { it.name == "Predefined 1" })
            assertTrue(accounts.any { it.name == "Predefined 2" })
            assertTrue(accounts.any { it.name == "My Account" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAccountsByType_returnsOnlyMatchingType() = runTest {
        // Arrange
        val bankAccount1 = TestDataBuilders.createTestAccountEntity(name = "Bank 1", type = "BANK")
        val bankAccount2 = TestDataBuilders.createTestAccountEntity(name = "Bank 2", type = "BANK")
        val cardAccount = TestDataBuilders.createTestAccountEntity(name = "Card", type = "CARD")
        val walletAccount =
            TestDataBuilders.createTestAccountEntity(name = "Wallet", type = "WALLET")

        dao.insert(bankAccount1)
        dao.insert(bankAccount2)
        dao.insert(cardAccount)
        dao.insert(walletAccount)

        // Act & Assert - BANK type
        dao.getAccountsByType("BANK").test {
            val accounts = awaitItem()
            // Should have 2 new + 1 default = 3 BANK accounts
            assertEquals(3, accounts.size)
            assertTrue(accounts.all { it.type == "BANK" })
            cancelAndIgnoreRemainingEvents()
        }

        // Act & Assert - CARD type
        dao.getAccountsByType("CARD").test {
            val accounts = awaitItem()
            assertEquals(1, accounts.size)
            assertEquals("Card", accounts[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Validation Tests

    @Test
    fun exists_withExistingAccount_returnsOne() = runBlocking {
        // Arrange
        val account = TestDataBuilders.createTestAccountEntity(name = "Test")
        val id = dao.insert(account)

        // Act
        val count = dao.exists(id)

        // Assert
        assertEquals(1, count)
    }

    @Test
    fun exists_withNonexistentAccount_returnsZero() = runBlocking {
        // Act
        val count = dao.exists(999L)

        // Assert
        assertEquals(0, count)
    }

    @Test
    fun getAccountByName_withExistingName_returnsAccount() = runBlocking {
        // Arrange
        val account = TestDataBuilders.createTestAccountEntity(name = "Unique Name")
        dao.insert(account)

        // Act
        val retrieved = dao.getAccountByName("Unique Name")

        // Assert
        assertNotNull(retrieved)
        assertEquals("Unique Name", retrieved?.name)
    }

    @Test
    fun getAccountByName_withNonexistentName_returnsNull() = runBlocking {
        // Act
        val retrieved = dao.getAccountByName("Nonexistent")

        // Assert
        assertNull(retrieved)
    }

    // Transaction Counting Tests

    @Test
    fun getExpenseCountByAccount_returnsCorrectCount() = runBlocking {
        // Arrange
        val account = TestDataBuilders.createTestAccountEntity(name = "Expense Account")
        val accountId = dao.insert(account)

        val expense1 = TestDataBuilders.createTestExpenseEntity(accountId = accountId)
        val expense2 = TestDataBuilders.createTestExpenseEntity(accountId = accountId)
        val expense3 = TestDataBuilders.createTestExpenseEntity(accountId = accountId)
        expenseDao.insert(expense1)
        expenseDao.insert(expense2)
        expenseDao.insert(expense3)

        // Act
        val count = dao.getExpenseCountByAccount(accountId)

        // Assert
        assertEquals(3, count)
    }

    @Test
    fun getExpenseCountByAccount_withNoExpenses_returnsZero() = runBlocking {
        // Arrange
        val account = TestDataBuilders.createTestAccountEntity(name = "Empty Account")
        val accountId = dao.insert(account)

        // Act
        val count = dao.getExpenseCountByAccount(accountId)

        // Assert
        assertEquals(0, count)
    }

    @Test
    fun getIncomeCountByAccount_returnsCorrectCount() = runBlocking {
        // Arrange
        val account = TestDataBuilders.createTestAccountEntity(name = "Income Account")
        val accountId = dao.insert(account)

        val income1 = TestDataBuilders.createTestIncomeEntity(accountId = accountId)
        val income2 = TestDataBuilders.createTestIncomeEntity(accountId = accountId)
        incomeDao.insert(income1)
        incomeDao.insert(income2)

        // Act
        val count = dao.getIncomeCountByAccount(accountId)

        // Assert
        assertEquals(2, count)
    }

    @Test
    fun getIncomeCountByAccount_withNoIncome_returnsZero() = runBlocking {
        // Arrange
        val account = TestDataBuilders.createTestAccountEntity(name = "Empty Account")
        val accountId = dao.insert(account)

        // Act
        val count = dao.getIncomeCountByAccount(accountId)

        // Assert
        assertEquals(0, count)
    }

    // Reassignment Tests

    @Test
    fun reassignExpensesToAccount_updatesAllExpenses() = runBlocking {
        // Arrange
        val oldAccount = TestDataBuilders.createTestAccountEntity(name = "Old Account")
        val oldAccountId = dao.insert(oldAccount)
        val newAccount = TestDataBuilders.createTestAccountEntity(name = "New Account")
        val newAccountId = dao.insert(newAccount)

        // Create expenses with old account
        val expense1 = TestDataBuilders.createTestExpenseEntity(accountId = oldAccountId)
        val expense2 = TestDataBuilders.createTestExpenseEntity(accountId = oldAccountId)
        val expense3 = TestDataBuilders.createTestExpenseEntity(accountId = oldAccountId)
        val id1 = expenseDao.insert(expense1)
        val id2 = expenseDao.insert(expense2)
        val id3 = expenseDao.insert(expense3)

        // Verify initial state
        assertEquals(3, dao.getExpenseCountByAccount(oldAccountId))
        assertEquals(0, dao.getExpenseCountByAccount(newAccountId))

        // Act
        val timestamp = System.currentTimeMillis()
        dao.reassignExpensesToAccount(oldAccountId, newAccountId, timestamp)

        // Assert
        assertEquals(0, dao.getExpenseCountByAccount(oldAccountId))
        assertEquals(3, dao.getExpenseCountByAccount(newAccountId))

        // Verify expenses now reference new account
        expenseDao.getExpenseById(id1).test {
            assertEquals(newAccountId, awaitItem()?.accountId)
            cancelAndIgnoreRemainingEvents()
        }
        expenseDao.getExpenseById(id2).test {
            assertEquals(newAccountId, awaitItem()?.accountId)
            cancelAndIgnoreRemainingEvents()
        }
        expenseDao.getExpenseById(id3).test {
            assertEquals(newAccountId, awaitItem()?.accountId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun reassignIncomesToAccount_updatesAllIncome() = runBlocking {
        // Arrange
        val oldAccount = TestDataBuilders.createTestAccountEntity(name = "Old Account")
        val oldAccountId = dao.insert(oldAccount)
        val newAccount = TestDataBuilders.createTestAccountEntity(name = "New Account")
        val newAccountId = dao.insert(newAccount)

        // Create income entries with old account
        val income1 = TestDataBuilders.createTestIncomeEntity(accountId = oldAccountId)
        val income2 = TestDataBuilders.createTestIncomeEntity(accountId = oldAccountId)
        val id1 = incomeDao.insert(income1)
        val id2 = incomeDao.insert(income2)

        // Verify initial state
        assertEquals(2, dao.getIncomeCountByAccount(oldAccountId))
        assertEquals(0, dao.getIncomeCountByAccount(newAccountId))

        // Act
        val timestamp = System.currentTimeMillis()
        dao.reassignIncomesToAccount(oldAccountId, newAccountId, timestamp)

        // Assert
        assertEquals(0, dao.getIncomeCountByAccount(oldAccountId))
        assertEquals(2, dao.getIncomeCountByAccount(newAccountId))

        // Verify income entries now reference new account
        incomeDao.getIncomeById(id1).test {
            assertEquals(newAccountId, awaitItem()?.accountId)
            cancelAndIgnoreRemainingEvents()
        }
        incomeDao.getIncomeById(id2).test {
            assertEquals(newAccountId, awaitItem()?.accountId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Flow Reactivity Tests

    @Test
    fun getAllAccounts_emitsUpdatesOnInsert() = runTest {
        // Act & Assert
        dao.getAllAccounts().test {
            // Initial emission (1 default account)
            assertEquals(1, awaitItem().size)

            // Insert account
            dao.insert(TestDataBuilders.createTestAccountEntity())

            // Verify Flow emits updated list
            assertEquals(2, awaitItem().size)

            // Insert another
            dao.insert(TestDataBuilders.createTestAccountEntity(name = "Another"))
            assertEquals(3, awaitItem().size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllAccounts_emitsUpdatesOnDelete() = runTest {
        // Arrange
        val account1 = TestDataBuilders.createTestAccountEntity(name = "First")
        val account2 = TestDataBuilders.createTestAccountEntity(name = "Second")
        dao.insert(account1)
        dao.insert(account2)

        // Act & Assert
        dao.getAllAccounts().test {
            // Initial emission (2 new + 1 default = 3)
            val initial = awaitItem()
            assertEquals(3, initial.size)

            // Delete one (not the default)
            dao.delete(initial.first { it.name == "First" })

            // Verify Flow emits updated list
            val afterDelete = awaitItem()
            assertEquals(2, afterDelete.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAccountById_emitsUpdatesOnUpdate() = runTest {
        // Arrange
        val account = TestDataBuilders.createTestAccountEntity(name = "Original")
        val id = dao.insert(account)

        // Act & Assert
        dao.getAccountById(id).test {
            val original = awaitItem()!!
            assertEquals("Original", original.name)

            // Update
            val updated = original.copy(name = "Updated")
            dao.update(updated)

            // Verify Flow emits updated account
            val afterUpdate = awaitItem()
            assertEquals("Updated", afterUpdate?.name)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
