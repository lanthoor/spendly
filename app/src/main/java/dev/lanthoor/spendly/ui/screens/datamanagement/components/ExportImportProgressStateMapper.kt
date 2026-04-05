package dev.lanthoor.spendly.ui.screens.datamanagement.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.domain.repository.ExportProgress
import dev.lanthoor.spendly.domain.repository.ImportProgress

internal data class ProgressUiState(
    val progressPercent: Int,
    val statusText: String,
    val itemText: String
)

@Composable
internal fun toProgressUiState(progressState: Any): ProgressUiState {
    return when (progressState) {
        is ExportProgress.QueryingData -> ProgressUiState(
            progressPercent = progressState.progress,
            statusText = stringResource(R.string.status_querying_data),
            itemText = ""
        )

        is ExportProgress.ProcessingReceipts -> ProgressUiState(
            progressPercent = progressState.progress,
            statusText = stringResource(R.string.status_processing_receipts),
            itemText = "${progressState.current} / ${progressState.total}"
        )

        is ExportProgress.Building -> ProgressUiState(
            progressPercent = progressState.progress,
            statusText = stringResource(R.string.status_building_export),
            itemText = ""
        )

        is ExportProgress.Serializing -> ProgressUiState(
            progressPercent = progressState.progress,
            statusText = stringResource(R.string.status_serializing_json),
            itemText = ""
        )

        is ExportProgress.Writing -> ProgressUiState(
            progressPercent = progressState.progress,
            statusText = stringResource(R.string.status_writing_file),
            itemText = ""
        )

        is ImportProgress.Parsing -> ProgressUiState(
            progressPercent = progressState.progress,
            statusText = stringResource(R.string.status_parsing_json),
            itemText = ""
        )

        is ImportProgress.Validating -> ProgressUiState(
            progressPercent = progressState.progress,
            statusText = stringResource(R.string.status_validating_data),
            itemText = ""
        )

        is ImportProgress.ClearingData -> ProgressUiState(
            progressPercent = progressState.progress,
            statusText = stringResource(R.string.status_clearing_old_data),
            itemText = ""
        )

        is ImportProgress.ImportingMasterData -> ProgressUiState(
            progressPercent = progressState.progress,
            statusText = stringResource(R.string.status_importing_master_data),
            itemText = ""
        )

        is ImportProgress.ImportingExpenses -> ProgressUiState(
            progressPercent = progressState.progress,
            statusText = stringResource(R.string.status_importing_expenses),
            itemText = "${progressState.current} / ${progressState.total}"
        )

        is ImportProgress.ImportingReceipts -> ProgressUiState(
            progressPercent = progressState.progress,
            statusText = stringResource(R.string.status_importing_receipts),
            itemText = "${progressState.current} / ${progressState.total}"
        )

        is ImportProgress.ImportingIncome -> ProgressUiState(
            progressPercent = progressState.progress,
            statusText = stringResource(R.string.status_importing_income),
            itemText = "${progressState.current} / ${progressState.total}"
        )

        is ImportProgress.ImportingBudgets -> ProgressUiState(
            progressPercent = progressState.progress,
            statusText = stringResource(R.string.status_importing_budgets),
            itemText = ""
        )

        is ImportProgress.ImportingRecurring -> ProgressUiState(
            progressPercent = progressState.progress,
            statusText = stringResource(R.string.status_importing_recurring),
            itemText = ""
        )

        is ImportProgress.Finalizing -> ProgressUiState(
            progressPercent = progressState.progress,
            statusText = stringResource(R.string.status_finalizing),
            itemText = ""
        )

        else -> ProgressUiState(
            progressPercent = 0,
            statusText = stringResource(R.string.status_processing_receipts),
            itemText = ""
        )
    }
}
