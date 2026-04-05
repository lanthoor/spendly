package dev.lanthoor.spendly.ui.screens.datamanagement

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.ui.components.SpendlyTopAppBar
import dev.lanthoor.spendly.ui.screens.datamanagement.components.DataManagementActionCards
import dev.lanthoor.spendly.ui.screens.datamanagement.components.DataManagementDialogs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: DataManagementViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val exportProgress by viewModel.exportProgress.collectAsStateWithLifecycle()
    val importProgress by viewModel.importProgress.collectAsStateWithLifecycle()

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            val outputStream = context.contentResolver.openOutputStream(uri)
            viewModel.exportData(outputStream)
        }
    }

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
            } catch (_: Exception) {
                viewModel.resetImportState()
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
        DataManagementActionCards(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            onExportClick = {
                val timestamp = System.currentTimeMillis()
                exportLauncher.launch("spendly_backup_$timestamp.json")
            },
            onImportClick = {
                importLauncher.launch("application/json")
            }
        )
    }

    DataManagementDialogs(
        exportProgress = exportProgress,
        importProgress = importProgress,
        onDismissExport = { viewModel.resetExportState() },
        onDismissImport = { viewModel.resetImportState() },
        onCancelExport = { viewModel.cancelExport() },
        onCancelImport = { viewModel.cancelImport() }
    )
}
