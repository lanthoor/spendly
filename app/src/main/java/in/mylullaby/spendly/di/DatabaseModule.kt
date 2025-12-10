package `in`.mylullaby.spendly.di

import android.content.Context
import androidx.room.Room
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
     * Uses fallbackToDestructiveMigration for development (proper migrations in task 235).
     */
    @Provides
    @Singleton
    fun provideSpendlyDatabase(@ApplicationContext context: Context): SpendlyDatabase {
        return Room.databaseBuilder(
            context,
            SpendlyDatabase::class.java,
            "spendly_database"
        )
            .fallbackToDestructiveMigration() // Temporary for development
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
