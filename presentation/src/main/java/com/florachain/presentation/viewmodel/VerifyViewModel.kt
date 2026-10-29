package com.florachain.presentation.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florachain.domain.model.ReportStatus
import com.florachain.domain.model.SuspiciousReport
import com.florachain.domain.model.VerificationResult
import com.florachain.domain.usecase.SubmitSuspiciousReportUseCase
import com.florachain.domain.usecase.VerifyProductUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@Immutable
data class VerifyUiState(
    val isScanning: Boolean = true,
    val isLoading: Boolean = false,
    val scannedIdentifier: String = "",
    val result: VerificationResult? = null,
    val error: String? = null,
    val navigationEvent: String? = null,
    val reportSubmitted: Boolean = false
)

@HiltViewModel
class VerifyViewModel @Inject constructor(
    private val verifyProductUseCase: VerifyProductUseCase,
    private val submitSuspiciousReportUseCase: SubmitSuspiciousReportUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VerifyUiState())
    val uiState: StateFlow<VerifyUiState> = _uiState.asStateFlow()

    fun onQrCodeScanned(code: String) {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isScanning = false, scannedIdentifier = code) }
        verifyBatch(code)
    }

    fun verifyBatch(identifier: String) {
        val cleanId = verifyProductUseCase.extractBatchId(identifier)
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, scannedIdentifier = cleanId) }
            val result = verifyProductUseCase(cleanId)
            result.fold(
                onSuccess = { verification ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            result = verification,
                            navigationEvent = verification.product?.batchId ?: cleanId
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, error = err.localizedMessage ?: "Verification failed") }
                }
            )
        }
    }

    fun submitReport(
        batchId: String,
        issueType: String,
        description: String,
        reporterName: String,
        reporterContact: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val report = SuspiciousReport(
                id = "REP-" + UUID.randomUUID().toString().take(8).uppercase(),
                batchId = batchId,
                reporterName = reporterName.ifBlank { "Consumer Verifier" },
                reporterContact = reporterContact.takeIf { it.isNotBlank() },
                issueType = issueType,
                description = description,
                evidencePhotoUrl = null,
                status = ReportStatus.PENDING_REVIEW,
                adminNotes = null,
                createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())
            )
            submitSuspiciousReportUseCase(report).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, reportSubmitted = true) }
                    onSuccess()
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false) }
                    onError(err.localizedMessage ?: "Failed to submit fraud report")
                }
            )
        }
    }

    fun onNavigationHandled() {
        _uiState.update { it.copy(navigationEvent = null, isScanning = true) }
    }

    fun resetScanner() {
        _uiState.update { VerifyUiState(isScanning = true) }
    }
}
