package `in`.co.spendly.ui.screens.datamanagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.co.spendly.domain.repository.ExportImportRepository
import `in`.co.spendly.domain.repository.ExportProgress
import `in`.co.spendly.domain.repository.ImportProgress
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.OutputStream
import javax.inject.Inject

/**
 * ViewModel for Data Management screen
 * Handles export/import operations with progress tracking and cancellation support
 */
@HiltViewModel
class DataManagementViewModel @Inject constructor(
    private val repository: ExportImportRepository
) : ViewModel() {

    private val _exportProgress = MutableStateFlow<ExportProgress>(ExportProgress.Idle)
    val exportProgress: StateFlow<ExportProgress> = _exportProgress.asStateFlow()

    private val _importProgress = MutableStateFlow<ImportProgress>(ImportProgress.Idle)
    val importProgress: StateFlow<ImportProgress> = _importProgress.asStateFlow()

    private var exportJob: Job? = null
    private var importJob: Job? = null

    /**
     * Export all app data to JSON
     *
     * @param outputStream The stream to write JSON data to (from SAF)
     */
    fun exportData(outputStream: OutputStream?) {
        if (outputStream == null) {
            _exportProgress.value = ExportProgress.Error("Output stream is null")
            return
        }

        // Cancel any existing export job
        exportJob?.cancel()

        exportJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                _exportProgress.value = ExportProgress.Idle
                repository.exportAllData(outputStream) { progress ->
                    _exportProgress.value = progress
                }
            } catch (e: CancellationException) {
                _exportProgress.value = ExportProgress.Cancelled
                throw e // Re-throw to allow coroutine cancellation to propagate
            } catch (e: Exception) {
                _exportProgress.value = ExportProgress.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Import all app data from JSON
     *
     * @param jsonContent The JSON string containing export data
     */
    fun importData(jsonContent: String) {
        if (jsonContent.isEmpty()) {
            _importProgress.value = ImportProgress.Error("JSON content is empty")
            return
        }

        // Cancel any existing import job
        importJob?.cancel()

        importJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                _importProgress.value = ImportProgress.Idle
                repository.importAllData(jsonContent) { progress ->
                    _importProgress.value = progress
                }
            } catch (e: CancellationException) {
                _importProgress.value = ImportProgress.Cancelled
                throw e // Re-throw to allow coroutine cancellation to propagate
            } catch (e: Exception) {
                _importProgress.value = ImportProgress.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Cancel ongoing export operation
     */
    fun cancelExport() {
        exportJob?.cancel()
        _exportProgress.value = ExportProgress.Cancelled
    }

    /**
     * Cancel ongoing import operation
     */
    fun cancelImport() {
        importJob?.cancel()
        _importProgress.value = ImportProgress.Cancelled
    }

    /**
     * Reset export state to idle
     */
    fun resetExportState() {
        _exportProgress.value = ExportProgress.Idle
    }

    /**
     * Reset import state to idle
     */
    fun resetImportState() {
        _importProgress.value = ImportProgress.Idle
    }
}
