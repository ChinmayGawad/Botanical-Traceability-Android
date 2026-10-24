package com.florachain.domain.usecase

import com.florachain.domain.model.BlockchainTx
import com.florachain.domain.repository.BlockchainRepository

class GetBlockchainTransactionsUseCase(
    private val blockchainRepository: BlockchainRepository
) {
    suspend operator fun invoke(stage: String? = null): Result<List<BlockchainTx>> {
        return blockchainRepository.getAllTransactions(stage)
    }

    suspend fun getByProduct(productId: String): Result<List<BlockchainTx>> {
        if (productId.isBlank()) {
            return Result.failure(IllegalArgumentException("Product ID cannot be blank"))
        }
        return blockchainRepository.getTransactionsByProduct(productId)
    }
}
