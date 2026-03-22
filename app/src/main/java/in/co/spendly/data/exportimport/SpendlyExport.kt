package `in`.co.spendly.data.exportimport

import kotlinx.serialization.Serializable

/**
 * Root export structure containing all app data
 *
 * Version history:
 * - Version 1: Initial schema (all 7 entities)
 */
@Serializable
data class SpendlyExport(
    val metadata: ExportMetadata,
    val categories: List<CategoryExport>,
    val accounts: List<AccountExport>,
    val expenses: List<ExpenseExport>,
    val income: List<IncomeExport>,
    val receipts: List<ReceiptExport>,
    val budgets: List<BudgetExport>,
    val recurringTransactions: List<RecurringTransactionExport>
)

/**
 * Metadata about the export for validation and version compatibility
 */
@Serializable
data class ExportMetadata(
    val exportVersion: Int = 1,
    val appVersion: String,
    val databaseVersion: Int,
    val exportDate: Long,
    val currency: String = "INR",
    val recordCounts: Map<String, Int>
)

/**
 * Category export model
 */
@Serializable
data class CategoryExport(
    val id: Long,
    val name: String,
    val icon: String,
    val color: Int,
    val isCustom: Boolean,
    val sortOrder: Int
)

/**
 * Account export model
 */
@Serializable
data class AccountExport(
    val id: Long,
    val name: String,
    val type: String,  // BANK, CARD, WALLET, CASH, LOAN, INVESTMENT
    val icon: String,
    val color: Int,
    val isCustom: Boolean,
    val sortOrder: Int,
    val createdAt: Long,
    val modifiedAt: Long
)

/**
 * Expense export model with SMS metadata
 */
@Serializable
data class ExpenseExport(
    val id: Long,
    val amount: Long,  // paise
    val categoryId: Long?,
    val date: Long,
    val description: String,
    val accountId: Long,
    val createdAt: Long,
    val modifiedAt: Long,
    val smsSourceId: Long?,
    val smsBody: String?,
    val smsConfidence: Float?,
    val smsTimestamp: Long?
)

/**
 * Income export model with SMS metadata and refund linking
 */
@Serializable
data class IncomeExport(
    val id: Long,
    val amount: Long,  // paise
    val categoryId: Long?,
    val source: String?,  // Deprecated field, kept for backwards compatibility
    val date: Long,
    val description: String,
    val accountId: Long,
    val isRecurring: Boolean,
    val linkedExpenseId: Long?,
    val createdAt: Long,
    val modifiedAt: Long,
    val smsSourceId: Long?,
    val smsBody: String?,
    val smsConfidence: Float?,
    val smsTimestamp: Long?
)

/**
 * Receipt export model with Base64-encoded file data
 */
@Serializable
data class ReceiptExport(
    val id: Long,
    val expenseId: Long,
    val filePath: String,  // Original path for reference
    val fileType: String,  // JPG, PNG, WebP, PDF
    val fileSizeBytes: Long,
    val compressed: Boolean,
    val createdAt: Long,
    val base64Data: String  // Base64-encoded file content
)

/**
 * Budget export model with notification tracking
 */
@Serializable
data class BudgetExport(
    val id: Long,
    val categoryId: Long?,  // null = overall budget
    val amount: Long,  // paise
    val month: Int,  // 1-12
    val year: Int,
    val notification75Sent: Boolean,
    val notification100Sent: Boolean,
    val createdAt: Long,
    val modifiedAt: Long
)

/**
 * Recurring transaction export model
 */
@Serializable
data class RecurringTransactionExport(
    val id: Long,
    val transactionType: String,  // EXPENSE or INCOME
    val amount: Long,  // paise
    val categoryId: Long,
    val accountId: Long,
    val description: String,
    val frequency: String,  // DAILY, WEEKLY, MONTHLY
    val nextDate: Long,
    val lastProcessed: Long?,
    val createdAt: Long,
    val modifiedAt: Long
)
