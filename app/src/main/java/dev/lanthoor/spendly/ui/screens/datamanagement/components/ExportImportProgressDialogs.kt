package dev.lanthoor.spendly.ui.screens.datamanagement.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.domain.repository.ExportProgress
import dev.lanthoor.spendly.domain.repository.ImportProgress

@Composable
internal fun ExportProgressDialog(
    progress: ExportProgress,
    onDismiss: () -> Unit,
    onCancel: () -> Unit
) {
    when (progress) {
        is ExportProgress.Success -> {
            ExportImportFeedbackDialog(
                title = stringResource(R.string.title_export_success),
                message = stringResource(
                    R.string.string_export_success_message,
                    progress.recordCount,
                    getFileSize(progress.fileSizeBytes)
                ),
                isError = false,
                onDismiss = onDismiss
            )
        }

        is ExportProgress.Error -> {
            ExportImportFeedbackDialog(
                title = stringResource(R.string.title_export_failed),
                message = progress.message,
                isError = true,
                onDismiss = onDismiss
            )
        }

        is ExportProgress.Cancelled -> {
            LaunchedEffect(Unit) { onDismiss() }
        }

        else -> {
            ExportImportProgressDialog(
                title = stringResource(R.string.title_exporting_data),
                progressState = progress,
                onCancel = onCancel
            )
        }
    }
}

@Composable
internal fun ImportProgressDialog(
    progress: ImportProgress,
    onDismiss: () -> Unit,
    onCancel: () -> Unit
) {
    when (progress) {
        is ImportProgress.Success -> {
            ExportImportFeedbackDialog(
                title = stringResource(R.string.title_import_success),
                message = buildString {
                    append(stringResource(R.string.string_import_success_header))
                    progress.recordCounts.forEach { (type, count) ->
                        append("• $count $type\n")
                    }
                },
                isError = false,
                onDismiss = onDismiss
            )
        }

        is ImportProgress.Error -> {
            ExportImportFeedbackDialog(
                title = stringResource(R.string.title_import_failed),
                message = progress.message,
                isError = true,
                onDismiss = onDismiss
            )
        }

        is ImportProgress.Cancelled -> {
            LaunchedEffect(Unit) { onDismiss() }
        }

        else -> {
            ExportImportProgressDialog(
                title = stringResource(R.string.title_importing_data),
                progressState = progress,
                onCancel = onCancel
            )
        }
    }
}

private fun getFileSize(bytes: Long): String {
    return if (bytes < 1024 * 1024) {
        "${bytes / 1024} KB"
    } else {
        "${bytes / 1024 / 1024} MB"
    }
}
