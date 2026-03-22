package dev.lanthoor.spendly.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.ui.components.SpendlyTopAppBar
import dev.lanthoor.spendly.utils.AppLanguage

/**
 * Language settings screen displaying language options with radio buttons.
 * Allows users to select their preferred language (English, Hindi, Malayalam).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val selectedLanguage by viewModel.language.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            SpendlyTopAppBar(
                title = stringResource(R.string.title_language_settings),
                onNavigationClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            items(AppLanguage.entries) { language ->
                LanguageOption(
                    language = language,
                    isSelected = selectedLanguage == language,
                    onClick = { viewModel.updateLanguage(language) }
                )
            }
        }
    }
}

/**
 * A single language option row with a radio button.
 *
 * @param language The language option
 * @param isSelected Whether this language is currently selected
 * @param onClick Callback when this option is clicked
 */
@Composable
private fun LanguageOption(
    language: AppLanguage,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(language.displayNameRes),
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )

        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
    }
}
