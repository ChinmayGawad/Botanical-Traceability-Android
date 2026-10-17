package com.florachain.domain.repository

import kotlinx.coroutines.flow.Flow

interface SyncRepository {
    fun observePendingSyncCount(): Flow<Int>
    suspend fun enqueueAction(actionType: String, endpointUrl: String, payloadJson: String)
    suspend fun processSyncQueue(): Result<Int>
}
