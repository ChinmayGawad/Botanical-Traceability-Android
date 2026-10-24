package com.florachain.data.mapper

import com.florachain.data.remote.dto.*
import com.florachain.domain.model.*

fun ReportResponseDto.toDomain(): SuspiciousReport {
    val reportStatus = try {
        ReportStatus.valueOf(status.uppercase())
    } catch (e: Exception) {
        ReportStatus.PENDING_REVIEW
    }
    return SuspiciousReport(
        id = id,
        batchId = batchId,
        reporterName = reporterName,
        reporterContact = reporterContact,
        issueType = issueType,
        description = description,
        evidencePhotoUrl = evidencePhotoUrl,
        status = reportStatus,
        adminNotes = adminNotes,
        createdAt = createdAt
    )
}

fun SuspiciousReport.toCreateRequestDto(): CreateReportRequestDto {
    return CreateReportRequestDto(
        batchId = batchId,
        reporterName = reporterName,
        reporterContact = reporterContact,
        issueType = issueType,
        description = description,
        evidencePhotoUrl = evidencePhotoUrl
    )
}

fun BlockchainStatsResponseDto.toDomain(): NetworkStats {
    return NetworkStats(
        blockHeight = blockHeight,
        totalTransactions = totalTransactions,
        activeNodes = activeNodes,
        gasUsed = gasUsed,
        contractAddress = contractAddress,
        networkName = networkName ?: "Polygon Amoy Testnet (Chain ID 80002)"
    )
}

fun TransactionDto.toDomain(): BlockchainTx {
    return BlockchainTx(
        txId = txId,
        blockNumber = blockNumber,
        from = from,
        to = to,
        stage = stage,
        action = action,
        actor = actor,
        timestamp = timestamp,
        status = status,
        gasUsed = gasUsed,
        rawPayload = rawPayload
    )
}
