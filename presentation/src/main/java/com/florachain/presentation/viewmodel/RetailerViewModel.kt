package com.florachain.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florachain.domain.model.BotanicalProduct
import com.florachain.domain.model.RetailRecord
import com.florachain.domain.repository.ProductRepository
import com.florachain.domain.usecase.ReceiveRetailStockUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class RetailerUiState(
    val products: List<BotanicalProduct> = emptyList(),
    val selectedProduct: BotanicalProduct? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class RetailerViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val receiveRetailStockUseCase: ReceiveRetailStockUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RetailerUiState())
    val uiState: StateFlow<RetailerUiState> = _uiState.asStateFlow()

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

    fun intakeStock(
        retailerId: String,
        retailerName: String,
        storeLocation: String,
        shelfBatchId: String?,
        unitPrice: Double,
        notes: String?
    ) {
        val product = _uiState.value.selectedProduct ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isSuccess = false) }

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val retail = RetailRecord(
                retailerId = retailerId,
                retailerName = retailerName,
                storeLocation = storeLocation,
                receivedDate = today,
                shelfBatchId = shelfBatchId ?: ("SHELF-" + UUID.randomUUID().toString().take(6).uppercase()),
                unitPrice = unitPrice,
                qrCodeGenerated = true,
                notes = notes,
                txHash = null
            )

            val result = receiveRetailStockUseCase(product.id, retail)
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
