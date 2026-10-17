package com.florachain.domain.usecase

import com.florachain.domain.model.RetailRecord
import com.florachain.domain.repository.ProductRepository

class ReceiveRetailStockUseCase(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(productId: String, retail: RetailRecord): Result<RetailRecord> {
        if (productId.isBlank()) return Result.failure(IllegalArgumentException("Product ID cannot be blank"))
        if (retail.retailerId.isBlank()) return Result.failure(IllegalArgumentException("Retailer ID cannot be blank"))
        if (retail.storeLocation.isBlank()) return Result.failure(IllegalArgumentException("Store location cannot be blank"))
        if (retail.unitPrice <= 0.0) return Result.failure(IllegalArgumentException("Unit price must be positive"))

        return repository.receiveRetailStock(productId, retail)
    }
}
