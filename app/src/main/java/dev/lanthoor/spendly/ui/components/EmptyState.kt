package dev.lanthoor.spendly.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.lanthoor.spendly.R

/**
 * Empty state view to show when no data is available.
 * Displays a message and optional action button.
 *
 * @param message Primary message to display
 * @param modifier Optional modifier
 * @param description Optional secondary description text
 * @param actionLabel Optional action button label (null to hide button)
 * @param onActionClick Callback for action button click
 */
@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (description != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (actionLabel != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onActionClick) {
                Text(actionLabel)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStatePreview() {
    EmptyState(
        message = stringResource(R.string.label_no_expenses_yet),
        description = stringResource(R.string.msg_start_tracking_expenses),
        actionLabel = stringResource(R.string.button_add_expense),
        onActionClick = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun EmptyStateNoActionPreview() {
    EmptyState(
        message = stringResource(R.string.label_no_results_found),
        description = stringResource(R.string.msg_try_adjusting_filters)
    )
}
