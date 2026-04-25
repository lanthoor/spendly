package dev.lanthoor.spendly.domain.usecase.transactions

import dev.lanthoor.spendly.core.model.finance.AiEnrichmentStatus
import dev.lanthoor.spendly.core.model.finance.TransactionType
import dev.lanthoor.spendly.core.model.preferences.AiEnrichmentSettings
import dev.lanthoor.spendly.core.model.preferences.AiModelAvailability
import dev.lanthoor.spendly.domain.model.Category
import dev.lanthoor.spendly.domain.model.Expense
import dev.lanthoor.spendly.domain.model.Income
import dev.lanthoor.spendly.domain.model.TransactionAiEnrichment
import dev.lanthoor.spendly.domain.model.TransactionAiEnrichmentUpdate
import dev.lanthoor.spendly.domain.model.ai.AiGenerationResult
import dev.lanthoor.spendly.domain.model.ai.AiModelAvailabilityResult
import dev.lanthoor.spendly.domain.repository.CategoryRepository
import dev.lanthoor.spendly.domain.repository.ExpenseRepository
import dev.lanthoor.spendly.domain.repository.IncomeRepository
import dev.lanthoor.spendly.domain.repository.PreferencesRepository
import dev.lanthoor.spendly.domain.repository.TransactionAiModelGateway
import dev.lanthoor.spendly.domain.repository.TransactionAiEnrichmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EnrichSmsTransactionsUseCaseTest {
    @Test
    fun `runForTransactionIds skips when model unavailable`() = runBlocking {
        val prefs = FakePreferencesRepository(
            availability = AiModelAvailability.UNKNOWN,
            enabled = true,
            batchSize = 10,
            promptVersion = 1
        )
        val gateway = FakeAiGateway(
            availability = AiModelAvailability.UNAVAILABLE,
            responseText = ""
        )
        val useCase = EnrichSmsTransactionsUseCase(
            categoryRepository = FakeCategoryRepository(
                listOf(
                    category(13L, "Others"),
                    category(12L, "Groceries")
                )
            ),
            expenseRepository = FakeExpenseRepository(
                listOf(expense(1L, "sms"))
            ),
            incomeRepository = FakeIncomeRepository(emptyList()),
            preferencesRepository = prefs,
            enrichmentRepository = FakeEnrichmentRepository(),
            aiGateway = gateway
        )

        val result = useCase.runForTransactionIds(expenseIds = listOf(1L), incomeIds = emptyList())

        assertEquals(0, result.attempted)
        assertEquals(1, result.skipped)
    }

    @Test
    fun `runForTransactionIds enriches using batch response`() = runBlocking {
        val prefs = FakePreferencesRepository(
            availability = AiModelAvailability.AVAILABLE,
            enabled = true,
            batchSize = 10,
            promptVersion = 1
        )
        val gateway = FakeAiGateway(
            availability = AiModelAvailability.AVAILABLE,
            responseText = """
                {
                  "schema_version": 1,
                  "results": [
                    {
                      "tx_key": "EXPENSE:1",
                      "status": "ENRICHED",
                      "display_description": "Paid to Test Merchant",
                      "counterparty_name": "Test Merchant",
                      "counterparty_role": "MERCHANT",
                      "counterparty_type": "BUSINESS",
                      "identifier_type": "NONE",
                      "identifier_value": null,
                      "payment_rail": "UPI",
                      "category_name": "Groceries",
                      "confidence": 0.9,
                      "reason": "matched"
                    }
                  ]
                }
            """.trimIndent()
        )
        val enrichmentRepo = FakeEnrichmentRepository()
        val useCase = EnrichSmsTransactionsUseCase(
            categoryRepository = FakeCategoryRepository(
                listOf(
                    category(13L, "Others"),
                    category(12L, "Groceries")
                )
            ),
            expenseRepository = FakeExpenseRepository(
                listOf(expense(1L, "sms body"))
            ),
            incomeRepository = FakeIncomeRepository(emptyList()),
            preferencesRepository = prefs,
            enrichmentRepository = enrichmentRepo,
            aiGateway = gateway
        )

        val result = useCase.runForTransactionIds(expenseIds = listOf(1L), incomeIds = emptyList())

        assertEquals(1, result.attempted)
        assertEquals(1, result.enriched)
        assertTrue(enrichmentRepo.lastApplied.any { it.transactionId == 1L })
        assertEquals(12L, enrichmentRepo.lastApplied.first().categoryId)
    }

    @Test
    fun `runForTransactionIds marks failed when response invalid`() = runBlocking {
        val prefs = FakePreferencesRepository(
            availability = AiModelAvailability.AVAILABLE,
            enabled = true,
            batchSize = 10,
            promptVersion = 1
        )
        val gateway = FakeAiGateway(
            availability = AiModelAvailability.AVAILABLE,
            responseText = "not a json"
        )
        val enrichmentRepo = FakeEnrichmentRepository()
        val useCase = EnrichSmsTransactionsUseCase(
            categoryRepository = FakeCategoryRepository(
                listOf(
                    category(13L, "Others"),
                    category(12L, "Groceries")
                )
            ),
            expenseRepository = FakeExpenseRepository(
                listOf(expense(1L, "sms body"))
            ),
            incomeRepository = FakeIncomeRepository(emptyList()),
            preferencesRepository = prefs,
            enrichmentRepository = enrichmentRepo,
            aiGateway = gateway
        )

        val result = useCase.runForTransactionIds(expenseIds = listOf(1L), incomeIds = emptyList())

        assertEquals(1, result.attempted)
        assertEquals(1, result.failed)
        assertEquals(AiEnrichmentStatus.FAILED, enrichmentRepo.lastApplied.first().status)
    }

    private fun category(id: Long, name: String): Category {
        return Category(
            id = id,
            name = name,
            icon = "category",
            color = 0,
            isCustom = false,
            sortOrder = id.toInt()
        )
    }

    private fun expense(id: Long, body: String): Expense {
        val now = System.currentTimeMillis()
        return Expense(
            id = id,
            amount = 1000L,
            categoryId = null,
            date = now,
            description = "regex",
            accountId = 1L,
            createdAt = now,
            modifiedAt = now,
            smsBody = body,
            smsTimestamp = now
        )
    }

    private class FakeAiGateway(
        private val availability: AiModelAvailability,
        private val responseText: String
    ) : TransactionAiModelGateway {
        override suspend fun checkAvailability(): AiModelAvailabilityResult {
            return AiModelAvailabilityResult(availability, "nano", null)
        }

        override suspend fun generate(prompt: String): AiGenerationResult {
            return AiGenerationResult(responseText = responseText, modelName = "nano")
        }
    }

    private class FakeExpenseRepository(
        private val expenses: List<Expense>
    ) : ExpenseRepository {
        private val store = expenses.associateBy { it.id }.toMutableMap()

        override suspend fun insertExpense(expense: Expense): Long = 0L
        override suspend fun updateExpense(expense: Expense) {
            store[expense.id] = expense
        }
        override suspend fun deleteExpense(expense: Expense) = Unit
        override fun getExpenseById(id: Long): Flow<Expense?> = flowOf(null)
        override fun getAllExpenses(): Flow<List<Expense>> = flowOf(store.values.toList())
        override fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>> = flowOf(emptyList())
        override fun getExpensesByCategory(categoryId: Long): Flow<List<Expense>> = flowOf(emptyList())
        override fun getExpensesByAccount(accountId: Long): Flow<List<Expense>> = flowOf(emptyList())
        override fun getTotalSpentInRange(startDate: Long, endDate: Long): Flow<Long> = flowOf(0L)
        override fun getTotalSpentByCategory(categoryId: Long, startDate: Long, endDate: Long): Flow<Long> = flowOf(0L)
        override fun getCategorySpendingBreakdown(startDate: Long, endDate: Long): Flow<Map<Long, Long>> = flowOf(emptyMap())
        override fun getRecentExpenses(limit: Int): Flow<List<Expense>> = flowOf(emptyList())
        override suspend fun getSmsLinkedExpensesSince(minSmsTimestamp: Long): List<Expense> = store.values.toList()
        override suspend fun getExpensesByIds(ids: List<Long>): List<Expense> = ids.mapNotNull { store[it] }
    }

    private class FakeIncomeRepository(
        private val incomes: List<Income>
    ) : IncomeRepository {
        private val store = incomes.associateBy { it.id }.toMutableMap()

        override suspend fun insertIncome(income: Income): Long = 0L
        override suspend fun updateIncome(income: Income) {
            store[income.id] = income
        }
        override suspend fun deleteIncome(income: Income) = Unit
        override fun getIncomeById(id: Long): Flow<Income?> = flowOf(null)
        override fun getAllIncome(): Flow<List<Income>> = flowOf(store.values.toList())
        override fun getIncomeByDateRange(startDate: Long, endDate: Long): Flow<List<Income>> = flowOf(emptyList())
        override fun getIncomeBySource(source: dev.lanthoor.spendly.core.model.finance.IncomeSource): Flow<List<Income>> = flowOf(emptyList())
        override fun getIncomeByAccount(accountId: Long): Flow<List<Income>> = flowOf(emptyList())
        override fun getRefunds(): Flow<List<Income>> = flowOf(emptyList())
        override fun getRecurringIncome(): Flow<List<Income>> = flowOf(emptyList())
        override fun getTotalIncomeInRange(startDate: Long, endDate: Long): Flow<Long> = flowOf(0L)
        override fun getTotalIncomeBySource(source: dev.lanthoor.spendly.core.model.finance.IncomeSource, startDate: Long, endDate: Long): Flow<Long> = flowOf(0L)
        override fun getRecentIncome(limit: Int): Flow<List<Income>> = flowOf(emptyList())
        override suspend fun getSmsLinkedIncomeSince(minSmsTimestamp: Long): List<Income> = store.values.toList()
        override suspend fun getIncomeByIds(ids: List<Long>): List<Income> = ids.mapNotNull { store[it] }
    }

    private class FakeCategoryRepository(
        private val categories: List<Category>
    ) : CategoryRepository {
        override suspend fun insertCategory(category: Category): Long = 0L
        override suspend fun updateCategory(category: Category) = Unit
        override suspend fun deleteCategory(categoryId: Long, replacementCategoryId: Long) = Unit
        override fun getCategoryById(id: Long): Flow<Category?> = flowOf(categories.firstOrNull { it.id == id })
        override fun getAllCategories(): Flow<List<Category>> = flowOf(categories)
        override fun getPredefinedCategories(): Flow<List<Category>> = flowOf(categories)
        override fun getCustomCategories(): Flow<List<Category>> = flowOf(emptyList())
        override suspend fun seedPredefinedCategories() = Unit
        override suspend fun isPredefinedSeeded(): Boolean = true
        override suspend fun isCategoryNameUnique(name: String): Boolean = true
    }

    private class FakePreferencesRepository(
        availability: AiModelAvailability,
        enabled: Boolean,
        batchSize: Int,
        promptVersion: Int
    ) : PreferencesRepository {
        private val aiSettings = MutableStateFlow(
            AiEnrichmentSettings(
                enabled = enabled,
                availability = availability,
                baseModelName = null,
                lastAvailabilityCheckAt = null,
                lastErrorCode = null,
                promptVersion = promptVersion,
                batchSize = batchSize
            )
        )

        override fun getTheme() = flowOf(dev.lanthoor.spendly.core.model.preferences.AppTheme.SYSTEM)
        override suspend fun setTheme(theme: dev.lanthoor.spendly.core.model.preferences.AppTheme) = Unit
        override fun getLanguage() = flowOf(dev.lanthoor.spendly.core.model.preferences.AppLanguage.ENGLISH)
        override suspend fun setLanguage(language: dev.lanthoor.spendly.core.model.preferences.AppLanguage) = Unit
        override fun getSmsAutoDetectionEnabled() = flowOf(false)
        override suspend fun setSmsAutoDetectionEnabled(enabled: Boolean) = Unit
        override fun getYearType() = flowOf(dev.lanthoor.spendly.core.model.preferences.YearType.CALENDAR)
        override suspend fun setYearType(yearType: dev.lanthoor.spendly.core.model.preferences.YearType) = Unit
        override fun getAppLockEnabled() = flowOf(false)
        override suspend fun setAppLockEnabled(enabled: Boolean) = Unit
        override fun getLockTimeout() = flowOf(dev.lanthoor.spendly.core.model.preferences.LockTimeout.IMMEDIATELY)
        override suspend fun setLockTimeout(timeout: dev.lanthoor.spendly.core.model.preferences.LockTimeout) = Unit
        override fun getAnalyticsTimePeriod() = flowOf(dev.lanthoor.spendly.core.model.preferences.TimePeriod.ThisMonth)
        override suspend fun setAnalyticsTimePeriod(period: dev.lanthoor.spendly.core.model.preferences.TimePeriod) = Unit
        override fun getAiEnrichmentSettings(): Flow<AiEnrichmentSettings> = aiSettings
        override suspend fun setAiEnrichmentEnabled(enabled: Boolean) {
            aiSettings.value = aiSettings.value.copy(enabled = enabled)
        }
        override suspend fun setAiModelAvailability(
            availability: AiModelAvailability,
            checkedAt: Long,
            baseModelName: String?,
            lastErrorCode: String?
        ) {
            aiSettings.value = aiSettings.value.copy(
                availability = availability,
                baseModelName = baseModelName,
                lastAvailabilityCheckAt = checkedAt,
                lastErrorCode = lastErrorCode
            )
        }
        override suspend fun setAiEnrichmentBatchSize(batchSize: Int) {
            aiSettings.value = aiSettings.value.copy(batchSize = batchSize)
        }
        override suspend fun setAiPromptVersion(promptVersion: Int) {
            aiSettings.value = aiSettings.value.copy(promptVersion = promptVersion)
        }
    }

    private class FakeEnrichmentRepository : TransactionAiEnrichmentRepository {
        val lastApplied = mutableListOf<TransactionAiEnrichmentUpdate>()
        private val state = MutableStateFlow<List<TransactionAiEnrichment>>(emptyList())

        override suspend fun upsert(entry: TransactionAiEnrichment) = Unit
        override suspend fun upsertPending(transactionType: TransactionType, transactionId: Long, promptVersion: Int) = Unit
        override suspend fun upsertAll(entries: List<TransactionAiEnrichment>) = Unit
        override suspend fun applyUpdates(updates: List<TransactionAiEnrichmentUpdate>) {
            lastApplied.clear()
            lastApplied.addAll(updates)
        }
        override suspend fun getByTransaction(transactionType: TransactionType, transactionId: Long): TransactionAiEnrichment? = null
        override fun observeByTransaction(transactionType: TransactionType, transactionId: Long): Flow<TransactionAiEnrichment?> = flowOf(null)
        override suspend fun getByStatuses(statuses: Set<AiEnrichmentStatus>, limit: Int): List<TransactionAiEnrichment> = emptyList()
        override suspend fun getByStatusesForMixedTransactions(
            statuses: Set<AiEnrichmentStatus>,
            expenseIds: List<Long>,
            incomeIds: List<Long>,
            limit: Int
        ): List<TransactionAiEnrichment> = emptyList()
        override fun observeAll(): Flow<List<TransactionAiEnrichment>> = state
    }
}
