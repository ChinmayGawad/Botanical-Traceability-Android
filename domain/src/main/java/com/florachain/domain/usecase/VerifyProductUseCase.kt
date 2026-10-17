package com.florachain.domain.usecase

import com.florachain.domain.model.VerificationResult
import com.florachain.domain.repository.ProductRepository

class VerifyProductUseCase(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(identifier: String): Result<VerificationResult> {
        val cleanIdentifier = identifier.trim()
            .removePrefix("florachain:batch:")
            .removePrefix("florachain://verify?batchId=")
        if (cleanIdentifier.isBlank()) {
            return Result.failure(IllegalArgumentException("Batch identifier cannot be blank"))
        }
        return repository.verifyProduct(cleanIdentifier)
    }
}
