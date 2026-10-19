package com.florachain.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florachain.domain.model.AuthUser
import com.florachain.domain.model.UserRole
import com.florachain.domain.repository.AuthRepository
import com.florachain.domain.usecase.LoginUseCase
import com.florachain.domain.usecase.ValidateAadhaarUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val currentUser: AuthUser? = null,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val loginUseCase: LoginUseCase,
    private val validateAadhaarUseCase: ValidateAadhaarUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.observeCurrentUser().collect { user ->
                _uiState.update { it.copy(currentUser = user) }
            }
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isSuccess = false) }
            val result = loginUseCase(email, pass)
            result.fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false, currentUser = user, isSuccess = true) }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, error = err.localizedMessage ?: "Login failed") }
                }
            )
        }
    }

    fun register(
        name: String,
        email: String,
        pass: String,
        role: UserRole,
        org: String?,
        location: String?,
        aadhaar: String?
    ) {
        viewModelScope.launch {
            if (!validateAadhaarUseCase(aadhaar)) {
                _uiState.update { it.copy(error = "Invalid 12-digit Aadhaar Number (Verhoeff checksum failed)") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null, isSuccess = false) }
            val result = authRepository.register(name, email, pass, role, org, location, aadhaar)
            result.fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false, currentUser = user, isSuccess = true) }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, error = err.localizedMessage ?: "Registration failed") }
                }
            )
        }
    }

    fun switchRole(newRole: UserRole) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.switchRole(newRole)
            result.fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false, currentUser = user) }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, error = err.localizedMessage) }
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { AuthUiState() }
        }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
