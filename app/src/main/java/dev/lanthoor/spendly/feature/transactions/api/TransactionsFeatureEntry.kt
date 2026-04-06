package dev.lanthoor.spendly.feature.transactions.api

import androidx.compose.runtime.Composable
import dev.lanthoor.spendly.ui.screens.transactions.TransactionListScreen

@Composable
fun TransactionsFeatureEntry(onNavigateBack: (() -> Unit)?) {
    TransactionListScreen(onNavigateBack = onNavigateBack)
}
