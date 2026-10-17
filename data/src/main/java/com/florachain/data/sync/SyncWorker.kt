package com.florachain.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.florachain.core.security.EncryptedTokenManager
import com.florachain.data.local.FloraChainDatabase
import com.florachain.data.remote.FloraChainApi
import com.florachain.data.remote.interceptor.AuthInterceptor
import com.florachain.data.repository.SyncRepositoryImpl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val tokenManager = EncryptedTokenManager(applicationContext)
            val db = FloraChainDatabase.getInstance(applicationContext)

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(tokenManager))
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(tokenManager.getBaseUrl())
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val api = retrofit.create(FloraChainApi::class.java)
            val syncRepository = SyncRepositoryImpl(db.syncQueueDao(), db.productDao(), api)

            val syncResult = syncRepository.processSyncQueue()
            if (syncResult.isSuccess) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
