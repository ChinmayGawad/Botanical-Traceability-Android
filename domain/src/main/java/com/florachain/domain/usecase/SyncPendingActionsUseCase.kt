package com.florachain.domain.usecase

import com.florachain.domain.repository.SyncRepository

class SyncPendingActionsUseCase(
    private val syncRepository: SyncRepository
) {
    suspend operator fun invoke(): Result<Int> {
        return syncRepository.processSyncQueue()
    }
}
