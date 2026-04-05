package dev.lanthoor.spendly.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CalendarBlank
import com.adamglin.phosphoricons.regular.CaretLeft
import com.adamglin.phosphoricons.regular.CaretRight
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.core.model.finance.RecentTransaction
import dev.lanthoor.spendly.ui.components.EditExpenseBottomSheet
import dev.lanthoor.spendly.ui.components.EditIncomeBottomSheet
import dev.lanthoor.spendly.ui.components.EmptyState
import dev.lanthoor.spendly.ui.components.LoadingIndicator
import dev.lanthoor.spendly.ui.components.MonthPickerDialog
import dev.lanthoor.spendly.ui.screens.dashboard.components.BudgetProgressWidget
import dev.lanthoor.spendly.ui.screens.dashboard.components.FinancialSummaryCard
import dev.lanthoor.spendly.ui.screens.dashboard.components.RecentTransactionsWidget
import dev.lanthoor.spendly.ui.screens.dashboard.components.TopCategoriesChart
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Dashboard screen showing financial overview, recent transactions, and top categories
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    onNavigateToBudgets: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val dashboardState by viewModel.dashboardState.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()

    // Modal sheet states
    var showEditExpenseSheet by remember { mutableStateOf(false) }
    var showEditIncomeSheet by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var editExpenseId by remember { mutableLongStateOf(0L) }
    var editIncomeId by remember { mutableLongStateOf(0L) }

    Scaffold(
        topBar = {
            // Custom top bar with month navigation
            TopAppBar(
                title = {
                    // Full-width row with three sections for proper centering
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left arrow - previous month
                        IconButton(
                            onClick = {
                                val prevCalendar = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, selectedYear)
                                    set(Calendar.MONTH, selectedMonth - 1)
                                    add(Calendar.MONTH, -1)
                                }
                                viewModel.selectMonth(
                                    prevCalendar.get(Calendar.YEAR),
                                    prevCalendar.get(Calendar.MONTH) + 1
                                )
                            }
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.CaretLeft,
                                contentDescription = stringResource(R.string.cd_previous_month)
                            )
                        }

                        // Center - Month selector chip
                        val monthLabel = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(
                            Date(Calendar.getInstance().apply {
                                set(Calendar.YEAR, selectedYear)
                                set(Calendar.MONTH, selectedMonth - 1)
                            }.timeInMillis)
                        )

                        AssistChip(
                            onClick = { showMonthPicker = true },
                            label = { Text(monthLabel) },
                            leadingIcon = {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.CalendarBlank,
                                    contentDescription = stringResource(R.string.cd_select_month)
                                )
                            }
                        )

                        // Right arrow - next month
                        IconButton(
                            onClick = {
                                val nextCalendar = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, selectedYear)
                                    set(Calendar.MONTH, selectedMonth - 1)
                                    add(Calendar.MONTH, 1)
                                }
                                viewModel.selectMonth(
                                    nextCalendar.get(Calendar.YEAR),
                                    nextCalendar.get(Calendar.MONTH) + 1
                                )
                            }
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.CaretRight,
                                contentDescription = stringResource(R.string.cd_next_month)
                            )
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        when (dashboardState) {
            is DashboardUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }

            is DashboardUiState.Success -> {
                val state = dashboardState as DashboardUiState.Success

                if (!state.hasTransactions) {
                    // Empty state when no transactions
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            message = stringResource(R.string.empty_no_transactions_title),
                            description = stringResource(R.string.empty_no_transactions_desc)
                        )
                    }
                } else {
                    // Show dashboard content
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Financial Summary Card
                        item {
                            FinancialSummaryCard(
                                summary = state.financialSummary,
                                onYearTypeChange = { yearType ->
                                    viewModel.updateYearType(yearType)
                                }
                            )
                        }

                        // 2. Budget Progress Widget (only show if budgets exist)
                        if (state.budgets.isNotEmpty()) {
                            item {
                                BudgetProgressWidget(
                                    budgets = state.budgets,
                                    onViewAllClick = onNavigateToBudgets
                                )
                            }
                        }

                        // 3. Top Categories Chart
                        if (state.topCategories.isNotEmpty()) {
                            item {
                                TopCategoriesChart(
                                    categories = state.topCategories
                                )
                            }
                        }

                        // 4. Recent Transactions Widget (only show if transactions exist)
                        if (state.recentTransactions.isNotEmpty()) {
                            item {
                                RecentTransactionsWidget(
                                    transactions = state.recentTransactions,
                                    categories = state.allCategories,
                                    accounts = state.allAccounts,
                                    onTransactionClick = { transaction ->
                                        when (transaction) {
                                            is RecentTransaction.ExpenseTransaction -> {
                                                editExpenseId = transaction.expense.id
                                                showEditExpenseSheet = true
                                            }

                                            is RecentTransaction.IncomeTransaction -> {
                                                editIncomeId = transaction.income.id
                                                showEditIncomeSheet = true
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            is DashboardUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        message = "Error loading dashboard",
                        description = (dashboardState as DashboardUiState.Error).message
                    )
                }
            }
        }
    }

    // Edit Expense Modal Bottom Sheet
    if (showEditExpenseSheet) {
        EditExpenseBottomSheet(
            expenseId = editExpenseId,
            onDismiss = { showEditExpenseSheet = false }
        )
    }

    // Edit Income Modal Bottom Sheet
    if (showEditIncomeSheet) {
        EditIncomeBottomSheet(
            incomeId = editIncomeId,
            onDismiss = { showEditIncomeSheet = false }
        )
    }

    // Month Picker Dialog
    if (showMonthPicker) {
        MonthPickerDialog(
            selectedYear = selectedYear,
            selectedMonth = selectedMonth,
            onMonthSelected = { year, month ->
                viewModel.selectMonth(year, month)
                showMonthPicker = false
            },
            onDismiss = { showMonthPicker = false }
        )
    }
}
