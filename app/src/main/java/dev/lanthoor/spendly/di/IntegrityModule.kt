package dev.lanthoor.spendly.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.lanthoor.spendly.data.repository.PlayIntegrityRepositoryImpl
import dev.lanthoor.spendly.domain.repository.PlayIntegrityRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class IntegrityModule {

    @Binds
    @Singleton
    abstract fun bindPlayIntegrityRepository(
        impl: PlayIntegrityRepositoryImpl,
    ): PlayIntegrityRepository
}
