package com.florachain.domain.usecase

import com.florachain.domain.repository.AdminRepository

class RejectUserUseCase(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(userId: String): Result<Unit> {
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("User ID cannot be blank"))
        }
        return adminRepository.rejectUser(userId)
    }
}
