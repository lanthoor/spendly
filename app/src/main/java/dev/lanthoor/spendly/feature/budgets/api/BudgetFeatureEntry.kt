package dev.lanthoor.spendly.feature.budgets.api

import androidx.compose.runtime.Composable
import dev.lanthoor.spendly.ui.screens.budgets.api.BudgetManagementScreen

@Composable
fun BudgetFeatureEntry(onNavigateBack: () -> Unit) {
    BudgetManagementScreen(onNavigateBack = onNavigateBack)
}
