package dev.lanthoor.spendly.domain.repository

import java.io.OutputStream

/**
 * Repository interface for import/export functionality
 */
interface ExportImportRepository {
    /**
     * Export all app data to JSON format
     *
     * @param outputStream The stream to write JSON data to
     * @param onProgress Callback for progress updates
     * @return Result of the export operation
     */
    suspend fun exportAllData(
        outputStream: OutputStream?,
        onProgress: (ExportProgress) -> Unit
    ): ExportResult

    /**
     * Import all app data from JSON format
     *
     * @param jsonContent The JSON string containing export data
     * @param onProgress Callback for progress updates
     * @return Result of the import operation
     */
    suspend fun importAllData(
        jsonContent: String,
        onProgress: (ImportProgress) -> Unit
    ): ImportResult

    /**
     * Validate import data without actually importing
     *
     * @param jsonContent The JSON string to validate
     * @return Validation result with details
     */
    suspend fun validateImportData(jsonContent: String): ImportValidation
}

/**
 * Progress states for export operation
 */
sealed class ExportProgress {
    object Idle : ExportProgress()
    data class QueryingData(val progress: Int = 10) : ExportProgress()
    data class ProcessingReceipts(val current: Int, val total: Int) : ExportProgress() {
        val progress: Int
            get() = if (total > 0) 10 + ((current.toFloat() / total) * 60).toInt() else 10
    }

    data class Building(val progress: Int = 75) : ExportProgress()
    data class Serializing(val progress: Int = 85) : ExportProgress()
    data class Writing(val progress: Int = 95) : ExportProgress()
    data class Success(val recordCount: Int, val fileSizeBytes: Long) : ExportProgress()
    data class Error(val message: String) : ExportProgress()
    object Cancelled : ExportProgress()
}

/**
 * Progress states for import operation
 */
sealed class ImportProgress {
    object Idle : ImportProgress()
    data class Parsing(val progress: Int = 5) : ImportProgress()
    data class Validating(val progress: Int = 15) : ImportProgress()
    data class ClearingData(val progress: Int = 22) : ImportProgress()
    data class ImportingMasterData(val progress: Int = 30) : ImportProgress()
    data class ImportingExpenses(val current: Int, val total: Int) : ImportProgress() {
        val progress: Int
            get() = if (total > 0) 35 + ((current.toFloat() / total) * 15).toInt() else 35
    }

    data class ImportingReceipts(val current: Int, val total: Int) : ImportProgress() {
        val progress: Int
            get() = if (total > 0) 50 + ((current.toFloat() / total) * 20).toInt() else 50
    }

    data class ImportingIncome(val current: Int, val total: Int) : ImportProgress() {
        val progress: Int
            get() = if (total > 0) 70 + ((current.toFloat() / total) * 10).toInt() else 70
    }

    data class ImportingBudgets(val progress: Int = 85) : ImportProgress()
    data class ImportingRecurring(val progress: Int = 92) : ImportProgress()
    data class Finalizing(val progress: Int = 98) : ImportProgress()
    data class Success(val recordCounts: Map<String, Int>) : ImportProgress()
    data class Error(val message: String) : ImportProgress()
    object Cancelled : ImportProgress()
}

/**
 * Result of an export operation
 */
sealed class ExportResult {
    data class Success(val recordCount: Int, val fileSizeBytes: Long) : ExportResult()
    data class Failure(val message: String) : ExportResult()
}

/**
 * Result of an import operation
 */
sealed class ImportResult {
    data class Success(val recordCounts: Map<String, Int>) : ImportResult()
    data class Failure(val message: String) : ImportResult()
}

/**
 * Validation result for import data
 */
sealed class ImportValidation {
    object Valid : ImportValidation()
    data class Invalid(val errors: List<String>) : ImportValidation()
}
