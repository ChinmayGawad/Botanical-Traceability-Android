package com.florachain.domain.usecase

import com.florachain.domain.model.BotanicalProduct
import com.florachain.domain.repository.ProductRepository

class RegisterHarvestUseCase(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(product: BotanicalProduct): Result<BotanicalProduct> {
        if (product.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Product name cannot be blank"))
        }
        if (product.botanicalName.isBlank()) {
            return Result.failure(IllegalArgumentException("Botanical name cannot be blank"))
        }
        if (product.batchId.isBlank()) {
            return Result.failure(IllegalArgumentException("Batch ID cannot be blank"))
        }
        if (product.quantityKg <= 0.0) {
            return Result.failure(IllegalArgumentException("Quantity must be greater than 0 kg"))
        }
        if (product.farmLocation.isBlank()) {
            return Result.failure(IllegalArgumentException("Farm location cannot be blank"))
        }

        return repository.registerHarvest(product)
    }
}
