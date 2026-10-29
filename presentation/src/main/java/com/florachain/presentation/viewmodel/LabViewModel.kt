package com.florachain.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florachain.domain.model.*
import com.florachain.domain.repository.ProductRepository
import com.florachain.domain.usecase.SubmitLabReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class LabUiState(
    val products: List<BotanicalProduct> = emptyList(),
    val selectedProduct: BotanicalProduct? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LabViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val submitLabReportUseCase: SubmitLabReportUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LabUiState())
    val uiState: StateFlow<LabUiState> = _uiState.asStateFlow()

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

    fun submitAssay(
        labId: String,
        labName: String,
        testedBy: String?,
        purity: Double,
        moisture: Double,
        heavyMetals: TestStatus,
        microbial: TestStatus,
        pesticide: TestStatus,
        isApproved: Boolean,
        notes: String?,
        certificateIpfsCid: String? = null
    ) {
        val product = _uiState.value.selectedProduct ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isSuccess = false) }

            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val resolvedCid = certificateIpfsCid?.ifBlank { null } ?: ("QmTestCoA" + UUID.randomUUID().toString().take(8))

            val report = LabReport(
                labId = labId,
                labName = labName,
                testDate = currentDate,
                testedBy = testedBy,
                purityPercentage = purity,
                moisturePercentage = moisture,
                heavyMetalsStatus = heavyMetals,
                microbialTestStatus = microbial,
                pesticideResidueStatus = pesticide,
                parameters = listOf(
                    LabTestParameter("HPLC Purity Assay", "$purity%", "%", ">= 95.0%", purity >= 95.0),
                    LabTestParameter("Loss on Drying (Moisture)", "$moisture%", "%", "<= 8.0%", moisture <= 8.0)
                ),
                certificateIpfsCid = resolvedCid,
                certificateUrl = "https://gateway.pinata.cloud/ipfs/$resolvedCid",
                overallResult = if (isApproved) "APPROVED" else "REJECTED",
                notes = notes,
                txHash = null
            )

            val result = submitLabReportUseCase(product.id, report)
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
