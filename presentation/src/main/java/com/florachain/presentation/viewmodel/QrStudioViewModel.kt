package com.florachain.presentation.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florachain.core.qr.QrLabelGenerator
import com.florachain.domain.model.BotanicalProduct
import com.florachain.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class QrStudioUiState(
    val products: List<BotanicalProduct> = emptyList(),
    val selectedProduct: BotanicalProduct? = null,
    val labelType: QrLabelGenerator.LabelType = QrLabelGenerator.LabelType.CARTON,
    val generatedBitmap: Bitmap? = null,
    val verificationUrl: String = "",
    val isCopied: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class QrStudioViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QrStudioUiState())
    val uiState: StateFlow<QrStudioUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            productRepository.observeAllProducts().collect { list ->
                val current = _uiState.value.selectedProduct ?: list.firstOrNull()
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        products = list,
                        selectedProduct = current
                    )
                }
                current?.let { regenerateLabel(it, _uiState.value.labelType) }
            }
        }
    }

    fun selectProduct(productId: String) {
        val prod = _uiState.value.products.find { it.id == productId || it.batchId == productId }
        if (prod != null) {
            _uiState.update { it.copy(selectedProduct = prod) }
            regenerateLabel(prod, _uiState.value.labelType)
        }
    }

    fun setLabelType(type: QrLabelGenerator.LabelType) {
        _uiState.update { it.copy(labelType = type) }
        _uiState.value.selectedProduct?.let { regenerateLabel(it, type) }
    }

    private fun regenerateLabel(product: BotanicalProduct, type: QrLabelGenerator.LabelType) {
        val url = "https://blockchain-botanical-traceability.vercel.app/verify/${product.batchId}"
        val bmp = QrLabelGenerator.generatePrintableLabel(
            batchId = product.batchId,
            productName = product.name,
            botanicalName = product.botanicalName,
            category = product.category.name,
            verificationUrl = url,
            labelType = type
        )
        _uiState.update { it.copy(generatedBitmap = bmp, verificationUrl = url) }
    }

    fun printCurrentLabel(context: Context) {
        val bmp = _uiState.value.generatedBitmap ?: return
        val batch = _uiState.value.selectedProduct?.batchId ?: "FloraChain"
        QrLabelGenerator.printLabel(context, "FloraChain_Label_$batch", bmp)
    }

    fun setCopied(copied: Boolean) {
        _uiState.update { it.copy(isCopied = copied) }
    }
}
