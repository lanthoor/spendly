package dev.lanthoor.spendly.data.repository

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.room.withTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.lanthoor.spendly.BuildConfig
import dev.lanthoor.spendly.data.exportimport.AccountExport
import dev.lanthoor.spendly.data.exportimport.BudgetExport
import dev.lanthoor.spendly.data.exportimport.CategoryExport
import dev.lanthoor.spendly.data.exportimport.ExpenseExport
import dev.lanthoor.spendly.data.exportimport.ExportMetadata
import dev.lanthoor.spendly.data.exportimport.IncomeExport
import dev.lanthoor.spendly.data.exportimport.ReceiptExport
import dev.lanthoor.spendly.data.exportimport.RecurringTransactionExport
import dev.lanthoor.spendly.data.exportimport.SpendlyExport
import dev.lanthoor.spendly.data.local.SpendlyDatabase
import dev.lanthoor.spendly.data.local.dao.AccountDao
import dev.lanthoor.spendly.data.local.dao.BudgetDao
import dev.lanthoor.spendly.data.local.dao.CategoryDao
import dev.lanthoor.spendly.data.local.dao.ExpenseDao
import dev.lanthoor.spendly.data.local.dao.IncomeDao
import dev.lanthoor.spendly.data.local.dao.ReceiptDao
import dev.lanthoor.spendly.data.local.dao.RecurringTransactionDao
import dev.lanthoor.spendly.data.local.dao.TransactionAiEnrichmentDao
import dev.lanthoor.spendly.domain.usecase.transactions.EnrichSmsTransactionsUseCase
import dev.lanthoor.spendly.domain.repository.ExportImportRepository
import dev.lanthoor.spendly.domain.repository.ExportProgress
import dev.lanthoor.spendly.domain.repository.ExportResult
import dev.lanthoor.spendly.domain.repository.ImportProgress
import dev.lanthoor.spendly.domain.repository.ImportResult
import dev.lanthoor.spendly.domain.repository.ImportValidation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ExportImportRepository"
private const val MAX_EXPORT_SIZE_BYTES = 500L * 1024 * 1024 // 500MB
private const val MAX_JSON_SIZE_BYTES = 100L * 1024 * 1024 // 100MB

/**
 * Implementation of export/import repository
 */
@Singleton
class ExportImportRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val accountDao: AccountDao,
    private val expenseDao: ExpenseDao,
    private val incomeDao: IncomeDao,
    private val receiptDao: ReceiptDao,
    private val budgetDao: BudgetDao,
    private val recurringTransactionDao: RecurringTransactionDao,
    private val transactionAiEnrichmentDao: TransactionAiEnrichmentDao,
    private val enrichSmsTransactionsUseCase: EnrichSmsTransactionsUseCase,
    private val database: SpendlyDatabase,
    @param:ApplicationContext private val context: Context
) : ExportImportRepository {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    private val validator = ExportImportValidator()
    private val importOperations = ExportImportImportOperations(
        categoryDao = categoryDao,
        accountDao = accountDao,
        expenseDao = expenseDao,
        incomeDao = incomeDao,
        receiptDao = receiptDao,
        budgetDao = budgetDao,
        recurringTransactionDao = recurringTransactionDao,
        transactionAiEnrichmentDao = transactionAiEnrichmentDao,
        context = context
    )

    override suspend fun exportAllData(
        outputStream: OutputStream?,
        onProgress: (ExportProgress) -> Unit
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            if (outputStream == null) {
                return@withContext ExportResult.Failure("Output stream is null")
            }

            // Phase 1: Query all entities (10%)
            ensureActive()
            onProgress(ExportProgress.QueryingData())

            val categories = categoryDao.getAllSnapshot()
            val accounts = accountDao.getAllSnapshot()
            val expenses = expenseDao.getAllSnapshot()
            val income = incomeDao.getAllSnapshot()
            val receipts = receiptDao.getAllSnapshot()
            val budgets = budgetDao.getAllSnapshot()
            val recurring = recurringTransactionDao.getAllSnapshot()
            val aiEnrichments = transactionAiEnrichmentDao.getAllSnapshot()

            // Phase 2: Process receipts with Base64 encoding (10-70%)
            val receiptExports = mutableListOf<ReceiptExport>()
            receipts.forEachIndexed { index, receipt ->
                ensureActive()
                onProgress(ExportProgress.ProcessingReceipts(index + 1, receipts.size))

                val base64Data = try {
                    val file = File(context.filesDir, receipt.filePath)
                    if (file.exists()) {
                        val bytes = file.readBytes()
                        Base64.encodeToString(bytes, Base64.NO_WRAP)
                    } else {
                        Log.w(TAG, "Receipt file not found: ${receipt.filePath}")
                        "" // Empty string for missing files
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error reading receipt file: ${receipt.filePath}", e)
                    ""
                }

                receiptExports.add(receipt.toExport(base64Data))
            }

            // Phase 3: Build export object (75%)
            ensureActive()
            onProgress(ExportProgress.Building())

            val recordCounts = mapOf(
                "categories" to categories.size,
                "accounts" to accounts.size,
                "expenses" to expenses.size,
                "income" to income.size,
                "aiEnrichments" to aiEnrichments.size,
                "receipts" to receipts.size,
                "budgets" to budgets.size,
                "recurringTransactions" to recurring.size
            )

            val export = SpendlyExport(
                metadata = ExportMetadata(
                    exportVersion = 2,
                    appVersion = BuildConfig.VERSION_NAME,
                    databaseVersion = database.openHelper.readableDatabase.version,
                    exportDate = System.currentTimeMillis(),
                    currency = "INR",
                    recordCounts = recordCounts
                ),
                categories = categories.map { it.toExport() },
                accounts = accounts.map { it.toExport() },
                expenses = expenses.map { it.toExport() },
                income = income.map { it.toExport() },
                aiEnrichments = aiEnrichments.map { it.toExport() },
                receipts = receiptExports,
                budgets = budgets.map { it.toExport() },
                recurringTransactions = recurring.map { it.toExport() }
            )

            // Phase 4: Serialize to JSON (85%)
            ensureActive()
            onProgress(ExportProgress.Serializing())

            val jsonString = json.encodeToString(export)

            // Check size limit
            val jsonBytes = jsonString.toByteArray()
            if (jsonBytes.size > MAX_EXPORT_SIZE_BYTES) {
                return@withContext ExportResult.Failure(
                    "Export too large: ${jsonBytes.size / 1024 / 1024}MB (max 500MB)"
                )
            }

            // Phase 5: Write to output stream (95%)
            ensureActive()
            onProgress(ExportProgress.Writing())

            outputStream.use { stream ->
                stream.write(jsonBytes)
                stream.flush()
            }

            val totalRecords = recordCounts.values.sum()
            val result = ExportResult.Success(totalRecords, jsonBytes.size.toLong())
            onProgress(ExportProgress.Success(totalRecords, jsonBytes.size.toLong()))

            Log.i(TAG, "Export completed: $totalRecords records, ${jsonBytes.size / 1024}KB")
            result

        } catch (e: Exception) {
            Log.e(TAG, "Export failed", e)
            val error = ExportProgress.Error(e.message ?: "Unknown error")
            onProgress(error)
            ExportResult.Failure(e.message ?: "Unknown error")
        }
    }

    override suspend fun importAllData(
        jsonContent: String,
        onProgress: (ImportProgress) -> Unit,
        onAfterImport: (suspend () -> Unit)?
    ): ImportResult = withContext(Dispatchers.IO) {
        try {
            // Phase 1: Parse JSON (5%)
            ensureActive()
            onProgress(ImportProgress.Parsing())

            if (jsonContent.toByteArray().size > MAX_JSON_SIZE_BYTES) {
                return@withContext ImportResult.Failure("JSON too large (max 100MB)")
            }

            val export = try {
                json.decodeFromString<SpendlyExport>(jsonContent)
            } catch (e: SerializationException) {
                Log.e(TAG, "JSON parsing failed", e)
                return@withContext ImportResult.Failure("Invalid JSON format")
            }

            // Phase 2: Validate (15%)
            ensureActive()
            onProgress(ImportProgress.Validating())

            val validation = validateExport(export)
            if (validation is ImportValidation.Invalid) {
                return@withContext ImportResult.Failure(validation.errors.joinToString("\n"))
            }

            // Phase 3-10: Import data in transaction
            database.withTransaction {
                // Phase 3: Clear data (22%)
                ensureActive()
                onProgress(ImportProgress.ClearingData())
                importOperations.clearUserData()

                // Phase 4: Import master data (30%)
                ensureActive()
                onProgress(ImportProgress.ImportingMasterData())
                val idMappings = importOperations.importMasterData(export)

                // Phase 5: Import expenses (35-50%)
                importOperations.importExpenses(export.expenses, idMappings, onProgress)

                // Phase 6: Import receipts (50-70%)
                importOperations.importReceipts(export.receipts, idMappings, onProgress)

                // Phase 7: Import income (70-80%)
                importOperations.importIncome(export.income, idMappings, onProgress)

                // Phase 8: Import budgets (85%)
                ensureActive()
                onProgress(ImportProgress.ImportingBudgets())
                importOperations.importBudgets(export.budgets, idMappings)

                // Phase 9: Import recurring transactions (92%)
                ensureActive()
                onProgress(ImportProgress.ImportingRecurring())
                importOperations.importRecurringTransactions(export.recurringTransactions, idMappings)

                importOperations.importAiEnrichments(export.aiEnrichments, idMappings)

                // Phase 10: Finalize (98%)
                ensureActive()
                onProgress(ImportProgress.Finalizing())
            }

            val recordCounts = export.metadata.recordCounts

            if (export.aiEnrichments.isEmpty()) {
                enrichSmsTransactionsUseCase.markImportedPendingIfMissing()
            }
            onAfterImport?.invoke()

            val result = ImportResult.Success(recordCounts)
            onProgress(ImportProgress.Success(recordCounts))

            Log.i(TAG, "Import completed: ${recordCounts.values.sum()} records")
            result

        } catch (e: Exception) {
            Log.e(TAG, "Import failed", e)
            val error = ImportProgress.Error(e.message ?: "Unknown error")
            onProgress(error)
            ImportResult.Failure(e.message ?: "Unknown error")
        }
    }

    override suspend fun validateImportData(jsonContent: String): ImportValidation {
        return try {
            val export = json.decodeFromString<SpendlyExport>(jsonContent)
            validateExport(export)
        } catch (e: SerializationException) {
            ImportValidation.Invalid(listOf("Invalid JSON format: ${e.message}"))
        } catch (e: Exception) {
            ImportValidation.Invalid(listOf("Validation error: ${e.message}"))
        }
    }

    private fun validateExport(export: SpendlyExport): ImportValidation {
        return validator.validateExport(export)
    }
}
