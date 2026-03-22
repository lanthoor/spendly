package dev.lanthoor.spendly.ui.screens.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ChatText
import com.adamglin.phosphoricons.regular.Funnel
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.domain.model.Account
import dev.lanthoor.spendly.domain.model.Category
import dev.lanthoor.spendly.ui.components.EmptyState
import dev.lanthoor.spendly.ui.components.IconMapper
import dev.lanthoor.spendly.ui.components.LoadingIndicator
import dev.lanthoor.spendly.ui.screens.dashboard.RecentTransaction
import dev.lanthoor.spendly.ui.screens.expenses.EditExpenseScreen
import dev.lanthoor.spendly.ui.screens.income.EditIncomeScreen
import dev.lanthoor.spendly.ui.screens.transactions.components.FilterBottomSheet
import dev.lanthoor.spendly.ui.theme.adjustForTheme
import dev.lanthoor.spendly.ui.theme.expenseColor
import dev.lanthoor.spendly.ui.theme.incomeColor
import dev.lanthoor.spendly.ui.theme.isDark
import dev.lanthoor.spendly.utils.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Screen showing all transactions (both expenses and income) in chronological order
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    viewModel: TransactionListViewModel = hiltViewModel()
) {
    val transactionListState by viewModel.transactionListState.collectAsStateWithLifecycle()
    val startDate by viewModel.startDate.collectAsStateWithLifecycle()
    val endDate by viewModel.endDate.collectAsStateWithLifecycle()
    val selectedType by viewModel.selectedType.collectAsStateWithLifecycle()
    val selectedCategories by viewModel.selectedCategories.collectAsStateWithLifecycle()

    // Modal sheet states
    var showEditExpenseSheet by remember { mutableStateOf(false) }
    var showEditIncomeSheet by remember { mutableStateOf(false) }
    var editExpenseId by remember { mutableLongStateOf(0L) }
    var editIncomeId by remember { mutableLongStateOf(0L) }
    var showFilterSheet by remember { mutableStateOf(false) }

    val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // Check if any filters are applied
    val hasActiveFilters = startDate != null || endDate != null ||
            selectedType != TransactionType.ALL ||
            selectedCategories.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_transactions_title)) },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        BadgedBox(
                            badge = {
                                if (hasActiveFilters) {
                                    Badge()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.Funnel,
                                contentDescription = stringResource(R.string.label_filter)
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        when (val state = transactionListState) {
            is TransactionListUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }

            is TransactionListUiState.Success -> {
                if (!state.hasTransactions) {
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
                } else if (state.transactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            message = stringResource(R.string.empty_no_matching_transactions_title),
                            description = stringResource(R.string.empty_no_matching_transactions_desc)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = state.transactions,
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
                                accounts = state.allAccounts,
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

            is TransactionListUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        message = stringResource(R.string.error_loading_transactions),
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

    // Filter Bottom Sheet
    if (showFilterSheet) {
        FilterBottomSheet(
            startDate = startDate,
            endDate = endDate,
            selectedType = selectedType,
            selectedCategories = selectedCategories,
            allCategories = when (val state = transactionListState) {
                is TransactionListUiState.Success -> state.allCategories
                else -> emptyList()
            },
            allTransactions = when (val state = transactionListState) {
                is TransactionListUiState.Success -> state.allTransactions
                else -> emptyList()
            },
            onApplyFilters = { start, end, type, categories ->
                viewModel.setDateRange(start, end)
                viewModel.setTransactionType(type)
                // Update selected categories
                val currentCategories = selectedCategories
                val toRemove = currentCategories - categories
                val toAdd = categories - currentCategories
                toRemove.forEach { viewModel.toggleCategory(it) }
                toAdd.forEach { viewModel.toggleCategory(it) }
            },
            onClearFilters = {
                viewModel.setDateRange(null, null)
                viewModel.setTransactionType(TransactionType.ALL)
                viewModel.clearCategoryFilters()
            },
            onDismiss = { showFilterSheet = false },
            sheetState = filterSheetState
        )
    }
}

/**
 * Single transaction item in the list
 */
@Composable
private fun TransactionListItem(
    transaction: RecentTransaction,
    categories: List<Category>,
    accounts: List<Account>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val categoryMap = categories.associateBy { it.id }
    val accountMap = accounts.associateBy { it.id }

    when (transaction) {
        is RecentTransaction.ExpenseTransaction -> {
            val expense = transaction.expense
            val category = expense.categoryId?.let { categoryMap[it] }
            val account = accountMap[expense.accountId]
            val formattedDate = dateFormatter.format(Date(expense.date))

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category icon
                val isDark = MaterialTheme.colorScheme.isDark
                Icon(
                    imageVector = IconMapper.getIcon(category?.icon ?: "category"),
                    contentDescription = category?.name ?: stringResource(R.string.label_others),
                    tint = if (category != null) Color(category.color).adjustForTheme(isDark) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 12.dp)
                )

                // Description and details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = expense.description,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        // SMS envelope icon if transaction was created from SMS
                        if (expense.smsBody != null) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.ChatText,
                                contentDescription = stringResource(R.string.cd_created_from_sms),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = "$formattedDate • ${account?.name ?: stringResource(R.string.label_unknown)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Amount
                Text(
                    text = "- ${CurrencyUtils.formatPaise(expense.amount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.expenseColor()
                )
            }
        }

        is RecentTransaction.IncomeTransaction -> {
            val income = transaction.income
            val category = income.categoryId?.let { categoryMap[it] }
            val account = accountMap[income.accountId]
            val formattedDate = dateFormatter.format(Date(income.date))

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category icon (or generic income icon if no category)
                val isDarkIncome = MaterialTheme.colorScheme.isDark
                Icon(
                    imageVector = IconMapper.getIcon(category?.icon ?: "attach_money"),
                    contentDescription = category?.name ?: stringResource(R.string.label_income),
                    tint = if (category != null) Color(category.color).adjustForTheme(isDarkIncome) else MaterialTheme.colorScheme.incomeColor(),
                    modifier = Modifier.padding(end = 12.dp)
                )

                // Description and details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = income.description,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        // SMS envelope icon if transaction was created from SMS
                        if (income.smsBody != null) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.ChatText,
                                contentDescription = stringResource(R.string.cd_created_from_sms),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = "$formattedDate • ${account?.name ?: stringResource(R.string.label_unknown)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Amount
                Text(
                    text = "+ ${CurrencyUtils.formatPaise(income.amount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.incomeColor()
                )
            }
        }
    }
}
