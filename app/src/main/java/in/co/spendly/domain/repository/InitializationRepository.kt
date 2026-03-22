package `in`.co.spendly.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing app initialization tasks.
 * Orchestrates loading of master data and preferences before the UI is shown.
 */
interface InitializationRepository {
    /**
     * Performs all app initialization tasks in parallel.
     * Emits initialization state as tasks progress.
     *
     * Tasks include:
     * - Seeding predefined categories and accounts
     * - Loading all preferences from DataStore
     *
     * @return Flow emitting initialization state (Loading → Success/Error)
     */
    fun initialize(): Flow<InitializationState>

    /**
     * Checks if the app has been initialized in the current session.
     * Used for optimization to avoid redundant initialization.
     *
     * @return true if initialization completed successfully, false otherwise
     */
    suspend fun isInitialized(): Boolean
}

/**
 * Sealed interface representing the state of app initialization.
 */
sealed interface InitializationState {
    /**
     * Initialization in progress.
     */
    data object Loading : InitializationState

    /**
     * Initialization completed successfully.
     *
     * @param masterDataSeeded Whether categories and accounts were seeded
     * @param preferencesLoaded Whether all preferences were loaded from DataStore
     */
    data class Success(
        val masterDataSeeded: Boolean,
        val preferencesLoaded: Boolean
    ) : InitializationState

    /**
     * Initialization failed with an error.
     *
     * @param message Error message describing what went wrong
     */
    data class Error(val message: String) : InitializationState
}
