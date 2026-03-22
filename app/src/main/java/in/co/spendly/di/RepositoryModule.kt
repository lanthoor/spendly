package `in`.co.spendly.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.co.spendly.data.repository.AccountRepositoryImpl
import `in`.co.spendly.data.repository.BudgetRepositoryImpl
import `in`.co.spendly.data.repository.CategoryRepositoryImpl
import `in`.co.spendly.data.repository.ExpenseRepositoryImpl
import `in`.co.spendly.data.repository.ExportImportRepositoryImpl
import `in`.co.spendly.data.repository.IncomeRepositoryImpl
import `in`.co.spendly.data.repository.InitializationRepositoryImpl
import `in`.co.spendly.data.repository.PreferencesRepositoryImpl
import `in`.co.spendly.data.repository.ReceiptRepositoryImpl
import `in`.co.spendly.data.repository.RecurringTransactionRepositoryImpl
import `in`.co.spendly.domain.repository.AccountRepository
import `in`.co.spendly.domain.repository.BudgetRepository
import `in`.co.spendly.domain.repository.CategoryRepository
import `in`.co.spendly.domain.repository.ExpenseRepository
import `in`.co.spendly.domain.repository.ExportImportRepository
import `in`.co.spendly.domain.repository.IncomeRepository
import `in`.co.spendly.domain.repository.InitializationRepository
import `in`.co.spendly.domain.repository.PreferencesRepository
import `in`.co.spendly.domain.repository.ReceiptRepository
import `in`.co.spendly.domain.repository.RecurringTransactionRepository
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

    /**
     * Binds AccountRepositoryImpl to AccountRepository interface.
     */
    @Binds
    @Singleton
    abstract fun bindAccountRepository(
        impl: AccountRepositoryImpl
    ): AccountRepository

    /**
     * Binds PreferencesRepositoryImpl to PreferencesRepository interface.
     */
    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(
        impl: PreferencesRepositoryImpl
    ): PreferencesRepository

    /**
     * Binds InitializationRepositoryImpl to InitializationRepository interface.
     */
    @Binds
    @Singleton
    abstract fun bindInitializationRepository(
        impl: InitializationRepositoryImpl
    ): InitializationRepository

    /**
     * Binds ExportImportRepositoryImpl to ExportImportRepository interface.
     */
    @Binds
    @Singleton
    abstract fun bindExportImportRepository(
        impl: ExportImportRepositoryImpl
    ): ExportImportRepository
}
