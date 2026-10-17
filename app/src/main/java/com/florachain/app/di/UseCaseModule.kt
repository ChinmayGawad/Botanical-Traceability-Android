package com.florachain.app.di

import com.florachain.domain.repository.AuthRepository
import com.florachain.domain.repository.ProductRepository
import com.florachain.domain.repository.SyncRepository
import com.florachain.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideRegisterHarvestUseCase(productRepository: ProductRepository): RegisterHarvestUseCase {
        return RegisterHarvestUseCase(productRepository)
    }

    @Provides
    @Singleton
    fun provideVerifyProductUseCase(productRepository: ProductRepository): VerifyProductUseCase {
        return VerifyProductUseCase(productRepository)
    }

    @Provides
    @Singleton
    fun provideSubmitProcessingUseCase(productRepository: ProductRepository): SubmitProcessingUseCase {
        return SubmitProcessingUseCase(productRepository)
    }

    @Provides
    @Singleton
    fun provideSubmitLabReportUseCase(productRepository: ProductRepository): SubmitLabReportUseCase {
        return SubmitLabReportUseCase(productRepository)
    }

    @Provides
    @Singleton
    fun provideCreateShipmentUseCase(productRepository: ProductRepository): CreateShipmentUseCase {
        return CreateShipmentUseCase(productRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateShipmentStatusUseCase(productRepository: ProductRepository): UpdateShipmentStatusUseCase {
        return UpdateShipmentStatusUseCase(productRepository)
    }

    @Provides
    @Singleton
    fun provideReceiveRetailStockUseCase(productRepository: ProductRepository): ReceiveRetailStockUseCase {
        return ReceiveRetailStockUseCase(productRepository)
    }

    @Provides
    @Singleton
    fun provideLoginUseCase(authRepository: AuthRepository): LoginUseCase {
        return LoginUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideValidateAadhaarUseCase(): ValidateAadhaarUseCase {
        return ValidateAadhaarUseCase()
    }

    @Provides
    @Singleton
    fun provideSyncPendingActionsUseCase(syncRepository: SyncRepository): SyncPendingActionsUseCase {
        return SyncPendingActionsUseCase(syncRepository)
    }
}
