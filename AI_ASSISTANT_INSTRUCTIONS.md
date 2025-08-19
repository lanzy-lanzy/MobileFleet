# 🤖 AI Assistant Instructions for Mobile Fleet App Integration

## 📋 Context & Overview

You are helping to integrate a **Kotlin Android mobile app** with an existing **Django Web Dashboard** for a Mobile Fleet Monitoring System. Both systems sync data through **Firebase Firestore** in real-time.

### System Components:
- **Backend**: Django REST API with Firebase integration
- **Frontend**: Web dashboard with real-time updates
- **Mobile**: Kotlin Android app (your focus)
- **Database**: Firebase Firestore (shared)

## 🎯 Your Primary Objectives

### 1. **API Integration**
Implement the mobile app to use these exact API endpoints:

```
Base URL: http://localhost:8000/api/ (development)

POST /api/scan-qr/              - Validate QR codes
POST /api/trips/start/          - Start new trips  
POST /api/trips/{id}/passengers/ - Update passenger count
POST /api/trips/{id}/stop/      - Complete trips
GET  /api/trips/active/         - Get active trips
GET  /api/trips/{id}/           - Get trip details
GET  /api/drivers/{id}/         - Get driver info
```

### 2. **Real-time Synchronization**
Ensure all mobile actions immediately sync with:
- Firebase Firestore database
- Web dashboard (shows changes instantly)
- Other mobile clients

### 3. **User Experience**
Create intuitive flows for:
- QR code scanning (start/end terminals)
- Trip management (start/stop/update)
- Passenger counting (real-time updates)
- Error handling and offline scenarios

## 🔄 Required Mobile App Flow

### **Step 1: Driver Authentication**
```kotlin
// Implement driver login/selection
// Store driver_id for API calls
val driverId = "vWCHpexWfZNTj6Jf85D5" // Example
```

### **Step 2: QR Code Scanning**
```kotlin
// Scan terminal QR code
val qrCode = "terminal_id:PjYqQNctdioMF2OvrbiN"

// Validate with API
POST /api/scan-qr/
{
  "qr_code": "terminal_id:PjYqQNctdioMF2OvrbiN"
}

// Expected response:
{
  "success": true,
  "terminal": {
    "terminal_id": "PjYqQNctdioMF2OvrbiN",
    "name": "Dipolog Terminal",
    "latitude": 8.589,
    "longitude": 123.3456
  }
}
```

### **Step 3: Trip Creation**
```kotlin
// After scanning start terminal, select destination and passenger count
POST /api/trips/start/
{
  "driver_id": "vWCHpexWfZNTj6Jf85D5",
  "start_terminal": "PjYqQNctdioMF2OvrbiN",
  "destination_terminal": "cu38ZbDlEWIiglCD5P54",
  "passengers": 15
}

// Store returned trip_id for updates
val tripId = response.trip_id
```

### **Step 4: Passenger Updates (During Trip)**
```kotlin
// Allow driver to update passenger count as people board/exit
POST /api/trips/{tripId}/passengers/
{
  "passengers": 20
}
```

### **Step 5: Trip Completion**
```kotlin
// When reaching destination, scan end terminal QR and complete trip
POST /api/trips/{tripId}/stop/
{
  "passengers": 18  // Final passenger count
}
```

## 🛠️ Technical Implementation Requirements

### **HTTP Client Setup**
Use Retrofit with these configurations:
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
}
```

### **Error Handling**
Handle these HTTP status codes:
- **200**: Success
- **400**: Bad request (invalid data)
- **404**: Not found (invalid trip/driver/terminal)
- **500**: Server error

### **Data Models**
Create these Kotlin data classes:
```kotlin
data class Terminal(
    val terminal_id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val is_active: Boolean
)

data class Trip(
    val trip_id: String,
    val driver_id: String,
    val start_terminal: String,
    val destination_terminal: String,
    val passengers: Int,
    val status: String, // "in_progress", "completed", "cancelled"
    val start_time: String?,
    val arrival_time: String?
)

data class Driver(
    val driver_id: String,
    val name: String,
    val contact: String,
    val license_number: String,
    val is_active: Boolean
)
```

## 📱 UI/UX Guidelines

### **Screen Flow**
1. **Login Screen** → Driver selection
2. **QR Scanner Screen** → Scan start terminal
3. **Trip Setup Screen** → Select destination, enter passengers
4. **Active Trip Screen** → Update passengers, show trip info
5. **QR Scanner Screen** → Scan end terminal
6. **Trip Complete Screen** → Confirm completion

### **Real-time Updates**
- Show loading states during API calls
- Display success/error messages clearly
- Update passenger count immediately after API success
- Show trip status changes in real-time

### **Offline Handling**
- Cache trip data locally
- Queue API calls when offline
- Sync when connection restored
- Show offline status to user

## 🧪 Testing Requirements

### **API Integration Tests**
Test each endpoint with:
- Valid data (should succeed)
- Invalid data (should fail gracefully)
- Network errors (should retry/queue)
- Server errors (should show user-friendly messages)

### **Synchronization Tests**
Verify that:
- Started trips appear in web dashboard immediately
- Passenger updates reflect in dashboard in real-time
- Completed trips show correct final data
- Multiple mobile devices stay synchronized

### **User Flow Tests**
Test complete workflows:
- Full trip cycle (start → update → complete)
- Error scenarios (invalid QR, network issues)
- Edge cases (duplicate scans, rapid updates)

## 🚨 Critical Success Criteria

### **Must Have Features**
✅ QR code scanning with validation
✅ Trip start/stop functionality
✅ Real-time passenger count updates
✅ Immediate sync with web dashboard
✅ Proper error handling and user feedback

### **Data Integrity**
✅ All API calls must succeed before updating UI
✅ Handle network failures gracefully
✅ Validate data before sending to API
✅ Maintain consistency between mobile and web

### **Performance**
✅ API calls should complete within 3 seconds
✅ UI should remain responsive during network operations
✅ Implement proper loading states
✅ Cache data to improve user experience

## 📊 Validation & Testing

### **Test with Real Data**
Use these actual IDs from the system:
- **Driver ID**: `vWCHpexWfZNTj6Jf85D5`
- **Terminal IDs**: 
  - `PjYqQNctdioMF2OvrbiN` (Dipolog Terminal)
  - `cu38ZbDlEWIiglCD5P54` (Molave Terminal)
  - `UWRuagw6fBAvG8s6eUW0` (Ozamiz Terminal)

### **Verify Synchronization**
After each mobile action, check:
1. **Mobile app** shows updated data
2. **Web dashboard** reflects changes immediately
3. **Firebase console** shows updated documents

## 🎯 Final Deliverables

When implementation is complete, provide:
1. **Complete Kotlin source code** with all API integrations
2. **Test cases** covering all scenarios
3. **Documentation** explaining the implementation
4. **Demo video** showing mobile-to-web synchronization

---

**🔗 Reference Files:**
- `MOBILE_API_INTEGRATION.md` - Complete API documentation
- `test_mobile_api.py` - API testing script
- `monitoring/api_views.py` - Backend API implementation
