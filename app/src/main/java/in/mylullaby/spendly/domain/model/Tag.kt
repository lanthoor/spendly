package `in`.mylullaby.spendly.domain.model

import `in`.mylullaby.spendly.utils.TransactionType

/**
 * Domain model representing a tag for flexible transaction organization.
 * Tags can be associated with both expenses and income.
 */
data class Tag(
    val id: Long = 0,
    val name: String
)

/**
 * Domain model representing the many-to-many relationship
 * between transactions (expenses/income) and tags.
 */
data class TransactionTag(
    val transactionType: TransactionType, // EXPENSE or INCOME
    val transactionId: Long,
    val tagId: Long
)
