package com.florachain.domain.repository

import com.florachain.domain.model.AuthUser
import com.florachain.domain.model.ReportStatus
import com.florachain.domain.model.SuspiciousReport
import kotlinx.coroutines.flow.Flow

interface AdminRepository {
    fun observePendingUsers(): Flow<List<AuthUser>>
    suspend fun getPendingUsers(): Result<List<AuthUser>>
    suspend fun approveUser(userId: String): Result<AuthUser>
    suspend fun rejectUser(userId: String): Result<Unit>
    
    fun observeSuspiciousReports(): Flow<List<SuspiciousReport>>
    suspend fun getSuspiciousReports(status: ReportStatus? = null): Result<List<SuspiciousReport>>
    suspend fun updateReportStatus(reportId: String, status: ReportStatus, notes: String): Result<SuspiciousReport>
    suspend fun submitSuspiciousReport(report: SuspiciousReport): Result<SuspiciousReport>
}
