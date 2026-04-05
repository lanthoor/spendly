package dev.lanthoor.spendly.feature.income.api

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import dev.lanthoor.spendly.ui.screens.income.AddIncomeScreen
import dev.lanthoor.spendly.ui.screens.income.EditIncomeScreen

@Composable
fun AddIncomeFeatureEntry(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    resetTrigger: NavBackStackEntry
) {
    AddIncomeScreen(
        onDismiss = onDismiss,
        onSuccess = onSuccess,
        resetTrigger = resetTrigger
    )
}

@Composable
fun EditIncomeFeatureEntry(
    incomeId: Long,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    onDelete: () -> Unit
) {
    EditIncomeScreen(
        incomeId = incomeId,
        onDismiss = onDismiss,
        onSuccess = onSuccess,
        onDelete = onDelete
    )
}
