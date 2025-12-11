package `in`.mylullaby.spendly.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import `in`.mylullaby.spendly.data.local.dao.*
import `in`.mylullaby.spendly.data.local.entities.*

/**
 * Room database for Spendly expense tracker.
 *
 * This is the main database class that manages all entities and provides
 * access to DAOs for database operations.
 *
 * **Version**: 3 (added categoryId to income)
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
    version = 3,
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

        /**
         * Migration from version 1 to 2: Add type field to categories table
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add type column with default 'EXPENSE' for backwards compatibility
                db.execSQL("ALTER TABLE categories ADD COLUMN type TEXT NOT NULL DEFAULT 'EXPENSE'")

                // Create index on type column for efficient filtering
                db.execSQL("CREATE INDEX IF NOT EXISTS index_categories_type ON categories(type)")

                // Insert predefined income categories (IDs 101-110)
                db.execSQL("""
                    INSERT INTO categories (id, name, icon, color, is_custom, sort_order, type) VALUES
                    (101, 'Salary', 'briefcase', ${0xFF2E7D32.toInt()}, 0, 1, 'INCOME'),
                    (102, 'Freelance', 'laptop', ${0xFF00897B.toInt()}, 0, 2, 'INCOME'),
                    (103, 'Business', 'storefront', ${0xFF1976D2.toInt()}, 0, 3, 'INCOME'),
                    (104, 'Investments', 'trending_up', ${0xFF1DD1A1.toInt()}, 0, 4, 'INCOME'),
                    (105, 'Rental', 'home', ${0xFF7B1FA2.toInt()}, 0, 5, 'INCOME'),
                    (106, 'Interest', 'bank', ${0xFFE65100.toInt()}, 0, 6, 'INCOME'),
                    (107, 'Gifts', 'card_giftcard', ${0xFFC44569.toInt()}, 0, 7, 'INCOME'),
                    (108, 'Refund', 'receipt', ${0xFF00ACC1.toInt()}, 0, 8, 'INCOME'),
                    (109, 'Bonus', 'gift', ${0xFFF57C00.toInt()}, 0, 9, 'INCOME'),
                    (110, 'Other', 'category', ${0xFF9E9E9E.toInt()}, 0, 10, 'INCOME')
                """.trimIndent())
            }
        }

        /**
         * Migration from version 2 to 3: Add category_id field to income table
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add category_id column (nullable for backwards compatibility)
                db.execSQL("ALTER TABLE income ADD COLUMN category_id INTEGER")

                // Create index on category_id column
                db.execSQL("CREATE INDEX IF NOT EXISTS index_income_category_id ON income(category_id)")

                // Create foreign key constraint (handled by Room's @ForeignKey annotation)
                // Note: Room will regenerate the table with proper constraints on next schema change
            }
        }
    }
}
