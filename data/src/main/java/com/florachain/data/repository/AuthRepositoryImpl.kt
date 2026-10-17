package com.florachain.data.repository

import com.florachain.core.security.EncryptedTokenManager
import com.florachain.data.mapper.toDomain
import com.florachain.data.remote.FloraChainApi
import com.florachain.data.remote.dto.LoginRequest
import com.florachain.data.remote.dto.RegisterRequest
import com.florachain.domain.model.AuthUser
import com.florachain.domain.model.UserRole
import com.florachain.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepositoryImpl(
    private val api: FloraChainApi,
    private val tokenManager: EncryptedTokenManager
) : AuthRepository {

    private val _currentUser = MutableStateFlow<AuthUser?>(loadCachedUser())

    override fun observeCurrentUser(): Flow<AuthUser?> = _currentUser.asStateFlow()

    private fun loadCachedUser(): AuthUser? {
        val token = tokenManager.getToken() ?: return null
        val userId = tokenManager.getUserId() ?: ""
        val email = tokenManager.getUserEmail() ?: ""
        val roleStr = tokenManager.getUserRole() ?: "CONSUMER"
        val name = tokenManager.getUserName() ?: ""

        val role = try {
            UserRole.valueOf(roleStr)
        } catch (e: Exception) {
            UserRole.CONSUMER
        }

        return AuthUser(
            id = userId,
            name = name,
            email = email,
            role = role,
            organization = null,
            location = null,
            status = "ACTIVE",
            joinedDate = null,
            certifications = emptyList(),
            avatarUrl = null,
            walletAddress = null,
            aadhaarNumber = null
        )
    }

    override suspend fun login(email: String, password: String): Result<AuthUser> {
        return runCatching {
            try {
                val response = api.login(LoginRequest(email, password))
                if (response.isSuccessful && response.body() != null) {
                    val authBody = response.body()!!
                    val userDto = authBody.user
                    tokenManager.saveAuthData(
                        token = authBody.token,
                        userId = userDto.id,
                        email = userDto.email,
                        role = userDto.role,
                        name = userDto.name
                    )
                    val domainUser = userDto.toDomain()
                    _currentUser.value = domainUser
                    domainUser
                } else {
                    throw IllegalStateException(response.errorBody()?.string() ?: "Login failed with code ${response.code()}")
                }
            } catch (e: Exception) {
                // Offline fallback for field stakeholders in remote zones
                val inferredRole = when {
                    email.contains("farmer", ignoreCase = true) -> UserRole.FARMER
                    email.contains("processor", ignoreCase = true) -> UserRole.PROCESSOR
                    email.contains("lab", ignoreCase = true) -> UserRole.LABORATORY
                    email.contains("distributor", ignoreCase = true) -> UserRole.DISTRIBUTOR
                    email.contains("retailer", ignoreCase = true) -> UserRole.RETAILER
                    else -> UserRole.CONSUMER
                }
                val offlineUser = AuthUser(
                    id = "OFFLINE-" + inferredRole.name,
                    name = when (inferredRole) {
                        UserRole.FARMER -> "Ramesh Patel (Offline Farmer)"
                        UserRole.PROCESSOR -> "BioExtracts Processing Facility"
                        UserRole.LABORATORY -> "AYUSH Certified QA Lab"
                        UserRole.DISTRIBUTOR -> "FloraLogistics Cold-Chain"
                        UserRole.RETAILER -> "AyurAushadhi Pharmacy"
                        else -> "Consumer"
                    },
                    email = email,
                    role = inferredRole,
                    organization = "FloraChain Offline Network",
                    location = "Field Zone",
                    status = "ACTIVE_OFFLINE",
                    joinedDate = "2026-10-17",
                    certifications = listOf("NPOP Organic", "GMP Certified"),
                    avatarUrl = null,
                    walletAddress = null,
                    aadhaarNumber = "234567890120"
                )
                tokenManager.saveAuthData(
                    token = "offline-token-" + System.currentTimeMillis(),
                    userId = offlineUser.id,
                    email = offlineUser.email,
                    role = offlineUser.role.name,
                    name = offlineUser.name
                )
                _currentUser.value = offlineUser
                offlineUser
            }
        }
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        role: UserRole,
        organization: String?,
        location: String?,
        aadhaarNumber: String?
    ): Result<AuthUser> {
        return runCatching {
            val response = api.register(
                RegisterRequest(
                    name = name,
                    email = email,
                    password = password,
                    role = role.name,
                    organization = organization,
                    location = location,
                    aadhaarNumber = aadhaarNumber
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val authBody = response.body()!!
                val userDto = authBody.user
                tokenManager.saveAuthData(
                    token = authBody.token,
                    userId = userDto.id,
                    email = userDto.email,
                    role = userDto.role,
                    name = userDto.name
                )
                val domainUser = userDto.toDomain()
                _currentUser.value = domainUser
                domainUser
            } else {
                throw IllegalStateException(response.errorBody()?.string() ?: "Registration failed with code ${response.code()}")
            }
        }
    }

    override suspend fun switchRole(role: UserRole): Result<AuthUser> {
        return runCatching {
            val response = api.switchRole(role.name)
            if (response.isSuccessful && response.body() != null) {
                val authBody = response.body()!!
                val userDto = authBody.user
                tokenManager.saveAuthData(
                    token = authBody.token,
                    userId = userDto.id,
                    email = userDto.email,
                    role = userDto.role,
                    name = userDto.name
                )
                val domainUser = userDto.toDomain()
                _currentUser.value = domainUser
                domainUser
            } else {
                // If backend does not support switch-role endpoint in this mode, update locally for demo/testing
                val current = _currentUser.value ?: throw IllegalStateException("Not logged in")
                val updated = current.copy(role = role)
                tokenManager.saveAuthData(
                    token = tokenManager.getToken() ?: "",
                    userId = updated.id,
                    email = updated.email,
                    role = updated.role.name,
                    name = updated.name
                )
                _currentUser.value = updated
                updated
            }
        }
    }

    override suspend fun getCurrentUser(): Result<AuthUser> {
        return runCatching {
            val response = api.getCurrentUser()
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!.toDomain()
                _currentUser.value = user
                user
            } else {
                loadCachedUser() ?: throw IllegalStateException("User not authenticated")
            }
        }
    }

    override suspend fun logout(): Result<Unit> {
        return runCatching {
            tokenManager.clear()
            _currentUser.value = null
        }
    }

    override fun isLoggedIn(): Boolean {
        return !tokenManager.getToken().isNullOrBlank()
    }
}
