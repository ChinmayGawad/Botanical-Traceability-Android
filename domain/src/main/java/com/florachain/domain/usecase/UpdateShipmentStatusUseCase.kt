package com.florachain.domain.usecase

import com.florachain.domain.model.ShipmentRecord
import com.florachain.domain.repository.ProductRepository

class UpdateShipmentStatusUseCase(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(shipmentId: String, status: String, deliveryDate: String? = null): Result<ShipmentRecord> {
        if (shipmentId.isBlank()) return Result.failure(IllegalArgumentException("Shipment ID cannot be blank"))
        if (status.isBlank()) return Result.failure(IllegalArgumentException("Status cannot be blank"))

        return repository.updateShipmentStatus(shipmentId, status, deliveryDate)
    }
}
