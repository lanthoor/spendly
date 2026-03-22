package dev.lanthoor.spendly.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import dev.lanthoor.spendly.utils.YearType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.seconds

/**
 * Instrumented tests for YearType preference storage.
 *
 * Tests DataStore persistence and retrieval of year type preference.
 * Pattern: methodName_inputCondition_expectedResult
 */
@RunWith(AndroidJUnit4::class)
class PreferencesRepositoryYearTypeTest {

    private lateinit var context: Context
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var testScope: CoroutineScope
    private lateinit var repository: PreferencesRepositoryImpl

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        testScope = CoroutineScope(Dispatchers.Unconfined + SupervisorJob())

        // Create a test DataStore with a unique name for each test
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { context.preferencesDataStoreFile("test_preferences_${System.currentTimeMillis()}") }
        )

        repository = PreferencesRepositoryImpl(dataStore)
    }

    @After
    fun tearDown() {
        testScope.cancel()
    }

    @Test
    fun getYearType_defaultsToCalendar() = runTest(timeout = 10.seconds) {
        // Act & Assert
        repository.getYearType().test {
            assertEquals(YearType.CALENDAR, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setYearType_persistsValue() = runTest(timeout = 10.seconds) {
        // Arrange & Act
        repository.setYearType(YearType.FINANCIAL)

        // Assert
        repository.getYearType().test {
            assertEquals(YearType.FINANCIAL, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setYearType_toCalendar_updatesFlow() = runTest(timeout = 10.seconds) {
        // Arrange - Set to FINANCIAL first
        repository.setYearType(YearType.FINANCIAL)

        // Act & Assert
        repository.getYearType().test {
            assertEquals(YearType.FINANCIAL, awaitItem())

            // Update to CALENDAR
            repository.setYearType(YearType.CALENDAR)

            // Verify Flow emits update
            assertEquals(YearType.CALENDAR, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setYearType_multipleUpdates_persistsLatest() = runTest(timeout = 10.seconds) {
        // Arrange & Act
        repository.setYearType(YearType.FINANCIAL)
        repository.setYearType(YearType.CALENDAR)
        repository.setYearType(YearType.FINANCIAL)

        // Assert - Should have latest value
        repository.getYearType().test {
            assertEquals(YearType.FINANCIAL, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
