package dev.lanthoor.spendly.ui.screens.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowsClockwise
import com.adamglin.phosphoricons.regular.Bank
import com.adamglin.phosphoricons.regular.CaretRight
import com.adamglin.phosphoricons.regular.ChartBar
import com.adamglin.phosphoricons.regular.ChatText
import com.adamglin.phosphoricons.regular.Download
import com.adamglin.phosphoricons.regular.Export
import com.adamglin.phosphoricons.regular.Info
import com.adamglin.phosphoricons.regular.Lock
import com.adamglin.phosphoricons.regular.Translate
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.core.ui.format.displayNameRes
import dev.lanthoor.spendly.ui.components.LockTimeoutDropdown
import dev.lanthoor.spendly.ui.components.ThemeSegmentedButton
import dev.lanthoor.spendly.utils.BiometricAuthManager

/**
 * Settings screen with account management and other settings sections.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToAccounts: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToRecurringTransactions: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToDataManagement: () -> Unit,
    onNavigateToLanguageSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val smsAutoDetectionEnabled by viewModel.smsAutoDetectionEnabled.collectAsStateWithLifecycle()
    val appLockEnabled by viewModel.appLockEnabled.collectAsStateWithLifecycle()
    val lockTimeout by viewModel.lockTimeout.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showNoSecurityDialog by remember { mutableStateOf(false) }

    // SMS permission launcher
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            viewModel.setSmsAutoDetectionEnabled(true)
        } else {
            // Permissions denied, show dialog to go to settings
            showPermissionDialog = true
        }
    }

    // Permission denied dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text(stringResource(R.string.title_sms_permission_required)) },
            text = {
                Text(stringResource(R.string.msg_sms_permission_required_desc))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        // Open app settings
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text(stringResource(R.string.button_open_settings))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text(stringResource(R.string.button_cancel))
                }
            }
        )
    }

    Scaffold(
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Account Management Section
            item {
                SectionHeader(text = stringResource(R.string.section_account_management))
            }

            item {
                SettingsItem(
                    icon = PhosphorIcons.Regular.Bank,
                    title = stringResource(R.string.settings_manage_accounts),
                    subtitle = stringResource(R.string.settings_manage_accounts_desc),
                    onClick = onNavigateToAccounts
                )
            }

            item {
                SettingsItem(
                    icon = PhosphorIcons.Regular.ChartBar,
                    title = stringResource(R.string.settings_manage_budgets),
                    subtitle = stringResource(R.string.settings_manage_budgets_desc),
                    onClick = onNavigateToBudgets
                )
            }

            item {
                SettingsItem(
                    icon = PhosphorIcons.Regular.ArrowsClockwise,
                    title = stringResource(R.string.settings_recurring_transactions),
                    subtitle = stringResource(R.string.settings_recurring_transactions_desc),
                    onClick = onNavigateToRecurringTransactions
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            // SMS Auto-Detection Section
            item {
                SectionHeader(text = stringResource(R.string.section_sms_auto_detection))
            }

            item {
                SettingsSwitchItem(
                    icon = PhosphorIcons.Regular.ChatText,
                    title = stringResource(R.string.settings_enable_sms_auto_detection),
                    subtitle = stringResource(R.string.settings_enable_sms_auto_detection_desc),
                    checked = smsAutoDetectionEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            // Request SMS permissions
                            smsPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.RECEIVE_SMS,
                                    Manifest.permission.READ_SMS
                                )
                            )
                        } else {
                            // Disable directly
                            viewModel.setSmsAutoDetectionEnabled(false)
                        }
                    }
                )
            }

            // "Scan Now" immediate action
            item {
                // Launcher specifically for Scan Now action
                val scanNowLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val allGranted = permissions.values.all { it }
                    if (allGranted) {
                        // Enqueue historical scan worker
                        val wm = androidx.work.WorkManager.getInstance(context)
                        val request =
                            androidx.work.OneTimeWorkRequestBuilder<dev.lanthoor.spendly.workers.HistoricalSmsScanWorker>()
                                .addTag(dev.lanthoor.spendly.workers.HistoricalSmsScanWorker.WORK_TAG)
                                .build()
                        wm.enqueue(request)
                    } else {
                        showPermissionDialog = true
                    }
                }

                SettingsItem(
                    icon = PhosphorIcons.Regular.Download,
                    title = stringResource(R.string.settings_scan_historical_sms),
                    subtitle = stringResource(R.string.settings_scan_historical_sms_desc),
                    onClick = {
                        // Request SMS permissions and then start scan
                        scanNowLauncher.launch(
                            arrayOf(
                                Manifest.permission.READ_SMS,
                                Manifest.permission.RECEIVE_SMS
                            )
                        )
                    }
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            // Security Section
            item {
                SectionHeader(text = stringResource(R.string.section_security))
            }

            item {
                val biometricAuthManager = remember { BiometricAuthManager(context) }

                SettingsSwitchItem(
                    icon = PhosphorIcons.Regular.Lock,
                    title = stringResource(R.string.settings_app_lock),
                    subtitle = stringResource(R.string.settings_app_lock_desc),
                    checked = appLockEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            // Check device has security set up
                            if (biometricAuthManager.isDeviceSecure()) {
                                viewModel.setAppLockEnabled(true)
                            } else {
                                showNoSecurityDialog = true
                            }
                        } else {
                            viewModel.setAppLockEnabled(false)
                        }
                    }
                )
            }

            // Show timeout dropdown only when lock is enabled
            if (appLockEnabled) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        LockTimeoutDropdown(
                            selectedTimeout = lockTimeout,
                            onTimeoutSelected = { viewModel.setLockTimeout(it) },
                            label = stringResource(R.string.label_lock_timeout)
                        )
                    }
                }

                item {
                    Text(
                        text = "App will lock after being inactive for the selected duration",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            // Appearance Section
            item {
                SectionHeader(text = stringResource(R.string.section_appearance))
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.label_theme),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    ThemeSegmentedButton(
                        selectedTheme = theme,
                        onThemeSelected = { viewModel.updateTheme(it) },
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Language Settings
            item {
                SettingsItem(
                    icon = PhosphorIcons.Regular.Translate,
                    title = stringResource(R.string.settings_language),
                    subtitle = stringResource(language.displayNameRes),
                    onClick = onNavigateToLanguageSettings
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            // Data Management Section
            item {
                SectionHeader(text = stringResource(R.string.screen_data_management_title))
            }

            item {
                SettingsItem(
                    icon = PhosphorIcons.Regular.Export,
                    title = stringResource(R.string.screen_data_management_title),
                    subtitle = stringResource(R.string.settings_data_management_desc),
                    onClick = onNavigateToDataManagement,
                    enabled = true
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            // About Section
            item {
                SectionHeader(text = stringResource(R.string.section_about))
            }

            item {
                SettingsItem(
                    icon = PhosphorIcons.Regular.Info,
                    title = stringResource(R.string.settings_about_spendly),
                    subtitle = stringResource(R.string.settings_about_desc),
                    onClick = onNavigateToAbout
                )
            }
        }
    }

    // Device security not set dialog
    if (showNoSecurityDialog) {
        AlertDialog(
            onDismissRequest = { showNoSecurityDialog = false },
            title = { Text(stringResource(R.string.title_device_security_required)) },
            text = {
                Text(stringResource(R.string.msg_device_security_required_desc))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showNoSecurityDialog = false
                        // Open device security settings
                        val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                        context.startActivity(intent)
                    }
                ) {
                    Text(stringResource(R.string.button_open_settings))
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoSecurityDialog = false }) {
                    Text(stringResource(R.string.button_cancel))
                }
            }
        )
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
private fun SettingsSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
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
                tint = MaterialTheme.colorScheme.onSurface
            )

            // Title and subtitle
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Switch
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
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
