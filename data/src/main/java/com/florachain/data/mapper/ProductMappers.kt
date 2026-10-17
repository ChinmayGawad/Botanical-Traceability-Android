package com.florachain.data.mapper

import com.florachain.data.local.entity.ProductEntity
import com.florachain.data.remote.dto.*
import com.florachain.domain.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private val gson = Gson()

fun ProductResponse.toDomain(): BotanicalProduct {
    return BotanicalProduct(
        id = id,
        batchId = batchId,
        name = name,
        botanicalName = botanicalName,
        category = try { ProductCategory.valueOf(category) } catch (e: Exception) { ProductCategory.OTHER },
        cultivationMethod = try { CultivationMethod.valueOf(cultivationMethod) } catch (e: Exception) { CultivationMethod.CONVENTIONAL },
        quantityKg = quantityKg,
        harvestDate = harvestDate,
        farmLocation = farmLocation,
        gpsCoordinates = gpsCoordinates?.let { GpsCoordinates(it.lat, it.lng) },
        farmerId = farmerId ?: "",
        farmerName = farmerName ?: "",
        farmerOrg = farmerOrg,
        status = try { ProductStatus.valueOf(status) } catch (e: Exception) { ProductStatus.HARVESTED },
        verificationState = try { VerificationState.valueOf(verificationState) } catch (e: Exception) { VerificationState.PENDING },
        qrCodeValue = qrCodeValue,
        description = description,
        imageUrl = imageUrl,
        createdTimestamp = createdTimestamp,
        activeCompounds = activeCompounds ?: emptyList(),
        certificates = certificates?.map { it.toDomain() } ?: emptyList(),
        processingDetails = processingDetails?.toDomain(),
        labReport = labReport?.toDomain(),
        shipmentDetails = shipmentDetails?.toDomain(),
        retailDetails = retailDetails?.toDomain(),
        timeline = timeline?.map { it.toDomain() } ?: emptyList(),
        isLocalPending = false
    )
}

fun ProductResponse.toEntity(): ProductEntity {
    return ProductEntity(
        id = id,
        batchId = batchId,
        name = name,
        botanicalName = botanicalName,
        category = category,
        cultivationMethod = cultivationMethod,
        quantityKg = quantityKg,
        harvestDate = harvestDate,
        farmLocation = farmLocation,
        latitude = gpsCoordinates?.lat,
        longitude = gpsCoordinates?.lng,
        farmerId = farmerId ?: "",
        farmerName = farmerName ?: "",
        farmerOrg = farmerOrg,
        status = status,
        verificationState = verificationState,
        qrCodeValue = qrCodeValue,
        description = description,
        imageUrl = imageUrl,
        createdTimestamp = createdTimestamp,
        activeCompounds = activeCompounds?.joinToString(",") ?: "",
        isLocalPending = false,
        detailsJson = gson.toJson(this)
    )
}

fun BotanicalProduct.toEntity(): ProductEntity {
    return ProductEntity(
        id = id,
        batchId = batchId,
        name = name,
        botanicalName = botanicalName,
        category = category.name,
        cultivationMethod = cultivationMethod.name,
        quantityKg = quantityKg,
        harvestDate = harvestDate,
        farmLocation = farmLocation,
        latitude = gpsCoordinates?.latitude,
        longitude = gpsCoordinates?.longitude,
        farmerId = farmerId,
        farmerName = farmerName,
        farmerOrg = farmerOrg,
        status = status.name,
        verificationState = verificationState.name,
        qrCodeValue = qrCodeValue,
        description = description,
        imageUrl = imageUrl,
        createdTimestamp = createdTimestamp,
        activeCompounds = activeCompounds.joinToString(","),
        isLocalPending = isLocalPending
    )
}

fun ProductEntity.toDomain(): BotanicalProduct {
    if (!detailsJson.isNullOrBlank()) {
        try {
            val response = gson.fromJson(detailsJson, ProductResponse::class.java)
            return response.toDomain().copy(isLocalPending = isLocalPending)
        } catch (_: Exception) { }
    }

    return BotanicalProduct(
        id = id,
        batchId = batchId,
        name = name,
        botanicalName = botanicalName,
        category = try { ProductCategory.valueOf(category) } catch (e: Exception) { ProductCategory.OTHER },
        cultivationMethod = try { CultivationMethod.valueOf(cultivationMethod) } catch (e: Exception) { CultivationMethod.CONVENTIONAL },
        quantityKg = quantityKg,
        harvestDate = harvestDate,
        farmLocation = farmLocation,
        gpsCoordinates = if (latitude != null && longitude != null) GpsCoordinates(latitude, longitude) else null,
        farmerId = farmerId,
        farmerName = farmerName,
        farmerOrg = farmerOrg,
        status = try { ProductStatus.valueOf(status) } catch (e: Exception) { ProductStatus.HARVESTED },
        verificationState = try { VerificationState.valueOf(verificationState) } catch (e: Exception) { VerificationState.PENDING },
        qrCodeValue = qrCodeValue,
        description = description,
        imageUrl = imageUrl,
        createdTimestamp = createdTimestamp,
        activeCompounds = if (activeCompounds.isNotBlank()) activeCompounds.split(",") else emptyList(),
        isLocalPending = isLocalPending
    )
}

fun CertificateDto.toDomain() = Certificate(
    id = id ?: "",
    type = type,
    certificateNumber = certificateNumber,
    issuingAuthority = issuingAuthority,
    issueDate = issueDate,
    expiryDate = expiryDate,
    ipfsCid = ipfsCid,
    status = status
)

fun ProcessingResponse.toDomain() = ProcessingRecord(
    processorId = processorId,
    processorName = processorName,
    processingDate = processingDate,
    method = method,
    facilityLocation = facilityLocation,
    initialQuantityKg = initialQuantityKg,
    processedQuantityKg = processedQuantityKg,
    yieldLossPercentage = yieldLossPercentage,
    equipmentUsed = equipmentUsed ?: emptyList(),
    ipfsDocumentCid = ipfsDocumentCid,
    notes = notes,
    txHash = txHash
)

fun LabReportResponse.toDomain() = LabReport(
    labId = labId,
    labName = labName,
    testDate = testDate,
    testedBy = testedBy,
    purityPercentage = purityPercentage,
    moisturePercentage = moisturePercentage,
    heavyMetalsStatus = try { TestStatus.valueOf(heavyMetalsStatus) } catch (e: Exception) { TestStatus.NOT_TESTED },
    microbialTestStatus = try { TestStatus.valueOf(microbialTestStatus) } catch (e: Exception) { TestStatus.NOT_TESTED },
    pesticideResidueStatus = try { TestStatus.valueOf(pesticideResidueStatus) } catch (e: Exception) { TestStatus.NOT_TESTED },
    parameters = parameters?.map { LabTestParameter(it.name, it.value, it.unit, it.standardLimit, it.passed) } ?: emptyList(),
    certificateIpfsCid = certificateIpfsCid,
    certificateUrl = certificateUrl,
    overallResult = overallResult,
    notes = notes,
    txHash = txHash
)

fun ShipmentResponse.toDomain() = ShipmentRecord(
    shipmentId = shipmentId,
    distributorId = distributorId,
    distributorName = distributorName,
    sourceLocation = sourceLocation,
    destinationLocation = destinationLocation,
    vehicleNumber = vehicleNumber,
    transportType = try { TransportType.valueOf(transportType) } catch (e: Exception) { TransportType.STANDARD_TRUCK },
    temperatureRange = temperatureRange,
    dispatchDate = dispatchDate,
    expectedDeliveryDate = expectedDeliveryDate,
    actualDeliveryDate = actualDeliveryDate,
    status = status,
    trackingNumber = trackingNumber,
    txHash = txHash
)

fun RetailResponse.toDomain() = RetailRecord(
    retailerId = retailerId,
    retailerName = retailerName,
    storeLocation = storeLocation,
    receivedDate = receivedDate,
    shelfBatchId = shelfBatchId,
    unitPrice = unitPrice,
    qrCodeGenerated = qrCodeGenerated ?: false,
    notes = notes,
    txHash = txHash
)

fun TimelineEventDto.toDomain() = TimelineEvent(
    id = id,
    title = title,
    stage = stage,
    timestamp = timestamp,
    actorName = actorName,
    actorRole = try { UserRole.valueOf(actorRole) } catch (e: Exception) { UserRole.CONSUMER },
    location = location,
    description = description,
    txHash = txHash,
    status = status,
    ipfsHash = ipfsHash
)

fun VerificationResultResponse.toDomain() = VerificationResult(
    queryIdentifier = queryIdentifier,
    verificationState = try { VerificationState.valueOf(verificationState) } catch (e: Exception) { VerificationState.PENDING },
    trustScore = trustScore,
    onChainVerified = onChainVerified,
    verifiedContractAddress = verifiedContractAddress,
    rootOriginTxHash = rootOriginTxHash,
    product = product?.toDomain(),
    stageIntegrity = stageIntegrity?.map {
        StageIntegrity(
            stageName = it.stageName,
            completed = it.completed,
            verifiedOnChain = it.verifiedOnChain,
            txHash = it.txHash,
            timestamp = it.timestamp,
            actorName = it.actorName,
            remarks = it.remarks
        )
    } ?: emptyList(),
    verificationSummary = verificationSummary
)
