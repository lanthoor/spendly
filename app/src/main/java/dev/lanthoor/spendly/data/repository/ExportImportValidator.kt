package dev.lanthoor.spendly.data.repository

import dev.lanthoor.spendly.data.exportimport.SpendlyExport
import dev.lanthoor.spendly.domain.repository.ImportValidation

class ExportImportValidator {

    fun validateExport(export: SpendlyExport): ImportValidation {
        val errors = mutableListOf<String>()

        if (export.metadata.currency != "INR") {
            errors.add("Unsupported currency: ${export.metadata.currency}")
        }

        if (export.metadata.exportVersion > 2) {
            errors.add("Export version ${export.metadata.exportVersion} not supported")
        }

        if (export.categories.size != export.metadata.recordCounts["categories"]) {
            errors.add("Category count mismatch")
        }

        export.metadata.recordCounts["aiEnrichments"]?.let { expected ->
            if (export.aiEnrichments.size != expected) {
                errors.add("AI enrichment count mismatch")
            }
        }

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

        val allowedStatuses = setOf("PENDING", "ENRICHED", "FAILED")
        val incomeIds = export.income.map { it.id }.toSet()
        export.aiEnrichments.forEach { enrichment ->
            if (enrichment.transactionType !in setOf("EXPENSE", "INCOME")) {
                errors.add("AI enrichment ${enrichment.id} has invalid transaction type ${enrichment.transactionType}")
            }
            if (enrichment.status !in allowedStatuses) {
                errors.add("AI enrichment ${enrichment.id} has invalid status ${enrichment.status}")
            }
            when (enrichment.transactionType) {
                "EXPENSE" -> {
                    if (enrichment.transactionId !in expenseIds) {
                        errors.add("AI enrichment ${enrichment.id} references missing expense ${enrichment.transactionId}")
                    }
                }

                "INCOME" -> {
                    if (enrichment.transactionId !in incomeIds) {
                        errors.add("AI enrichment ${enrichment.id} references missing income ${enrichment.transactionId}")
                    }
                }
            }
        }

        return if (errors.isEmpty()) {
            ImportValidation.Valid
        } else {
            ImportValidation.Invalid(errors)
        }
    }
}
