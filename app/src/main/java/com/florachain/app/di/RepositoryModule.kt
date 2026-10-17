package com.florachain.app.di

import com.florachain.core.security.EncryptedTokenManager
import com.florachain.data.local.dao.ProductDao
import com.florachain.data.local.dao.SyncQueueDao
import com.florachain.data.remote.FloraChainApi
import com.florachain.data.repository.AuthRepositoryImpl
import com.florachain.data.repository.ProductRepositoryImpl
import com.florachain.data.repository.SyncRepositoryImpl
import com.florachain.domain.repository.AuthRepository
import com.florachain.domain.repository.ProductRepository
import com.florachain.domain.repository.SyncRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideProductRepository(
        api: FloraChainApi,
        productDao: ProductDao,
        syncQueueDao: SyncQueueDao
    ): ProductRepository {
        return ProductRepositoryImpl(api, productDao, syncQueueDao)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        api: FloraChainApi,
        tokenManager: EncryptedTokenManager
    ): AuthRepository {
        return AuthRepositoryImpl(api, tokenManager)
    }

    @Provides
    @Singleton
    fun provideSyncRepository(
        syncQueueDao: SyncQueueDao,
        productDao: ProductDao,
        api: FloraChainApi
    ): SyncRepository {
        return SyncRepositoryImpl(syncQueueDao, productDao, api)
    }
}
