package com.florachain.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val queueId: Long = 0,
    val actionType: String,      // "REGISTER_HARVEST", "SUBMIT_PROCESSING", "SUBMIT_LAB", "CREATE_SHIPMENT", "RETAIL_RECEIVE"
    val endpointUrl: String,     // e.g. "products", "processing"
    val payloadJson: String,     // Serialized JSON request body
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val lastError: String? = null
)
