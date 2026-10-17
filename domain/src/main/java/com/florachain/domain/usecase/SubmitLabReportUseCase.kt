package com.florachain.domain.usecase

import com.florachain.domain.model.LabReport
import com.florachain.domain.repository.ProductRepository

class SubmitLabReportUseCase(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(productId: String, report: LabReport): Result<LabReport> {
        if (productId.isBlank()) return Result.failure(IllegalArgumentException("Product ID cannot be blank"))
        if (report.labId.isBlank()) return Result.failure(IllegalArgumentException("Lab ID cannot be blank"))
        if (report.purityPercentage < 0.0 || report.purityPercentage > 100.0) {
            return Result.failure(IllegalArgumentException("Purity percentage must be between 0 and 100"))
        }
        if (report.moisturePercentage < 0.0 || report.moisturePercentage > 100.0) {
            return Result.failure(IllegalArgumentException("Moisture percentage must be between 0 and 100"))
        }

        return repository.submitLabReport(productId, report)
    }
}
