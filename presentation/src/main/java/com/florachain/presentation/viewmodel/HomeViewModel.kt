package com.florachain.presentation.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florachain.domain.model.BotanicalProduct
import com.florachain.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class HomeUiState(
    val products: List<BotanicalProduct> = emptyList(),
    val featuredProduct: BotanicalProduct? = null,
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val blockHeight: Long = 1048291L,
    val activeNodes: Int = 5,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            productRepository.observeAllProducts().collect { list ->
                val featured = list.find { it.id == "BOT-2024-8901" || it.batchId == "ASH-2024-089" }
                    ?: list.firstOrNull()
                _uiState.update { it.copy(products = list, featuredProduct = featured) }
            }
        }
        refresh()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = productRepository.getProducts()
            result.fold(
                onSuccess = { list ->
                    val featured = list.find { it.id == "BOT-2024-8901" || it.batchId == "ASH-2024-089" }
                        ?: list.firstOrNull()
                    _uiState.update { it.copy(isLoading = false, products = list, featuredProduct = featured) }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, error = err.localizedMessage) }
                }
            )
        }
    }
}
