package `in`.mylullaby.spendly.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import `in`.mylullaby.spendly.data.local.dao.*
import `in`.mylullaby.spendly.data.local.entities.*

/**
 * Room database for Spendly expense tracker.
 *
 * This is the main database class that manages all entities and provides
 * access to DAOs for database operations.
 *
 * **Version**: 1 (initial schema)
 * **Export Schema**: true (for migration testing)
 *
 * **Entities**:
 * - CategoryEntity: Expense categories (predefined + custom)
 * - ExpenseEntity: Expense transactions
 * - ReceiptEntity: Attached receipt files
 * - IncomeEntity: Income transactions
 * - BudgetEntity: Monthly budgets per category
 * - RecurringTransactionEntity: Recurring transaction configurations
 * - TagEntity: Custom tags for transactions
 * - TransactionTagEntity: Many-to-many junction table for tags
 */
@Database(
    entities = [
        CategoryEntity::class,
        ExpenseEntity::class,
        ReceiptEntity::class,
        IncomeEntity::class,
        BudgetEntity::class,
        RecurringTransactionEntity::class,
        TagEntity::class,
        TransactionTagEntity::class
    ],
    version = 1,
    exportSchema = true
)
// @TypeConverters annotation will be added when converters are needed
abstract class SpendlyDatabase : RoomDatabase() {

    // DAOs
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun receiptDao(): ReceiptDao
    abstract fun incomeDao(): IncomeDao
    abstract fun budgetDao(): BudgetDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun tagDao(): TagDao
    abstract fun transactionTagDao(): TransactionTagDao

    companion object {
        const val DATABASE_NAME = "spendly_database"
    }
}
