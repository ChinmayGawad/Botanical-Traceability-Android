package com.florachain.domain.usecase

import com.florachain.domain.model.ReportStatus
import com.florachain.domain.model.SuspiciousReport
import com.florachain.domain.repository.AdminRepository

class UpdateReportStatusUseCase(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(reportId: String, status: ReportStatus, notes: String): Result<SuspiciousReport> {
        if (reportId.isBlank()) {
            return Result.failure(IllegalArgumentException("Report ID cannot be blank"))
        }
        return adminRepository.updateReportStatus(reportId, status, notes)
    }
}
