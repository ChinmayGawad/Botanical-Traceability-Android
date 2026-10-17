package com.florachain.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florachain.domain.model.VerificationResult
import com.florachain.domain.usecase.VerifyProductUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VerifyUiState(
    val isScanning: Boolean = true,
    val isLoading: Boolean = false,
    val scannedIdentifier: String = "",
    val result: VerificationResult? = null,
    val error: String? = null
)

@HiltViewModel
class VerifyViewModel @Inject constructor(
    private val verifyProductUseCase: VerifyProductUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VerifyUiState())
    val uiState: StateFlow<VerifyUiState> = _uiState.asStateFlow()

    fun onQrCodeScanned(code: String) {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isScanning = false, scannedIdentifier = code) }
        verifyBatch(code)
    }

    fun verifyBatch(identifier: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, scannedIdentifier = identifier) }
            val result = verifyProductUseCase(identifier)
            result.fold(
                onSuccess = { verification ->
                    _uiState.update { it.copy(isLoading = false, result = verification) }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, error = err.localizedMessage ?: "Verification failed") }
                }
            )
        }
    }

    fun resetScanner() {
        _uiState.update { VerifyUiState(isScanning = true) }
    }
}
