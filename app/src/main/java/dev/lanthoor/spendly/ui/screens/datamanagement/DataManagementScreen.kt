package dev.lanthoor.spendly.ui.screens.datamanagement

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CheckCircle
import com.adamglin.phosphoricons.regular.Download
import com.adamglin.phosphoricons.regular.DownloadSimple
import com.adamglin.phosphoricons.regular.Export
import com.adamglin.phosphoricons.regular.Upload
import com.adamglin.phosphoricons.regular.Warning
import com.adamglin.phosphoricons.regular.XCircle
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.domain.repository.ExportProgress
import dev.lanthoor.spendly.domain.repository.ImportProgress
import dev.lanthoor.spendly.ui.components.SpendlyTopAppBar

/**
 * Data Management screen for import/export functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: DataManagementViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val exportProgress by viewModel.exportProgress.collectAsStateWithLifecycle()
    val importProgress by viewModel.importProgress.collectAsStateWithLifecycle()

    // File picker for export (create document)
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            val outputStream = context.contentResolver.openOutputStream(uri)
            viewModel.exportData(outputStream)
        }
    }

    // File picker for import (select document)
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val jsonContent = context.contentResolver
                    .openInputStream(uri)
                    ?.readBytes()
                    ?.toString(Charsets.UTF_8)
                viewModel.importData(jsonContent ?: "")
            } catch (e: Exception) {
                viewModel.resetImportState()
                // Error handled in ViewModel
            }
        }
    }

    Scaffold(
        topBar = {
            SpendlyTopAppBar(
                title = stringResource(R.string.screen_data_management_title),
                onNavigationClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Export Section
            ExportCard(
                onExportClick = {
                    val timestamp = System.currentTimeMillis()
                    exportLauncher.launch("spendly_backup_$timestamp.json")
                }
            )

            // Import Section
            ImportCard(
                onImportClick = {
                    importLauncher.launch("application/json")
                }
            )

            // Warning Card
            WarningCard()
        }
    }

    // Export Progress Dialog
    if (exportProgress !is ExportProgress.Idle) {
        ExportProgressDialog(
            progress = exportProgress,
            onDismiss = { viewModel.resetExportState() },
            onCancel = { viewModel.cancelExport() }
        )
    }

    // Import Progress Dialog
    if (importProgress !is ImportProgress.Idle) {
        ImportProgressDialog(
            progress = importProgress,
            onDismiss = { viewModel.resetImportState() },
            onCancel = { viewModel.cancelImport() }
        )
    }
}

@Composable
private fun ExportCard(onExportClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.Export,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = stringResource(R.string.label_export_data),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.label_backup_to_json),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = stringResource(R.string.desc_export_data),
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = onExportClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.Download,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.button_export_json))
            }
        }
    }
}

@Composable
private fun ImportCard(onImportClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.DownloadSimple,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = stringResource(R.string.label_import_data),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.label_restore_from_json),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = stringResource(R.string.desc_import_data),
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = onImportClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.Upload,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.button_import_json))
            }
        }
    }
}

@Composable
private fun WarningCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = PhosphorIcons.Regular.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.label_important),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = stringResource(R.string.desc_import_warning),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun ExportProgressDialog(
    progress: ExportProgress,
    onDismiss: () -> Unit,
    onCancel: () -> Unit
) {
    when (progress) {
        is ExportProgress.Success -> {
            SuccessDialog(
                title = stringResource(R.string.title_export_success),
                message = stringResource(
                    R.string.string_export_success_message,
                    progress.recordCount,
                    getFileSize(progress.fileSizeBytes)
                ),
                onDismiss = onDismiss
            )
        }

        is ExportProgress.Error -> {
            ErrorDialog(
                title = stringResource(R.string.title_export_failed),
                message = progress.message,
                onDismiss = onDismiss
            )
        }

        is ExportProgress.Cancelled -> {
            // Auto-dismiss cancelled state
            LaunchedEffect(Unit) {
                onDismiss()
            }
        }

        else -> {
            ProgressDialog(
                title = stringResource(R.string.title_exporting_data),
                progress = progress,
                onCancel = onCancel
            )
        }
    }
}

@Composable
private fun ImportProgressDialog(
    progress: ImportProgress,
    onDismiss: () -> Unit,
    onCancel: () -> Unit
) {
    when (progress) {
        is ImportProgress.Success -> {
            SuccessDialog(
                title = stringResource(R.string.title_import_success),
                message = buildString {
                    append(stringResource(R.string.string_import_success_header))
                    progress.recordCounts.forEach { (type, count) ->
                        append("• $count $type\n")
                    }
                },
                onDismiss = onDismiss
            )
        }

        is ImportProgress.Error -> {
            ErrorDialog(
                title = stringResource(R.string.title_import_failed),
                message = progress.message,
                onDismiss = onDismiss
            )
        }

        is ImportProgress.Cancelled -> {
            // Auto-dismiss cancelled state
            LaunchedEffect(Unit) {
                onDismiss()
            }
        }

        else -> {
            ProgressDialog(
                title = stringResource(R.string.title_importing_data),
                progress = progress,
                onCancel = onCancel
            )
        }
    }
}

@Composable
private fun ProgressDialog(
    title: String,
    progress: Any, // ExportProgress or ImportProgress
    onCancel: () -> Unit
) {
    val (progressPercent, statusText, itemText) = when (progress) {
        is ExportProgress.QueryingData -> Triple(
            progress.progress,
            stringResource(R.string.status_querying_data),
            ""
        )

        is ExportProgress.ProcessingReceipts -> Triple(
            progress.progress,
            stringResource(R.string.status_processing_receipts),
            "${progress.current} / ${progress.total}"
        )

        is ExportProgress.Building -> Triple(
            progress.progress,
            stringResource(R.string.status_building_export),
            ""
        )

        is ExportProgress.Serializing -> Triple(
            progress.progress,
            stringResource(R.string.status_serializing_json),
            ""
        )

        is ExportProgress.Writing -> Triple(
            progress.progress,
            stringResource(R.string.status_writing_file),
            ""
        )

        is ImportProgress.Parsing -> Triple(
            progress.progress,
            stringResource(R.string.status_parsing_json),
            ""
        )

        is ImportProgress.Validating -> Triple(
            progress.progress,
            stringResource(R.string.status_validating_data),
            ""
        )

        is ImportProgress.ClearingData -> Triple(
            progress.progress,
            stringResource(R.string.status_clearing_old_data),
            ""
        )

        is ImportProgress.ImportingMasterData -> Triple(
            progress.progress,
            stringResource(R.string.status_importing_master_data),
            ""
        )

        is ImportProgress.ImportingExpenses -> Triple(
            progress.progress,
            stringResource(R.string.status_importing_expenses),
            "${progress.current} / ${progress.total}"
        )

        is ImportProgress.ImportingReceipts -> Triple(
            progress.progress,
            stringResource(R.string.status_importing_receipts),
            "${progress.current} / ${progress.total}"
        )

        is ImportProgress.ImportingIncome -> Triple(
            progress.progress,
            stringResource(R.string.status_importing_income),
            "${progress.current} / ${progress.total}"
        )

        is ImportProgress.ImportingBudgets -> Triple(
            progress.progress,
            stringResource(R.string.status_importing_budgets),
            ""
        )

        is ImportProgress.ImportingRecurring -> Triple(
            progress.progress,
            stringResource(R.string.status_importing_recurring),
            ""
        )

        is ImportProgress.Finalizing -> Triple(
            progress.progress,
            stringResource(R.string.status_finalizing),
            ""
        )

        else -> Triple(0, stringResource(R.string.status_processing_receipts), "")
    }

    AlertDialog(
        onDismissRequest = { /* Non-dismissible */ },
        title = { Text(title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(statusText)
                if (itemText.isNotEmpty()) {
                    Text(
                        text = itemText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                LinearProgressIndicator(
                    progress = { progressPercent / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = stringResource(R.string.label_percentage, progressPercent),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.button_cancel))
            }
        }
    )
}

@Composable
private fun SuccessDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = PhosphorIcons.Regular.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_ok))
            }
        }
    )
}

@Composable
private fun ErrorDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = PhosphorIcons.Regular.XCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_ok))
            }
        }
    )
}

private fun getFileSize(bytes: Long): String {
    return if (bytes < 1024 * 1024) {
        "${bytes / 1024} KB"
    } else {
        "${bytes / 1024 / 1024} MB"
    }
}
