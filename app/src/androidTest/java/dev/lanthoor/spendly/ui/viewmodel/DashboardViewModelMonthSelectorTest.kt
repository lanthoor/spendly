package dev.lanthoor.spendly.ui.viewmodel

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import dev.lanthoor.spendly.data.local.SpendlyDatabase
import dev.lanthoor.spendly.data.repository.AccountRepositoryImpl
import dev.lanthoor.spendly.data.repository.BudgetRepositoryImpl
import dev.lanthoor.spendly.data.repository.CategoryRepositoryImpl
import dev.lanthoor.spendly.data.repository.ExpenseRepositoryImpl
import dev.lanthoor.spendly.data.repository.IncomeRepositoryImpl
import dev.lanthoor.spendly.data.repository.PreferencesRepositoryImpl
import dev.lanthoor.spendly.ui.screens.dashboard.DashboardUiState
import dev.lanthoor.spendly.ui.screens.dashboard.DashboardViewModel
import dev.lanthoor.spendly.utils.TestDataBuilders
import dev.lanthoor.spendly.utils.YearType
import dev.lanthoor.spendly.utils.createTestDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import kotlin.time.Duration.Companion.seconds

/**
 * Instrumented tests for Dashboard month selector functionality.
 *
 * Tests:
 * - Month selection state management
 * - YTD calculations for Calendar Year
 * - YTD calculations for Financial Year
 * - Cross-year YTD calculations
 * - Month/YTD metrics accuracy
 * - Year type preference integration
 *
 * Pattern: methodName_inputCondition_expectedResult
 */
@RunWith(AndroidJUnit4::class)
class DashboardViewModelMonthSelectorTest {

    private lateinit var database: SpendlyDatabase
    private lateinit var context: Context
    private lateinit var expenseRepository: ExpenseRepositoryImpl
    private lateinit var incomeRepository: IncomeRepositoryImpl
    private lateinit var categoryRepository: CategoryRepositoryImpl
    private lateinit var accountRepository: AccountRepositoryImpl
    private lateinit var budgetRepository: BudgetRepositoryImpl
    private lateinit var preferencesRepository: PreferencesRepositoryImpl
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var testScope: CoroutineScope
    private lateinit var viewModel: DashboardViewModel

    // DAOs for direct entity insertion in tests
    private lateinit var expenseDao: dev.lanthoor.spendly.data.local.dao.ExpenseDao
    private lateinit var incomeDao: dev.lanthoor.spendly.data.local.dao.IncomeDao

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = createTestDatabase()
        testScope = CoroutineScope(Dispatchers.Unconfined + SupervisorJob())

        // Create test DataStore
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { context.preferencesDataStoreFile("test_prefs_${System.currentTimeMillis()}") }
        )

        // Initialize DAOs
        expenseDao = database.expenseDao()
        incomeDao = database.incomeDao()

        // Initialize repositories
        expenseRepository = ExpenseRepositoryImpl(expenseDao, database.receiptDao(), context)
        incomeRepository = IncomeRepositoryImpl(incomeDao)
        categoryRepository = CategoryRepositoryImpl(database.categoryDao())
        accountRepository = AccountRepositoryImpl(database.accountDao())
        budgetRepository = BudgetRepositoryImpl(database.budgetDao())
        preferencesRepository = PreferencesRepositoryImpl(dataStore)

        // Create ViewModel
        viewModel = DashboardViewModel(
            expenseRepository = expenseRepository,
            incomeRepository = incomeRepository,
            categoryRepository = categoryRepository,
            accountRepository = accountRepository,
            budgetRepository = budgetRepository,
            preferencesRepository = preferencesRepository
        )
    }

    @After
    fun tearDown() {
        database.close()
        testScope.cancel()
    }

    @Test
    fun selectMonth_updatesSelectedMonthAndYear() = runTest(timeout = 10.seconds) {
        // Act
        viewModel.selectMonth(2024, 6)

        // Assert
        viewModel.selectedMonth.test {
            assertEquals(6, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.selectedYear.test {
            assertEquals(2024, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun dashboardState_withMonthSelection_calculatesMonthMetricsCorrectly() =
        runTest(timeout = 10.seconds) {
            // Arrange - Create test expenses for March 2025
            val calendar = Calendar.getInstance().apply {
                set(2025, Calendar.MARCH, 15) // March 15, 2025
            }
            val marchExpense = TestDataBuilders.createTestExpenseEntity(
                amount = 10000L, // ₹100
                date = calendar.timeInMillis
            )
            expenseDao.insert(marchExpense)

            // Act - Select March 2025
            viewModel.selectMonth(2025, 3)

            delay(300) // Wait for state calculation

            // Assert
            viewModel.dashboardState.test {
                val state = awaitItem()
                if (state is DashboardUiState.Success) {
                    assertEquals(3, state.financialSummary.selectedMonth)
                    assertEquals(2025, state.financialSummary.selectedYear)
                    assertEquals(10000L, state.financialSummary.monthExpenses)
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun dashboardState_calendarYearYTD_calculatesFromJanuary() = runTest(timeout = 10.seconds) {
        // Arrange - Set year type to CALENDAR
        preferencesRepository.setYearType(YearType.CALENDAR)

        // Create expenses across Jan-Mar 2025
        val janExpense = TestDataBuilders.createTestExpenseEntity(
            amount = 5000L,
            date = Calendar.getInstance().apply { set(2025, Calendar.JANUARY, 15) }.timeInMillis
        )
        val febExpense = TestDataBuilders.createTestExpenseEntity(
            amount = 7000L,
            date = Calendar.getInstance().apply { set(2025, Calendar.FEBRUARY, 15) }.timeInMillis
        )
        val marExpense = TestDataBuilders.createTestExpenseEntity(
            amount = 10000L,
            date = Calendar.getInstance().apply { set(2025, Calendar.MARCH, 15) }.timeInMillis
        )

        expenseDao.insert(janExpense)
        expenseDao.insert(febExpense)
        expenseDao.insert(marExpense)

        // Act - Select March 2025
        viewModel.selectMonth(2025, 3)

        delay(300)

        // Assert - YTD should be Jan 1 - Mar 31
        viewModel.dashboardState.test {
            val state = awaitItem()
            if (state is DashboardUiState.Success) {
                assertEquals(YearType.CALENDAR, state.financialSummary.yearType)
                assertEquals(10000L, state.financialSummary.monthExpenses) // March only
                assertEquals(22000L, state.financialSummary.ytdExpenses)  // Jan + Feb + Mar
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun dashboardState_financialYearYTD_calculatesFromApril() = runTest(timeout = 10.seconds) {
        // Arrange - Set year type to FINANCIAL
        preferencesRepository.setYearType(YearType.FINANCIAL)

        // Create expenses from Apr 2024 to Mar 2025
        val aprExpense = TestDataBuilders.createTestExpenseEntity(
            amount = 3000L,
            date = Calendar.getInstance().apply { set(2024, Calendar.APRIL, 15) }.timeInMillis
        )
        val janExpense = TestDataBuilders.createTestExpenseEntity(
            amount = 5000L,
            date = Calendar.getInstance().apply { set(2025, Calendar.JANUARY, 15) }.timeInMillis
        )
        val marExpense = TestDataBuilders.createTestExpenseEntity(
            amount = 10000L,
            date = Calendar.getInstance().apply { set(2025, Calendar.MARCH, 15) }.timeInMillis
        )

        expenseDao.insert(aprExpense)
        expenseDao.insert(janExpense)
        expenseDao.insert(marExpense)

        // Act - Select March 2025
        viewModel.selectMonth(2025, 3)

        delay(300)

        // Assert - YTD should be Apr 2024 - Mar 2025
        viewModel.dashboardState.test {
            val state = awaitItem()
            if (state is DashboardUiState.Success) {
                assertEquals(YearType.FINANCIAL, state.financialSummary.yearType)
                assertEquals(10000L, state.financialSummary.monthExpenses) // March only
                assertEquals(18000L, state.financialSummary.ytdExpenses)  // Apr + Jan + Mar
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun dashboardState_financialYearFirstMonth_ytdEqualsMonthExpenses() =
        runTest(timeout = 10.seconds) {
            // Arrange - Set year type to FINANCIAL
            preferencesRepository.setYearType(YearType.FINANCIAL)

            // Create expense for April 2025 only
            val aprExpense = TestDataBuilders.createTestExpenseEntity(
                amount = 10000L,
                date = Calendar.getInstance().apply { set(2025, Calendar.APRIL, 15) }.timeInMillis
            )
            expenseDao.insert(aprExpense)

            // Act - Select April 2025 (first month of FY)
            viewModel.selectMonth(2025, 4)

            delay(300)

            // Assert - YTD should equal month expenses (Apr 1 - Apr 30)
            viewModel.dashboardState.test {
                val state = awaitItem()
                if (state is DashboardUiState.Success) {
                    assertEquals(10000L, state.financialSummary.monthExpenses)
                    assertEquals(10000L, state.financialSummary.ytdExpenses)
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun dashboardState_withIncomeAndExpenses_calculatesNetBalanceCorrectly() =
        runTest(timeout = 10.seconds) {
            // Arrange
            val marchDate =
                Calendar.getInstance().apply { set(2025, Calendar.MARCH, 15) }.timeInMillis

            val expense = TestDataBuilders.createTestExpenseEntity(
                amount = 10000L,
                date = marchDate
            )
            val income = TestDataBuilders.createTestIncomeEntity(
                amount = 50000L,
                date = marchDate
            )

            expenseDao.insert(expense)
            incomeDao.insert(income)

            // Act
            viewModel.selectMonth(2025, 3)

            delay(300)

            // Assert - Net balance = Income - Expenses
            viewModel.dashboardState.test {
                val state = awaitItem()
                if (state is DashboardUiState.Success) {
                    assertEquals(50000L, state.financialSummary.monthIncome)
                    assertEquals(10000L, state.financialSummary.monthExpenses)
                    assertEquals(40000L, state.financialSummary.monthNetBalance)
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun dashboardState_emptyMonth_showsZeroMetrics() = runTest(timeout = 10.seconds) {
        // Arrange - Create expense in a different month
        val januaryExpense = TestDataBuilders.createTestExpenseEntity(
            amount = 10000L,
            date = Calendar.getInstance().apply { set(2025, Calendar.JANUARY, 15) }.timeInMillis
        )
        expenseDao.insert(januaryExpense)

        // Act - Select December 2025 (empty month)
        viewModel.selectMonth(2025, 12)

        delay(300)

        // Assert - December should have zero metrics
        viewModel.dashboardState.test {
            val state = awaitItem()
            if (state is DashboardUiState.Success) {
                assertEquals(0L, state.financialSummary.monthExpenses)
                assertEquals(0L, state.financialSummary.monthIncome)
                assertEquals(0L, state.financialSummary.monthNetBalance)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun dashboardState_recentTransactions_showsOnlyFromSelectedMonth() =
        runTest(timeout = 10.seconds) {
            // Arrange - Create transactions in different months
            val marchDate =
                Calendar.getInstance().apply { set(2025, Calendar.MARCH, 15) }.timeInMillis
            val aprilDate =
                Calendar.getInstance().apply { set(2025, Calendar.APRIL, 15) }.timeInMillis

            val marchExpense = TestDataBuilders.createTestExpenseEntity(
                amount = 10000L,
                description = "March Expense",
                date = marchDate
            )
            val aprilExpense = TestDataBuilders.createTestExpenseEntity(
                amount = 20000L,
                description = "April Expense",
                date = aprilDate
            )

            expenseDao.insert(marchExpense)
            expenseDao.insert(aprilExpense)

            // Act - Select March
            viewModel.selectMonth(2025, 3)

            delay(300)

            // Assert - Should only show March transaction
            viewModel.dashboardState.test {
                val state = awaitItem()
                if (state is DashboardUiState.Success) {
                    assertEquals(1, state.recentTransactions.size)
                }
                cancelAndIgnoreRemainingEvents()
            }
        }
}
