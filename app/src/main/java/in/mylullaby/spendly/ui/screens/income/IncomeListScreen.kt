package `in`.mylullaby.spendly.ui.screens.income

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.MagnifyingGlass
import com.adamglin.phosphoricons.regular.Plus
import `in`.mylullaby.spendly.ui.components.EmptyState
import `in`.mylullaby.spendly.ui.components.LoadingIndicator
import `in`.mylullaby.spendly.ui.components.SpendlyTopAppBar
import `in`.mylullaby.spendly.ui.screens.income.components.IncomeListItem
import kotlinx.coroutines.launch

/**
 * Screen displaying list of income with filtering options.
 * Uses IncomeViewModel for data and state management.
 * Add and Edit income are shown in modal bottom sheets.
 *
 * @param onNavigateToAdd Callback to navigate to add income screen (deprecated, kept for compatibility)
 * @param onNavigateToEdit Callback to navigate to edit income screen with income ID (deprecated, kept for compatibility)
 * @param viewModel IncomeViewModel instance (injected by Hilt)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeListScreen(
    onNavigateToAdd: () -> Unit = {},
    onNavigateToEdit: (Long) -> Unit = {},
    viewModel: IncomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showAddSheet by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }
    var editIncomeId by remember { mutableStateOf(0L) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            SpendlyTopAppBar(
                title = "Income",
                actions = {
                    IconButton(onClick = { /* TODO: Open filter sheet */ }) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.MagnifyingGlass,
                            contentDescription = "Filter income"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(
                    imageVector = PhosphorIcons.Regular.Plus,
                    contentDescription = "Add income"
                )
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is IncomeListUiState.Loading -> {
                LoadingIndicator(
                    message = "Loading income...",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            is IncomeListUiState.Success -> {
                if (state.incomes.isEmpty()) {
                    EmptyState(
                        message = "No income yet",
                        description = "Start tracking your income by adding your first one",
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
                        // Total income header
                        item {
                            Box(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Total: ${state.totalIncome}",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }

                        // Income list
                        items(
                            items = state.incomes,
                            key = { income -> income.id }
                        ) { income ->
                            IncomeListItem(
                                income = income,
                                onClick = {
                                    editIncomeId = income.id
                                    showEditSheet = true
                                }
                            )
                        }
                    }
                }
            }

            is IncomeListUiState.Error -> {
                EmptyState(
                    message = "Error loading income",
                    description = state.message,
                    actionLabel = "Retry",
                    onActionClick = { viewModel.loadIncomes() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }

    // Add Income Bottom Sheet
    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showAddSheet = false
                viewModel.resetForm()
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            AddIncomeScreen(
                viewModel = viewModel,
                onDismiss = {
                    showAddSheet = false
                    viewModel.resetForm()
                },
                onSuccess = {
                    showAddSheet = false
                    viewModel.resetForm()
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Income added successfully")
                    }
                }
            )
        }
    }

    // Edit Income Bottom Sheet
    if (showEditSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showEditSheet = false
                viewModel.resetForm()
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            EditIncomeScreen(
                incomeId = editIncomeId,
                viewModel = viewModel,
                onDismiss = {
                    showEditSheet = false
                    viewModel.resetForm()
                },
                onSuccess = {
                    showEditSheet = false
                    viewModel.resetForm()
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Income updated successfully")
                    }
                },
                onDelete = {
                    showEditSheet = false
                    viewModel.resetForm()
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Income deleted successfully")
                    }
                }
            )
        }
    }
}
