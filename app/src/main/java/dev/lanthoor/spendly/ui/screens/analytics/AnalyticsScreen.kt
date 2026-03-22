package dev.lanthoor.spendly.ui.screens.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.ui.screens.analytics.components.AnalyticsPeriodSegmentedButton
import dev.lanthoor.spendly.ui.screens.analytics.components.CategoryPieChart
import dev.lanthoor.spendly.ui.screens.analytics.components.SpendingTrendLineChart
import dev.lanthoor.spendly.ui.theme.isDark
import dev.lanthoor.spendly.utils.CurrencyUtils

/**
 * Analytics screen with segmented period selection (Financial Year / Calendar Year).
 * Shows pie chart (top) and spending trend line chart (bottom).
 */
@Composable
fun AnalyticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val analyticsState by viewModel.analyticsState.collectAsStateWithLifecycle()
    val selectedPeriodType by viewModel.selectedPeriodType.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AnalyticsTopAppBar(
                selectedPeriod = selectedPeriodType,
                onPeriodSelected = { viewModel.selectPeriodType(it) }
            )
        }
    ) { paddingValues ->
        when (val state = analyticsState) {
            is AnalyticsUiState.Loading -> {
                LoadingState(modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues))
            }

            is AnalyticsUiState.Empty -> {
                EmptyState(modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues))
            }

            is AnalyticsUiState.Success -> {
                AnalyticsContent(
                    state = state,
                    periodType = selectedPeriodType,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            is AnalyticsUiState.Error -> {
                ErrorState(
                    message = state.message,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = stringResource(R.string.msg_no_data_available),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.msg_add_transactions_for_analytics),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = stringResource(R.string.msg_error_loading_analytics),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun AnalyticsContent(
    state: AnalyticsUiState.Success,
    periodType: AnalyticsPeriodType,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.isDark
    val pieChartData = state.getPieChartData(isDark)

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top spacing
        item {
            Box(modifier = Modifier.padding(top = 16.dp))
        }

        // Total spending summary
        item {
            TotalExpenseSummary(totalExpense = state.totalExpense)
        }

        // Pie chart (centered horizontally)
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CategoryPieChart(
                    data = pieChartData,
                    modifier = Modifier.fillMaxWidth(0.95f)
                )
            }
        }

        // Spending trend line chart
        item {
            val periodLabel = when (periodType) {
                AnalyticsPeriodType.FINANCIAL_YEAR -> stringResource(R.string.label_this_financial_year)
                AnalyticsPeriodType.CALENDAR_YEAR -> stringResource(R.string.label_this_year)
            }

            SpendingTrendLineChart(
                incomeData = state.incomeTrendData,
                expenseData = state.expenseTrendData,
                netWorthData = state.netWorthTrendData,
                periodLabel = periodLabel
            )
        }

        // Bottom spacing
        item {
            Box(modifier = Modifier.padding(bottom = 16.dp))
        }
    }
}

@Composable
private fun TotalExpenseSummary(
    totalExpense: Long,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.label_total_spending),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = CurrencyUtils.paiseToRupeeString(totalExpense),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Custom top app bar with centered segmented button for period selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnalyticsTopAppBar(
    selectedPeriod: AnalyticsPeriodType,
    onPeriodSelected: (AnalyticsPeriodType) -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            // Segmented button centered in the title area
            AnalyticsPeriodSegmentedButton(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = onPeriodSelected
            )
        },
        navigationIcon = {
            // Empty to remove reserved navigation icon space
        },
        actions = {
            // Empty to remove reserved actions space
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
    )
}
