package `in`.mylullaby.spendly.domain.repository

import `in`.mylullaby.spendly.domain.model.Expense
import `in`.mylullaby.spendly.utils.PaymentMethod
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for expense operations.
 * Provides CRUD operations, queries, and aggregations for expense data.
 * All queries return expenses sorted by date DESC (newest first) by default.
 */
interface ExpenseRepository {

    // CRUD operations

    /**
     * Inserts a new expense into the database.
     * @param expense The expense to insert
     * @return The ID of the inserted expense
     */
    suspend fun insertExpense(expense: Expense): Long

    /**
     * Updates an existing expense in the database.
     * @param expense The expense to update
     */
    suspend fun updateExpense(expense: Expense)

    /**
     * Deletes an expense from the database.
     * @param expense The expense to delete
     */
    suspend fun deleteExpense(expense: Expense)

    /**
     * Retrieves an expense by its ID.
     * @param id The expense ID
     * @return Flow emitting the expense or null if not found
     */
    fun getExpenseById(id: Long): Flow<Expense?>

    /**
     * Retrieves all expenses.
     * @return Flow emitting list of all expenses (sorted by date DESC)
     */
    fun getAllExpenses(): Flow<List<Expense>>

    // Queries with default sort: date DESC (newest first)

    /**
     * Retrieves expenses within a date range.
     * @param startDate Start date timestamp (inclusive)
     * @param endDate End date timestamp (inclusive)
     * @return Flow emitting list of expenses in the date range
     */
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>>

    /**
     * Retrieves expenses for a specific category.
     * @param categoryId The category ID
     * @return Flow emitting list of expenses in the category
     */
    fun getExpensesByCategory(categoryId: Long): Flow<List<Expense>>

    /**
     * Retrieves expenses filtered by payment method.
     * @param method The payment method
     * @return Flow emitting list of expenses using the payment method
     */
    fun getExpensesByPaymentMethod(method: PaymentMethod): Flow<List<Expense>>

    /**
     * Retrieves expenses that have any of the specified tags.
     * @param tagIds List of tag IDs to filter by
     * @return Flow emitting list of expenses with the tags
     */
    fun getExpensesByTags(tagIds: List<Long>): Flow<List<Expense>>

    // Aggregations (returns amounts in paise)

    /**
     * Calculates total spent within a date range.
     * @param startDate Start date timestamp (inclusive)
     * @param endDate End date timestamp (inclusive)
     * @return Flow emitting total amount spent in paise
     */
    fun getTotalSpentInRange(startDate: Long, endDate: Long): Flow<Long>

    /**
     * Calculates total spent for a specific category within a date range.
     * @param categoryId The category ID
     * @param startDate Start date timestamp (inclusive)
     * @param endDate End date timestamp (inclusive)
     * @return Flow emitting total amount spent in paise
     */
    fun getTotalSpentByCategory(categoryId: Long, startDate: Long, endDate: Long): Flow<Long>

    /**
     * Gets spending breakdown by category within a date range.
     * @param startDate Start date timestamp (inclusive)
     * @param endDate End date timestamp (inclusive)
     * @return Flow emitting map of category ID to total spent (in paise)
     */
    fun getCategorySpendingBreakdown(startDate: Long, endDate: Long): Flow<Map<Long, Long>>

    // Recent transactions

    /**
     * Retrieves the most recent expenses.
     * @param limit Maximum number of expenses to retrieve (default: 5)
     * @return Flow emitting list of recent expenses
     */
    fun getRecentExpenses(limit: Int = 5): Flow<List<Expense>>
}
