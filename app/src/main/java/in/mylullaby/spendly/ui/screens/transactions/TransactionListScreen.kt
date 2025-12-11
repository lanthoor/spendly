package `in`.mylullaby.spendly.ui.screens.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import `in`.mylullaby.spendly.domain.model.Category
import `in`.mylullaby.spendly.ui.components.EmptyState
import `in`.mylullaby.spendly.ui.components.IconMapper
import `in`.mylullaby.spendly.ui.components.LoadingIndicator
import `in`.mylullaby.spendly.ui.components.SpendlyTopAppBar
import `in`.mylullaby.spendly.ui.screens.dashboard.DashboardViewModel
import `in`.mylullaby.spendly.ui.screens.dashboard.RecentTransaction
import `in`.mylullaby.spendly.ui.screens.expenses.EditExpenseScreen
import `in`.mylullaby.spendly.ui.screens.income.EditIncomeScreen
import `in`.mylullaby.spendly.utils.CurrencyUtils
import `in`.mylullaby.spendly.utils.toDisplayName
import `in`.mylullaby.spendly.utils.toDisplayString
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Screen showing all transactions (both expenses and income) in chronological order
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val dashboardState by viewModel.dashboardState.collectAsState()

    // Modal sheet states
    var showEditExpenseSheet by remember { mutableStateOf(false) }
    var showEditIncomeSheet by remember { mutableStateOf(false) }
    var editExpenseId by remember { mutableStateOf(0L) }
    var editIncomeId by remember { mutableStateOf(0L) }

    Scaffold(
        topBar = {
            SpendlyTopAppBar(
                title = "All Transactions",
                onNavigationClick = onNavigateBack
            )
        },
        modifier = modifier
    ) { paddingValues ->
        when (val state = dashboardState) {
            is `in`.mylullaby.spendly.ui.screens.dashboard.DashboardUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }

            is `in`.mylullaby.spendly.ui.screens.dashboard.DashboardUiState.Success -> {
                if (!state.hasTransactions) {
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
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = state.recentTransactions,
                                key = { transaction ->
                                    when (transaction) {
                                        is RecentTransaction.ExpenseTransaction -> "expense_${transaction.expense.id}"
                                        is RecentTransaction.IncomeTransaction -> "income_${transaction.income.id}"
                                    }
                                }
                            ) { transaction ->
                                TransactionListItem(
                                    transaction = transaction,
                                    categories = state.allCategories,
                                    onClick = {
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
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }

            is `in`.mylullaby.spendly.ui.screens.dashboard.DashboardUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        message = "Error loading transactions",
                        description = state.message
                    )
                }
            }
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

/**
 * Single transaction item in the list
 */
@Composable
private fun TransactionListItem(
    transaction: RecentTransaction,
    categories: List<Category>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
    val categoryMap = categories.associateBy { it.id }

    when (transaction) {
        is RecentTransaction.ExpenseTransaction -> {
            val expense = transaction.expense
            val category = expense.categoryId?.let { categoryMap[it] }
            val formattedDate = dateFormatter.format(Date(expense.date))

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category icon
                Icon(
                    imageVector = IconMapper.getIcon(category?.icon ?: "category"),
                    contentDescription = category?.name ?: "Uncategorized",
                    tint = if (category != null) Color(category.color) else Color.Gray,
                    modifier = Modifier.padding(end = 12.dp)
                )

                // Description and details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = expense.description,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = expense.paymentMethod.toDisplayName(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Amount
                Text(
                    text = "- ${CurrencyUtils.formatPaise(expense.amount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFC62828) // Red for expense
                )
            }
        }

        is RecentTransaction.IncomeTransaction -> {
            val income = transaction.income
            val category = income.categoryId?.let { categoryMap[it] }
            val formattedDate = dateFormatter.format(Date(income.date))

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category icon (or generic income icon if no category)
                Icon(
                    imageVector = IconMapper.getIcon(category?.icon ?: "attach_money"),
                    contentDescription = category?.name ?: "Income",
                    tint = if (category != null) Color(category.color) else Color(0xFF2E7D32),
                    modifier = Modifier.padding(end = 12.dp)
                )

                // Description and details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = income.description,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = category?.name ?: income.source.toDisplayString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Amount
                Text(
                    text = "+ ${CurrencyUtils.formatPaise(income.amount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2E7D32) // Green for income
                )
            }
        }
    }
}
