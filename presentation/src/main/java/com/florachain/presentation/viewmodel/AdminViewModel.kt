package com.florachain.presentation.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florachain.domain.model.AuthUser
import com.florachain.domain.model.NetworkStats
import com.florachain.domain.model.ReportStatus
import com.florachain.domain.model.SuspiciousReport
import com.florachain.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class AdminUiState(
    val pendingUsers: List<AuthUser> = emptyList(),
    val reports: List<SuspiciousReport> = emptyList(),
    val networkStats: NetworkStats? = null,
    val selectedReportFilter: ReportStatus? = null,
    val isLoading: Boolean = false,
    val approvedUserId: String? = null,
    val feedbackMessage: String? = null,
    val error: String? = null
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val getPendingUsersUseCase: GetPendingUsersUseCase,
    private val approveUserUseCase: ApproveUserUseCase,
    private val rejectUserUseCase: RejectUserUseCase,
    private val getSuspiciousReportsUseCase: GetSuspiciousReportsUseCase,
    private val updateReportStatusUseCase: UpdateReportStatusUseCase,
    private val getBlockchainStatsUseCase: GetBlockchainStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 1. Fetch pending users
            getPendingUsersUseCase().onSuccess { users ->
                _uiState.update { it.copy(pendingUsers = users) }
            }.onFailure { err ->
                _uiState.update { it.copy(error = err.message) }
            }

            // 2. Fetch fraud reports
            getSuspiciousReportsUseCase(_uiState.value.selectedReportFilter).onSuccess { reports ->
                _uiState.update { it.copy(reports = reports) }
            }

            // 3. Fetch network stats
            getBlockchainStatsUseCase().onSuccess { stats ->
                _uiState.update { it.copy(networkStats = stats) }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun approveUser(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, approvedUserId = null) }
            approveUserUseCase(userId).fold(
                onSuccess = { approved ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            approvedUserId = userId,
                            pendingUsers = state.pendingUsers.filter { it.id != userId },
                            feedbackMessage = "Node approved: ${approved.name} (${approved.role})"
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, error = err.message) }
                }
            )
        }
    }

    fun rejectUser(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            rejectUserUseCase(userId).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            pendingUsers = state.pendingUsers.filter { it.id != userId },
                            feedbackMessage = "Node registration rejected"
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, error = err.message) }
                }
            )
        }
    }

    fun filterReports(status: ReportStatus?) {
        _uiState.update { it.copy(selectedReportFilter = status) }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getSuspiciousReportsUseCase(status).fold(
                onSuccess = { reports ->
                    _uiState.update { it.copy(isLoading = false, reports = reports) }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, error = err.message) }
                }
            )
        }
    }

    fun updateReportStatus(reportId: String, status: ReportStatus, notes: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            updateReportStatusUseCase(reportId, status, notes).fold(
                onSuccess = { updated ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            reports = state.reports.map { if (it.id == reportId) updated else it },
                            feedbackMessage = "Incident report #${updated.id} status updated to ${updated.status}"
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, error = err.message) }
                }
            )
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(feedbackMessage = null, error = null, approvedUserId = null) }
    }
}
