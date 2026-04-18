package dev.lanthoor.spendly.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.lanthoor.spendly.domain.usecase.analytics.AnalyticsPeriod
import dev.lanthoor.spendly.domain.usecase.analytics.AnalyticsStateInput
import dev.lanthoor.spendly.domain.usecase.analytics.BuildAnalyticsStateUseCase
import dev.lanthoor.spendly.domain.model.Category
import dev.lanthoor.spendly.domain.model.Expense
import dev.lanthoor.spendly.domain.model.Income
import dev.lanthoor.spendly.domain.model.LineChartEntry
import dev.lanthoor.spendly.domain.model.PieChartEntry
import dev.lanthoor.spendly.domain.repository.CategoryRepository
import dev.lanthoor.spendly.domain.repository.ExpenseRepository
import dev.lanthoor.spendly.domain.repository.IncomeRepository
import dev.lanthoor.spendly.utils.ChartDataTransformer
import dev.lanthoor.spendly.core.model.preferences.TimePeriod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel for Analytics screen.
 * Manages time period selection and provides chart data for various analytics visualizations.
 */
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val categoryRepository: CategoryRepository,
    private val buildAnalyticsStateUseCase: BuildAnalyticsStateUseCase
) : ViewModel() {

    // Period type selection (FY / Calendar Year)
    private val _selectedPeriodType = MutableStateFlow(AnalyticsPeriodType.FINANCIAL_YEAR)
    val selectedPeriodType: StateFlow<AnalyticsPeriodType> = _selectedPeriodType

    /**
     * Select analytics period type (FY / Year).
     */
    fun selectPeriodType(periodType: AnalyticsPeriodType) {
        _selectedPeriodType.value = periodType
    }

    /**
     * Combined analytics state with chart data.
     */
    val analyticsState: StateFlow<AnalyticsUiState> = combine(
        expenseRepository.getAllExpenses(),
        incomeRepository.getAllIncome(),
        categoryRepository.getAllCategories(),
        _selectedPeriodType
    ) { expenses, incomes, categories, periodType ->
        try {
            val stateResult = buildAnalyticsStateUseCase.execute(
                AnalyticsStateInput(
                    expenses = expenses,
                    incomes = incomes,
                    categories = categories,
                    period = periodType.toDomainPeriod()
                )
            )

            // If no transactions in period, return empty state
            if (stateResult.filteredExpenses.isEmpty() && stateResult.filteredIncome.isEmpty()) {
                return@combine AnalyticsUiState.Empty(stateResult.period)
            }

            AnalyticsUiState.Success(
                period = stateResult.period,
                totalExpense = stateResult.totalExpense,
                totalIncome = stateResult.totalIncome,
                netBalance = stateResult.netBalance,
                prevExpense = stateResult.prevExpense,
                prevIncome = stateResult.prevIncome,
                prevNetBalance = stateResult.prevNetBalance,
                expenseChange = stateResult.expenseChange,
                incomeChange = stateResult.incomeChange,
                balanceChange = stateResult.balanceChange,
                categoryPieChartData = emptyList(), // Will be calculated in the UI state
                filteredExpenses = stateResult.filteredExpenses,
                filteredIncome = stateResult.filteredIncome,
                categories = stateResult.categories,
                expenseTrendData = stateResult.expenseTrendData,
                incomeTrendData = stateResult.incomeTrendData,
                netWorthTrendData = stateResult.netWorthTrendData
            )
        } catch (e: Exception) {
            AnalyticsUiState.Error(e.message ?: "Failed to load analytics data")
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsUiState.Loading
    )

    private fun AnalyticsPeriodType.toDomainPeriod(): AnalyticsPeriod = when (this) {
        AnalyticsPeriodType.FINANCIAL_YEAR -> AnalyticsPeriod.FINANCIAL_YEAR
        AnalyticsPeriodType.CALENDAR_YEAR -> AnalyticsPeriod.CALENDAR_YEAR
    }

}

/**
 * UI state for Analytics screen.
 */
sealed interface AnalyticsUiState {
    /**
     * Initial loading state.
     */
    data object Loading : AnalyticsUiState

    /**
     * Empty state - no transactions in selected period.
     */
    data class Empty(val period: TimePeriod) : AnalyticsUiState

    /**
     * Success state with analytics data.
     */
    data class Success(
        val period: TimePeriod,
        val totalExpense: Long,
        val totalIncome: Long,
        val netBalance: Long,

        // Previous period comparison
        val prevExpense: Long,
        val prevIncome: Long,
        val prevNetBalance: Long,
        val expenseChange: Float,
        val incomeChange: Float,
        val balanceChange: Float,

        val categoryPieChartData: List<PieChartEntry>,
        val filteredExpenses: List<Expense>,
        val filteredIncome: List<Income>,
        val categories: List<Category>,

        // Trend data (monthly aggregation for FY/Year)
        val expenseTrendData: List<LineChartEntry>,
        val incomeTrendData: List<LineChartEntry>,
        val netWorthTrendData: List<LineChartEntry>
    ) : AnalyticsUiState {
        /**
         * Generate pie chart data with theme awareness.
         * Called from UI layer when theme is known.
         */
        fun getPieChartData(isDark: Boolean): List<PieChartEntry> {
            @Suppress("UNUSED_PARAMETER")
            val ignored = isDark
            return ChartDataTransformer.expensesToPieChartData(
                expenses = filteredExpenses,
                categories = categories
            )
        }
    }

    /**
     * Error state.
     */
    data class Error(val message: String) : AnalyticsUiState
}
