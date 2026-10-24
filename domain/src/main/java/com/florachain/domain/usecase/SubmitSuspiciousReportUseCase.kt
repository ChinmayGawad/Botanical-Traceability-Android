package com.florachain.domain.usecase

import com.florachain.domain.model.SuspiciousReport
import com.florachain.domain.repository.AdminRepository

class SubmitSuspiciousReportUseCase(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(report: SuspiciousReport): Result<SuspiciousReport> {
        if (report.batchId.isBlank()) {
            return Result.failure(IllegalArgumentException("Batch ID cannot be blank"))
        }
        if (report.description.isBlank()) {
            return Result.failure(IllegalArgumentException("Report description is required"))
        }
        return adminRepository.submitSuspiciousReport(report)
    }
}
