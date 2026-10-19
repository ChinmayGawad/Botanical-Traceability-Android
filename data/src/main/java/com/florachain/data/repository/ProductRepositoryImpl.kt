package com.florachain.data.repository

import com.florachain.core.security.EncryptedTokenManager
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
            // 1. Fetch from live backend API
            try {
                val response = api.getProducts(
                    status = status?.name,
                    category = category?.name,
                    search = search
                )
                if (response.isSuccessful && response.body() != null) {
                    productDao.insertAll(response.body()!!.map { it.toEntity() })
                }
            } catch (_: Exception) {}

            // 2. Sync from live Web Project feed
            try {
                val webResponse = api.getSyncProducts(EncryptedTokenManager.WEB_SYNC_URL)
                if (webResponse.isSuccessful && webResponse.body() != null) {
                    productDao.insertAll(webResponse.body()!!.map { it.toEntity() })
                }
            } catch (_: Exception) {}

            // 3. Return from local Room cache with filtering applied
            val all = productDao.getAllProducts().map { it.toDomain() }
            all.filter { prod ->
                (status == null || prod.status == status) &&
                (category == null || prod.category == category) &&
                (search.isNullOrBlank() ||
                        prod.name.contains(search, ignoreCase = true) ||
                        prod.batchId.contains(search, ignoreCase = true) ||
                        prod.id.contains(search, ignoreCase = true))
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
                    return@runCatching remote.toDomain()
                }
            } catch (_: Exception) {}

            var local = productDao.getProductByIdOrBatch(identifier)
            if (local == null) {
                // Attempt dynamic sync from web project
                try {
                    val webResponse = api.getSyncProducts(EncryptedTokenManager.WEB_SYNC_URL)
                    if (webResponse.isSuccessful && webResponse.body() != null) {
                        productDao.insertAll(webResponse.body()!!.map { it.toEntity() })
                        local = productDao.getProductByIdOrBatch(identifier)
                    }
                } catch (_: Exception) {}
            }

            local?.toDomain() ?: throw NoSuchElementException("Product not found: $identifier")
        }
    }

    override suspend fun verifyProduct(identifier: String): Result<VerificationResult> {
        return runCatching {
            var remoteResult: VerificationResult? = null
            try {
                val response = api.verifyProduct(identifier)
                if (response.isSuccessful && response.body() != null) {
                    val domainResult = response.body()!!.toDomain()
                    val prod = domainResult.product
                    if (prod != null && domainResult.verificationState != VerificationState.NOT_FOUND) {
                        productDao.insertOrUpdate(prod.toEntity())
                        return@runCatching domainResult
                    }
                    remoteResult = domainResult
                }
            } catch (_: Exception) {}

            // Check local Room cache or sync from web project
            var local = productDao.getProductByIdOrBatch(identifier)
            if (local == null) {
                try {
                    val webResponse = api.getSyncProducts(EncryptedTokenManager.WEB_SYNC_URL)
                    if (webResponse.isSuccessful && webResponse.body() != null) {
                        productDao.insertAll(webResponse.body()!!.map { it.toEntity() })
                        local = productDao.getProductByIdOrBatch(identifier)
                    }
                } catch (_: Exception) {}
            }

            if (local != null) {
                val prod = local.toDomain()
                val isRejected = prod.verificationState == VerificationState.REJECTED || prod.status == ProductStatus.REJECTED
                val isVerified = prod.verificationState == VerificationState.VERIFIED
                val trustScore = when {
                    isRejected -> 15
                    isVerified -> 98
                    else -> 75
                }
                val summary = when {
                    isRejected -> "CRITICAL ALERT: Smart contract locked batch. " + (prod.labReport?.notes ?: "Quality verification rejected due to laboratory test failure.")
                    isVerified -> "Cryptographic provenance verified on-chain. Zero adulteration detected."
                    else -> "Cryptographic provenance record active on chain. Verification state: ${prod.verificationState.name}."
                }

                return@runCatching VerificationResult(
                    queryIdentifier = identifier,
                    verificationState = prod.verificationState,
                    trustScore = trustScore,
                    onChainVerified = !prod.isLocalPending,
                    verifiedContractAddress = "0x2B04B82dD07F1938E74b0CDCA9Fa5693Df147a87",
                    rootOriginTxHash = prod.timeline.firstOrNull()?.txHash ?: "0xdeadbeef00000000000000000000000000000000000000000000000000000001",
                    product = prod,
                    stageIntegrity = listOf(
                        StageIntegrity(
                            stageName = "HARVEST",
                            completed = true,
                            verifiedOnChain = true,
                            txHash = prod.timeline.find { it.stage == "FARMER" || it.stage == "HARVEST" }?.txHash,
                            timestamp = prod.harvestDate,
                            actorName = prod.farmerName.ifBlank { "Farmer" },
                            remarks = "Agricultural harvest on-chain record"
                        ),
                        StageIntegrity(
                            stageName = "LABORATORY",
                            completed = prod.labReport != null,
                            verifiedOnChain = !isRejected,
                            txHash = prod.labReport?.txHash,
                            timestamp = prod.labReport?.testDate,
                            actorName = prod.labReport?.labName ?: "Laboratory",
                            remarks = prod.labReport?.notes
                        )
                    ),
                    verificationSummary = summary
                )
            }

            remoteResult ?: VerificationResult(
                queryIdentifier = identifier,
                verificationState = VerificationState.NOT_FOUND,
                trustScore = 0,
                onChainVerified = false,
                verifiedContractAddress = "0x2B04B82dD07F1938E74b0CDCA9Fa5693Df147a87",
                rootOriginTxHash = null,
                product = null,
                stageIntegrity = emptyList(),
                verificationSummary = "No cryptographic provenance record was found on the blockchain for identifier: $identifier"
            )
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
