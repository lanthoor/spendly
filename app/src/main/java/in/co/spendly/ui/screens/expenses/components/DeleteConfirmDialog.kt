package `in`.co.spendly.ui.screens.expenses.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import `in`.co.spendly.R

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
        title = { Text(stringResource(R.string.title_delete_expense)) },
        text = {
            Text(
                stringResource(R.string.msg_delete_expense_body)
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    stringResource(R.string.button_delete),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_cancel))
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
