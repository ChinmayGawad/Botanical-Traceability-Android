package com.florachain.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val batchId: String,
    val name: String,
    val botanicalName: String,
    val category: String,
    val cultivationMethod: String,
    val quantityKg: Double,
    val harvestDate: String,
    val farmLocation: String,
    val latitude: Double?,
    val longitude: Double?,
    val farmerId: String,
    val farmerName: String,
    val farmerOrg: String?,
    val status: String,
    val verificationState: String,
    val qrCodeValue: String?,
    val description: String?,
    val imageUrl: String?,
    val createdTimestamp: String?,
    val activeCompounds: String, // Comma-separated or JSON
    val isLocalPending: Boolean = false,
    val detailsJson: String? = null // Serialized full payload with reports/shipment
)
