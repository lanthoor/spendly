package `in`.mylullaby.spendly.utils

import `in`.mylullaby.spendly.domain.model.Expense
import `in`.mylullaby.spendly.domain.model.Income
import `in`.mylullaby.spendly.domain.repository.ExpenseRepository
import `in`.mylullaby.spendly.domain.repository.IncomeRepository
import `in`.mylullaby.spendly.domain.repository.RecurringTransactionRepository
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Processor for recurring transactions.
 * Handles automatic creation of expenses/income based on recurring schedules.
 *
 * Design decision: Transactions are auto-created without prompts and are fully editable/deletable.
 */
@Singleton
class RecurringTransactionProcessor @Inject constructor(
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository
) {

    /**
     * Processes all recurring transactions that are due.
     * Creates new expense/income records and updates next occurrence dates.
     *
     * @param currentDate Current timestamp in milliseconds
     */
    suspend fun processRecurringTransactions(currentDate: Long = System.currentTimeMillis()) {
        // Get all recurring transactions
        val recurringTransactions = recurringTransactionRepository.getAllRecurringTransactions().first()

        for (transaction in recurringTransactions) {
            // Check if transaction is due (next_date <= currentDate)
            if (transaction.nextDate <= currentDate) {
                // Check for missed occurrences (last 3 months)
                val missedOccurrences = calculateMissedOccurrences(
                    nextDate = transaction.nextDate,
                    lastProcessed = transaction.lastProcessed,
                    frequency = transaction.frequency,
                    currentDate = currentDate
                )

                // Create transactions for each missed occurrence
                for (occurrenceDate in missedOccurrences) {
                    createTransaction(
                        transaction = transaction,
                        occurrenceDate = occurrenceDate
                    )
                }

                // Update next_date and last_processed
                val newNextDate = calculateNextDate(
                    currentDate = currentDate,
                    frequency = transaction.frequency
                )

                val updatedTransaction = transaction.copy(
                    nextDate = newNextDate,
                    lastProcessed = currentDate
                )

                recurringTransactionRepository.updateRecurringTransaction(updatedTransaction)
            }
        }
    }

    /**
     * Creates an expense or income based on the recurring transaction.
     */
    private suspend fun createTransaction(
        transaction: `in`.mylullaby.spendly.domain.model.RecurringTransaction,
        occurrenceDate: Long
    ) {
        when (transaction.transactionType) {
            "EXPENSE" -> {
                val paymentMethod = try {
                    PaymentMethod.valueOf(transaction.paymentMethod ?: "CASH")
                } catch (e: IllegalArgumentException) {
                    PaymentMethod.CASH
                }

                val expense = Expense(
                    id = 0L, // Auto-generate
                    amount = transaction.amount,
                    categoryId = transaction.categoryId,
                    date = occurrenceDate,
                    description = transaction.description,
                    paymentMethod = paymentMethod,
                    createdAt = System.currentTimeMillis(),
                    modifiedAt = System.currentTimeMillis()
                )
                expenseRepository.insertExpense(expense)
            }
            "INCOME" -> {
                val source = try {
                    IncomeSource.valueOf(transaction.paymentMethod ?: "OTHER")
                } catch (e: IllegalArgumentException) {
                    IncomeSource.OTHER
                }

                val income = Income(
                    id = 0L, // Auto-generate
                    amount = transaction.amount,
                    categoryId = transaction.categoryId, // Use recurring transaction's category
                    source = source,
                    date = occurrenceDate,
                    description = transaction.description,
                    isRecurring = false, // The created transaction is not recurring itself
                    linkedExpenseId = null,
                    createdAt = System.currentTimeMillis(),
                    modifiedAt = System.currentTimeMillis()
                )
                incomeRepository.insertIncome(income)
            }
        }
    }

    /**
     * Calculates missed occurrences within the last 3 months.
     * Returns a list of dates when transactions should have been created.
     */
    private fun calculateMissedOccurrences(
        nextDate: Long,
        lastProcessed: Long?,
        frequency: String,
        currentDate: Long
    ): List<Long> {
        val occurrences = mutableListOf<Long>()

        // If never processed, only create current occurrence
        if (lastProcessed == null) {
            if (nextDate <= currentDate) {
                occurrences.add(nextDate)
            }
            return occurrences
        }

        // Calculate 3 months ago
        val threeMonthsAgo = Calendar.getInstance().apply {
            timeInMillis = currentDate
            add(Calendar.MONTH, -3)
        }.timeInMillis

        // Start from the next scheduled date
        var checkDate = nextDate

        // Find all missed occurrences in the last 3 months
        while (checkDate <= currentDate) {
            // Only include dates within the last 3 months
            if (checkDate >= threeMonthsAgo) {
                occurrences.add(checkDate)
            }

            // Move to next occurrence
            checkDate = calculateNextDate(checkDate, frequency)
        }

        return occurrences
    }

    /**
     * Calculates the next occurrence date based on frequency.
     * Handles edge cases like leap years and month-end boundaries.
     */
    private fun calculateNextDate(currentDate: Long, frequency: String): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentDate
        }

        when (frequency.uppercase()) {
            "DAILY" -> {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            "WEEKLY" -> {
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
            }
            "MONTHLY" -> {
                // Handle month-end edge cases
                val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
                calendar.add(Calendar.MONTH, 1)

                // Handle cases like Jan 31 -> Feb 28/29
                val maxDayInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                if (dayOfMonth > maxDayInMonth) {
                    calendar.set(Calendar.DAY_OF_MONTH, maxDayInMonth)
                }
            }
            else -> {
                // Default to monthly if frequency is unknown
                calendar.add(Calendar.MONTH, 1)
            }
        }

        return calendar.timeInMillis
    }
}
