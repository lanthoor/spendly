package dev.lanthoor.spendly.feature.expenses.api

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import dev.lanthoor.spendly.ui.screens.expenses.AddExpenseScreen
import dev.lanthoor.spendly.ui.screens.expenses.EditExpenseScreen

@Composable
fun AddExpenseFeatureEntry(
    onNavigateBack: (String?) -> Unit,
    resetTrigger: NavBackStackEntry
) {
    AddExpenseScreen(
        onNavigateBack = onNavigateBack,
        resetTrigger = resetTrigger
    )
}

@Composable
fun EditExpenseFeatureEntry(
    expenseId: Long,
    onNavigateBack: (String?) -> Unit
) {
    EditExpenseScreen(
        expenseId = expenseId,
        onNavigateBack = onNavigateBack
    )
}
