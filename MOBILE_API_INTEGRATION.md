# 📱 Mobile Fleet Monitoring - API Integration Guide

## 🎯 Overview

This document provides complete API integration instructions for connecting a **Kotlin Android mobile app** with the **Django Web Dashboard**. Both systems sync data through **Firebase Firestore** in real-time.

## 🏗️ System Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Kotlin Mobile │◄──►│ Django REST API │◄──►│ Firebase Store  │
│      App        │    │   (Backend)     │    │   (Database)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                        │                        │
         │                        │                        │
         └────────────────────────┼────────────────────────┘
                                  │
                            ┌─────────────────┐
                            │ Web Dashboard   │
                            │   (Frontend)    │
                            └─────────────────┘
```

## 🚀 API Base Configuration

### Base URL
```
Production: https://your-domain.com/api/
Development: http://localhost:8000/api/
```

### Headers Required
```json
{
  "Content-Type": "application/json",
  "Accept": "application/json"
}
```

## 📋 Complete API Endpoints

### 1. QR Code Scanning
**Endpoint:** `POST /api/scan-qr/`

**Purpose:** Validate terminal QR codes and get terminal information

**Request:**
```json
{
  "qr_code": "terminal_id:PjYqQNctdioMF2OvrbiN"
}
```

**Response (Success):**
```json
{
  "success": true,
  "terminal": {
    "terminal_id": "PjYqQNctdioMF2OvrbiN",
    "name": "Dipolog Terminal",
    "latitude": 8.589,
    "longitude": 123.3456,
    "is_active": true
  }
}
```

### 2. Start Trip
**Endpoint:** `POST /api/trips/start/`

**Purpose:** Create new trip when driver scans start terminal QR

**Request:**
```json
{
  "driver_id": "vWCHpexWfZNTj6Jf85D5",
  "start_terminal": "PjYqQNctdioMF2OvrbiN",
  "destination_terminal": "cu38ZbDlEWIiglCD5P54",
  "passengers": 15
}
```

**Response (Success):**
```json
{
  "success": true,
  "trip_id": "ABC123XYZ789",
  "trip": {
    "trip_id": "ABC123XYZ789",
    "driver_id": "vWCHpexWfZNTj6Jf85D5",
    "start_terminal": "PjYqQNctdioMF2OvrbiN",
    "destination_terminal": "cu38ZbDlEWIiglCD5P54",
    "passengers": 15,
    "status": "in_progress",
    "start_time": "2025-08-19T10:30:00Z",
    "arrival_time": null
  },
  "message": "Trip started successfully"
}
```

### 3. Update Passenger Count
**Endpoint:** `POST /api/trips/{trip_id}/passengers/`

**Purpose:** Update passenger count during trip (boarding/alighting)

**Request:**
```json
{
  "passengers": 20
}
```

**Response (Success):**
```json
{
  "success": true,
  "trip": {
    "trip_id": "ABC123XYZ789",
    "passengers": 20,
    "status": "in_progress",
    "updated_at": "2025-08-19T10:45:00Z"
  },
  "message": "Passenger count updated successfully"
}
```

### 4. Stop/Complete Trip
**Endpoint:** `POST /api/trips/{trip_id}/stop/`

**Purpose:** Complete trip when reaching destination terminal

**Request:**
```json
{
  "passengers": 18
}
```

**Response (Success):**
```json
{
  "success": true,
  "trip": {
    "trip_id": "ABC123XYZ789",
    "status": "completed",
    "passengers": 18,
    "arrival_time": "2025-08-19T11:15:00Z",
    "updated_at": "2025-08-19T11:15:00Z"
  },
  "message": "Trip completed successfully"
}
```

### 5. Get Active Trips
**Endpoint:** `GET /api/trips/active/`

**Purpose:** Get all active trips or trips for specific driver

**Query Parameters:**
- `driver_id` (optional): Filter by specific driver

**Response (Success):**
```json
{
  "success": true,
  "trips": [
    {
      "trip_id": "ABC123XYZ789",
      "driver_id": "vWCHpexWfZNTj6Jf85D5",
      "passengers": 15,
      "status": "in_progress",
      "start_time": "2025-08-19T10:30:00Z"
    }
  ],
  "count": 1
}
```

### 6. Get Trip Details
**Endpoint:** `GET /api/trips/{trip_id}/`

**Purpose:** Get detailed information about specific trip

**Response (Success):**
```json
{
  "success": true,
  "trip": {
    "trip_id": "ABC123XYZ789",
    "driver_id": "vWCHpexWfZNTj6Jf85D5",
    "passengers": 15,
    "status": "in_progress"
  },
  "driver": {
    "driver_id": "vWCHpexWfZNTj6Jf85D5",
    "name": "Juan Dela Cruz",
    "contact": "+639123456789"
  },
  "start_terminal": {
    "terminal_id": "PjYqQNctdioMF2OvrbiN",
    "name": "Dipolog Terminal"
  },
  "destination_terminal": {
    "terminal_id": "cu38ZbDlEWIiglCD5P54",
    "name": "Molave Terminal"
  }
}
```

### 7. Get Driver Information
**Endpoint:** `GET /api/drivers/{driver_id}/`

**Purpose:** Get driver details and active trips

**Response (Success):**
```json
{
  "success": true,
  "driver": {
    "driver_id": "vWCHpexWfZNTj6Jf85D5",
    "name": "Juan Dela Cruz",
    "contact": "+639123456789",
    "license_number": "D01-23-456789",
    "is_active": true
  },
  "active_trips": [
    {
      "trip_id": "ABC123XYZ789",
      "passengers": 15,
      "status": "in_progress"
    }
  ],
  "active_trip_count": 1
}
```

## 🔄 Mobile App Flow Diagram

```
┌─────────────────┐
│ 1. Driver Login │
└─────────┬───────┘
          │
┌─────────▼───────┐
│ 2. Scan Start   │
│    Terminal QR  │
└─────────┬───────┘
          │
┌─────────▼───────┐
│ 3. Select       │
│    Destination  │
└─────────┬───────┘
          │
┌─────────▼───────┐
│ 4. Enter Initial│
│    Passenger    │
│    Count        │
└─────────┬───────┘
          │
┌─────────▼───────┐
│ 5. Start Trip   │
│    (API Call)   │
└─────────┬───────┘
          │
┌─────────▼───────┐
│ 6. During Trip: │
│    Update       │
│    Passengers   │
│    (API Calls)  │
└─────────┬───────┘
          │
┌─────────▼───────┐
│ 7. Scan End     │
│    Terminal QR  │
└─────────┬───────┘
          │
┌─────────▼───────┐
│ 8. Complete     │
│    Trip         │
│    (API Call)   │
└─────────────────┘
```

## 🛠️ Error Handling

### Common Error Responses

**400 Bad Request:**
```json
{
  "error": "Invalid JSON format"
}
```

**404 Not Found:**
```json
{
  "error": "Trip not found"
}
```

**500 Internal Server Error:**
```json
{
  "error": "Internal server error"
}
```

## 📱 Kotlin Implementation Examples

### HTTP Client Setup (Retrofit)
```kotlin
interface FleetApiService {
    @POST("scan-qr/")
    suspend fun scanQrCode(@Body request: QrScanRequest): Response<QrScanResponse>
    
    @POST("trips/start/")
    suspend fun startTrip(@Body request: StartTripRequest): Response<StartTripResponse>
    
    @POST("trips/{tripId}/passengers/")
    suspend fun updatePassengers(
        @Path("tripId") tripId: String,
        @Body request: UpdatePassengersRequest
    ): Response<UpdatePassengersResponse>
    
    @POST("trips/{tripId}/stop/")
    suspend fun stopTrip(
        @Path("tripId") tripId: String,
        @Body request: StopTripRequest
    ): Response<StopTripResponse>
    
    @GET("trips/active/")
    suspend fun getActiveTrips(@Query("driver_id") driverId: String? = null): Response<ActiveTripsResponse>
}
```

### Data Classes
```kotlin
data class QrScanRequest(val qr_code: String)
data class StartTripRequest(
    val driver_id: String,
    val start_terminal: String,
    val destination_terminal: String,
    val passengers: Int
)
data class UpdatePassengersRequest(val passengers: Int)
data class StopTripRequest(val passengers: Int)
```

## 🤖 AI Assistant Instructions

When working with this mobile app integration, AI assistants should:

### 1. **API Integration Focus**
- Always use the documented endpoints above
- Handle all HTTP status codes properly
- Implement proper error handling and user feedback
- Use coroutines for network calls in Kotlin

### 2. **Data Synchronization**
- Ensure all trip operations call the appropriate API endpoints
- Implement real-time updates where possible
- Handle offline scenarios gracefully
- Validate data before sending to API

### 3. **User Experience**
- Show loading states during API calls
- Provide clear error messages to users
- Implement retry mechanisms for failed requests
- Cache data locally when appropriate

### 4. **Security Considerations**
- Validate QR codes before processing
- Implement proper authentication if required
- Handle sensitive data securely
- Use HTTPS in production

### 5. **Testing Requirements**
- Test all API endpoints thoroughly
- Implement unit tests for API calls
- Test offline/online scenarios
- Validate data synchronization with web dashboard

## 🔧 Development Setup

### 1. Start Django Server
```bash
cd MobileFleet
python manage.py runserver
```

### 2. Test API Endpoints
```bash
python test_mobile_api.py
```

### 3. Verify Web Dashboard
Visit: `http://localhost:8000/`

## 📊 Real-time Synchronization

All API calls automatically sync with:
- **Firebase Firestore** (immediate)
- **Web Dashboard** (real-time via HTMX)
- **Other Mobile Clients** (via Firebase listeners)

## 🎯 Success Criteria

✅ Mobile app can scan QR codes and validate terminals
✅ Trips started from mobile appear instantly in web dashboard
✅ Passenger count updates reflect in real-time
✅ Completed trips show accurate data in dashboard
✅ Error handling provides clear user feedback
✅ Offline scenarios are handled gracefully

## 🎨 Complete Kotlin Implementation Template

### Repository Pattern Implementation
```kotlin
class TripRepository(private val apiService: FleetApiService) {

    suspend fun scanQrCode(qrCode: String): Result<Terminal> {
        return try {
            val response = apiService.scanQrCode(QrScanRequest(qrCode))
            if (response.isSuccessful) {
                Result.success(response.body()!!.terminal)
            } else {
                Result.failure(Exception("QR scan failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun startTrip(
        driverId: String,
        startTerminal: String,
        destinationTerminal: String,
        passengers: Int
    ): Result<Trip> {
        return try {
            val request = StartTripRequest(driverId, startTerminal, destinationTerminal, passengers)
            val response = apiService.startTrip(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!.trip)
            } else {
                Result.failure(Exception("Failed to start trip"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePassengers(tripId: String, passengers: Int): Result<Trip> {
        return try {
            val response = apiService.updatePassengers(tripId, UpdatePassengersRequest(passengers))
            if (response.isSuccessful) {
                Result.success(response.body()!!.trip)
            } else {
                Result.failure(Exception("Failed to update passengers"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun stopTrip(tripId: String, finalPassengers: Int): Result<Trip> {
        return try {
            val response = apiService.stopTrip(tripId, StopTripRequest(finalPassengers))
            if (response.isSuccessful) {
                Result.success(response.body()!!.trip)
            } else {
                Result.failure(Exception("Failed to stop trip"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### ViewModel Implementation
```kotlin
class TripViewModel(private val repository: TripRepository) : ViewModel() {

    private val _uiState = MutableLiveData<TripUiState>()
    val uiState: LiveData<TripUiState> = _uiState

    private val _currentTrip = MutableLiveData<Trip?>()
    val currentTrip: LiveData<Trip?> = _currentTrip

    fun scanQrCode(qrCode: String) {
        viewModelScope.launch {
            _uiState.value = TripUiState.Loading
            repository.scanQrCode(qrCode).fold(
                onSuccess = { terminal ->
                    _uiState.value = TripUiState.QrScanned(terminal)
                },
                onFailure = { error ->
                    _uiState.value = TripUiState.Error(error.message ?: "QR scan failed")
                }
            )
        }
    }

    fun startTrip(driverId: String, startTerminal: String, destTerminal: String, passengers: Int) {
        viewModelScope.launch {
            _uiState.value = TripUiState.Loading
            repository.startTrip(driverId, startTerminal, destTerminal, passengers).fold(
                onSuccess = { trip ->
                    _currentTrip.value = trip
                    _uiState.value = TripUiState.TripStarted(trip)
                },
                onFailure = { error ->
                    _uiState.value = TripUiState.Error(error.message ?: "Failed to start trip")
                }
            )
        }
    }

    fun updatePassengers(passengers: Int) {
        val tripId = _currentTrip.value?.trip_id ?: return
        viewModelScope.launch {
            repository.updatePassengers(tripId, passengers).fold(
                onSuccess = { trip ->
                    _currentTrip.value = trip
                    _uiState.value = TripUiState.PassengersUpdated(trip)
                },
                onFailure = { error ->
                    _uiState.value = TripUiState.Error(error.message ?: "Failed to update passengers")
                }
            )
        }
    }

    fun stopTrip(finalPassengers: Int) {
        val tripId = _currentTrip.value?.trip_id ?: return
        viewModelScope.launch {
            _uiState.value = TripUiState.Loading
            repository.stopTrip(tripId, finalPassengers).fold(
                onSuccess = { trip ->
                    _currentTrip.value = null
                    _uiState.value = TripUiState.TripCompleted(trip)
                },
                onFailure = { error ->
                    _uiState.value = TripUiState.Error(error.message ?: "Failed to stop trip")
                }
            )
        }
    }
}

sealed class TripUiState {
    object Loading : TripUiState()
    data class QrScanned(val terminal: Terminal) : TripUiState()
    data class TripStarted(val trip: Trip) : TripUiState()
    data class PassengersUpdated(val trip: Trip) : TripUiState()
    data class TripCompleted(val trip: Trip) : TripUiState()
    data class Error(val message: String) : TripUiState()
}
```

## 🔄 Step-by-Step Integration Flow

### Phase 1: QR Code Scanning
1. **Scan QR Code** → `POST /api/scan-qr/`
2. **Validate Terminal** → Show terminal info
3. **Select Destination** → Choose from available terminals

### Phase 2: Trip Management
1. **Start Trip** → `POST /api/trips/start/`
2. **Monitor Trip** → Real-time passenger updates
3. **Update Passengers** → `POST /api/trips/{id}/passengers/`
4. **Complete Trip** → `POST /api/trips/{id}/stop/`

### Phase 3: Real-time Sync
1. **API Call** → Mobile app sends data
2. **Firebase Update** → Backend updates Firestore
3. **Dashboard Refresh** → Web dashboard shows changes instantly
4. **Mobile Confirmation** → App receives success response

## 🧪 Testing Checklist

### API Integration Tests
- [ ] QR code scanning works with valid codes
- [ ] QR code scanning fails gracefully with invalid codes
- [ ] Trip creation succeeds with valid data
- [ ] Trip creation fails with missing required fields
- [ ] Passenger updates work during active trips
- [ ] Trip completion works and sets arrival time
- [ ] Error handling displays user-friendly messages

### Synchronization Tests
- [ ] Started trips appear in web dashboard immediately
- [ ] Passenger updates reflect in dashboard in real-time
- [ ] Completed trips show in dashboard with correct data
- [ ] Multiple mobile clients stay synchronized

### User Experience Tests
- [ ] Loading states show during API calls
- [ ] Success messages confirm actions
- [ ] Error messages are clear and actionable
- [ ] Offline scenarios are handled gracefully
- [ ] App recovers properly when connection is restored

---

**📞 Support:** For integration issues, refer to the API test script or Django logs for debugging.
