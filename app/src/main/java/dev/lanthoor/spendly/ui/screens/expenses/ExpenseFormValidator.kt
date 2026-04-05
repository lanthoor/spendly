package dev.lanthoor.spendly.ui.screens.expenses

object ExpenseFormValidator {
    fun validateAmount(amount: String): String? {
        val cleanAmount = amount.replace(",", "")
        return when {
            cleanAmount.isBlank() -> "Amount is required"
            cleanAmount.toDoubleOrNull() == null -> "Invalid amount format"
            cleanAmount.toDouble() <= 0 -> "Amount must be greater than 0"
            else -> null
        }
    }

    fun validateDescription(description: String): String? {
        return when {
            description.isBlank() -> "Description is required"
            description.length < 3 -> "Description must be at least 3 characters"
            description.length > 200 -> "Description must not exceed 200 characters"
            else -> null
        }
    }
}
