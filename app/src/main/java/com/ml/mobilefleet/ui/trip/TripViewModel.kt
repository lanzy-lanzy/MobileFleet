package com.ml.mobilefleet.ui.trip

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ml.mobilefleet.data.models.Terminal
import com.ml.mobilefleet.data.models.Trip
import com.ml.mobilefleet.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing trip operations
 */
class TripViewModel(
    private val repository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {
    
    // Current driver ID (hardcoded as per requirements)
    private val currentDriverId = "DRV001"
    
    // UI State
    private val _uiState = MutableStateFlow(TripUiState())
    val uiState: StateFlow<TripUiState> = _uiState.asStateFlow()
    
    // Available terminals
    private val _terminals = MutableStateFlow<List<Terminal>>(emptyList())
    val terminals: StateFlow<List<Terminal>> = _terminals.asStateFlow()
    
    // Current active trip
    private val _currentTrip = MutableStateFlow<Trip?>(null)
    val currentTrip: StateFlow<Trip?> = _currentTrip.asStateFlow()
    
    init {
        loadTerminals()
        loadCurrentTrip()
    }
    
    /**
     * Load all terminals from Firebase
     */
    private fun loadTerminals() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            repository.getAllTerminals()
                .onSuccess { terminalList ->
                    _terminals.value = terminalList
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to load terminals: ${exception.message}"
                    )
                }
        }
    }
    
    /**
     * Load current active trip for the driver
     */
    private fun loadCurrentTrip() {
        viewModelScope.launch {
            repository.getCurrentTripForDriver(currentDriverId)
                .onSuccess { trip ->
                    _currentTrip.value = trip
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Failed to load current trip: ${exception.message}"
                    )
                }
        }
    }
    
    /**
     * Handle QR code scan for start terminal
     * Firebase provides real-time sync with web dashboard automatically
     */
    fun onStartTerminalScanned(qrCode: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            repository.getTerminalByQrCode(qrCode)
                .onSuccess { terminal ->
                    if (terminal != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            startTerminal = terminal,
                            showPassengerInput = true
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Terminal not found for QR code: $qrCode"
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to find terminal: ${exception.message}"
                    )
                }
        }
    }
    
    /**
     * Start a new trip
     */
    fun startTrip(destinationTerminalId: String, passengers: Int) {
        val startTerminal = _uiState.value.startTerminal
        if (startTerminal == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Start terminal not selected")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            repository.startTrip(
                driverId = currentDriverId,
                startTerminalId = startTerminal.id,
                destinationTerminalId = destinationTerminalId,
                passengers = passengers
            )
                .onSuccess { tripId ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        tripStarted = true,
                        currentTripId = tripId
                    )
                    loadCurrentTrip() // Reload current trip
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to start trip: ${exception.message}"
                    )
                }
        }
    }
    
    /**
     * Handle QR code scan for destination terminal
     */
    fun onDestinationTerminalScanned(qrCode: String) {
        val currentTrip = _currentTrip.value
        if (currentTrip == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "No active trip found")
            return
        }



        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            repository.getTerminalByQrCode(qrCode)
                .onSuccess { terminal ->
                    if (terminal != null) {
                        if (terminal.id == currentTrip.destination_terminal) {
                            // Correct destination - complete the trip
                            completeTrip(currentTrip.id, terminal.id)
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = "This is not your assigned destination. Expected: ${currentTrip.destination_terminal}, Scanned: ${terminal.id}"
                            )
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Terminal not found for QR code: $qrCode"
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to find terminal: ${exception.message}"
                    )
                }
        }
    }
    
    /**
     * Update passenger count during trip
     * Firebase automatically syncs with web dashboard in real-time
     */
    fun updatePassengerCount(newPassengerCount: Int) {
        val currentTrip = _currentTrip.value
        if (currentTrip == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "No active trip found")
            return
        }

        viewModelScope.launch {
            repository.updateTripPassengers(currentTrip.id, newPassengerCount)
                .onSuccess {
                    // Update local state - Firebase handles real-time sync automatically
                    _currentTrip.value = currentTrip.copy(passengers = newPassengerCount)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Failed to update passenger count: ${exception.message}"
                    )
                }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Complete the current trip
     * Firebase automatically syncs with web dashboard in real-time
     */
    private fun completeTrip(tripId: String, arrivalTerminalId: String) {
        viewModelScope.launch {
            repository.completeTrip(tripId, arrivalTerminalId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        tripCompleted = true
                    )
                    _currentTrip.value = null
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to complete trip: ${exception.message}"
                    )
                }
        }
    }

    /**
     * Reset trip state
     */
    fun resetTripState() {
        _uiState.value = TripUiState()
        loadCurrentTrip()
    }
}

/**
 * UI State for Trip operations
 */
data class TripUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val startTerminal: Terminal? = null,
    val showPassengerInput: Boolean = false,
    val tripStarted: Boolean = false,
    val tripCompleted: Boolean = false,
    val currentTripId: String? = null
)
