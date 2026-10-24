package com.florachain.domain.usecase

import com.florachain.domain.model.AuthUser
import com.florachain.domain.repository.AdminRepository

class ApproveUserUseCase(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(userId: String): Result<AuthUser> {
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("User ID cannot be blank"))
        }
        return adminRepository.approveUser(userId)
    }
}
