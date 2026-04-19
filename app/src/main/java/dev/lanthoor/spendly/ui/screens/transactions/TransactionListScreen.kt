package dev.lanthoor.spendly.ui.screens.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Funnel
import com.adamglin.phosphoricons.regular.MagicWand
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.core.model.finance.RecentTransaction
import dev.lanthoor.spendly.core.model.preferences.AiModelAvailability
import dev.lanthoor.spendly.ui.components.EditExpenseBottomSheet
import dev.lanthoor.spendly.ui.components.EditIncomeBottomSheet
import dev.lanthoor.spendly.ui.components.EmptyState
import dev.lanthoor.spendly.ui.components.LoadingIndicator
import dev.lanthoor.spendly.ui.screens.transactions.components.FilterBottomSheet
import dev.lanthoor.spendly.ui.screens.transactions.components.TransactionListItem

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
    val isEnrichmentRunning by viewModel.isEnrichmentRunning.collectAsStateWithLifecycle()
    val aiSettings by viewModel.aiSettings.collectAsStateWithLifecycle()
    val noEligibleRowsMessage = stringResource(R.string.msg_ai_enrichment_no_eligible_rows)

    // Modal sheet states
    var showEditExpenseSheet by remember { mutableStateOf(false) }
    var showEditIncomeSheet by remember { mutableStateOf(false) }
    var editExpenseId by remember { mutableLongStateOf(0L) }
    var editIncomeId by remember { mutableLongStateOf(0L) }
    var showFilterSheet by remember { mutableStateOf(false) }

    val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.enrichmentResultEvents.collect { result ->
            val message = if (result.attempted == 0) {
                noEligibleRowsMessage
            } else {
                "${result.enriched} enriched, ${result.failed} failed, ${result.skipped} skipped"
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    // Check if any filters are applied
    val hasActiveFilters = startDate != null || endDate != null ||
            selectedType != TransactionType.ALL ||
            selectedCategories.isNotEmpty()
    val isAiAvailable = aiSettings.availability == AiModelAvailability.AVAILABLE
    val aiRateLimited = aiSettings.lastErrorCode == "QUOTA_EXCEEDED" ||
            aiSettings.lastErrorCode == "RATE_LIMIT_EXCEEDED"
    val canRunAiEnrichment = !isEnrichmentRunning && !aiRateLimited

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_transactions_title)) },
                actions = {
                    if (isAiAvailable) {
                        IconButton(
                            onClick = {
                                val state = transactionListState
                                if (state is TransactionListUiState.Success) {
                                    viewModel.enrichTransactions(state.transactions)
                                }
                            },
                            enabled = canRunAiEnrichment
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.MagicWand,
                                contentDescription = stringResource(R.string.cd_ai_enrich_transactions)
                            )
                        }
                    }
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
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
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
                                enrichmentByKey = state.enrichmentByKey,
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
