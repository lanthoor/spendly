package dev.lanthoor.spendly.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.lanthoor.spendly.data.local.SpendlyDatabase
import dev.lanthoor.spendly.data.local.dao.AccountDao
import dev.lanthoor.spendly.data.local.dao.BudgetDao
import dev.lanthoor.spendly.data.local.dao.CategoryDao
import dev.lanthoor.spendly.data.local.dao.ExpenseDao
import dev.lanthoor.spendly.data.local.dao.IncomeDao
import dev.lanthoor.spendly.data.local.dao.ReceiptDao
import dev.lanthoor.spendly.data.local.dao.RecurringTransactionDao
import dev.lanthoor.spendly.data.local.dao.TransactionAiEnrichmentDao
import javax.inject.Singleton

/**
 * Hilt module providing Room database and DAOs.
 * All DAOs are provided as singletons through the database instance.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides the Spendly database instance.
     *
     * **Migration Strategy**:
     * - Version 1 is the baseline (established 2025-12-14)
     * - Future schema changes MUST include proper migration logic
     * - No destructive migrations - all user data is preserved
     * - See MIGRATIONS.md for detailed migration guide
     *
     * **Security Features**:
     * - Foreign key constraints explicitly enabled
     * - Write-ahead logging for better concurrency
     */
    @Provides
    @Singleton
    fun provideSpendlyDatabase(@ApplicationContext context: Context): SpendlyDatabase {
        return Room.databaseBuilder(
            context,
            SpendlyDatabase::class.java,
            SpendlyDatabase.DATABASE_NAME
        )
            // Migrations: version 1 → 2 → 3 → 4
            .addMigrations(
                SpendlyDatabase.MIGRATION_1_2,
                SpendlyDatabase.MIGRATION_2_3,
                SpendlyDatabase.MIGRATION_3_4,
                SpendlyDatabase.MIGRATION_4_5
            )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    // CRITICAL: Explicitly enable foreign key constraints
                    // This ensures referential integrity is enforced
                    db.execSQL("PRAGMA foreign_keys = ON;")
                }
            })
            .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
            .build()
    }

    /**
     * Provides ExpenseDao from the database.
     */
    @Provides
    fun provideExpenseDao(database: SpendlyDatabase): ExpenseDao =
        database.expenseDao()

    /**
     * Provides IncomeDao from the database.
     */
    @Provides
    fun provideIncomeDao(database: SpendlyDatabase): IncomeDao =
        database.incomeDao()

    /**
     * Provides CategoryDao from the database.
     */
    @Provides
    fun provideCategoryDao(database: SpendlyDatabase): CategoryDao =
        database.categoryDao()

    /**
     * Provides BudgetDao from the database.
     */
    @Provides
    fun provideBudgetDao(database: SpendlyDatabase): BudgetDao =
        database.budgetDao()

    /**
     * Provides ReceiptDao from the database.
     */
    @Provides
    fun provideReceiptDao(database: SpendlyDatabase): ReceiptDao =
        database.receiptDao()

    /**
     * Provides RecurringTransactionDao from the database.
     */
    @Provides
    fun provideRecurringTransactionDao(database: SpendlyDatabase): RecurringTransactionDao =
        database.recurringTransactionDao()

    /**
     * Provides AccountDao from the database.
     */
    @Provides
    fun provideAccountDao(database: SpendlyDatabase): AccountDao =
        database.accountDao()

    @Provides
    fun provideTransactionAiEnrichmentDao(database: SpendlyDatabase): TransactionAiEnrichmentDao =
        database.transactionAiEnrichmentDao()
}
