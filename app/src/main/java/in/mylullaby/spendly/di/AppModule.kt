package `in`.mylullaby.spendly.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Hilt module providing app-level dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides application context.
     */
    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context {
        return context
    }

    /**
     * Provides app-level CoroutineScope for background operations.
     * Uses SupervisorJob to prevent child coroutine failures from canceling the scope.
     */
    @Provides
    @Singleton
    @ApplicationScope
    fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}

/**
 * Qualifier annotation for the application-level CoroutineScope.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
