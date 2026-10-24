package com.florachain.domain.usecase

import com.florachain.domain.model.AuthUser
import com.florachain.domain.repository.AdminRepository

class GetPendingUsersUseCase(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(): Result<List<AuthUser>> {
        return adminRepository.getPendingUsers()
    }
}
