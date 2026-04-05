package dev.lanthoor.spendly.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowsClockwise
import com.adamglin.phosphoricons.regular.Bank
import com.adamglin.phosphoricons.regular.ChartBar
import com.adamglin.phosphoricons.regular.ChatText
import com.adamglin.phosphoricons.regular.Download
import com.adamglin.phosphoricons.regular.Export
import com.adamglin.phosphoricons.regular.Info
import com.adamglin.phosphoricons.regular.Lock
import com.adamglin.phosphoricons.regular.Translate
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.core.ui.format.displayNameRes
import dev.lanthoor.spendly.ui.screens.settings.components.AppLockTimeoutInfo
import dev.lanthoor.spendly.ui.screens.settings.components.SettingsDialogs
import dev.lanthoor.spendly.ui.screens.settings.components.SettingsItems
import dev.lanthoor.spendly.ui.screens.settings.components.SettingsSectionHeader
import dev.lanthoor.spendly.ui.screens.settings.components.ThemeSettingsSection
import dev.lanthoor.spendly.ui.screens.settings.model.SettingsDialogState
import dev.lanthoor.spendly.utils.BiometricAuthManager
import dev.lanthoor.spendly.workers.HistoricalSmsScanWorker

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
    var dialogState by remember { mutableStateOf<SettingsDialogState>(SettingsDialogState.None) }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            viewModel.setSmsAutoDetectionEnabled(true)
        } else {
            dialogState = SettingsDialogState.SmsPermissionRequired
        }
    }

    val scanNowLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            val wm = WorkManager.getInstance(context)
            val request = OneTimeWorkRequestBuilder<HistoricalSmsScanWorker>()
                .addTag(HistoricalSmsScanWorker.WORK_TAG)
                .build()
            wm.enqueue(request)
        } else {
            dialogState = SettingsDialogState.SmsPermissionRequired
        }
    }

    Scaffold(modifier = modifier) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                SettingsSectionHeader(text = stringResource(R.string.section_account_management))
            }
            item {
                SettingsItems.Navigation(
                    icon = PhosphorIcons.Regular.Bank,
                    title = stringResource(R.string.settings_manage_accounts),
                    subtitle = stringResource(R.string.settings_manage_accounts_desc),
                    onClick = onNavigateToAccounts
                )
            }
            item {
                SettingsItems.Navigation(
                    icon = PhosphorIcons.Regular.ChartBar,
                    title = stringResource(R.string.settings_manage_budgets),
                    subtitle = stringResource(R.string.settings_manage_budgets_desc),
                    onClick = onNavigateToBudgets
                )
            }
            item {
                SettingsItems.Navigation(
                    icon = PhosphorIcons.Regular.ArrowsClockwise,
                    title = stringResource(R.string.settings_recurring_transactions),
                    subtitle = stringResource(R.string.settings_recurring_transactions_desc),
                    onClick = onNavigateToRecurringTransactions
                )
            }
            item { SettingsItems.SectionDivider() }

            item {
                SettingsSectionHeader(text = stringResource(R.string.section_sms_auto_detection))
            }
            item {
                SettingsItems.Toggle(
                    icon = PhosphorIcons.Regular.ChatText,
                    title = stringResource(R.string.settings_enable_sms_auto_detection),
                    subtitle = stringResource(R.string.settings_enable_sms_auto_detection_desc),
                    checked = smsAutoDetectionEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            smsPermissionLauncher.launch(SettingsScreenPermissions.Sms)
                        } else {
                            viewModel.setSmsAutoDetectionEnabled(false)
                        }
                    }
                )
            }
            item {
                SettingsItems.Navigation(
                    icon = PhosphorIcons.Regular.Download,
                    title = stringResource(R.string.settings_scan_historical_sms),
                    subtitle = stringResource(R.string.settings_scan_historical_sms_desc),
                    onClick = {
                        scanNowLauncher.launch(SettingsScreenPermissions.Sms)
                    }
                )
            }
            item { SettingsItems.SectionDivider() }

            item {
                SettingsSectionHeader(text = stringResource(R.string.section_security))
            }
            item {
                val biometricAuthManager = remember { BiometricAuthManager(context) }
                SettingsItems.Toggle(
                    icon = PhosphorIcons.Regular.Lock,
                    title = stringResource(R.string.settings_app_lock),
                    subtitle = stringResource(R.string.settings_app_lock_desc),
                    checked = appLockEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            if (biometricAuthManager.isDeviceSecure()) {
                                viewModel.setAppLockEnabled(true)
                            } else {
                                dialogState = SettingsDialogState.DeviceSecurityRequired
                            }
                        } else {
                            viewModel.setAppLockEnabled(false)
                        }
                    }
                )
            }
            if (appLockEnabled) {
                item {
                    SettingsItems.LockTimeout(
                        selectedTimeout = lockTimeout,
                        onTimeoutSelected = { viewModel.setLockTimeout(it) },
                        label = stringResource(R.string.label_lock_timeout)
                    )
                }
                item { AppLockTimeoutInfo() }
            }
            item { SettingsItems.SectionDivider() }

            item {
                SettingsSectionHeader(text = stringResource(R.string.section_appearance))
            }
            item {
                ThemeSettingsSection(
                    selectedTheme = theme,
                    onThemeSelected = { viewModel.updateTheme(it) },
                    label = stringResource(R.string.label_theme)
                )
            }
            item {
                SettingsItems.Navigation(
                    icon = PhosphorIcons.Regular.Translate,
                    title = stringResource(R.string.settings_language),
                    subtitle = stringResource(language.displayNameRes),
                    onClick = onNavigateToLanguageSettings
                )
            }
            item { SettingsItems.SectionDivider() }

            item {
                SettingsSectionHeader(text = stringResource(R.string.screen_data_management_title))
            }
            item {
                SettingsItems.Navigation(
                    icon = PhosphorIcons.Regular.Export,
                    title = stringResource(R.string.screen_data_management_title),
                    subtitle = stringResource(R.string.settings_data_management_desc),
                    onClick = onNavigateToDataManagement
                )
            }
            item { SettingsItems.SectionDivider() }

            item {
                SettingsSectionHeader(text = stringResource(R.string.section_about))
            }
            item {
                SettingsItems.Navigation(
                    icon = PhosphorIcons.Regular.Info,
                    title = stringResource(R.string.settings_about_spendly),
                    subtitle = stringResource(R.string.settings_about_desc),
                    onClick = onNavigateToAbout
                )
            }
        }
    }

    SettingsDialogs(
        state = dialogState,
        onDismiss = { dialogState = SettingsDialogState.None },
        onOpenAppSettings = {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
            dialogState = SettingsDialogState.None
        },
        onOpenSecuritySettings = {
            context.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
            dialogState = SettingsDialogState.None
        }
    )
}

private object SettingsScreenPermissions {
    val Sms = arrayOf(
        android.Manifest.permission.RECEIVE_SMS,
        android.Manifest.permission.READ_SMS
    )
}
