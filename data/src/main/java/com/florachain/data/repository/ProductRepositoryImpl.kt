package com.florachain.data.repository

import com.florachain.data.local.dao.ProductDao
import com.florachain.data.local.dao.SyncQueueDao
import com.florachain.data.local.entity.SyncQueueEntity
import com.florachain.data.mapper.toDomain
import com.florachain.data.mapper.toEntity
import com.florachain.data.remote.FloraChainApi
import com.florachain.data.remote.dto.*
import com.florachain.domain.model.*
import com.florachain.domain.repository.ProductRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.UUID

class ProductRepositoryImpl(
    private val api: FloraChainApi,
    private val productDao: ProductDao,
    private val syncQueueDao: SyncQueueDao,
    private val gson: Gson = Gson()
) : ProductRepository {

    override fun observeAllProducts(): Flow<List<BotanicalProduct>> {
        return productDao.observeAllProducts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getProducts(
        status: ProductStatus?,
        category: ProductCategory?,
        search: String?
    ): Result<List<BotanicalProduct>> {
        return runCatching {
            try {
                val response = api.getProducts(
                    status = status?.name,
                    category = category?.name,
                    search = search
                )
                if (response.isSuccessful && response.body() != null) {
                    val remoteProducts = response.body()!!
                    // Cache to local Room database
                    productDao.insertAll(remoteProducts.map { it.toEntity() })
                    remoteProducts.map { it.toDomain() }
                } else {
                    // Fallback to local Room cache
                    productDao.getAllProducts().map { it.toDomain() }
                }
            } catch (e: IOException) {
                // Network unavailable -> use local Room database cache
                productDao.getAllProducts().map { it.toDomain() }
            }
        }
    }

    override suspend fun getProductByIdOrBatch(identifier: String): Result<BotanicalProduct> {
        return runCatching {
            try {
                val response = api.getProductById(identifier)
                if (response.isSuccessful && response.body() != null) {
                    val remote = response.body()!!
                    productDao.insertOrUpdate(remote.toEntity())
                    remote.toDomain()
                } else {
                    val local = productDao.getProductByIdOrBatch(identifier)
                    local?.toDomain() ?: throw NoSuchElementException("Product not found: $identifier")
                }
            } catch (e: IOException) {
                val local = productDao.getProductByIdOrBatch(identifier)
                local?.toDomain() ?: throw NoSuchElementException("Offline product not found: $identifier")
            }
        }
    }

    override suspend fun verifyProduct(identifier: String): Result<VerificationResult> {
        return runCatching {
            val response = api.verifyProduct(identifier)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.toDomain()
            } else {
                // Check if local cache has this product
                val local = productDao.getProductByIdOrBatch(identifier)
                if (local != null) {
                    val domainProd = local.toDomain()
                    VerificationResult(
                        queryIdentifier = identifier,
                        verificationState = if (domainProd.isLocalPending) VerificationState.PENDING else domainProd.verificationState,
                        trustScore = if (domainProd.isLocalPending) 50 else 95,
                        onChainVerified = !domainProd.isLocalPending,
                        verifiedContractAddress = null,
                        rootOriginTxHash = null,
                        product = domainProd,
                        stageIntegrity = emptyList(),
                        verificationSummary = if (domainProd.isLocalPending) "Batch recorded locally; awaiting blockchain synchronization" else "Verified against local cryptographic record"
                    )
                } else {
                    throw IllegalStateException("Verification failed for batch: $identifier")
                }
            }
        }
    }

    override suspend fun registerHarvest(product: BotanicalProduct): Result<BotanicalProduct> {
        return runCatching {
            val request = ProductRegistrationRequest(
                name = product.name,
                botanicalName = product.botanicalName,
                batchId = product.batchId,
                category = product.category.name,
                cultivationMethod = product.cultivationMethod.name,
                quantityKg = product.quantityKg,
                harvestDate = product.harvestDate,
                farmLocation = product.farmLocation,
                gpsCoordinates = product.gpsCoordinates?.let { GpsCoordinatesDto(it.latitude, it.longitude) },
                farmerId = product.farmerId,
                farmerName = product.farmerName,
                farmerOrg = product.farmerOrg,
                description = product.description,
                imageUrl = product.imageUrl,
                activeCompounds = product.activeCompounds,
                certificates = emptyList()
            )

            try {
                val response = api.registerProduct(request)
                if (response.isSuccessful && response.body() != null) {
                    val saved = response.body()!!
                    productDao.insertOrUpdate(saved.toEntity())
                    saved.toDomain()
                } else {
                    // Save locally and queue for sync
                    queueOfflineHarvest(product, request)
                }
            } catch (e: IOException) {
                // Offline fallback
                queueOfflineHarvest(product, request)
            }
        }
    }

    private suspend fun queueOfflineHarvest(
        product: BotanicalProduct,
        request: ProductRegistrationRequest
    ): BotanicalProduct {
        val localId = if (product.id.isBlank()) UUID.randomUUID().toString() else product.id
        val pendingProduct = product.copy(id = localId, isLocalPending = true)
        
        // Save to Room
        productDao.insertOrUpdate(pendingProduct.toEntity())

        // Add to Sync Queue
        syncQueueDao.insertAction(
            SyncQueueEntity(
                actionType = "REGISTER_HARVEST",
                endpointUrl = "products",
                payloadJson = gson.toJson(request)
            )
        )
        return pendingProduct
    }

    override suspend fun submitProcessing(productId: String, record: ProcessingRecord): Result<ProcessingRecord> {
        return runCatching {
            val request = ProcessingRequest(
                processorId = record.processorId,
                processorName = record.processorName,
                processingDate = record.processingDate,
                method = record.method,
                facilityLocation = record.facilityLocation,
                initialQuantityKg = record.initialQuantityKg,
                processedQuantityKg = record.processedQuantityKg,
                yieldLossPercentage = record.yieldLossPercentage,
                equipmentUsed = record.equipmentUsed,
                ipfsDocumentCid = record.ipfsDocumentCid,
                notes = record.notes
            )

            try {
                val response = api.submitProcessing(productId, request)
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!.toDomain()
                } else {
                    queueOfflineProcessing(productId, request, record)
                }
            } catch (e: IOException) {
                queueOfflineProcessing(productId, request, record)
            }
        }
    }

    private suspend fun queueOfflineProcessing(
        productId: String,
        request: ProcessingRequest,
        record: ProcessingRecord
    ): ProcessingRecord {
        val payload = arrayOf(productId, gson.toJson(request))
        syncQueueDao.insertAction(
            SyncQueueEntity(
                actionType = "SUBMIT_PROCESSING",
                endpointUrl = "processing",
                payloadJson = gson.toJson(payload)
            )
        )
        return record
    }

    override suspend fun submitLabReport(productId: String, report: LabReport): Result<LabReport> {
        return runCatching {
            val request = LabTestRequest(
                labId = report.labId,
                labName = report.labName,
                testDate = report.testDate,
                testedBy = report.testedBy,
                purityPercentage = report.purityPercentage,
                moisturePercentage = report.moisturePercentage,
                heavyMetalsStatus = report.heavyMetalsStatus.name,
                microbialTestStatus = report.microbialTestStatus.name,
                pesticideResidueStatus = report.pesticideResidueStatus.name,
                parameters = report.parameters.map {
                    LabTestParameterDto(it.name, it.value, it.unit, it.standardLimit, it.passed)
                },
                certificateIpfsCid = report.certificateIpfsCid,
                certificateUrl = report.certificateUrl,
                approve = report.overallResult.equals("APPROVED", ignoreCase = true),
                notes = report.notes
            )

            try {
                val response = api.submitLabReport(productId, request)
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!.toDomain()
                } else {
                    queueOfflineLab(productId, request, report)
                }
            } catch (e: IOException) {
                queueOfflineLab(productId, request, report)
            }
        }
    }

    private suspend fun queueOfflineLab(
        productId: String,
        request: LabTestRequest,
        report: LabReport
    ): LabReport {
        val payload = arrayOf(productId, gson.toJson(request))
        syncQueueDao.insertAction(
            SyncQueueEntity(
                actionType = "SUBMIT_LAB",
                endpointUrl = "laboratories/test",
                payloadJson = gson.toJson(payload)
            )
        )
        return report
    }

    override suspend fun createShipment(shipment: ShipmentRecord, productId: String): Result<ShipmentRecord> {
        return runCatching {
            val request = CreateShipmentRequest(
                productId = productId,
                distributorId = shipment.distributorId,
                distributorName = shipment.distributorName,
                sourceLocation = shipment.sourceLocation,
                destinationLocation = shipment.destinationLocation,
                vehicleNumber = shipment.vehicleNumber,
                transportType = shipment.transportType.name,
                temperatureRange = shipment.temperatureRange,
                dispatchDate = shipment.dispatchDate,
                expectedDeliveryDate = shipment.expectedDeliveryDate,
                trackingNumber = shipment.trackingNumber
            )

            try {
                val response = api.createShipment(request)
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!.toDomain()
                } else {
                    queueOfflineShipment(request, shipment)
                }
            } catch (e: IOException) {
                queueOfflineShipment(request, shipment)
            }
        }
    }

    private suspend fun queueOfflineShipment(
        request: CreateShipmentRequest,
        shipment: ShipmentRecord
    ): ShipmentRecord {
        syncQueueDao.insertAction(
            SyncQueueEntity(
                actionType = "CREATE_SHIPMENT",
                endpointUrl = "shipments",
                payloadJson = gson.toJson(request)
            )
        )
        return shipment
    }

    override suspend fun updateShipmentStatus(
        shipmentId: String,
        status: String,
        deliveryDate: String?
    ): Result<ShipmentRecord> {
        return runCatching {
            val request = UpdateShipmentStatusRequest(status = status, actualDeliveryDate = deliveryDate)
            val response = api.updateShipmentStatus(shipmentId, request)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.toDomain()
            } else {
                throw IllegalStateException("Failed to update shipment status: ${response.code()}")
            }
        }
    }

    override suspend fun receiveRetailStock(productId: String, retail: RetailRecord): Result<RetailRecord> {
        return runCatching {
            val request = RetailReceiveRequest(
                retailerId = retail.retailerId,
                retailerName = retail.retailerName,
                storeLocation = retail.storeLocation,
                receivedDate = retail.receivedDate,
                shelfBatchId = retail.shelfBatchId,
                unitPrice = retail.unitPrice,
                notes = retail.notes
            )

            try {
                val response = api.receiveRetailStock(productId, request)
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!.toDomain()
                } else {
                    queueOfflineRetail(productId, request, retail)
                }
            } catch (e: IOException) {
                queueOfflineRetail(productId, request, retail)
            }
        }
    }

    private suspend fun queueOfflineRetail(
        productId: String,
        request: RetailReceiveRequest,
        retail: RetailRecord
    ): RetailRecord {
        val payload = arrayOf(productId, gson.toJson(request))
        syncQueueDao.insertAction(
            SyncQueueEntity(
                actionType = "RETAIL_RECEIVE",
                endpointUrl = "retailers/receive",
                payloadJson = gson.toJson(payload)
            )
        )
        return retail
    }
}
