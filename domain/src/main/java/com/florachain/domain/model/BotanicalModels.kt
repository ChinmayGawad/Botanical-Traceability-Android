package com.florachain.domain.model

enum class UserRole {
    ADMIN,
    FARMER,
    PROCESSOR,
    LABORATORY,
    DISTRIBUTOR,
    RETAILER,
    CONSUMER
}

enum class ProductCategory {
    HERB,
    SPICE,
    MEDICINAL_PLANT,
    ESSENTIAL_OIL,
    EXTRACT,
    DRIED_LEAF,
    ROOT_POWDER,
    OTHER
}

enum class CultivationMethod {
    ORGANIC,
    WILD_CRAFTED,
    HYDROPONIC,
    CONVENTIONAL,
    BIODYNAMIC,
    NATURAL_FARMING
}

enum class ProductStatus {
    HARVESTED,
    IN_PROCESSING,
    PROCESSED,
    IN_TESTING,
    TESTED_PASSED,
    TESTED_FAILED,
    IN_TRANSIT,
    DELIVERED,
    IN_RETAIL,
    SOLD,
    RECALLED
}

enum class VerificationState {
    VERIFIED,
    COMPROMISED,
    SUSPICIOUS,
    PENDING
}

enum class TestStatus {
    PASSED,
    FAILED,
    IN_PROGRESS,
    NOT_TESTED
}

enum class TransportType {
    REFRIGERATED_TRUCK,
    STANDARD_TRUCK,
    AIR_FREIGHT,
    RAIL,
    SEA_CONTAINER
}

data class GpsCoordinates(
    val latitude: Double,
    val longitude: Double
)

data class Certificate(
    val id: String,
    val type: String,
    val certificateNumber: String,
    val issuingAuthority: String,
    val issueDate: String,
    val expiryDate: String,
    val ipfsCid: String?,
    val status: String
)

data class BotanicalProduct(
    val id: String,
    val batchId: String,
    val name: String,
    val botanicalName: String,
    val category: ProductCategory,
    val cultivationMethod: CultivationMethod,
    val quantityKg: Double,
    val harvestDate: String,
    val farmLocation: String,
    val gpsCoordinates: GpsCoordinates?,
    val farmerId: String,
    val farmerName: String,
    val farmerOrg: String?,
    val status: ProductStatus,
    val verificationState: VerificationState,
    val qrCodeValue: String?,
    val description: String?,
    val imageUrl: String?,
    val createdTimestamp: String?,
    val activeCompounds: List<String> = emptyList(),
    val certificates: List<Certificate> = emptyList(),
    val processingDetails: ProcessingRecord? = null,
    val labReport: LabReport? = null,
    val shipmentDetails: ShipmentRecord? = null,
    val retailDetails: RetailRecord? = null,
    val timeline: List<TimelineEvent> = emptyList(),
    val isLocalPending: Boolean = false
)

data class ProcessingRecord(
    val processorId: String,
    val processorName: String,
    val processingDate: String,
    val method: String,
    val facilityLocation: String,
    val initialQuantityKg: Double,
    val processedQuantityKg: Double,
    val yieldLossPercentage: Double?,
    val equipmentUsed: List<String> = emptyList(),
    val ipfsDocumentCid: String?,
    val notes: String?,
    val txHash: String?
)

data class LabTestParameter(
    val name: String,
    val value: String,
    val unit: String,
    val standardLimit: String,
    val passed: Boolean
)

data class LabReport(
    val labId: String,
    val labName: String,
    val testDate: String,
    val testedBy: String?,
    val purityPercentage: Double,
    val moisturePercentage: Double,
    val heavyMetalsStatus: TestStatus,
    val microbialTestStatus: TestStatus,
    val pesticideResidueStatus: TestStatus,
    val parameters: List<LabTestParameter> = emptyList(),
    val certificateIpfsCid: String?,
    val certificateUrl: String?,
    val overallResult: String, // "APPROVED" or "REJECTED"
    val notes: String?,
    val txHash: String?
)

data class ShipmentRecord(
    val shipmentId: String,
    val distributorId: String,
    val distributorName: String,
    val sourceLocation: String,
    val destinationLocation: String,
    val vehicleNumber: String,
    val transportType: TransportType,
    val temperatureRange: String,
    val dispatchDate: String,
    val expectedDeliveryDate: String,
    val actualDeliveryDate: String?,
    val status: String,
    val trackingNumber: String?,
    val txHash: String?
)

data class RetailRecord(
    val retailerId: String,
    val retailerName: String,
    val storeLocation: String,
    val receivedDate: String,
    val shelfBatchId: String?,
    val unitPrice: Double,
    val qrCodeGenerated: Boolean,
    val notes: String?,
    val txHash: String?
)

data class TimelineEvent(
    val id: String,
    val title: String,
    val stage: String,
    val timestamp: String,
    val actorName: String,
    val actorRole: UserRole,
    val location: String,
    val description: String,
    val txHash: String?,
    val status: String,
    val ipfsHash: String?
)

data class StageIntegrity(
    val stageName: String,
    val completed: Boolean,
    val verifiedOnChain: Boolean,
    val txHash: String?,
    val timestamp: String?,
    val actorName: String?,
    val remarks: String?
)

data class VerificationResult(
    val queryIdentifier: String,
    val verificationState: VerificationState,
    val trustScore: Int, // 0 to 100%
    val onChainVerified: Boolean,
    val verifiedContractAddress: String?,
    val rootOriginTxHash: String?,
    val product: BotanicalProduct?,
    val stageIntegrity: List<StageIntegrity> = emptyList(),
    val verificationSummary: String
)

data class AuthUser(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val organization: String?,
    val location: String?,
    val status: String?,
    val joinedDate: String?,
    val certifications: List<String> = emptyList(),
    val avatarUrl: String?,
    val walletAddress: String?,
    val aadhaarNumber: String?
)
