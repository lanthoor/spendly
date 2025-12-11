package `in`.mylullaby.spendly.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.mylullaby.spendly.data.repository.*
import `in`.mylullaby.spendly.domain.repository.*
import javax.inject.Singleton

/**
 * Hilt module for repository bindings.
 * Binds repository implementations to their interfaces.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds ExpenseRepositoryImpl to ExpenseRepository interface.
     */
    @Binds
    @Singleton
    abstract fun bindExpenseRepository(
        impl: ExpenseRepositoryImpl
    ): ExpenseRepository

    /**
     * Binds IncomeRepositoryImpl to IncomeRepository interface.
     */
    @Binds
    @Singleton
    abstract fun bindIncomeRepository(
        impl: IncomeRepositoryImpl
    ): IncomeRepository

    /**
     * Binds CategoryRepositoryImpl to CategoryRepository interface.
     */
    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository

    /**
     * Binds BudgetRepositoryImpl to BudgetRepository interface.
     */
    @Binds
    @Singleton
    abstract fun bindBudgetRepository(
        impl: BudgetRepositoryImpl
    ): BudgetRepository

    /**
     * Binds TagRepositoryImpl to TagRepository interface.
     */
    @Binds
    @Singleton
    abstract fun bindTagRepository(
        impl: TagRepositoryImpl
    ): TagRepository

    /**
     * Binds ReceiptRepositoryImpl to ReceiptRepository interface.
     */
    @Binds
    @Singleton
    abstract fun bindReceiptRepository(
        impl: ReceiptRepositoryImpl
    ): ReceiptRepository

    /**
     * Binds RecurringTransactionRepositoryImpl to RecurringTransactionRepository interface.
     */
    @Binds
    @Singleton
    abstract fun bindRecurringTransactionRepository(
        impl: RecurringTransactionRepositoryImpl
    ): RecurringTransactionRepository
}
