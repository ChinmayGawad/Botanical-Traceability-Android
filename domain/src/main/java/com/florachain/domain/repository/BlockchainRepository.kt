package com.florachain.domain.repository

import com.florachain.domain.model.BlockchainTx
import com.florachain.domain.model.NetworkStats
import kotlinx.coroutines.flow.Flow

interface BlockchainRepository {
    fun observeNetworkStats(): Flow<NetworkStats>
    suspend fun getNetworkStats(): Result<NetworkStats>
    suspend fun getAllTransactions(stage: String? = null): Result<List<BlockchainTx>>
    suspend fun getTransactionsByProduct(productId: String): Result<List<BlockchainTx>>
}
