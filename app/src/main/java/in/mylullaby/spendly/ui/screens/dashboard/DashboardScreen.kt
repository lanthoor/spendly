package `in`.mylullaby.spendly.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowDown
import com.adamglin.phosphoricons.regular.ArrowUp
import com.adamglin.phosphoricons.regular.Plus
import `in`.mylullaby.spendly.ui.components.EmptyState
import `in`.mylullaby.spendly.ui.components.LoadingIndicator
import `in`.mylullaby.spendly.ui.components.SpendlyTopAppBar
import `in`.mylullaby.spendly.ui.screens.dashboard.components.FinancialSummaryCard
import `in`.mylullaby.spendly.ui.screens.dashboard.components.RecentTransactionsWidget
import `in`.mylullaby.spendly.ui.screens.dashboard.components.TopCategoriesChart
import `in`.mylullaby.spendly.ui.screens.expenses.AddExpenseScreen
import `in`.mylullaby.spendly.ui.screens.expenses.EditExpenseScreen
import `in`.mylullaby.spendly.ui.screens.income.AddIncomeScreen
import `in`.mylullaby.spendly.ui.screens.income.EditIncomeScreen

/**
 * Dashboard screen showing financial overview, recent transactions, and top categories
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val dashboardState by viewModel.dashboardState.collectAsStateWithLifecycle()
    var fabExpanded by remember { mutableStateOf(false) }

    // Modal sheet states
    var showAddExpenseSheet by remember { mutableStateOf(false) }
    var showAddIncomeSheet by remember { mutableStateOf(false) }
    var showEditExpenseSheet by remember { mutableStateOf(false) }
    var showEditIncomeSheet by remember { mutableStateOf(false) }
    var editExpenseId by remember { mutableStateOf(0L) }
    var editIncomeId by remember { mutableStateOf(0L) }

    Scaffold(
        topBar = {
            SpendlyTopAppBar(
                title = "Dashboard"
            )
        },
        floatingActionButton = {
            // Expandable FAB for adding expense or income
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Show add income and add expense options when expanded
                AnimatedVisibility(
                    visible = fabExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Add Income FAB
                        SmallFloatingActionButton(
                            onClick = {
                                fabExpanded = false
                                showAddIncomeSheet = true
                            },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.ArrowDown,
                                    contentDescription = "Add Income"
                                )
                                Text("Income")
                            }
                        }

                        // Add Expense FAB
                        SmallFloatingActionButton(
                            onClick = {
                                fabExpanded = false
                                showAddExpenseSheet = true
                            },
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.ArrowUp,
                                    contentDescription = "Add Expense"
                                )
                                Text("Expense")
                            }
                        }
                    }
                }

                // Main FAB with rotating plus icon
                val rotation by animateFloatAsState(
                    targetValue = if (fabExpanded) 45f else 0f,
                    label = "fab_rotation"
                )

                FloatingActionButton(
                    onClick = { fabExpanded = !fabExpanded },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Plus,
                        contentDescription = if (fabExpanded) "Close" else "Add Transaction",
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }
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
                            message = "No transactions yet",
                            description = "Add your first expense or income to get started"
                        )
                    }
                } else {
                    // Show dashboard content with pull-to-refresh
                    PullToRefreshBox(
                        isRefreshing = false,
                        onRefresh = { viewModel.refresh() },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Financial Summary Card
                            item {
                                FinancialSummaryCard(
                                    summary = state.financialSummary
                                )
                            }

                            // Top Categories Chart
                            if (state.topCategories.isNotEmpty()) {
                                item {
                                    TopCategoriesChart(
                                        categories = state.topCategories
                                    )
                                }
                            }

                            // Recent Transactions Widget
                            item {
                                RecentTransactionsWidget(
                                    transactions = state.recentTransactions,
                                    categories = state.allCategories,
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

    // Add Expense Modal Bottom Sheet
    if (showAddExpenseSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddExpenseSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            AddExpenseScreen(
                onNavigateBack = { showAddExpenseSheet = false }
            )
        }
    }

    // Add Income Modal Bottom Sheet
    if (showAddIncomeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddIncomeSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            AddIncomeScreen(
                onDismiss = { showAddIncomeSheet = false },
                onSuccess = { showAddIncomeSheet = false }
            )
        }
    }

    // Edit Expense Modal Bottom Sheet
    if (showEditExpenseSheet) {
        ModalBottomSheet(
            onDismissRequest = { showEditExpenseSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            EditExpenseScreen(
                expenseId = editExpenseId,
                onNavigateBack = { showEditExpenseSheet = false }
            )
        }
    }

    // Edit Income Modal Bottom Sheet
    if (showEditIncomeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showEditIncomeSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            EditIncomeScreen(
                incomeId = editIncomeId,
                onDismiss = { showEditIncomeSheet = false },
                onSuccess = { showEditIncomeSheet = false },
                onDelete = { showEditIncomeSheet = false }
            )
        }
    }
}
