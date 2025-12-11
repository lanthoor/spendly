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
     * **Development Mode**: Uses destructive migration strategy.
     * When database version changes, all data is dropped and tables are recreated.
     * This is safe since the app is not released yet.
     *
     * **Security Features**:
     * - Foreign key constraints explicitly enabled
     * - Write-ahead logging for better concurrency
     *
     * **Before Release**: Migration logic will be added to preserve user data.
     */
    @Provides
    @Singleton
    fun provideSpendlyDatabase(@ApplicationContext context: Context): SpendlyDatabase {
        return Room.databaseBuilder(
            context,
            SpendlyDatabase::class.java,
            SpendlyDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // Development mode - drops all data on schema change
            .addCallback(object : RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    // CRITICAL: Explicitly enable foreign key constraints
                    // This ensures referential integrity is enforced
                    db.execSQL("PRAGMA foreign_keys = ON;")
                }

                override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                    // Log destructive migration for development awareness
                    Log.w("DatabaseModule", "Database schema changed - all data cleared (development mode)")
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

    /**
     * Provides AccountDao from the database.
     */
    @Provides
    fun provideAccountDao(database: SpendlyDatabase): AccountDao =
        database.accountDao()
}
