package dev.lanthoor.spendly.data.repository

import dev.lanthoor.spendly.data.exportimport.AccountExport
import dev.lanthoor.spendly.data.exportimport.BudgetExport
import dev.lanthoor.spendly.data.exportimport.CategoryExport
import dev.lanthoor.spendly.data.exportimport.ExpenseExport
import dev.lanthoor.spendly.data.exportimport.IncomeExport
import dev.lanthoor.spendly.data.exportimport.ReceiptExport
import dev.lanthoor.spendly.data.exportimport.RecurringTransactionExport
import dev.lanthoor.spendly.data.local.entities.AccountEntity
import dev.lanthoor.spendly.data.local.entities.BudgetEntity
import dev.lanthoor.spendly.data.local.entities.CategoryEntity
import dev.lanthoor.spendly.data.local.entities.ExpenseEntity
import dev.lanthoor.spendly.data.local.entities.IncomeEntity
import dev.lanthoor.spendly.data.local.entities.ReceiptEntity
import dev.lanthoor.spendly.data.local.entities.RecurringTransactionEntity

data class IdMappings(
    val categories: MutableMap<Long, Long> = mutableMapOf(),
    val accounts: MutableMap<Long, Long> = mutableMapOf(),
    val expenses: MutableMap<Long, Long> = mutableMapOf()
)

fun CategoryEntity.toExport() = CategoryExport(
    id = id,
    name = name,
    icon = icon,
    color = color,
    isCustom = isCustom,
    sortOrder = sortOrder
)

fun CategoryExport.toEntity() = CategoryEntity(
    id = 0,
    name = name,
    icon = icon,
    color = color,
    isCustom = isCustom,
    sortOrder = sortOrder
)

fun AccountEntity.toExport() = AccountExport(
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

fun AccountExport.toEntity() = AccountEntity(
    id = 0,
    name = name,
    type = type,
    icon = icon,
    color = color,
    isCustom = isCustom,
    sortOrder = sortOrder,
    createdAt = createdAt,
    modifiedAt = modifiedAt
)

fun ExpenseEntity.toExport() = ExpenseExport(
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

fun ExpenseExport.toEntity(mappings: IdMappings) = ExpenseEntity(
    id = 0,
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

fun IncomeEntity.toExport() = IncomeExport(
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

fun IncomeExport.toEntity(mappings: IdMappings) = IncomeEntity(
    id = 0,
    amount = amount,
    categoryId = categoryId?.let { mappings.categories[it] },
    source = source ?: "",
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

fun ReceiptEntity.toExport(base64Data: String) = ReceiptExport(
    id = id,
    expenseId = expenseId,
    filePath = filePath,
    fileType = fileType,
    fileSizeBytes = fileSizeBytes,
    compressed = compressed,
    createdAt = createdAt,
    base64Data = base64Data
)

fun ReceiptExport.toEntity(newExpenseId: Long, newFilePath: String) = ReceiptEntity(
    id = 0,
    expenseId = newExpenseId,
    filePath = newFilePath,
    fileType = fileType,
    fileSizeBytes = fileSizeBytes,
    compressed = compressed,
    createdAt = createdAt
)

fun BudgetEntity.toExport() = BudgetExport(
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

fun BudgetExport.toEntity(mappings: IdMappings) = BudgetEntity(
    id = 0,
    categoryId = categoryId?.let { mappings.categories[it] },
    amount = amount,
    month = month,
    year = year,
    notification75Sent = notification75Sent,
    notification100Sent = notification100Sent,
    createdAt = createdAt,
    modifiedAt = modifiedAt
)

fun RecurringTransactionEntity.toExport() = RecurringTransactionExport(
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

fun RecurringTransactionExport.toEntity(mappings: IdMappings) = RecurringTransactionEntity(
    id = 0,
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
