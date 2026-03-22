package dev.lanthoor.spendly.ui.screens.expenses

import dev.lanthoor.spendly.domain.model.Account
import dev.lanthoor.spendly.domain.model.Receipt

/**
 * Form state for add/edit expense screens
 */
data class ExpenseFormState(
    val id: Long = 0,
    val amount: String = "",
    val amountError: String? = null,
    val categoryId: Long? = 13L, // Default to "Others" category
    val date: Long = System.currentTimeMillis(),
    val description: String = "",
    val descriptionError: String? = null,
    val accountId: Long = Account.DEFAULT_ACCOUNT_ID,
    val createdAt: Long? = null,
    val isEditMode: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val receipts: List<Receipt> = emptyList(),
    val receiptError: String? = null,
    val smsSourceId: Long? = null,
    val smsBody: String? = null,
    val smsConfidence: Float? = null,
    val smsTimestamp: Long? = null
)

/**
 * Form fields enum for type-safe updates
 */
enum class FormField {
    AMOUNT,
    CATEGORY_ID,
    DATE,
    DESCRIPTION,
    ACCOUNT_ID
}
