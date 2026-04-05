package dev.lanthoor.spendly.ui.screens.datamanagement.components

import androidx.compose.runtime.Composable
import dev.lanthoor.spendly.domain.repository.ExportProgress
import dev.lanthoor.spendly.domain.repository.ImportProgress

@Composable
fun DataManagementDialogs(
    exportProgress: ExportProgress,
    importProgress: ImportProgress,
    onDismissExport: () -> Unit,
    onDismissImport: () -> Unit,
    onCancelExport: () -> Unit,
    onCancelImport: () -> Unit
) {
    if (exportProgress !is ExportProgress.Idle) {
        ExportProgressDialog(
            progress = exportProgress,
            onDismiss = onDismissExport,
            onCancel = onCancelExport
        )
    }

    if (importProgress !is ImportProgress.Idle) {
        ImportProgressDialog(
            progress = importProgress,
            onDismiss = onDismissImport,
            onCancel = onCancelImport
        )
    }
}
