package com.florachain.domain.model

data class NetworkStats(
    val blockHeight: Long,
    val totalTransactions: Long,
    val activeNodes: Int,
    val gasUsed: Long,
    val contractAddress: String,
    val networkName: String = "Polygon Amoy Testnet (Chain ID 80002)"
)

data class BlockchainTx(
    val txId: String,
    val blockNumber: Long,
    val from: String,
    val to: String,
    val stage: String,
    val action: String,
    val actor: String,
    val timestamp: String,
    val status: String,
    val gasUsed: Long,
    val rawPayload: String? = null
)

enum class ReportStatus {
    PENDING_REVIEW,
    INVESTIGATING,
    RESOLVED_VALID,
    DISMISSED
}

data class SuspiciousReport(
    val id: String,
    val batchId: String,
    val reporterName: String,
    val reporterContact: String?,
    val issueType: String,
    val description: String,
    val evidencePhotoUrl: String?,
    val status: ReportStatus,
    val adminNotes: String?,
    val createdAt: String
)
