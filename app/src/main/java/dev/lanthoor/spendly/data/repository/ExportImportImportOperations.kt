package dev.lanthoor.spendly.data.repository

import android.content.Context
import android.util.Base64
import android.util.Log
import dev.lanthoor.spendly.data.exportimport.BudgetExport
import dev.lanthoor.spendly.data.exportimport.ExpenseExport
import dev.lanthoor.spendly.data.exportimport.IncomeExport
import dev.lanthoor.spendly.data.exportimport.ReceiptExport
import dev.lanthoor.spendly.data.exportimport.RecurringTransactionExport
import dev.lanthoor.spendly.data.exportimport.SpendlyExport
import dev.lanthoor.spendly.data.local.dao.AccountDao
import dev.lanthoor.spendly.data.local.dao.BudgetDao
import dev.lanthoor.spendly.data.local.dao.CategoryDao
import dev.lanthoor.spendly.data.local.dao.ExpenseDao
import dev.lanthoor.spendly.data.local.dao.IncomeDao
import dev.lanthoor.spendly.data.local.dao.ReceiptDao
import dev.lanthoor.spendly.data.local.dao.RecurringTransactionDao
import dev.lanthoor.spendly.data.local.dao.TransactionAiEnrichmentDao
import dev.lanthoor.spendly.domain.repository.ImportProgress
import dev.lanthoor.spendly.utils.FileUtils
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import java.io.File

private const val IMPORT_TAG = "ExportImportImportOperations"

class ExportImportImportOperations(
    private val categoryDao: CategoryDao,
    private val accountDao: AccountDao,
    private val expenseDao: ExpenseDao,
    private val incomeDao: IncomeDao,
    private val receiptDao: ReceiptDao,
    private val budgetDao: BudgetDao,
    private val recurringTransactionDao: RecurringTransactionDao,
    private val transactionAiEnrichmentDao: TransactionAiEnrichmentDao,
    private val context: Context
) {

    suspend fun clearUserData() {
        receiptDao.deleteAll()
        expenseDao.deleteAll()
        incomeDao.deleteAll()
        budgetDao.deleteAll()
        recurringTransactionDao.deleteAll()
        transactionAiEnrichmentDao.deleteAll()

        categoryDao.deleteCustomCategories()
        accountDao.deleteCustomAccounts()

        val receiptsDir = FileUtils.getReceiptsDirectory(context)
        receiptsDir.listFiles()?.forEach { file ->
            if (file.isFile) {
                file.delete()
            }
        }
    }

    suspend fun importMasterData(export: SpendlyExport): IdMappings {
        val mappings = IdMappings()

        export.categories.forEach { categoryExport ->
            if (!categoryExport.isCustom) {
                val existing = categoryDao.getCategoryByName(categoryExport.name)
                if (existing != null) {
                    mappings.categories[categoryExport.id] = existing.id
                } else {
                    val newId = categoryDao.insert(categoryExport.toEntity())
                    mappings.categories[categoryExport.id] = newId
                }
            } else {
                val newId = categoryDao.insert(categoryExport.toEntity())
                mappings.categories[categoryExport.id] = newId
            }
        }

        export.accounts.forEach { accountExport ->
            if (!accountExport.isCustom) {
                val existing = accountDao.getAccountByName(accountExport.name)
                if (existing != null) {
                    mappings.accounts[accountExport.id] = existing.id
                } else {
                    val newId = accountDao.insert(accountExport.toEntity())
                    mappings.accounts[accountExport.id] = newId
                }
            } else {
                val newId = accountDao.insert(accountExport.toEntity())
                mappings.accounts[accountExport.id] = newId
            }
        }

        return mappings
    }

    suspend fun importExpenses(
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

    suspend fun importReceipts(
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

            if (receiptExport.base64Data.isNotEmpty()) {
                try {
                    val bytes = Base64.decode(receiptExport.base64Data, Base64.NO_WRAP)
                    val fileName =
                        "receipt_${newExpenseId}_${System.currentTimeMillis()}.${receiptExport.fileType.lowercase()}"
                    val file = File(receiptsDir, fileName)
                    file.writeBytes(bytes)

                    val entity = receiptExport.toEntity(newExpenseId, fileName)
                    receiptDao.insert(entity)
                } catch (e: Exception) {
                    Log.e(IMPORT_TAG, "Failed to import receipt ${receiptExport.id}", e)
                }
            }
        }
    }

    suspend fun importIncome(
        income: List<IncomeExport>,
        mappings: IdMappings,
        onProgress: (ImportProgress) -> Unit
    ) = coroutineScope {
        income.forEachIndexed { index, incomeExport ->
            ensureActive()
            onProgress(ImportProgress.ImportingIncome(index + 1, income.size))

            val newId = incomeDao.insert(incomeExport.toEntity(mappings))
            mappings.income[incomeExport.id] = newId
        }
    }

    suspend fun importBudgets(
        budgets: List<BudgetExport>,
        mappings: IdMappings
    ) = coroutineScope {
        budgets.forEach { budgetExport ->
            ensureActive()
            budgetDao.insert(budgetExport.toEntity(mappings))
        }
    }

    suspend fun importRecurringTransactions(
        recurring: List<RecurringTransactionExport>,
        mappings: IdMappings
    ) = coroutineScope {
        recurring.forEach { recurringExport ->
            ensureActive()
            recurringTransactionDao.insert(recurringExport.toEntity(mappings))
        }
    }

    suspend fun importAiEnrichments(
        aiEnrichments: List<dev.lanthoor.spendly.data.exportimport.TransactionAiEnrichmentExport>,
        mappings: IdMappings
    ) = coroutineScope {
        aiEnrichments.forEach { enrichmentExport ->
            ensureActive()
            try {
                transactionAiEnrichmentDao.insert(enrichmentExport.toEntity(mappings))
            } catch (e: Exception) {
                Log.w(IMPORT_TAG, "Skipping invalid AI enrichment ${enrichmentExport.id}", e)
            }
        }
    }
}
