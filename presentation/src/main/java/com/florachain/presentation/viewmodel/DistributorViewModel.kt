package com.florachain.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florachain.domain.model.BotanicalProduct
import com.florachain.domain.model.ShipmentRecord
import com.florachain.domain.model.TransportType
import com.florachain.domain.repository.ProductRepository
import com.florachain.domain.usecase.CreateShipmentUseCase
import com.florachain.domain.usecase.UpdateShipmentStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class DistributorUiState(
    val products: List<BotanicalProduct> = emptyList(),
    val selectedProduct: BotanicalProduct? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DistributorViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val createShipmentUseCase: CreateShipmentUseCase,
    private val updateShipmentStatusUseCase: UpdateShipmentStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DistributorUiState())
    val uiState: StateFlow<DistributorUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = productRepository.getProducts()
            result.fold(
                onSuccess = { list ->
                    _uiState.update { it.copy(isLoading = false, products = list) }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, error = err.localizedMessage) }
                }
            )
        }
    }

    fun selectProduct(product: BotanicalProduct) {
        _uiState.update { it.copy(selectedProduct = product, isSuccess = false, error = null) }
    }

    fun dispatchShipment(
        distributorId: String,
        distributorName: String,
        sourceLocation: String,
        destinationLocation: String,
        vehicleNumber: String,
        transportType: TransportType,
        temperatureRange: String,
        trackingNumber: String? = null
    ) {
        val product = _uiState.value.selectedProduct ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isSuccess = false) }

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dispatchDate = sdf.format(Date())
            val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 3) }
            val expectedDate = sdf.format(calendar.time)

            val resolvedTracking = trackingNumber?.ifBlank { null }
                ?: ("TRK-" + UUID.randomUUID().toString().take(8).uppercase())

            val shipment = ShipmentRecord(
                shipmentId = "SHIP-" + UUID.randomUUID().toString().take(6).uppercase(),
                distributorId = distributorId,
                distributorName = distributorName,
                sourceLocation = sourceLocation,
                destinationLocation = destinationLocation,
                vehicleNumber = vehicleNumber,
                transportType = transportType,
                temperatureRange = temperatureRange,
                dispatchDate = dispatchDate,
                expectedDeliveryDate = expectedDate,
                actualDeliveryDate = null,
                status = "IN_TRANSIT",
                trackingNumber = resolvedTracking,
                txHash = null
            )

            val result = createShipmentUseCase(shipment, product.id)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    loadProducts()
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, error = err.localizedMessage) }
                }
            )
        }
    }

    fun markDelivered(shipmentId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val result = updateShipmentStatusUseCase(shipmentId, "DELIVERED", today)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    loadProducts()
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, error = err.localizedMessage) }
                }
            )
        }
    }
}
