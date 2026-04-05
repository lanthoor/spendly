package dev.lanthoor.spendly.core.model.finance

import dev.lanthoor.spendly.domain.model.Budget
import dev.lanthoor.spendly.domain.model.Category

data class BudgetWithProgress(
    val budget: Budget,
    val category: Category?,
    val currentSpent: Long,
    val progress: Float,
    val shouldNotify75: Boolean,
    val shouldNotify100: Boolean
)
