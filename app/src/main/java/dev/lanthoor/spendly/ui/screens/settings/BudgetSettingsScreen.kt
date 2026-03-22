package dev.lanthoor.spendly.ui.screens.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.lanthoor.spendly.ui.components.EmptyState
import dev.lanthoor.spendly.ui.components.SpendlyTopAppBar
import dev.lanthoor.spendly.ui.screens.budgets.AddBudgetScreen
import dev.lanthoor.spendly.ui.screens.budgets.BudgetListUiState
import dev.lanthoor.spendly.ui.screens.budgets.BudgetViewModel
import dev.lanthoor.spendly.ui.screens.budgets.EditBudgetScreen
import dev.lanthoor.spendly.ui.screens.settings.components.BudgetSettingsSection
import kotlinx.coroutines.launch

/**
 * Budget settings screen showing all configured budgets.
 * Similar to AccountListScreen structure.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val budgetUiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showAddBudgetSheet by remember { mutableStateOf(false) }
    var showEditBudgetSheet by remember { mutableStateOf(false) }
    var editBudgetId by remember { mutableLongStateOf(0L) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            SpendlyTopAppBar(
                title = "Manage Budgets",
                onNavigationClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        when (val state = budgetUiState) {
            is BudgetListUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is BudgetListUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    item {
                        BudgetSettingsSection(
                            budgets = state.budgets,
                            onAddBudget = { showAddBudgetSheet = true },
                            onEditBudget = { budgetId ->
                                editBudgetId = budgetId
                                showEditBudgetSheet = true
                            }
                        )
                    }
                }
            }

            is BudgetListUiState.Error -> {
                EmptyState(
                    message = "Error loading budgets",
                    description = state.message,
                    actionLabel = "Retry",
                    onActionClick = { viewModel.loadBudgets() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }

    // Add Budget Bottom Sheet
    if (showAddBudgetSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showAddBudgetSheet = false
                viewModel.resetForm()
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            AddBudgetScreen(
                viewModel = viewModel,
                onDismiss = {
                    showAddBudgetSheet = false
                    viewModel.resetForm()
                },
                onSuccess = {
                    showAddBudgetSheet = false
                    viewModel.resetForm()
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Budget added successfully")
                    }
                }
            )
        }
    }

    // Edit Budget Bottom Sheet
    if (showEditBudgetSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showEditBudgetSheet = false
                viewModel.resetForm()
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            EditBudgetScreen(
                budgetId = editBudgetId,
                viewModel = viewModel,
                onDismiss = {
                    showEditBudgetSheet = false
                    viewModel.resetForm()
                },
                onSuccess = {
                    showEditBudgetSheet = false
                    viewModel.resetForm()
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Budget updated successfully")
                    }
                },
                onDelete = {
                    showEditBudgetSheet = false
                    viewModel.resetForm()
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Budget deleted successfully")
                    }
                }
            )
        }
    }
}
