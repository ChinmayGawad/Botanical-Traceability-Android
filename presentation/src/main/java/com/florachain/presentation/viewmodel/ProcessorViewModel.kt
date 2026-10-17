package com.florachain.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florachain.domain.model.BotanicalProduct
import com.florachain.domain.model.ProcessingRecord
import com.florachain.domain.repository.ProductRepository
import com.florachain.domain.usecase.SubmitProcessingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class ProcessorUiState(
    val products: List<BotanicalProduct> = emptyList(),
    val selectedProduct: BotanicalProduct? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProcessorViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val submitProcessingUseCase: SubmitProcessingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProcessorUiState())
    val uiState: StateFlow<ProcessorUiState> = _uiState.asStateFlow()

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

    fun submitProcessing(
        processorId: String,
        processorName: String,
        method: String,
        facilityLocation: String,
        initialQty: Double,
        processedQty: Double,
        notes: String?
    ) {
        val product = _uiState.value.selectedProduct ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isSuccess = false) }

            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val yieldLoss = if (initialQty > 0) ((initialQty - processedQty) / initialQty) * 100 else 0.0

            val record = ProcessingRecord(
                processorId = processorId,
                processorName = processorName,
                processingDate = currentDate,
                method = method,
                facilityLocation = facilityLocation,
                initialQuantityKg = initialQty,
                processedQuantityKg = processedQty,
                yieldLossPercentage = yieldLoss,
                equipmentUsed = listOf("Supercritical CO2 Extractor", "Rotary Evaporator"),
                ipfsDocumentCid = null,
                notes = notes,
                txHash = null
            )

            val result = submitProcessingUseCase(product.id, record)
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
