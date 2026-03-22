package `in`.co.spendly.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CaretDown
import `in`.co.spendly.R
import `in`.co.spendly.utils.LockTimeout
import `in`.co.spendly.utils.toDisplayName

/**
 * Dropdown menu for selecting a lock timeout duration.
 * Displays timeout options in a scrollable list.
 *
 * @param selectedTimeout Currently selected lock timeout
 * @param onTimeoutSelected Callback when a timeout is selected
 * @param label Label for the dropdown field
 * @param modifier Optional modifier
 * @param enabled Whether the field is enabled
 */
@Composable
fun LockTimeoutDropdown(
    selectedTimeout: LockTimeout,
    onTimeoutSelected: (LockTimeout) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true
) {
    val displayLabel = label ?: stringResource(R.string.label_lock_timeout)
    var expanded by remember { mutableStateOf(false) }
    var buttonWidth by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current

    Box(modifier = modifier) {
        Column(
            modifier = Modifier.onSizeChanged { buttonWidth = it.width }
        ) {
            // Label text
            Text(
                text = displayLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
            )

            // Button trigger
            OutlinedButton(
                onClick = { if (enabled) expanded = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                contentPadding = PaddingValues(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Text
                    Text(
                        text = selectedTimeout.toDisplayName(),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start
                    )

                    // Trailing caret
                    Icon(
                        imageVector = PhosphorIcons.Regular.CaretDown,
                        contentDescription = stringResource(R.string.cd_select_lock_timeout),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Dropdown menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = with(density) { Modifier.width(buttonWidth.toDp()) }
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                LockTimeout.entries.forEach { timeout ->
                    DropdownMenuItem(
                        text = { Text(timeout.toDisplayName()) },
                        onClick = {
                            onTimeoutSelected(timeout)
                            expanded = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (selectedTimeout == timeout) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    Color.Transparent
                                }
                            )
                    )
                }
            }
        }
    }
}
