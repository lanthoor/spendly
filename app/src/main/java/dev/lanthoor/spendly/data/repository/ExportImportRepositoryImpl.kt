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
import dev.lanthoor.spendly.data.local.entities.AccountEntity
import dev.lanthoor.spendly.data.local.entities.BudgetEntity
import dev.lanthoor.spendly.data.local.entities.CategoryEntity
import dev.lanthoor.spendly.data.local.entities.ExpenseEntity
import dev.lanthoor.spendly.data.local.entities.IncomeEntity
import dev.lanthoor.spendly.data.local.entities.ReceiptEntity
import dev.lanthoor.spendly.data.local.entities.RecurringTransactionEntity
import dev.lanthoor.spendly.domain.repository.ExportImportRepository
import dev.lanthoor.spendly.domain.repository.ExportProgress
import dev.lanthoor.spendly.domain.repository.ExportResult
import dev.lanthoor.spendly.domain.repository.ImportProgress
import dev.lanthoor.spendly.domain.repository.ImportResult
import dev.lanthoor.spendly.domain.repository.ImportValidation
import dev.lanthoor.spendly.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
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
    private val database: SpendlyDatabase,
    @ApplicationContext private val context: Context
) : ExportImportRepository {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

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
                "receipts" to receipts.size,
                "budgets" to budgets.size,
                "recurringTransactions" to recurring.size
            )

            val export = SpendlyExport(
                metadata = ExportMetadata(
                    exportVersion = 1,
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
        onProgress: (ImportProgress) -> Unit
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
                clearUserData()

                // Phase 4: Import master data (30%)
                ensureActive()
                onProgress(ImportProgress.ImportingMasterData())
                val idMappings = importMasterData(export)

                // Phase 5: Import expenses (35-50%)
                importExpenses(export.expenses, idMappings, onProgress)

                // Phase 6: Import receipts (50-70%)
                importReceipts(export.receipts, idMappings, onProgress)

                // Phase 7: Import income (70-80%)
                importIncome(export.income, idMappings, onProgress)

                // Phase 8: Import budgets (85%)
                ensureActive()
                onProgress(ImportProgress.ImportingBudgets())
                importBudgets(export.budgets, idMappings)

                // Phase 9: Import recurring transactions (92%)
                ensureActive()
                onProgress(ImportProgress.ImportingRecurring())
                importRecurringTransactions(export.recurringTransactions, idMappings)

                // Phase 10: Finalize (98%)
                ensureActive()
                onProgress(ImportProgress.Finalizing())
            }

            val recordCounts = export.metadata.recordCounts
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

    // ========== VALIDATION ==========

    private fun validateExport(export: SpendlyExport): ImportValidation {
        val errors = mutableListOf<String>()

        // Check currency
        if (export.metadata.currency != "INR") {
            errors.add("Unsupported currency: ${export.metadata.currency}")
        }

        // Check export version
        if (export.metadata.exportVersion > 1) {
            errors.add("Export version ${export.metadata.exportVersion} not supported")
        }

        // Check record counts match
        if (export.categories.size != export.metadata.recordCounts["categories"]) {
            errors.add("Category count mismatch")
        }

        // Validate foreign key integrity
        val categoryIds = export.categories.map { it.id }.toSet()
        val accountIds = export.accounts.map { it.id }.toSet()
        val expenseIds = export.expenses.map { it.id }.toSet()

        export.expenses.forEach { expense ->
            expense.categoryId?.let {
                if (it !in categoryIds) errors.add("Expense ${expense.id} references missing category $it")
            }
            if (expense.accountId !in accountIds) {
                errors.add("Expense ${expense.id} references missing account ${expense.accountId}")
            }
        }

        export.income.forEach { income ->
            income.categoryId?.let {
                if (it !in categoryIds) errors.add("Income ${income.id} references missing category $it")
            }
            if (income.accountId !in accountIds) {
                errors.add("Income ${income.id} references missing account ${income.accountId}")
            }
            income.linkedExpenseId?.let {
                if (it !in expenseIds) errors.add("Income ${income.id} references missing expense $it")
            }
        }

        return if (errors.isEmpty()) {
            ImportValidation.Valid
        } else {
            ImportValidation.Invalid(errors)
        }
    }

    // ========== IMPORT HELPERS ==========

    private suspend fun clearUserData() {
        // Delete all transaction data
        receiptDao.deleteAll() // Must delete receipts first (FK CASCADE from expenses)
        expenseDao.deleteAll()
        incomeDao.deleteAll()
        budgetDao.deleteAll()
        recurringTransactionDao.deleteAll()

        // Delete custom categories and accounts (preserve predefined)
        categoryDao.deleteCustomCategories()
        accountDao.deleteCustomAccounts()

        // Delete receipt files
        val receiptsDir = FileUtils.getReceiptsDirectory(context)
        receiptsDir.listFiles()?.forEach { file ->
            if (file.isFile) {
                file.delete()
            }
        }
    }

    private data class IdMappings(
        val categories: MutableMap<Long, Long> = mutableMapOf(),
        val accounts: MutableMap<Long, Long> = mutableMapOf(),
        val expenses: MutableMap<Long, Long> = mutableMapOf()
    )

    private suspend fun importMasterData(export: SpendlyExport): IdMappings {
        val mappings = IdMappings()

        // Import categories
        export.categories.forEach { categoryExport ->
            if (!categoryExport.isCustom) {
                // Predefined category - match by name
                val existing = categoryDao.getCategoryByName(categoryExport.name)
                if (existing != null) {
                    mappings.categories[categoryExport.id] = existing.id
                } else {
                    // Predefined category missing - insert it
                    val newId = categoryDao.insert(categoryExport.toEntity())
                    mappings.categories[categoryExport.id] = newId
                }
            } else {
                // Custom category - insert with new ID
                val newId = categoryDao.insert(categoryExport.toEntity())
                mappings.categories[categoryExport.id] = newId
            }
        }

        // Import accounts
        export.accounts.forEach { accountExport ->
            if (!accountExport.isCustom) {
                // Predefined account - match by name
                val existing = accountDao.getAccountByName(accountExport.name)
                if (existing != null) {
                    mappings.accounts[accountExport.id] = existing.id
                } else {
                    // Predefined account missing - insert it
                    val newId = accountDao.insert(accountExport.toEntity())
                    mappings.accounts[accountExport.id] = newId
                }
            } else {
                // Custom account - insert with new ID
                val newId = accountDao.insert(accountExport.toEntity())
                mappings.accounts[accountExport.id] = newId
            }
        }

        return mappings
    }

    private suspend fun importExpenses(
        expenses: List<ExpenseExport>,
        mappings: IdMappings,
        onProgress: (ImportProgress) -> Unit
    ) = coroutineScope {
        expenses.forEachIndexed { index, expenseExport ->
            ensureActive()
            onProgress(ImportProgress.ImportingExpenses(index + 1, expenses.size))

            val newId = expenseDao.insert(expenseExport.toEntity(mappings))
            mappings.expenses[expenseExport.id] = newId
        }
    }

    private suspend fun importReceipts(
        receipts: List<ReceiptExport>,
        mappings: IdMappings,
        onProgress: (ImportProgress) -> Unit
    ) = coroutineScope {
        val receiptsDir = FileUtils.getReceiptsDirectory(context)

        receipts.forEachIndexed { index, receiptExport ->
            ensureActive()
            onProgress(ImportProgress.ImportingReceipts(index + 1, receipts.size))

            val newExpenseId = mappings.expenses[receiptExport.expenseId]
                ?: throw IllegalStateException("Missing expense ID mapping: ${receiptExport.expenseId}")

            // Decode Base64 and save file
            if (receiptExport.base64Data.isNotEmpty()) {
                try {
                    val bytes = Base64.decode(receiptExport.base64Data, Base64.NO_WRAP)
                    val fileName =
                        "receipt_${newExpenseId}_${System.currentTimeMillis()}.${receiptExport.fileType.lowercase()}"
                    val file = File(receiptsDir, fileName)
                    file.writeBytes(bytes)

                    // Insert receipt record with new expense ID and file path
                    val entity = receiptExport.toEntity(newExpenseId, fileName)
                    receiptDao.insert(entity)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to import receipt ${receiptExport.id}", e)
                    // Continue with other receipts
                }
            }
        }
    }

    private suspend fun importIncome(
        income: List<IncomeExport>,
        mappings: IdMappings,
        onProgress: (ImportProgress) -> Unit
    ) = coroutineScope {
        income.forEachIndexed { index, incomeExport ->
            ensureActive()
            onProgress(ImportProgress.ImportingIncome(index + 1, income.size))

            incomeDao.insert(incomeExport.toEntity(mappings))
        }
    }

    private suspend fun importBudgets(
        budgets: List<BudgetExport>,
        mappings: IdMappings
    ) = coroutineScope {
        budgets.forEach { budgetExport ->
            ensureActive()
            budgetDao.insert(budgetExport.toEntity(mappings))
        }
    }

    private suspend fun importRecurringTransactions(
        recurring: List<RecurringTransactionExport>,
        mappings: IdMappings
    ) = coroutineScope {
        recurring.forEach { recurringExport ->
            ensureActive()
            recurringTransactionDao.insert(recurringExport.toEntity(mappings))
        }
    }

    // ========== ENTITY MAPPERS ==========

    // Category
    private fun CategoryEntity.toExport() = CategoryExport(
        id = id,
        name = name,
        icon = icon,
        color = color,
        isCustom = isCustom,
        sortOrder = sortOrder
    )

    private fun CategoryExport.toEntity() = CategoryEntity(
        id = 0, // Auto-generate new ID
        name = name,
        icon = icon,
        color = color,
        isCustom = isCustom,
        sortOrder = sortOrder
    )

    // Account
    private fun AccountEntity.toExport() = AccountExport(
        id = id,
        name = name,
        type = type,
        icon = icon,
        color = color,
        isCustom = isCustom,
        sortOrder = sortOrder,
        createdAt = createdAt,
        modifiedAt = modifiedAt
    )

    private fun AccountExport.toEntity() = AccountEntity(
        id = 0, // Auto-generate new ID
        name = name,
        type = type,
        icon = icon,
        color = color,
        isCustom = isCustom,
        sortOrder = sortOrder,
        createdAt = createdAt,
        modifiedAt = modifiedAt
    )

    // Expense
    private fun ExpenseEntity.toExport() = ExpenseExport(
        id = id,
        amount = amount,
        categoryId = categoryId,
        date = date,
        description = description,
        accountId = accountId,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        smsSourceId = smsSourceId,
        smsBody = smsBody,
        smsConfidence = smsConfidence,
        smsTimestamp = smsTimestamp
    )

    private fun ExpenseExport.toEntity(mappings: IdMappings) = ExpenseEntity(
        id = 0, // Auto-generate new ID
        amount = amount,
        categoryId = categoryId?.let { mappings.categories[it] },
        date = date,
        description = description,
        accountId = mappings.accounts[accountId]
            ?: throw IllegalStateException("Missing account mapping: $accountId"),
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        smsSourceId = smsSourceId,
        smsBody = smsBody,
        smsConfidence = smsConfidence,
        smsTimestamp = smsTimestamp
    )

    // Income
    private fun IncomeEntity.toExport() = IncomeExport(
        id = id,
        amount = amount,
        categoryId = categoryId,
        source = source,
        date = date,
        description = description,
        accountId = accountId,
        isRecurring = isRecurring,
        linkedExpenseId = linkedExpenseId,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        smsSourceId = smsSourceId,
        smsBody = smsBody,
        smsConfidence = smsConfidence,
        smsTimestamp = smsTimestamp
    )

    private fun IncomeExport.toEntity(mappings: IdMappings) = IncomeEntity(
        id = 0, // Auto-generate new ID
        amount = amount,
        categoryId = categoryId?.let { mappings.categories[it] },
        source = source ?: "", // Default to empty string if null (deprecated field)
        date = date,
        description = description,
        accountId = mappings.accounts[accountId]
            ?: throw IllegalStateException("Missing account mapping: $accountId"),
        isRecurring = isRecurring,
        linkedExpenseId = linkedExpenseId?.let { mappings.expenses[it] },
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        smsSourceId = smsSourceId,
        smsBody = smsBody,
        smsConfidence = smsConfidence,
        smsTimestamp = smsTimestamp
    )

    // Receipt
    private fun ReceiptEntity.toExport(base64Data: String) = ReceiptExport(
        id = id,
        expenseId = expenseId,
        filePath = filePath,
        fileType = fileType,
        fileSizeBytes = fileSizeBytes,
        compressed = compressed,
        createdAt = createdAt,
        base64Data = base64Data
    )

    private fun ReceiptExport.toEntity(newExpenseId: Long, newFilePath: String) = ReceiptEntity(
        id = 0, // Auto-generate new ID
        expenseId = newExpenseId,
        filePath = newFilePath,
        fileType = fileType,
        fileSizeBytes = fileSizeBytes,
        compressed = compressed,
        createdAt = createdAt
    )

    // Budget
    private fun BudgetEntity.toExport() = BudgetExport(
        id = id,
        categoryId = categoryId,
        amount = amount,
        month = month,
        year = year,
        notification75Sent = notification75Sent,
        notification100Sent = notification100Sent,
        createdAt = createdAt,
        modifiedAt = modifiedAt
    )

    private fun BudgetExport.toEntity(mappings: IdMappings) = BudgetEntity(
        id = 0, // Auto-generate new ID
        categoryId = categoryId?.let { mappings.categories[it] },
        amount = amount,
        month = month,
        year = year,
        notification75Sent = notification75Sent,
        notification100Sent = notification100Sent,
        createdAt = createdAt,
        modifiedAt = modifiedAt
    )

    // Recurring Transaction
    private fun RecurringTransactionEntity.toExport() = RecurringTransactionExport(
        id = id,
        transactionType = transactionType,
        amount = amount,
        categoryId = categoryId,
        accountId = accountId,
        description = description,
        frequency = frequency,
        nextDate = nextDate,
        lastProcessed = lastProcessed,
        createdAt = createdAt,
        modifiedAt = modifiedAt
    )

    private fun RecurringTransactionExport.toEntity(mappings: IdMappings) =
        RecurringTransactionEntity(
            id = 0, // Auto-generate new ID
            transactionType = transactionType,
            amount = amount,
            categoryId = mappings.categories[categoryId]
                ?: throw IllegalStateException("Missing category mapping: $categoryId"),
            accountId = mappings.accounts[accountId]
                ?: throw IllegalStateException("Missing account mapping: $accountId"),
            description = description,
            frequency = frequency,
            nextDate = nextDate,
            lastProcessed = lastProcessed,
            createdAt = createdAt,
            modifiedAt = modifiedAt
        )
}
