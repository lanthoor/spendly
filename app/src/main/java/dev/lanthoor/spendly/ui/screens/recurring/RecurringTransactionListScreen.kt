package dev.lanthoor.spendly.ui.screens.recurring

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Plus
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.ui.components.EmptyState
import dev.lanthoor.spendly.ui.components.LoadingIndicator
import dev.lanthoor.spendly.ui.components.SpendlyTopAppBar
import dev.lanthoor.spendly.ui.screens.recurring.components.RecurringTransactionListItem
import kotlinx.coroutines.launch

/**
 * List screen for recurring transactions.
 * Uses modal bottom sheets for add/edit screens.
 *
 * @param onNavigateBack Callback to navigate back (deprecated, kept for compatibility)
 * @param onNavigateToAdd Callback to navigate to add screen (deprecated, kept for compatibility)
 * @param onNavigateToEdit Callback to navigate to edit screen (deprecated, kept for compatibility)
 * @param viewModel ViewModel for the screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringTransactionListScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToAdd: () -> Unit = {},
    onNavigateToEdit: (Long) -> Unit = {},
    viewModel: RecurringTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showAddSheet by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }
    var editTransactionId by remember { mutableLongStateOf(0L) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            SpendlyTopAppBar(
                title = stringResource(R.string.settings_recurring_transactions),
                onNavigationClick = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(
                    imageVector = PhosphorIcons.Regular.Plus,
                    contentDescription = stringResource(R.string.cd_add_recurring_transaction)
                )
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is RecurringTransactionListUiState.Loading -> {
                LoadingIndicator(
                    message = "Loading recurring transactions...",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            is RecurringTransactionListUiState.Success -> {
                if (state.transactions.isEmpty()) {
                    EmptyState(
                        message = "No recurring transactions yet",
                        description = "Start by adding your first recurring transaction",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
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
                            key = { it.id }
                        ) { transaction ->
                            val category = allCategories.find { it.id == transaction.categoryId }
                            val account = accounts.find { it.id == transaction.accountId }

                            RecurringTransactionListItem(
                                recurringTransaction = transaction,
                                category = category,
                                account = account,
                                onClick = {
                                    editTransactionId = transaction.id
                                    showEditSheet = true
                                }
                            )
                        }
                    }
                }
            }

            is RecurringTransactionListUiState.Error -> {
                EmptyState(
                    message = "Error loading recurring transactions",
                    description = state.message,
                    actionLabel = "Retry",
                    onActionClick = { viewModel.loadRecurringTransactions() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }

    // Add Recurring Transaction Bottom Sheet
    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showAddSheet = false
                viewModel.resetForm()
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            AddRecurringTransactionScreen(
                onNavigateBack = { message ->
                    showAddSheet = false
                    viewModel.resetForm()
                    message?.let {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(it)
                        }
                    }
                }
            )
        }
    }

    // Edit Recurring Transaction Bottom Sheet
    if (showEditSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showEditSheet = false
                viewModel.resetForm()
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            EditRecurringTransactionScreen(
                recurringTransactionId = editTransactionId,
                onNavigateBack = { message ->
                    showEditSheet = false
                    viewModel.resetForm()
                    message?.let {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(it)
                        }
                    }
                }
            )
        }
    }
}
