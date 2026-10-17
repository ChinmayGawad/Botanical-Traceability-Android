package com.florachain.app.di

import android.content.Context
import com.florachain.data.local.FloraChainDatabase
import com.florachain.data.local.dao.ProductDao
import com.florachain.data.local.dao.SyncQueueDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideFloraChainDatabase(@ApplicationContext context: Context): FloraChainDatabase {
        return FloraChainDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideProductDao(database: FloraChainDatabase): ProductDao {
        return database.productDao()
    }

    @Provides
    @Singleton
    fun provideSyncQueueDao(database: FloraChainDatabase): SyncQueueDao {
        return database.syncQueueDao()
    }
}
