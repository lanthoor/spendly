package `in`.co.spendly.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.co.spendly.utils.AppTheme
import `in`.co.spendly.utils.toDisplayName

/**
 * Segmented button control for theme selection.
 * Displays Light | Dark | System options inline.
 *
 * @param selectedTheme Currently selected theme
 * @param onThemeSelected Callback when theme is changed
 * @param modifier Optional modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSegmentedButton(
    selectedTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        AppTheme.entries.forEachIndexed { index, theme ->
            SegmentedButton(
                selected = selectedTheme == theme,
                onClick = { onThemeSelected(theme) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = AppTheme.entries.size
                )
            ) {
                Text(
                    text = theme.toDisplayName(),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
