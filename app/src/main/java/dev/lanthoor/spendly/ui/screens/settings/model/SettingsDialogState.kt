package dev.lanthoor.spendly.ui.screens.settings.model

sealed interface SettingsDialogState {
    data object None : SettingsDialogState
    data object SmsPermissionRequired : SettingsDialogState
    data object DeviceSecurityRequired : SettingsDialogState
}
