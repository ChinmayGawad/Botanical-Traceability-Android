package com.florachain.domain.usecase

import com.florachain.domain.model.ShipmentRecord
import com.florachain.domain.repository.ProductRepository

class CreateShipmentUseCase(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(shipment: ShipmentRecord, productId: String): Result<ShipmentRecord> {
        if (productId.isBlank()) return Result.failure(IllegalArgumentException("Product ID cannot be blank"))
        if (shipment.distributorId.isBlank()) return Result.failure(IllegalArgumentException("Distributor ID cannot be blank"))
        if (shipment.sourceLocation.isBlank()) return Result.failure(IllegalArgumentException("Source location cannot be blank"))
        if (shipment.destinationLocation.isBlank()) return Result.failure(IllegalArgumentException("Destination location cannot be blank"))
        if (shipment.vehicleNumber.isBlank()) return Result.failure(IllegalArgumentException("Vehicle number cannot be blank"))

        return repository.createShipment(shipment, productId)
    }
}
