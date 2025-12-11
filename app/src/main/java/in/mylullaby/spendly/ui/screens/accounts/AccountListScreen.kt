package `in`.mylullaby.spendly.ui.screens.accounts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Plus
import `in`.mylullaby.spendly.domain.model.Account
import `in`.mylullaby.spendly.ui.components.EmptyState
import `in`.mylullaby.spendly.ui.components.LoadingIndicator
import `in`.mylullaby.spendly.ui.components.SpendlyTopAppBar
import `in`.mylullaby.spendly.ui.screens.accounts.components.AccountListItem
import `in`.mylullaby.spendly.utils.AccountType
import `in`.mylullaby.spendly.utils.toDisplayName

/**
 * Screen displaying all accounts grouped by type.
 * Shows transaction counts and allows navigation to add/edit.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddAccount: () -> Unit,
    onNavigateToEditAccount: (Long) -> Unit,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showAddSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SpendlyTopAppBar(
                title = "Manage Accounts",
                onNavigationClick = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true }
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.Plus,
                    contentDescription = "Add account"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is AccountUiState.Loading -> LoadingIndicator()
                is AccountUiState.Error -> {
                    EmptyState(
                        message = state.message
                    )
                }
                is AccountUiState.Success -> {
                    if (state.accounts.isEmpty()) {
                        EmptyState(
                            message = "No accounts yet. Add your first account!"
                        )
                    } else {
                        AccountList(
                            accounts = state.accounts,
                            viewModel = viewModel,
                            onAccountClick = { account ->
                                onNavigateToEditAccount(account.id)
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Account Bottom Sheet
    if (showAddSheet) {
        AddAccountScreen(
            onDismiss = { showAddSheet = false },
            viewModel = viewModel
        )
    }
}

@Composable
private fun AccountList(
    accounts: List<Account>,
    viewModel: AccountViewModel,
    onAccountClick: (Account) -> Unit
) {
    // Group accounts by type
    val groupedAccounts = accounts.groupBy { it.type }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Iterate through all account types to maintain order
        AccountType.entries.forEach { type ->
            val accountsOfType = groupedAccounts[type] ?: emptyList()
            if (accountsOfType.isNotEmpty()) {
                // Type Header
                item(key = "header_$type") {
                    Text(
                        text = type.toDisplayName(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Accounts of this type
                items(
                    items = accountsOfType,
                    key = { it.id }
                ) { account ->
                    val transactionCount by viewModel.getTransactionCount(account.id)
                        .collectAsState()

                    AccountListItem(
                        account = account,
                        transactionCount = transactionCount,
                        onClick = { onAccountClick(account) }
                    )
                }
            }
        }
    }
}
