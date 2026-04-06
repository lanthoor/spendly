package dev.lanthoor.spendly.feature.dashboard.api

import androidx.compose.runtime.Composable
import dev.lanthoor.spendly.ui.screens.dashboard.DashboardScreen

@Composable
fun DashboardFeatureEntry(onNavigateToBudgets: () -> Unit) {
    DashboardScreen(onNavigateToBudgets = onNavigateToBudgets)
}
