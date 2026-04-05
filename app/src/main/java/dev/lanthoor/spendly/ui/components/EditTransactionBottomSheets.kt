package dev.lanthoor.spendly.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import dev.lanthoor.spendly.ui.screens.expenses.EditExpenseScreen
import dev.lanthoor.spendly.ui.screens.income.EditIncomeScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseBottomSheet(
    expenseId: Long,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        EditExpenseScreen(
            expenseId = expenseId,
            onNavigateBack = { onDismiss() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditIncomeBottomSheet(
    incomeId: Long,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        EditIncomeScreen(
            incomeId = incomeId,
            onDismiss = onDismiss,
            onSuccess = onDismiss,
            onDelete = onDismiss
        )
    }
}
