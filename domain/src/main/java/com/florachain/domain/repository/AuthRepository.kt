package com.florachain.domain.repository

import com.florachain.domain.model.AuthUser
import com.florachain.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeCurrentUser(): Flow<AuthUser?>
    suspend fun login(email: String, password: String): Result<AuthUser>
    suspend fun register(
        name: String,
        email: String,
        password: String,
        role: UserRole,
        organization: String?,
        location: String?,
        aadhaarNumber: String?
    ): Result<AuthUser>
    suspend fun switchRole(role: UserRole): Result<AuthUser>
    suspend fun getCurrentUser(): Result<AuthUser>
    suspend fun logout(): Result<Unit>
    fun isLoggedIn(): Boolean
}
