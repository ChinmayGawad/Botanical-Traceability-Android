package com.florachain.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florachain.domain.model.BotanicalProduct
import com.florachain.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface BatchDetailsUiState {
    data object Loading : BatchDetailsUiState
    data class Success(val product: BotanicalProduct) : BatchDetailsUiState
    data class Error(val message: String) : BatchDetailsUiState
}

@HiltViewModel
class BatchDetailsViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val batchId: String? = savedStateHandle["batchId"]

    private val _uiState = MutableStateFlow<BatchDetailsUiState>(BatchDetailsUiState.Loading)
    val uiState: StateFlow<BatchDetailsUiState> = _uiState.asStateFlow()

    init {
        loadProduct()
    }

    fun loadProduct(overrideId: String? = null) {
        val targetId = overrideId ?: batchId
        if (targetId.isNullOrBlank()) {
            _uiState.value = BatchDetailsUiState.Error("Invalid or missing Batch ID")
            return
        }

        viewModelScope.launch {
            _uiState.value = BatchDetailsUiState.Loading
            productRepository.getProductByIdOrBatch(targetId).fold(
                onSuccess = { product ->
                    _uiState.value = BatchDetailsUiState.Success(product)
                },
                onFailure = { error ->
                    _uiState.value = BatchDetailsUiState.Error(
                        error.localizedMessage ?: "Failed to find botanical batch '$targetId'"
                    )
                }
            )
        }
    }
}

