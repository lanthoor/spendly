package `in`.mylullaby.spendly.ui.screens.expenses

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.MagnifyingGlass
import com.adamglin.phosphoricons.regular.Plus
import `in`.mylullaby.spendly.ui.components.EmptyState
import `in`.mylullaby.spendly.ui.components.LoadingIndicator
import `in`.mylullaby.spendly.ui.components.SpendlyTopAppBar
import `in`.mylullaby.spendly.ui.screens.expenses.components.ExpenseListItem

/**
 * Screen displaying list of expenses with filtering options.
 * Uses ExpenseViewModel for data and state management.
 * Add and Edit expenses are shown in modal bottom sheets.
 *
 * @param onNavigateToAdd Callback to navigate to add expense screen (deprecated, kept for compatibility)
 * @param onNavigateToEdit Callback to navigate to edit expense screen with expense ID (deprecated, kept for compatibility)
 * @param viewModel ExpenseViewModel instance (injected by Hilt)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    onNavigateToAdd: () -> Unit = {},
    onNavigateToEdit: (Long) -> Unit = {},
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showAddSheet by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }
    var editExpenseId by remember { mutableStateOf(0L) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            SpendlyTopAppBar(
                title = "Expenses",
                actions = {
                    IconButton(onClick = { /* TODO: Open filter sheet */ }) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.MagnifyingGlass,
                            contentDescription = "Filter expenses"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(
                    imageVector = PhosphorIcons.Regular.Plus,
                    contentDescription = "Add expense"
                )
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is ExpenseListUiState.Loading -> {
                LoadingIndicator(
                    message = "Loading expenses...",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            is ExpenseListUiState.Success -> {
                if (state.expenses.isEmpty()) {
                    EmptyState(
                        message = "No expenses yet",
                        description = "Start tracking your expenses by adding your first one",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        // Total spent header
                        item {
                            Box(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Total: ${state.totalSpent}",
                                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge
                                )
                            }
                        }

                        // Expense list
                        items(
                            items = state.expenses,
                            key = { expense -> expense.id }
                        ) { expense ->
                            val category = expense.categoryId?.let { id ->
                                categories.find { it.id == id }
                            }

                            ExpenseListItem(
                                expense = expense,
                                category = category,
                                onClick = {
                                    editExpenseId = expense.id
                                    showEditSheet = true
                                }
                            )
                        }
                    }
                }
            }

            is ExpenseListUiState.Error -> {
                EmptyState(
                    message = "Error loading expenses",
                    description = state.message,
                    actionLabel = "Retry",
                    onActionClick = { viewModel.loadExpenses() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }

    // Add Expense Bottom Sheet
    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            AddExpenseScreen(
                onNavigateBack = { message ->
                    showAddSheet = false
                    message?.let {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(it)
                        }
                    }
                }
            )
        }
    }

    // Edit Expense Bottom Sheet
    if (showEditSheet) {
        ModalBottomSheet(
            onDismissRequest = { showEditSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            EditExpenseScreen(
                expenseId = editExpenseId,
                onNavigateBack = { message ->
                    showEditSheet = false
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

@Preview(showBackground = true)
@Composable
private fun ExpenseListScreenPreview() {
    // Preview would require mocked ViewModel - skipping for now
    Text("ExpenseListScreen Preview")
}
