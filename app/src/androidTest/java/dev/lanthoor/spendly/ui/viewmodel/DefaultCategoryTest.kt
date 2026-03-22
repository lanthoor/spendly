package dev.lanthoor.spendly.ui.viewmodel

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import dev.lanthoor.spendly.data.local.SpendlyDatabase
import dev.lanthoor.spendly.data.repository.AccountRepositoryImpl
import dev.lanthoor.spendly.data.repository.BudgetRepositoryImpl
import dev.lanthoor.spendly.data.repository.CategoryRepositoryImpl
import dev.lanthoor.spendly.data.repository.ExpenseRepositoryImpl
import dev.lanthoor.spendly.data.repository.IncomeRepositoryImpl
import dev.lanthoor.spendly.data.repository.ReceiptRepositoryImpl
import dev.lanthoor.spendly.domain.model.Category
import dev.lanthoor.spendly.ui.screens.expenses.ExpenseViewModel
import dev.lanthoor.spendly.ui.screens.income.IncomeViewModel
import dev.lanthoor.spendly.utils.BudgetNotificationService
import dev.lanthoor.spendly.utils.createTestDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.seconds

/**
 * Instrumented tests for default category behavior in ViewModels.
 *
 * Verifies that:
 * - ExpenseViewModel defaults to "Others" category (ID 13)
 * - IncomeViewModel defaults to "Salary" category (ID 101)
 *
 * Pattern: methodName_inputCondition_expectedResult
 */
@RunWith(AndroidJUnit4::class)
class DefaultCategoryTest {

    private lateinit var database: SpendlyDatabase
    private lateinit var context: Context
    private lateinit var categoryRepository: CategoryRepositoryImpl
    private lateinit var expenseRepository: ExpenseRepositoryImpl
    private lateinit var incomeRepository: IncomeRepositoryImpl
    private lateinit var receiptRepository: ReceiptRepositoryImpl
    private lateinit var accountRepository: AccountRepositoryImpl
    private lateinit var budgetRepository: BudgetRepositoryImpl
    private lateinit var budgetNotificationService: BudgetNotificationService

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = createTestDatabase()

        // Initialize repositories
        categoryRepository = CategoryRepositoryImpl(database.categoryDao())
        expenseRepository =
            ExpenseRepositoryImpl(database.expenseDao(), database.receiptDao(), context)
        incomeRepository = IncomeRepositoryImpl(database.incomeDao())
        receiptRepository = ReceiptRepositoryImpl(database.receiptDao(), context)
        accountRepository = AccountRepositoryImpl(database.accountDao())
        budgetRepository = BudgetRepositoryImpl(database.budgetDao())
        budgetNotificationService = BudgetNotificationService(
            context = context,
            budgetRepository = budgetRepository,
            expenseRepository = expenseRepository,
            categoryRepository = categoryRepository
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    /**
     * Test that ExpenseViewModel defaults to "Others" category when form is reset.
     */
    @Test
    fun expenseViewModel_resetForm_defaultsToOthersCategory() = runTest(timeout = 10.seconds) {
        // Arrange - Seed categories
        categoryRepository.seedPredefinedCategories()

        // Create ViewModel
        val viewModel = ExpenseViewModel(
            expenseRepository = expenseRepository,
            categoryRepository = categoryRepository,
            receiptRepository = receiptRepository,
            accountRepository = accountRepository,
            budgetNotificationService = budgetNotificationService
        )

        // Wait for categories to load by checking the categories StateFlow
        viewModel.categories.test {
            val categories = awaitItem()

            // Wait until categories are loaded (non-empty)
            if (categories.isEmpty()) {
                val loadedCategories = awaitItem()
                assertEquals("Should have 19 unified categories", 19, loadedCategories.size)
            }

            // Act - Reset form
            viewModel.resetForm()

            // Wait for form state to update
            delay(150)

            cancelAndIgnoreRemainingEvents()
        }

        // Assert
        viewModel.formState.test {
            val formState = awaitItem()

            // Verify default category is "Others" (ID 13)
            assertNotNull("Default category should not be null", formState.categoryId)
            assertEquals(
                "Default expense category should be 'Others' (ID 13)",
                Category.OTHERS_CATEGORY_ID,
                formState.categoryId
            )
            assertEquals(
                "Default expense category ID should be 13",
                13L,
                formState.categoryId
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Test that IncomeViewModel defaults to "Salary" category when form is reset.
     */
    @Test
    fun incomeViewModel_resetForm_defaultsToSalaryCategory() = runTest(timeout = 10.seconds) {
        // Arrange - Seed categories
        categoryRepository.seedPredefinedCategories()

        // Create ViewModel
        val viewModel = IncomeViewModel(
            incomeRepository = incomeRepository,
            categoryRepository = categoryRepository,
            expenseRepository = expenseRepository,
            accountRepository = accountRepository
        )

        // Wait for categories to load by checking the incomeCategories StateFlow
        viewModel.incomeCategories.test {
            val categories = awaitItem()

            // Wait until categories are loaded (non-empty)
            if (categories.isEmpty()) {
                val loadedCategories = awaitItem()
                assertEquals("Should have 19 unified categories", 19, loadedCategories.size)
            }

            // Act - Reset form
            viewModel.resetForm()

            // Wait for form state to update
            delay(150)

            cancelAndIgnoreRemainingEvents()
        }

        // Assert
        viewModel.formState.test {
            val formState = awaitItem()

            // Verify default category is "Salary" (ID 101)
            assertNotNull("Default category should not be null", formState.selectedCategory)
            assertEquals(
                "Default income category should be 'Salary'",
                "Salary",
                formState.selectedCategory?.name
            )
            assertEquals(
                "Default income category ID should be 101",
                101L,
                formState.selectedCategory?.id
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Test that expense "Others" category exists and has correct properties.
     */
    @Test
    fun expenseOthersCategory_afterSeeding_hasCorrectProperties() = runTest(timeout = 10.seconds) {
        // Arrange & Act
        categoryRepository.seedPredefinedCategories()

        // Wait for seeding
        delay(100)

        // Assert
        categoryRepository.getAllCategories().test {
            val categories = awaitItem()

            val othersCategory = categories.find { it.id == Category.OTHERS_CATEGORY_ID }

            assertNotNull("Others category should exist", othersCategory)
            assertEquals("Category name should be 'Others'", "Others", othersCategory?.name)
            assertEquals("Category ID should be 13", 13L, othersCategory?.id)

            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Test that income "Salary" category exists and has correct properties.
     */
    @Test
    fun incomeSalaryCategory_afterSeeding_hasCorrectProperties() = runTest(timeout = 10.seconds) {
        // Arrange & Act
        categoryRepository.seedPredefinedCategories()

        // Wait for seeding
        delay(100)

        // Assert
        categoryRepository.getAllCategories().test {
            val categories = awaitItem()

            val salaryCategory = categories.find { it.id == 101L }

            assertNotNull("Salary category should exist", salaryCategory)
            assertEquals("Category name should be 'Salary'", "Salary", salaryCategory?.name)
            assertEquals("Category ID should be 101", 101L, salaryCategory?.id)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
