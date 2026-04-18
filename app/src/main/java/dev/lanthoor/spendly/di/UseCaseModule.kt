package dev.lanthoor.spendly.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.lanthoor.spendly.domain.usecase.analytics.BuildAnalyticsStateUseCase
import dev.lanthoor.spendly.domain.usecase.budgets.BuildBudgetListStateUseCase
import dev.lanthoor.spendly.domain.usecase.dashboard.BuildDashboardSummaryUseCase

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun provideBuildBudgetListStateUseCase(): BuildBudgetListStateUseCase {
        return BuildBudgetListStateUseCase()
    }

    @Provides
    fun provideBuildAnalyticsStateUseCase(): BuildAnalyticsStateUseCase {
        return BuildAnalyticsStateUseCase()
    }

    @Provides
    fun provideBuildDashboardSummaryUseCase(): BuildDashboardSummaryUseCase {
        return BuildDashboardSummaryUseCase()
    }
}
