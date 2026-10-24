package com.florachain.data.remote.dto

import com.google.gson.annotations.SerializedName

// --- Auth DTOs ---
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("role") val role: String,
    @SerializedName("organization") val organization: String? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("certifications") val certifications: List<String>? = null,
    @SerializedName("avatarUrl") val avatarUrl: String? = null,
    @SerializedName("aadhaarNumber") val aadhaarNumber: String? = null
)

data class AuthResponse(
    @SerializedName("token") val token: String,
    @SerializedName("tokenType") val tokenType: String,
    @SerializedName("user") val user: UserDto
)

data class UserDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String,
    @SerializedName("organization") val organization: String? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("joinedDate") val joinedDate: String? = null,
    @SerializedName("certifications") val certifications: List<String>? = null,
    @SerializedName("avatarUrl") val avatarUrl: String? = null,
    @SerializedName("walletAddress") val walletAddress: String? = null,
    @SerializedName("aadhaarNumber") val aadhaarNumber: String? = null
)

// --- Product DTOs ---
data class GpsCoordinatesDto(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double
)

data class CertificateDto(
    @SerializedName("id") val id: String?,
    @SerializedName("type") val type: String,
    @SerializedName("certificateNumber") val certificateNumber: String,
    @SerializedName("issuingAuthority") val issuingAuthority: String,
    @SerializedName("issueDate") val issueDate: String,
    @SerializedName("expiryDate") val expiryDate: String,
    @SerializedName("ipfsCid") val ipfsCid: String?,
    @SerializedName("status") val status: String
)

data class ProductRegistrationRequest(
    @SerializedName("name") val name: String,
    @SerializedName("botanicalName") val botanicalName: String,
    @SerializedName("batchId") val batchId: String,
    @SerializedName("category") val category: String,
    @SerializedName("cultivationMethod") val cultivationMethod: String,
    @SerializedName("quantityKg") val quantityKg: Double,
    @SerializedName("harvestDate") val harvestDate: String,
    @SerializedName("farmLocation") val farmLocation: String,
    @SerializedName("gpsCoordinates") val gpsCoordinates: GpsCoordinatesDto?,
    @SerializedName("farmerId") val farmerId: String?,
    @SerializedName("farmerName") val farmerName: String?,
    @SerializedName("farmerOrg") val farmerOrg: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("activeCompounds") val activeCompounds: List<String>?,
    @SerializedName("certificates") val certificates: List<CertificateDto>?
)

data class ProductResponse(
    @SerializedName("id") val id: String,
    @SerializedName("batchId") val batchId: String,
    @SerializedName("name") val name: String,
    @SerializedName("botanicalName") val botanicalName: String,
    @SerializedName("category") val category: String,
    @SerializedName("cultivationMethod") val cultivationMethod: String,
    @SerializedName("quantityKg") val quantityKg: Double,
    @SerializedName("harvestDate") val harvestDate: String,
    @SerializedName("farmLocation") val farmLocation: String,
    @SerializedName("gpsCoordinates") val gpsCoordinates: GpsCoordinatesDto?,
    @SerializedName("farmerId") val farmerId: String?,
    @SerializedName("farmerName") val farmerName: String?,
    @SerializedName("farmerOrg") val farmerOrg: String?,
    @SerializedName("status") val status: String,
    @SerializedName("verificationState") val verificationState: String,
    @SerializedName("qrCodeValue") val qrCodeValue: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("createdTimestamp") val createdTimestamp: String?,
    @SerializedName("activeCompounds") val activeCompounds: List<String>?,
    @SerializedName("certificates") val certificates: List<CertificateDto>?,
    @SerializedName("processingDetails") val processingDetails: ProcessingResponse?,
    @SerializedName("labReport") val labReport: LabReportResponse?,
    @SerializedName("shipmentDetails") val shipmentDetails: ShipmentResponse?,
    @SerializedName("retailDetails") val retailDetails: RetailResponse?,
    @SerializedName("timeline") val timeline: List<TimelineEventDto>?
)

// --- Processing DTOs ---
data class ProcessingRequest(
    @SerializedName("processorId") val processorId: String,
    @SerializedName("processorName") val processorName: String,
    @SerializedName("processingDate") val processingDate: String,
    @SerializedName("method") val method: String,
    @SerializedName("facilityLocation") val facilityLocation: String,
    @SerializedName("initialQuantityKg") val initialQuantityKg: Double,
    @SerializedName("processedQuantityKg") val processedQuantityKg: Double,
    @SerializedName("yieldLossPercentage") val yieldLossPercentage: Double?,
    @SerializedName("equipmentUsed") val equipmentUsed: List<String>?,
    @SerializedName("ipfsDocumentCid") val ipfsDocumentCid: String?,
    @SerializedName("notes") val notes: String?
)

data class ProcessingResponse(
    @SerializedName("processorId") val processorId: String,
    @SerializedName("processorName") val processorName: String,
    @SerializedName("processingDate") val processingDate: String,
    @SerializedName("method") val method: String,
    @SerializedName("facilityLocation") val facilityLocation: String,
    @SerializedName("initialQuantityKg") val initialQuantityKg: Double,
    @SerializedName("processedQuantityKg") val processedQuantityKg: Double,
    @SerializedName("yieldLossPercentage") val yieldLossPercentage: Double?,
    @SerializedName("equipmentUsed") val equipmentUsed: List<String>?,
    @SerializedName("ipfsDocumentCid") val ipfsDocumentCid: String?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("txHash") val txHash: String?
)

// --- Lab DTOs ---
data class LabTestParameterDto(
    @SerializedName("name") val name: String,
    @SerializedName("value") val value: String,
    @SerializedName("unit") val unit: String,
    @SerializedName("standardLimit") val standardLimit: String,
    @SerializedName("passed") val passed: Boolean
)

data class LabTestRequest(
    @SerializedName("labId") val labId: String,
    @SerializedName("labName") val labName: String,
    @SerializedName("testDate") val testDate: String,
    @SerializedName("testedBy") val testedBy: String?,
    @SerializedName("purityPercentage") val purityPercentage: Double,
    @SerializedName("moisturePercentage") val moisturePercentage: Double,
    @SerializedName("heavyMetalsStatus") val heavyMetalsStatus: String,
    @SerializedName("microbialTestStatus") val microbialTestStatus: String,
    @SerializedName("pesticideResidueStatus") val pesticideResidueStatus: String,
    @SerializedName("parameters") val parameters: List<LabTestParameterDto>?,
    @SerializedName("certificateIpfsCid") val certificateIpfsCid: String?,
    @SerializedName("certificateUrl") val certificateUrl: String?,
    @SerializedName("approve") val approve: Boolean?,
    @SerializedName("notes") val notes: String?
)

data class LabReportResponse(
    @SerializedName("labId") val labId: String,
    @SerializedName("labName") val labName: String,
    @SerializedName("testDate") val testDate: String,
    @SerializedName("testedBy") val testedBy: String?,
    @SerializedName("purityPercentage") val purityPercentage: Double,
    @SerializedName("moisturePercentage") val moisturePercentage: Double,
    @SerializedName("heavyMetalsStatus") val heavyMetalsStatus: String,
    @SerializedName("microbialTestStatus") val microbialTestStatus: String,
    @SerializedName("pesticideResidueStatus") val pesticideResidueStatus: String,
    @SerializedName("parameters") val parameters: List<LabTestParameterDto>?,
    @SerializedName("certificateIpfsCid") val certificateIpfsCid: String?,
    @SerializedName("certificateUrl") val certificateUrl: String?,
    @SerializedName("overallResult") val overallResult: String,
    @SerializedName("notes") val notes: String?,
    @SerializedName("txHash") val txHash: String?
)

// --- Shipment DTOs ---
data class CreateShipmentRequest(
    @SerializedName("productId") val productId: String,
    @SerializedName("distributorId") val distributorId: String,
    @SerializedName("distributorName") val distributorName: String,
    @SerializedName("sourceLocation") val sourceLocation: String,
    @SerializedName("destinationLocation") val destinationLocation: String,
    @SerializedName("vehicleNumber") val vehicleNumber: String,
    @SerializedName("transportType") val transportType: String,
    @SerializedName("temperatureRange") val temperatureRange: String,
    @SerializedName("dispatchDate") val dispatchDate: String,
    @SerializedName("expectedDeliveryDate") val expectedDeliveryDate: String,
    @SerializedName("trackingNumber") val trackingNumber: String?
)

data class UpdateShipmentStatusRequest(
    @SerializedName("status") val status: String,
    @SerializedName("actualDeliveryDate") val actualDeliveryDate: String?
)

data class ShipmentResponse(
    @SerializedName("shipmentId") val shipmentId: String,
    @SerializedName("distributorId") val distributorId: String,
    @SerializedName("distributorName") val distributorName: String,
    @SerializedName("sourceLocation") val sourceLocation: String,
    @SerializedName("destinationLocation") val destinationLocation: String,
    @SerializedName("vehicleNumber") val vehicleNumber: String,
    @SerializedName("transportType") val transportType: String,
    @SerializedName("temperatureRange") val temperatureRange: String,
    @SerializedName("dispatchDate") val dispatchDate: String,
    @SerializedName("expectedDeliveryDate") val expectedDeliveryDate: String,
    @SerializedName("actualDeliveryDate") val actualDeliveryDate: String?,
    @SerializedName("status") val status: String,
    @SerializedName("trackingNumber") val trackingNumber: String?,
    @SerializedName("txHash") val txHash: String?
)

// --- Retail DTOs ---
data class RetailReceiveRequest(
    @SerializedName("retailerId") val retailerId: String,
    @SerializedName("retailerName") val retailerName: String,
    @SerializedName("storeLocation") val storeLocation: String,
    @SerializedName("receivedDate") val receivedDate: String,
    @SerializedName("shelfBatchId") val shelfBatchId: String?,
    @SerializedName("unitPrice") val unitPrice: Double,
    @SerializedName("notes") val notes: String?
)

data class RetailResponse(
    @SerializedName("retailerId") val retailerId: String,
    @SerializedName("retailerName") val retailerName: String,
    @SerializedName("storeLocation") val storeLocation: String,
    @SerializedName("receivedDate") val receivedDate: String,
    @SerializedName("shelfBatchId") val shelfBatchId: String?,
    @SerializedName("unitPrice") val unitPrice: Double,
    @SerializedName("qrCodeGenerated") val qrCodeGenerated: Boolean?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("txHash") val txHash: String?
)

// --- Verification DTOs ---
data class TimelineEventDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("stage") val stage: String,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("actorName") val actorName: String,
    @SerializedName("actorRole") val actorRole: String,
    @SerializedName("location") val location: String,
    @SerializedName("description") val description: String,
    @SerializedName("txHash") val txHash: String?,
    @SerializedName("status") val status: String,
    @SerializedName("ipfsHash") val ipfsHash: String?
)

data class StageIntegrityDto(
    @SerializedName("stageName") val stageName: String,
    @SerializedName("completed") val completed: Boolean,
    @SerializedName("verifiedOnChain") val verifiedOnChain: Boolean,
    @SerializedName("txHash") val txHash: String?,
    @SerializedName("timestamp") val timestamp: String?,
    @SerializedName("actorName") val actorName: String?,
    @SerializedName("remarks") val remarks: String?
)

data class VerificationResultResponse(
    @SerializedName("queryIdentifier") val queryIdentifier: String,
    @SerializedName("verificationState") val verificationState: String,
    @SerializedName("trustScore") val trustScore: Int,
    @SerializedName("onChainVerified") val onChainVerified: Boolean,
    @SerializedName("verifiedContractAddress") val verifiedContractAddress: String?,
    @SerializedName("rootOriginTxHash") val rootOriginTxHash: String?,
    @SerializedName("product") val product: ProductResponse?,
    @SerializedName("stageIntegrity") val stageIntegrity: List<StageIntegrityDto>?,
    @SerializedName("verificationSummary") val verificationSummary: String
)

// --- Report DTOs ---
data class CreateReportRequestDto(
    @SerializedName("batchId") val batchId: String,
    @SerializedName("reporterName") val reporterName: String,
    @SerializedName("reporterContact") val reporterContact: String?,
    @SerializedName("issueType") val issueType: String,
    @SerializedName("description") val description: String,
    @SerializedName("evidencePhotoUrl") val evidencePhotoUrl: String?
)

data class ReportResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("batchId") val batchId: String,
    @SerializedName("reporterName") val reporterName: String,
    @SerializedName("reporterContact") val reporterContact: String?,
    @SerializedName("issueType") val issueType: String,
    @SerializedName("description") val description: String,
    @SerializedName("evidencePhotoUrl") val evidencePhotoUrl: String?,
    @SerializedName("status") val status: String,
    @SerializedName("adminNotes") val adminNotes: String?,
    @SerializedName("createdAt") val createdAt: String
)

data class UpdateReportStatusRequestDto(
    @SerializedName("status") val status: String,
    @SerializedName("adminNotes") val adminNotes: String?
)

// --- Blockchain DTOs ---
data class BlockchainStatsResponseDto(
    @SerializedName("blockHeight") val blockHeight: Long,
    @SerializedName("totalTransactions") val totalTransactions: Long,
    @SerializedName("activeNodes") val activeNodes: Int,
    @SerializedName("gasUsed") val gasUsed: Long,
    @SerializedName("contractAddress") val contractAddress: String,
    @SerializedName("networkName") val networkName: String?
)

data class TransactionDto(
    @SerializedName("txId") val txId: String,
    @SerializedName("blockNumber") val blockNumber: Long,
    @SerializedName("from") val from: String,
    @SerializedName("to") val to: String,
    @SerializedName("stage") val stage: String,
    @SerializedName("action") val action: String,
    @SerializedName("actor") val actor: String,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("status") val status: String,
    @SerializedName("gasUsed") val gasUsed: Long,
    @SerializedName("rawPayload") val rawPayload: String?
)

