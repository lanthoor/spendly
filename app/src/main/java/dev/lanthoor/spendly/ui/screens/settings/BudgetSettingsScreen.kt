package dev.lanthoor.spendly.ui.screens.settings

import androidx.compose.runtime.Composable
import dev.lanthoor.spendly.ui.screens.budgets.api.BudgetManagementScreen

@Composable
fun BudgetSettingsScreen(onNavigateBack: () -> Unit) {
    BudgetManagementScreen(onNavigateBack = onNavigateBack)
}
