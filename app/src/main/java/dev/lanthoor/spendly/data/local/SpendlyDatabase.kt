package dev.lanthoor.spendly.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.lanthoor.spendly.data.local.dao.AccountDao
import dev.lanthoor.spendly.data.local.dao.BudgetDao
import dev.lanthoor.spendly.data.local.dao.CategoryDao
import dev.lanthoor.spendly.data.local.dao.ExpenseDao
import dev.lanthoor.spendly.data.local.dao.IncomeDao
import dev.lanthoor.spendly.data.local.dao.ReceiptDao
import dev.lanthoor.spendly.data.local.dao.RecurringTransactionDao
import dev.lanthoor.spendly.data.local.entities.AccountEntity
import dev.lanthoor.spendly.data.local.entities.BudgetEntity
import dev.lanthoor.spendly.data.local.entities.CategoryEntity
import dev.lanthoor.spendly.data.local.entities.ExpenseEntity
import dev.lanthoor.spendly.data.local.entities.IncomeEntity
import dev.lanthoor.spendly.data.local.entities.ReceiptEntity
import dev.lanthoor.spendly.data.local.entities.RecurringTransactionEntity

/**
 * Room database for Spendly expense tracker.
 *
 * This is the main database class that manages all entities and provides
 * access to DAOs for database operations.
 *
 * **Version**: 1 (Baseline as of 2025-12-14)
 * **Export Schema**: true (for future migration reference)
 *
 * **Current Schema**:
 * - 7 entities with full CRUD operations
 * - Foreign key constraints with CASCADE/SET_NULL
 * - Composite indexes for query performance
 * - Integer-only currency (paise) for precision
 * - SMS metadata for auto-detected transactions
 * - Audit timestamps on all transaction entities
 *
 * **Migration Strategy**:
 * - Version 1 is the baseline schema (consolidated from previous alpha versions)
 * - Future schema changes MUST include proper migration logic (no destructive migrations)
 * - Schema files are exported to `app/schemas/` for migration verification
 *
 * **Adding New Migrations**:
 * 1. Increment the database version in @Database annotation
 * 2. Create a new MIGRATION_X_Y object in the companion object
 * 3. Add the migration to DatabaseModule.provideSpendlyDatabase()
 * 4. Test the migration thoroughly before release
 * 5. See MIGRATIONS.md for detailed guide and examples
 *
 * **Entities**:
 * - CategoryEntity: Expense categories (predefined + custom)
 * - ExpenseEntity: Expense transactions (with SMS metadata)
 * - ReceiptEntity: Attached receipt files
 * - IncomeEntity: Income transactions (with SMS metadata)
 * - BudgetEntity: Monthly budgets per category
 * - RecurringTransactionEntity: Recurring transaction configurations
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
        AccountEntity::class
    ],
    version = 3,
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
    abstract fun accountDao(): AccountDao

    companion object {
        const val DATABASE_NAME = "spendly_database"

        /**
         * Migration 1 → 2: Unified Category System
         *
         * Changes:
         * - Removes `type` column from categories table
         * - Consolidates duplicate categories (Others, Investments, Gifts, Rent)
         * - Changes unique index from (name, type) to just (name)
         * - Updates all foreign key references in expenses, incomes, and budgets
         *
         * Data Migration:
         * - Maps duplicate category IDs to unified IDs
         * - Deletes 4 obsolete category rows (110, 104, 107, 105)
         * - Preserves all transaction data with updated category references
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Check if this is actually an upgrade from version 1
                // If tables don't exist, this migration shouldn't run
                val cursor =
                    database.query("SELECT name FROM sqlite_master WHERE type='table' AND name='categories'")
                val categoriesExists = cursor.moveToFirst()
                cursor.close()

                if (!categoriesExists) {
                    // Tables don't exist yet, this is a fresh install
                    // Room will create version 2 schema directly, no migration needed
                    return
                }

                // Step 1: Create temporary mapping table
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS category_migration (
                        old_id INTEGER PRIMARY KEY,
                        new_id INTEGER NOT NULL
                    )
                """
                )

                // Step 2: Define ID mappings for duplicate categories
                // Keep lower IDs, map higher IDs to lower ones
                val mappings = mapOf(
                    110L to 13L,  // Others income -> Others expense
                    104L to 11L,  // Investment Income -> Investments
                    107L to 9L,   // Gift Received -> Gifts
                    105L to 3L    // Rental -> Rent
                )

                mappings.forEach { (oldId, newId) ->
                    database.execSQL(
                        "INSERT INTO category_migration (old_id, new_id) VALUES (?, ?)",
                        arrayOf(oldId, newId)
                    )
                }

                // Step 3: Update foreign key references in expenses (if table exists)
                database.execSQL(
                    """
                    UPDATE expenses
                    SET category_id = (
                        SELECT new_id FROM category_migration WHERE old_id = category_id
                    )
                    WHERE category_id IN (SELECT old_id FROM category_migration)
                """
                )

                // Step 4: Update foreign key references in incomes (if table exists)
                database.execSQL(
                    """
                    UPDATE incomes
                    SET category_id = (
                        SELECT new_id FROM category_migration WHERE old_id = category_id
                    )
                    WHERE category_id IN (SELECT old_id FROM category_migration)
                """
                )

                // Step 5: Update foreign key references in budgets (if table exists)
                database.execSQL(
                    """
                    UPDATE budgets
                    SET category_id = (
                        SELECT new_id FROM category_migration WHERE old_id = category_id
                    )
                    WHERE category_id IN (SELECT old_id FROM category_migration)
                """
                )

                // Step 6: Update recurring_transactions table (if table exists)
                database.execSQL(
                    """
                    UPDATE recurring_transactions
                    SET category_id = (
                        SELECT new_id FROM category_migration WHERE old_id = category_id
                    )
                    WHERE category_id IN (SELECT old_id FROM category_migration)
                """
                )

                // Step 7: Delete obsolete category rows
                database.execSQL("DELETE FROM categories WHERE id IN (110, 104, 107, 105)")

                // Step 8: Drop old unique index on (name, type)
                database.execSQL("DROP INDEX IF EXISTS index_categories_name_type")

                // Step 9: Remove type column using table recreation
                database.execSQL(
                    """
                    CREATE TABLE categories_new (
                        id INTEGER PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        icon TEXT NOT NULL,
                        color INTEGER NOT NULL,
                        is_custom INTEGER NOT NULL,
                        sort_order INTEGER NOT NULL
                    )
                """
                )

                database.execSQL(
                    """
                    INSERT INTO categories_new (id, name, icon, color, is_custom, sort_order)
                    SELECT id, name, icon, color, is_custom, sort_order FROM categories
                """
                )

                database.execSQL("DROP TABLE categories")
                database.execSQL("ALTER TABLE categories_new RENAME TO categories")

                // Step 10: Create new unique index on just name
                database.execSQL("CREATE UNIQUE INDEX index_categories_name ON categories(name)")

                // Step 11: Create index for performance
                database.execSQL("CREATE INDEX index_categories_sort_order ON categories(sort_order)")

                // Step 12: Update category names for better semantics
                database.execSQL("UPDATE categories SET name = 'Investments' WHERE id = 11")
                database.execSQL("UPDATE categories SET name = 'Gifts' WHERE id = 9")
                database.execSQL("UPDATE categories SET name = 'Rent' WHERE id = 3")

                // Step 13: Drop migration table
                database.execSQL("DROP TABLE category_migration")
            }
        }

        /**
         * Migration 2 → 3: Fix SMS Transaction Timestamps
         *
         * Background:
         * Prior to this migration, SMS-detected transactions had their `date` field set to
         * midnight (00:00:00) when the SMS body contained a date string (e.g., "19-Dec-24").
         * The parser extracted the date but hardcoded the time to 00:00:00.
         *
         * Changes:
         * - Updates all SMS-linked expense transactions to use `sms_timestamp` as `date`
         * - Updates all SMS-linked income transactions to use `sms_timestamp` as `date`
         * - Updates `modified_at` timestamp to track this correction
         *
         * Data Migration:
         * - Only affects transactions where `sms_timestamp IS NOT NULL` (SMS-detected)
         * - Manually created transactions are unaffected
         * - Preserves all other transaction data
         *
         * Impact:
         * - SMS-detected transactions will now show actual SMS receipt time
         * - Fixes the bug where expense SMS showed midnight while income SMS showed correct time
         * - Makes timestamp behavior consistent across all SMS-detected transactions
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val currentTime = System.currentTimeMillis()

                // Update expenses: set date to sms_timestamp for all SMS-linked transactions
                database.execSQL(
                    """
                    UPDATE expenses
                    SET date = sms_timestamp,
                        modified_at = ?
                    WHERE sms_timestamp IS NOT NULL
                """, arrayOf(currentTime)
                )

                // Update income: set date to sms_timestamp for all SMS-linked transactions
                database.execSQL(
                    """
                    UPDATE income
                    SET date = sms_timestamp,
                        modified_at = ?
                    WHERE sms_timestamp IS NOT NULL
                """, arrayOf(currentTime)
                )
            }
        }
    }
}
