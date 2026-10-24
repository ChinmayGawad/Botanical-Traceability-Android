package com.florachain.domain.usecase

import com.florachain.domain.model.NetworkStats
import com.florachain.domain.repository.BlockchainRepository

class GetBlockchainStatsUseCase(
    private val blockchainRepository: BlockchainRepository
) {
    suspend operator fun invoke(): Result<NetworkStats> {
        return blockchainRepository.getNetworkStats()
    }
}
