package `in`.co.spendly.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import `in`.co.spendly.utils.AccountType
import `in`.co.spendly.utils.getDefaultIcon
import `in`.co.spendly.utils.toDisplayName

/**
 * Dropdown menu for selecting an account type.
 * Displays account type icon and name for each option in a scrollable list.
 *
 * @param selectedType Currently selected account type
 * @param onTypeSelected Callback when a type is selected
 * @param label Label for the dropdown field
 * @param modifier Optional modifier
 * @param enabled Whether the field is enabled
 */
@Composable
fun AccountTypeDropdown(
    selectedType: AccountType,
    onTypeSelected: (AccountType) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true
) {
    val displayLabel = label ?: stringResource(R.string.title_select_account_type)
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
                    // Leading icon
                    Icon(
                        imageVector = IconMapper.getIcon(selectedType.getDefaultIcon()),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // Text
                    Text(
                        text = selectedType.toDisplayName(),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start
                    )

                    // Trailing caret
                    Icon(
                        imageVector = PhosphorIcons.Regular.CaretDown,
                        contentDescription = stringResource(R.string.cd_select_account_type),
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
                AccountType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.toDisplayName()) },
                        onClick = {
                            onTypeSelected(type)
                            expanded = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = IconMapper.getIcon(type.getDefaultIcon()),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (selectedType == type) {
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
