package com.florachain.data.mapper

import com.florachain.data.remote.dto.UserDto
import com.florachain.domain.model.AuthUser
import com.florachain.domain.model.UserRole

fun UserDto.toDomain(): AuthUser {
    return AuthUser(
        id = id,
        name = name,
        email = email,
        role = try { UserRole.valueOf(role) } catch (e: Exception) { UserRole.CONSUMER },
        organization = organization,
        location = location,
        status = status,
        joinedDate = joinedDate,
        certifications = certifications ?: emptyList(),
        avatarUrl = avatarUrl,
        walletAddress = walletAddress,
        aadhaarNumber = aadhaarNumber
    )
}
