package com.ml.mobilefleet.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ml.mobilefleet.data.models.AuthState
import com.ml.mobilefleet.data.models.Driver
import com.ml.mobilefleet.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for driver authentication
 */
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    companion object {
        private const val TAG = "AuthViewModel"
    }
    
    init {
        checkExistingSession()
    }
    
    /**
     * Check if there's an existing login session
     */
    private fun checkExistingSession() {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true)
            
            authRepository.getCurrentDriver()
                .onSuccess { driver ->
                    _authState.value = _authState.value.copy(
                        isAuthenticated = driver != null,
                        currentDriver = driver,
                        isLoading = false,
                        errorMessage = null
                    )
                    
                    if (driver != null) {
                        Log.d(TAG, "Existing session found for driver: ${driver.driver_id}")
                    }
                }
                .onFailure { exception ->
                    _authState.value = _authState.value.copy(
                        isAuthenticated = false,
                        currentDriver = null,
                        isLoading = false,
                        errorMessage = null // Don't show error for session check
                    )
                    Log.e(TAG, "Failed to check existing session", exception)
                }
        }
    }
    
    /**
     * Login with email and password
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
            authRepository.login(email, password)
                .onSuccess { driver ->
                    _authState.value = _authState.value.copy(
                        isAuthenticated = true,
                        currentDriver = driver,
                        isLoading = false,
                        errorMessage = null
                    )
                    Log.d(TAG, "Login successful for driver: ${driver.driver_id}")
                }
                .onFailure { exception ->
                    _authState.value = _authState.value.copy(
                        isAuthenticated = false,
                        currentDriver = null,
                        isLoading = false,
                        errorMessage = exception.message ?: "Login failed"
                    )
                    Log.e(TAG, "Login failed", exception)
                }
        }
    }
    
    /**
     * Login with driver ID and password
     */
    fun loginWithDriverId(driverId: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
            authRepository.loginWithDriverId(driverId, password)
                .onSuccess { driver ->
                    _authState.value = _authState.value.copy(
                        isAuthenticated = true,
                        currentDriver = driver,
                        isLoading = false,
                        errorMessage = null
                    )
                    Log.d(TAG, "Login successful for driver: ${driver.driver_id}")
                }
                .onFailure { exception ->
                    _authState.value = _authState.value.copy(
                        isAuthenticated = false,
                        currentDriver = null,
                        isLoading = false,
                        errorMessage = exception.message ?: "Login failed"
                    )
                    Log.e(TAG, "Login with driver ID failed", exception)
                }
        }
    }
    
    /**
     * Logout current driver
     */
    fun logout() {
        authRepository.logout()
        _authState.value = AuthState(
            isAuthenticated = false,
            currentDriver = null,
            isLoading = false,
            errorMessage = null
        )
        Log.d(TAG, "Driver logged out")
    }
    
    /**
     * Update driver profile
     */
    fun updateProfile(driver: Driver) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true)
            
            authRepository.updateDriverProfile(driver)
                .onSuccess {
                    _authState.value = _authState.value.copy(
                        currentDriver = driver,
                        isLoading = false,
                        errorMessage = null
                    )
                    Log.d(TAG, "Profile updated successfully")
                }
                .onFailure { exception ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to update profile: ${exception.message}"
                    )
                    Log.e(TAG, "Failed to update profile", exception)
                }
        }
    }
    
    /**
     * Change password
     */
    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true)
            
            authRepository.changePassword(currentPassword, newPassword)
                .onSuccess {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = null
                    )
                    Log.d(TAG, "Password changed successfully")
                }
                .onFailure { exception ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to change password"
                    )
                    Log.e(TAG, "Failed to change password", exception)
                }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _authState.value = _authState.value.copy(errorMessage = null)
    }
    
    /**
     * Get current driver
     */
    fun getCurrentDriver(): Driver? {
        return _authState.value.currentDriver
    }
    
    /**
     * Check if driver is authenticated
     */
    fun isAuthenticated(): Boolean {
        return _authState.value.isAuthenticated
    }
}
