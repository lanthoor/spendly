package `in`.mylullaby.spendly.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.*
import `in`.mylullaby.spendly.ui.components.SpendlyTopAppBar

/**
 * Settings screen with account management and other settings sections.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToAccounts: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            SpendlyTopAppBar(
                title = "Settings"
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Account Management Section
            item {
                SectionHeader(text = "Account Management")
            }

            item {
                SettingsItem(
                    icon = PhosphorIcons.Regular.Bank,
                    title = "Manage Accounts",
                    subtitle = "Add, edit, or delete accounts",
                    onClick = onNavigateToAccounts
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            // Appearance Section (placeholder)
            item {
                SectionHeader(text = "Appearance")
            }

            item {
                SettingsItem(
                    icon = PhosphorIcons.Regular.Palette,
                    title = "Theme",
                    subtitle = "Coming soon",
                    onClick = { },
                    enabled = false
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            // Data Management Section (placeholder)
            item {
                SectionHeader(text = "Data Management")
            }

            item {
                SettingsItem(
                    icon = PhosphorIcons.Regular.Export,
                    title = "Export Data",
                    subtitle = "Coming soon",
                    onClick = { },
                    enabled = false
                )
            }

            item {
                SettingsItem(
                    icon = PhosphorIcons.Regular.Download,
                    title = "Import Data",
                    subtitle = "Coming soon",
                    onClick = { },
                    enabled = false
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            // About Section (placeholder)
            item {
                SectionHeader(text = "About")
            }

            item {
                SettingsItem(
                    icon = PhosphorIcons.Regular.Info,
                    title = "App Version",
                    subtitle = "1.0.0",
                    onClick = { },
                    enabled = false
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                }
            )

            // Title and Subtitle
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    }
                )
            }

            // Arrow
            if (enabled) {
                Icon(
                    imageVector = PhosphorIcons.Regular.CaretRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
