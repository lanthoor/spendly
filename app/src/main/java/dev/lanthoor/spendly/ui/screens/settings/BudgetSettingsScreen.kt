package dev.lanthoor.spendly.ui.screens.settings

import androidx.compose.runtime.Composable
import dev.lanthoor.spendly.feature.budgets.api.BudgetFeatureEntry

@Composable
fun BudgetSettingsScreen(onNavigateBack: () -> Unit) {
    BudgetFeatureEntry(onNavigateBack = onNavigateBack)
}
