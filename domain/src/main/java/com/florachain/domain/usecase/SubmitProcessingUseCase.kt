package com.florachain.domain.usecase

import com.florachain.domain.model.ProcessingRecord
import com.florachain.domain.repository.ProductRepository

class SubmitProcessingUseCase(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(productId: String, record: ProcessingRecord): Result<ProcessingRecord> {
        if (productId.isBlank()) return Result.failure(IllegalArgumentException("Product ID cannot be blank"))
        if (record.processorId.isBlank()) return Result.failure(IllegalArgumentException("Processor ID cannot be blank"))
        if (record.method.isBlank()) return Result.failure(IllegalArgumentException("Extraction method cannot be blank"))
        if (record.processedQuantityKg <= 0.0) return Result.failure(IllegalArgumentException("Processed quantity must be greater than 0"))

        return repository.submitProcessing(productId, record)
    }
}
