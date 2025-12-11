package `in`.mylullaby.spendly.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import `in`.mylullaby.spendly.data.local.dao.*
import `in`.mylullaby.spendly.data.local.entities.*

/**
 * Room database for Spendly expense tracker.
 *
 * This is the main database class that manages all entities and provides
 * access to DAOs for database operations.
 *
 * **Version**: 4 (accounts system implemented)
 * **Export Schema**: true (for future migration reference)
 *
 * **Migration Strategy**:
 * During development, the database uses `.fallbackToDestructiveMigration()` which
 * will drop and recreate all tables when the version changes. This is safe since
 * the app is not released yet.
 *
 * **Before Release**: Migration logic will be added to preserve user data when
 * updating from v4 to future versions.
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
 * - AccountEntity: Financial accounts (predefined + custom)
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
        TransactionTagEntity::class,
        AccountEntity::class
    ],
    version = 4,
    exportSchema = true
)
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
    abstract fun accountDao(): AccountDao

    companion object {
        const val DATABASE_NAME = "spendly_database"

        // Migration logic will be added here before app release
    }
}
