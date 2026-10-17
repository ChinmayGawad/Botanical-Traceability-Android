package com.florachain.domain.usecase

import com.florachain.domain.model.AuthUser
import com.florachain.domain.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthUser> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank()) return Result.failure(IllegalArgumentException("Email cannot be blank"))
        if (password.isBlank()) return Result.failure(IllegalArgumentException("Password cannot be blank"))

        return authRepository.login(trimmedEmail, password)
    }
}
