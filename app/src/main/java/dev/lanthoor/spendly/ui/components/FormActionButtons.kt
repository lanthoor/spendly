package dev.lanthoor.spendly.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.lanthoor.spendly.R

/**
 * Reusable action buttons for forms (Cancel + Save).
 * Provides a consistent button pattern across all form screens.
 *
 * @param onSave Callback when save button is clicked
 * @param onCancel Callback when cancel button is clicked (optional)
 * @param isSaving Whether the form is currently saving (shows loading indicator)
 * @param saveLabel Label for the save button (default: "Save")
 * @param cancelLabel Label for the cancel button (default: "Cancel")
 * @param modifier Optional modifier
 * @param enabled Whether the buttons are enabled (default: true, but save button respects isSaving)
 */
@Composable
fun FormActionButtons(
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    onCancel: (() -> Unit)? = null,
    isSaving: Boolean = false,
    saveLabel: String? = null,
    cancelLabel: String? = null,
    enabled: Boolean = true
) {
    val saveText = saveLabel ?: stringResource(R.string.button_save)
    val cancelText = cancelLabel ?: stringResource(R.string.button_cancel)
    if (onCancel != null) {
        // Two-button layout (Cancel + Save)
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                enabled = enabled && !isSaving
            ) {
                Text(cancelText)
            }

            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                enabled = enabled && !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(saveText)
                }
            }
        }
    } else {
        // Single save button layout
        Button(
            onClick = onSave,
            modifier = modifier.fillMaxWidth(),
            enabled = enabled && !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(saveText)
            }
        }
    }
}
