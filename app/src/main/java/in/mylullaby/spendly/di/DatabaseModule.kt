package `in`.mylullaby.spendly.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.mylullaby.spendly.data.local.SpendlyDatabase
import `in`.mylullaby.spendly.data.local.dao.*
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
     * Security features:
     * - Foreign key constraints explicitly enabled
     * - Write-ahead logging for better concurrency
     * - No destructive migration fallback (prevents accidental data loss)
     *
     * Migrations:
     * - 1→2: Add category type field
     * - 2→3: Add categoryId to income
     */
    @Provides
    @Singleton
    fun provideSpendlyDatabase(@ApplicationContext context: Context): SpendlyDatabase {
        return Room.databaseBuilder(
            context,
            SpendlyDatabase::class.java,
            "spendly_database"
        )
            .addMigrations(
                SpendlyDatabase.MIGRATION_1_2,
                SpendlyDatabase.MIGRATION_2_3
            )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    // CRITICAL: Explicitly enable foreign key constraints
                    // This ensures referential integrity is enforced
                    db.execSQL("PRAGMA foreign_keys = ON;")
                }

                override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                    // This should NEVER happen in production
                    // Prevents accidental data loss from missing migration paths
                    Log.e("DatabaseModule", "CRITICAL: Destructive migration attempted!")
                    throw IllegalStateException("Migration path missing - data loss prevented")
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
     * Provides TagDao from the database.
     */
    @Provides
    fun provideTagDao(database: SpendlyDatabase): TagDao =
        database.tagDao()

    /**
     * Provides TransactionTagDao from the database.
     */
    @Provides
    fun provideTransactionTagDao(database: SpendlyDatabase): TransactionTagDao =
        database.transactionTagDao()
}
