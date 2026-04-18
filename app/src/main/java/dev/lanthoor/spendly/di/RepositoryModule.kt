package dev.lanthoor.spendly.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.lanthoor.spendly.data.repository.AccountRepositoryImpl
import dev.lanthoor.spendly.data.repository.BudgetRepositoryImpl
import dev.lanthoor.spendly.data.repository.CategoryRepositoryImpl
import dev.lanthoor.spendly.data.repository.ExpenseRepositoryImpl
import dev.lanthoor.spendly.data.repository.ExportImportRepositoryImpl
import dev.lanthoor.spendly.data.repository.IncomeRepositoryImpl
import dev.lanthoor.spendly.data.repository.InitializationRepositoryImpl
import dev.lanthoor.spendly.data.repository.PreferencesRepositoryImpl
import dev.lanthoor.spendly.data.repository.ReceiptRepositoryImpl
import dev.lanthoor.spendly.data.repository.RecurringTransactionRepositoryImpl
import dev.lanthoor.spendly.data.repository.TransactionAiEnrichmentRepositoryImpl
import dev.lanthoor.spendly.domain.repository.AccountRepository
import dev.lanthoor.spendly.domain.repository.BudgetRepository
import dev.lanthoor.spendly.domain.repository.CategoryRepository
import dev.lanthoor.spendly.domain.repository.ExpenseRepository
import dev.lanthoor.spendly.domain.repository.ExportImportRepository
import dev.lanthoor.spendly.domain.repository.IncomeRepository
import dev.lanthoor.spendly.domain.repository.InitializationRepository
import dev.lanthoor.spendly.domain.repository.PreferencesRepository
import dev.lanthoor.spendly.domain.repository.ReceiptRepository
import dev.lanthoor.spendly.domain.repository.RecurringTransactionRepository
import dev.lanthoor.spendly.domain.repository.TransactionAiEnrichmentRepository
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

    @Binds
    @Singleton
    abstract fun bindTransactionAiEnrichmentRepository(
        impl: TransactionAiEnrichmentRepositoryImpl
    ): TransactionAiEnrichmentRepository
}
