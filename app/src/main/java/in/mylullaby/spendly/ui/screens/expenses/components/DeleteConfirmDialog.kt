package `in`.mylullaby.spendly.ui.screens.expenses.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

/**
 * Confirmation dialog for deleting an expense.
 * Shows warning message about permanent deletion.
 *
 * @param onConfirm Callback when user confirms deletion
 * @param onDismiss Callback when user dismisses dialog
 * @param modifier Optional modifier
 */
@Composable
fun DeleteConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Expense?") },
        text = {
            Text(
                "This will permanently delete this expense and all associated receipts. " +
                "This action cannot be undone."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    "Delete",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = modifier
    )
}

@Preview
@Composable
private fun DeleteConfirmDialogPreview() {
    DeleteConfirmDialog(
        onConfirm = {},
        onDismiss = {}
    )
}
