package com.florachain.presentation.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florachain.domain.model.BlockchainTx
import com.florachain.domain.model.NetworkStats
import com.florachain.domain.usecase.GetBlockchainStatsUseCase
import com.florachain.domain.usecase.GetBlockchainTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class BlockchainExplorerUiState(
    val stats: NetworkStats? = null,
    val transactions: List<BlockchainTx> = emptyList(),
    val filteredTransactions: List<BlockchainTx> = emptyList(),
    val selectedStage: String = "ALL",
    val searchQuery: String = "",
    val selectedTransaction: BlockchainTx? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class BlockchainExplorerViewModel @Inject constructor(
    private val getBlockchainStatsUseCase: GetBlockchainStatsUseCase,
    private val getBlockchainTransactionsUseCase: GetBlockchainTransactionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BlockchainExplorerUiState())
    val uiState: StateFlow<BlockchainExplorerUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getBlockchainStatsUseCase().onSuccess { stats ->
                _uiState.update { it.copy(stats = stats) }
            }

            getBlockchainTransactionsUseCase().fold(
                onSuccess = { txs ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            transactions = txs,
                            filteredTransactions = filterList(txs, state.selectedStage, state.searchQuery)
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, error = err.message) }
                }
            )
        }
    }

    fun onStageFilterSelected(stage: String) {
        _uiState.update { state ->
            val updated = state.copy(selectedStage = stage)
            updated.copy(filteredTransactions = filterList(state.transactions, stage, state.searchQuery))
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            val updated = state.copy(searchQuery = query)
            updated.copy(filteredTransactions = filterList(state.transactions, state.selectedStage, query))
        }
    }

    fun selectTransaction(tx: BlockchainTx?) {
        _uiState.update { it.copy(selectedTransaction = tx) }
    }

    private fun filterList(list: List<BlockchainTx>, stage: String, query: String): List<BlockchainTx> {
        return list.filter { tx ->
            val matchesStage = stage == "ALL" || tx.stage.equals(stage, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                    tx.txId.contains(query, ignoreCase = true) ||
                    tx.actor.contains(query, ignoreCase = true) ||
                    tx.action.contains(query, ignoreCase = true) ||
                    tx.blockNumber.toString().contains(query)
            matchesStage && matchesQuery
        }
    }
}
