package `in`.mylullaby.spendly.domain.repository

import `in`.mylullaby.spendly.domain.model.Budget
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for budget operations.
 * Handles both category-specific and overall budgets.
 */
interface BudgetRepository {

    // CRUD operations

    /**
     * Inserts a new budget into the database.
     * @param budget The budget to insert
     * @return The ID of the inserted budget
     */
    suspend fun insertBudget(budget: Budget): Long

    /**
     * Updates an existing budget in the database.
     * @param budget The budget to update
     */
    suspend fun updateBudget(budget: Budget)

    /**
     * Deletes a budget from the database.
     * @param budget The budget to delete
     */
    suspend fun deleteBudget(budget: Budget)

    /**
     * Retrieves a budget by its ID.
     * @param id The budget ID
     * @return Flow emitting the budget or null if not found
     */
    fun getBudgetById(id: Long): Flow<Budget?>

    /**
     * Retrieves all budgets.
     * @return Flow emitting list of all budgets
     */
    fun getAllBudgets(): Flow<List<Budget>>

    // Month-based queries

    /**
     * Retrieves all budgets for a specific month and year.
     * @param month The month (1-12)
     * @param year The year (e.g., 2025)
     * @return Flow emitting list of budgets for the month
     */
    fun getBudgetsForMonth(month: Int, year: Int): Flow<List<Budget>>

    /**
     * Retrieves the overall budget for a specific month and year.
     * @param month The month (1-12)
     * @param year The year (e.g., 2025)
     * @return Flow emitting the overall budget or null if not found
     */
    fun getOverallBudget(month: Int, year: Int): Flow<Budget?>

    /**
     * Retrieves the budget for a specific category in a given month and year.
     * @param categoryId The category ID
     * @param month The month (1-12)
     * @param year The year (e.g., 2025)
     * @return Flow emitting the category budget or null if not found
     */
    fun getCategoryBudget(categoryId: Long, month: Int, year: Int): Flow<Budget?>

    // Notification tracking

    /**
     * Marks the 75% notification as sent for a budget.
     * @param budgetId The budget ID
     */
    suspend fun markNotification75Sent(budgetId: Long)

    /**
     * Marks the 100% notification as sent for a budget.
     * @param budgetId The budget ID
     */
    suspend fun markNotification100Sent(budgetId: Long)

    // Budget vs actual calculation

    /**
     * Calculates the budget progress percentage.
     * @param budgetId The budget ID
     * @param currentSpent Current amount spent in paise
     * @return Progress percentage (0.0 to 100.0+)
     */
    suspend fun getBudgetProgress(budgetId: Long, currentSpent: Long): Float
}
