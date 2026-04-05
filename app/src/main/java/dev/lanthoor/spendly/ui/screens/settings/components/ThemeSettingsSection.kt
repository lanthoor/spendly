package dev.lanthoor.spendly.ui.screens.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.lanthoor.spendly.core.model.preferences.AppTheme
import dev.lanthoor.spendly.ui.components.ThemeSegmentedButton

@Composable
fun ThemeSettingsSection(
    selectedTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    label: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        ThemeSegmentedButton(
            selectedTheme = selectedTheme,
            onThemeSelected = onThemeSelected,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
