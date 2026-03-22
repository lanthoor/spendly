package `in`.co.spendly.data.repository

import `in`.co.spendly.di.ApplicationScope
import `in`.co.spendly.domain.repository.AccountRepository
import `in`.co.spendly.domain.repository.CategoryRepository
import `in`.co.spendly.domain.repository.InitializationRepository
import `in`.co.spendly.domain.repository.InitializationState
import `in`.co.spendly.domain.repository.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [InitializationRepository] that orchestrates parallel loading
 * of master data and preferences.
 *
 * Initialization tasks:
 * 1. Seed categories and accounts (sequential, but parallel with preferences)
 * 2. Load all preferences from DataStore (parallel)
 *
 * Uses coroutine async/await for parallel execution.
 */
@Singleton
class InitializationRepositoryImpl @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val preferencesRepository: PreferencesRepository,
    @ApplicationScope private val applicationScope: CoroutineScope
) : InitializationRepository {

    private val _isInitialized = MutableStateFlow(false)

    override fun initialize(): Flow<InitializationState> = flow {
        emit(InitializationState.Loading)

        try {
            // Use coroutineScope to execute tasks in parallel
            coroutineScope {
                // Task 1: Seed master data (sequential within: categories then accounts)
                val masterDataJob = async {
                    // Seed categories first
                    if (!categoryRepository.isPredefinedSeeded()) {
                        categoryRepository.seedPredefinedCategories()
                    }

                    // Seed accounts after categories (accounts may reference categories)
                    if (!accountRepository.isPredefinedSeeded()) {
                        accountRepository.seedPredefinedAccounts()
                    }
                }

                // Task 2: Load all preferences (parallel) to warm up DataStore cache
                val preferencesJob = async {
                    combine(
                        preferencesRepository.getTheme(),
                        preferencesRepository.getAppLockEnabled(),
                        preferencesRepository.getLockTimeout(),
                        preferencesRepository.getYearType(),
                        preferencesRepository.getSmsAutoDetectionEnabled()
                    ) { _, _, _, _, _ ->
                        // Just collect to warm up cache, don't need values
                    }.first() // Take first emission to complete
                }

                // Wait for both jobs to complete
                masterDataJob.await()
                preferencesJob.await()
            }

            // Mark as initialized
            _isInitialized.value = true

            // Emit success
            emit(
                InitializationState.Success(
                    masterDataSeeded = true,
                    preferencesLoaded = true
                )
            )
        } catch (e: Exception) {
            // Handle any errors during initialization
            emit(
                InitializationState.Error(
                    message = e.message ?: "Unknown initialization error occurred"
                )
            )
        }
    }.flowOn(Dispatchers.IO) // Run on IO dispatcher for disk operations

    override suspend fun isInitialized(): Boolean = _isInitialized.value
}
