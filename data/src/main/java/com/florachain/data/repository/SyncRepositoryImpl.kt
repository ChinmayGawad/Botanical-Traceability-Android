package com.florachain.data.repository

import com.florachain.data.local.dao.ProductDao
import com.florachain.data.local.dao.SyncQueueDao
import com.florachain.data.local.entity.SyncQueueEntity
import com.florachain.data.mapper.toDomain
import com.florachain.data.mapper.toEntity
import com.florachain.data.remote.FloraChainApi
import com.florachain.data.remote.dto.*
import com.florachain.domain.repository.SyncRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow

class SyncRepositoryImpl(
    private val syncQueueDao: SyncQueueDao,
    private val productDao: ProductDao,
    private val api: FloraChainApi,
    private val gson: Gson = Gson()
) : SyncRepository {

    override fun observePendingSyncCount(): Flow<Int> {
        return syncQueueDao.observePendingCount()
    }

    override suspend fun enqueueAction(actionType: String, endpointUrl: String, payloadJson: String) {
        val entity = SyncQueueEntity(
            actionType = actionType,
            endpointUrl = endpointUrl,
            payloadJson = payloadJson
        )
        syncQueueDao.insertAction(entity)
    }

    override suspend fun processSyncQueue(): Result<Int> {
        return runCatching {
            val pendingActions = syncQueueDao.getAllPendingActions()
            var syncedCount = 0

            for (action in pendingActions) {
                try {
                    val success = executeRemoteAction(action)
                    if (success) {
                        syncQueueDao.deleteAction(action)
                        syncedCount++
                    } else {
                        syncQueueDao.updateAction(
                            action.copy(
                                retryCount = action.retryCount + 1,
                                lastError = "Server responded with error"
                            )
                        )
                    }
                } catch (e: Exception) {
                    syncQueueDao.updateAction(
                        action.copy(
                            retryCount = action.retryCount + 1,
                            lastError = e.localizedMessage
                        )
                    )
                }
            }
            syncedCount
        }
    }

    private suspend fun executeRemoteAction(action: SyncQueueEntity): Boolean {
        return when (action.actionType) {
            "REGISTER_HARVEST" -> {
                val req = gson.fromJson(action.payloadJson, ProductRegistrationRequest::class.java)
                val resp = api.registerProduct(req)
                if (resp.isSuccessful && resp.body() != null) {
                    val saved = resp.body()!!
                    productDao.insertOrUpdate(saved.toEntity())
                    true
                } else false
            }
            "SUBMIT_PROCESSING" -> {
                val pair = gson.fromJson(action.payloadJson, Array<String>::class.java)
                val productId = pair[0]
                val req = gson.fromJson(pair[1], ProcessingRequest::class.java)
                val resp = api.submitProcessing(productId, req)
                resp.isSuccessful
            }
            "SUBMIT_LAB" -> {
                val pair = gson.fromJson(action.payloadJson, Array<String>::class.java)
                val productId = pair[0]
                val req = gson.fromJson(pair[1], LabTestRequest::class.java)
                val resp = api.submitLabReport(productId, req)
                resp.isSuccessful
            }
            "CREATE_SHIPMENT" -> {
                val req = gson.fromJson(action.payloadJson, CreateShipmentRequest::class.java)
                val resp = api.createShipment(req)
                resp.isSuccessful
            }
            "RETAIL_RECEIVE" -> {
                val pair = gson.fromJson(action.payloadJson, Array<String>::class.java)
                val productId = pair[0]
                val req = gson.fromJson(pair[1], RetailReceiveRequest::class.java)
                val resp = api.receiveRetailStock(productId, req)
                resp.isSuccessful
            }
            else -> false
        }
    }
}
