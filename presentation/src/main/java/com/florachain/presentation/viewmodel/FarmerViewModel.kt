package com.florachain.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florachain.core.location.Coordinates
import com.florachain.core.location.LocationTracker
import com.florachain.domain.model.*
import com.florachain.domain.repository.ProductRepository
import com.florachain.domain.repository.SyncRepository
import com.florachain.domain.usecase.RegisterHarvestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class FarmerUiState(
    val products: List<BotanicalProduct> = emptyList(),
    val pendingSyncCount: Int = 0,
    val isLoading: Boolean = false,
    val isLocationCapturing: Boolean = false,
    val capturedCoordinates: Coordinates? = null,
    val registrationSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FarmerViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val syncRepository: SyncRepository,
    private val registerHarvestUseCase: RegisterHarvestUseCase,
    private val locationTracker: LocationTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(FarmerUiState())
    val uiState: StateFlow<FarmerUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
        observeSyncQueue()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            productRepository.observeAllProducts().collect { list ->
                _uiState.update { it.copy(products = list) }
            }
        }
        refreshProducts()
    }

    fun refreshProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            productRepository.getProducts()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun observeSyncQueue() {
        viewModelScope.launch {
            syncRepository.observePendingSyncCount().collect { count ->
                _uiState.update { it.copy(pendingSyncCount = count) }
            }
        }
    }

    fun captureCurrentGps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLocationCapturing = true, error = null) }
            val coords = locationTracker.getCurrentLocation()
            _uiState.update {
                it.copy(
                    isLocationCapturing = false,
                    capturedCoordinates = coords,
                    error = if (coords == null) "GPS unavailable. Ensure location permissions and GPS are enabled." else null
                )
            }
        }
    }

    fun registerHarvest(
        name: String,
        botanicalName: String,
        batchId: String,
        category: ProductCategory,
        method: CultivationMethod,
        quantityKg: Double,
        farmLocation: String,
        farmerId: String,
        farmerName: String,
        farmerOrg: String?,
        description: String? = null,
        activeCompounds: List<String> = emptyList(),
        certificates: List<Certificate> = emptyList()
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, registrationSuccess = false) }

            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val coords = _uiState.value.capturedCoordinates

            val newProduct = BotanicalProduct(
                id = "",
                batchId = batchId,
                name = name,
                botanicalName = botanicalName,
                category = category,
                cultivationMethod = method,
                quantityKg = quantityKg,
                harvestDate = currentDate,
                farmLocation = farmLocation,
                gpsCoordinates = coords?.let { GpsCoordinates(it.latitude, it.longitude) },
                farmerId = farmerId,
                farmerName = farmerName,
                farmerOrg = farmerOrg,
                status = ProductStatus.HARVESTED,
                verificationState = VerificationState.PENDING,
                qrCodeValue = "florachain:batch:$batchId",
                description = description ?: "Cultivated via ${method.name} at $farmLocation",
                imageUrl = null,
                createdTimestamp = System.currentTimeMillis().toString(),
                activeCompounds = activeCompounds,
                certificates = certificates
            )

            val result = registerHarvestUseCase(newProduct)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, registrationSuccess = true) }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, error = err.localizedMessage) }
                }
            )
        }
    }

    fun clearRegistrationStatus() {
        _uiState.update { it.copy(registrationSuccess = false, error = null) }
    }
}
