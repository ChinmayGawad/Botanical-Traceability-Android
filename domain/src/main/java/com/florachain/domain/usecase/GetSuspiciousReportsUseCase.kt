package com.florachain.domain.usecase

import com.florachain.domain.model.ReportStatus
import com.florachain.domain.model.SuspiciousReport
import com.florachain.domain.repository.AdminRepository

class GetSuspiciousReportsUseCase(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(status: ReportStatus? = null): Result<List<SuspiciousReport>> {
        return adminRepository.getSuspiciousReports(status)
    }
}
