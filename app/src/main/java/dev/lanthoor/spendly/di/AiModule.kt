package dev.lanthoor.spendly.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.lanthoor.spendly.domain.repository.TransactionAiModelGateway
import dev.lanthoor.spendly.utils.ai.MlKitPromptTransactionAiModelGateway
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {
    @Binds
    @Singleton
    abstract fun bindTransactionAiModelGateway(
        impl: MlKitPromptTransactionAiModelGateway
    ): TransactionAiModelGateway
}
