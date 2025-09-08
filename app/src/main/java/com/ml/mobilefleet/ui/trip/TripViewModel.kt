package com.ml.mobilefleet.ui.trip

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ml.mobilefleet.data.models.Terminal
import com.ml.mobilefleet.data.models.Trip
import com.ml.mobilefleet.data.repository.AuthRepository
import com.ml.mobilefleet.data.repository.FirebaseRepository
import com.ml.mobilefleet.services.TextToSpeechService
import com.ml.mobilefleet.services.HapticFeedbackService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job

/**
 * ViewModel for managing trip operations with role-based access
 */
class TripViewModel(
    private val repository: FirebaseRepository = FirebaseRepository(),
    private val authRepository: AuthRepository,
    private var textToSpeechService: TextToSpeechService? = null,
    private val hapticFeedbackService: HapticFeedbackService? = null
) : ViewModel() {

    companion object {
        private const val TAG = "TripViewModel"
    }
    
    // UI State
    private val _uiState = MutableStateFlow(TripUiState())
    val uiState: StateFlow<TripUiState> = _uiState.asStateFlow()

    // Available terminals
    private val _terminals = MutableStateFlow<List<Terminal>>(emptyList())
    val terminals: StateFlow<List<Terminal>> = _terminals.asStateFlow()

    // Current active trip
    private val _currentTrip = MutableStateFlow<Trip?>(null)
    val currentTrip: StateFlow<Trip?> = _currentTrip.asStateFlow()

    // Trip history
    private val _tripHistory = MutableStateFlow<List<Trip>>(emptyList())
    val tripHistory: StateFlow<List<Trip>> = _tripHistory.asStateFlow()

    /**
     * Get current authenticated driver ID
     */
    private suspend fun getCurrentDriverId(): String? {
        val driver = authRepository.getCurrentDriver().getOrNull()
        return driver?.driver_id
    }

    /**
     * Initialize user-specific data after authentication
     */
    fun initializeUserData() {
        viewModelScope.launch {
            val driverId = getCurrentDriverId()
            if (driverId != null) {
                loadCurrentTrip()
                loadTripHistory()
                startTripStatusMonitoring()
            }
        }
    }

    // Terminal names for trip history display
    private val _terminalNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val terminalNames: StateFlow<Map<String, String>> = _terminalNames.asStateFlow()

    // Trip monitoring job for state persistence
    private var tripMonitoringJob: Job? = null
    
    init {
        loadTerminals()
        // Load user-specific data only when authenticated
        viewModelScope.launch {
            val driverId = getCurrentDriverId()
            if (driverId != null) {
                loadCurrentTrip()
                loadTripHistory()
                startTripStatusMonitoring()
            }
        }
    }

    /**
     * Set the TextToSpeech service for announcements
     */
    fun setTextToSpeechService(ttsService: TextToSpeechService) {
        textToSpeechService = ttsService
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
     * Load current active trip for the authenticated driver
     */
    private fun loadCurrentTrip() {
        viewModelScope.launch {
            val driverId = getCurrentDriverId()
            if (driverId == null) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "No authenticated driver found"
                )
                return@launch
            }

            repository.getCurrentTripForDriver(driverId)
                .onSuccess { trip ->
                    _currentTrip.value = trip
                    // Load terminal names for current trip if it exists
                    trip?.let { loadTerminalNamesForTrips(listOf(it)) }
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Failed to load current trip: ${exception.message}"
                    )
                }
        }
    }

    /**
     * Load trip history for the authenticated driver
     */
    private fun loadTripHistory() {
        viewModelScope.launch {
            val driverId = getCurrentDriverId()
            if (driverId == null) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "No authenticated driver found"
                )
                return@launch
            }

            repository.getCompletedTripsForDriver(driverId)
                .onSuccess { trips ->
                    _tripHistory.value = trips
                    // Load terminal names for the trips
                    loadTerminalNamesForTrips(trips)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Failed to load trip history: ${exception.message}"
                    )
                }
        }
    }

    /**
     * Refresh trip history (public method for manual refresh)
     */
    fun refreshTripHistory() {
        loadTripHistory()
    }

    /**
     * Load terminal names for trip history display
     */
    private fun loadTerminalNamesForTrips(trips: List<Trip>) {
        viewModelScope.launch {
            // Collect all unique terminal IDs from trips
            val terminalIds = mutableSetOf<String>()
            trips.forEach { trip ->
                terminalIds.add(trip.start_terminal)
                terminalIds.add(trip.destination_terminal)
            }

            if (terminalIds.isNotEmpty()) {
                repository.getTerminalsByIds(terminalIds.toList())
                    .onSuccess { terminalMap ->
                        // Convert Terminal objects to name map
                        val nameMap = terminalMap.mapValues { (_, terminal) -> terminal.name }
                        _terminalNames.value = nameMap
                    }
                    .onFailure { exception ->
                        Log.e("TripViewModel", "Failed to load terminal names", exception)
                    }
            }
        }
    }

    /**
     * Start monitoring trip status for state persistence
     */
    private fun startTripStatusMonitoring() {
        tripMonitoringJob?.cancel()
        tripMonitoringJob = viewModelScope.launch {
            while (true) {
                delay(30000) // Check every 30 seconds
                loadCurrentTrip() // Refresh trip status
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
                        // Trigger haptic feedback for successful scan
                        hapticFeedbackService?.qrScanSuccess()

                        // Trigger TTS announcement
                        textToSpeechService?.announceQrScanSuccess(terminal.name)

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
     * Start a new trip for the authenticated driver
     */
    fun startTrip(destinationTerminalId: String, passengers: Int) {
        val startTerminal = _uiState.value.startTerminal
        if (startTerminal == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Start terminal not selected")
            return
        }

        // Find destination terminal name
        val destinationTerminal = _terminals.value.find { it.id == destinationTerminalId }
        val destinationTerminalName = destinationTerminal?.name ?: destinationTerminalId

        viewModelScope.launch {
            val driverId = getCurrentDriverId()
            if (driverId == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "No authenticated driver found"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true)

            repository.startTrip(
                driverId = driverId,
                startTerminalId = startTerminal.id,
                destinationTerminalId = destinationTerminalId,
                passengers = passengers
            )
                .onSuccess { tripId ->
                    // Trigger haptic feedback
                    hapticFeedbackService?.tripStartSuccess()

                    // Trigger TTS announcement
                    textToSpeechService?.announceTripStart(
                        startTerminalName = startTerminal.name,
                        destinationTerminalName = destinationTerminalName,
                        passengerCount = passengers
                    )

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        tripStarted = true,
                        currentTripId = tripId,
                        showTripStartModal = true,
                        tripStartTime = System.currentTimeMillis(),
                        destinationTerminalName = destinationTerminalName
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
                            completeTrip(currentTrip.id, terminal.id, terminal.name)
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
                    // Trigger haptic feedback
                    hapticFeedbackService?.passengerCountChange()

                    // Trigger TTS announcement
                    textToSpeechService?.announcePassengerUpdate(newPassengerCount)

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
     * Dismiss trip start modal
     */
    fun dismissTripStartModal() {
        _uiState.value = _uiState.value.copy(showTripStartModal = false)
    }

    /**
     * Dismiss trip completion modal
     */
    fun dismissTripCompletionModal() {
        _uiState.value = _uiState.value.copy(
            showTripCompletionModal = false,
            tripCompleted = false,
            tripStarted = false, // Reset trip started state
            currentTripId = null, // Clear trip ID
            startTerminal = null, // Clear start terminal
            showPassengerInput = false // Reset passenger input
        )
        // Ensure current trip is cleared and state is fresh
        _currentTrip.value = null
        loadCurrentTrip() // Reload to ensure clean state
    }

    /**
     * Complete the current trip
     * Firebase automatically syncs with web dashboard in real-time
     */
    private fun completeTrip(tripId: String, arrivalTerminalId: String, terminalName: String) {
        val currentTrip = _currentTrip.value
        viewModelScope.launch {
            repository.completeTrip(tripId, arrivalTerminalId)
                .onSuccess {
                    // Trigger haptic feedback
                    hapticFeedbackService?.tripCompletionSuccess()

                    // Trigger TTS announcement
                    if (currentTrip != null) {
                        textToSpeechService?.announceTripCompletion(
                            destinationTerminalName = terminalName,
                            passengerCount = currentTrip.passengers
                        )
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        tripCompleted = true,
                        showTripCompletionModal = true,
                        destinationTerminalName = terminalName
                    )
                    // Clear current trip and reload history
                    _currentTrip.value = null
                    loadTripHistory() // Refresh trip history to include the newly completed trip
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
        _currentTrip.value = null // Explicitly clear current trip
        loadCurrentTrip() // Reload to ensure state is fresh
        loadTripHistory() // Refresh history
    }

    /**
     * Manually refresh trip status for state persistence
     */
    fun refreshTripStatus() {
        loadCurrentTrip()
        loadTripHistory()
    }

    /**
     * Delete a trip from history
     */
    fun deleteTrip(tripId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            repository.deleteTrip(tripId)
                .onSuccess {
                    // Remove trip from local state
                    _tripHistory.value = _tripHistory.value.filter { it.id != tripId }
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to delete trip: ${exception.message}"
                    )
                }
        }
    }

    /**
     * Cleanup resources when ViewModel is destroyed
     */
    override fun onCleared() {
        super.onCleared()
        tripMonitoringJob?.cancel()
        textToSpeechService?.shutdown()
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
    val currentTripId: String? = null,
    // Modal states
    val showTripStartModal: Boolean = false,
    val showTripCompletionModal: Boolean = false,
    val tripStartTime: Long? = null,
    val destinationTerminalName: String? = null
)
