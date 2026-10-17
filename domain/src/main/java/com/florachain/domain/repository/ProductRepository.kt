package com.florachain.domain.repository

import com.florachain.domain.model.BotanicalProduct
import com.florachain.domain.model.LabReport
import com.florachain.domain.model.ProcessingRecord
import com.florachain.domain.model.ProductCategory
import com.florachain.domain.model.ProductStatus
import com.florachain.domain.model.RetailRecord
import com.florachain.domain.model.ShipmentRecord
import com.florachain.domain.model.VerificationResult
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun observeAllProducts(): Flow<List<BotanicalProduct>>
    suspend fun getProducts(status: ProductStatus? = null, category: ProductCategory? = null, search: String? = null): Result<List<BotanicalProduct>>
    suspend fun getProductByIdOrBatch(identifier: String): Result<BotanicalProduct>
    suspend fun verifyProduct(identifier: String): Result<VerificationResult>
    suspend fun registerHarvest(product: BotanicalProduct): Result<BotanicalProduct>
    suspend fun submitProcessing(productId: String, record: ProcessingRecord): Result<ProcessingRecord>
    suspend fun submitLabReport(productId: String, report: LabReport): Result<LabReport>
    suspend fun createShipment(shipment: ShipmentRecord, productId: String): Result<ShipmentRecord>
    suspend fun updateShipmentStatus(shipmentId: String, status: String, deliveryDate: String?): Result<ShipmentRecord>
    suspend fun receiveRetailStock(productId: String, retail: RetailRecord): Result<RetailRecord>
}
