package dev.lanthoor.spendly.ui.screens.settings.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.ui.screens.settings.model.SettingsDialogState

@Composable
fun SettingsDialogs(
    state: SettingsDialogState,
    onDismiss: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onOpenSecuritySettings: () -> Unit
) {
    when (state) {
        SettingsDialogState.None -> Unit

        SettingsDialogState.SmsPermissionRequired -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.title_sms_permission_required)) },
                text = { Text(stringResource(R.string.msg_sms_permission_required_desc)) },
                confirmButton = {
                    TextButton(onClick = onOpenAppSettings) {
                        Text(stringResource(R.string.button_open_settings))
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.button_cancel))
                    }
                }
            )
        }

        SettingsDialogState.DeviceSecurityRequired -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.title_device_security_required)) },
                text = { Text(stringResource(R.string.msg_device_security_required_desc)) },
                confirmButton = {
                    TextButton(onClick = onOpenSecuritySettings) {
                        Text(stringResource(R.string.button_open_settings))
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.button_cancel))
                    }
                }
            )
        }
    }
}
