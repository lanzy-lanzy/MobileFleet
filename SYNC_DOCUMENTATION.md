# Django-Android Synchronization Documentation

## Overview
This document outlines the synchronization between your Django + Firebase fleet monitoring system and the Kotlin Android driver app, specifically focusing on Cloudinary QR code integration.

## Data Schema Synchronization

### Terminal Schema
**Django Backend (Firebase)**:
```json
{
  "terminal_id": "3kkrj6sGsHt3SOe6GNby",
  "name": "Ozamiz Terminal",
  "latitude": 8.1567,
  "longitude": 123.8901,
  "is_active": true,
  "qr_code": "terminal_id:3kkrj6sGsHt3SOe6GNby",
  "qr_code_url": "https://res.cloudinary.com/dquzz14x9/image/upload/v1755531771/qr_codes/terminal_3kkrj6sGsHt3SOe6GNby.png",
  "created_at": "2025-08-19T07:42:50Z",
  "updated_at": "2025-08-19T07:42:51Z"
}
```

**Android App Model**:
```kotlin
data class Terminal(
    @DocumentId val id: String = "",
    val terminal_id: String = "",
    val name: String = "",
    val qr_code: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val qr_code_url: String = "",
    val is_active: Boolean = true,
    @ServerTimestamp val created_at: Timestamp? = null,
    @ServerTimestamp val updated_at: Timestamp? = null
)
```

### Driver Schema
**Django Backend**:
```json
{
  "driver_id": "auto-generated-id",
  "name": "Juan Dela Cruz",
  "contact": "+63 912 345 6789",
  "license_number": "N01-12-345678",
  "is_active": true,
  "created_at": "timestamp",
  "updated_at": "timestamp"
}
```

**Android App Model**:
```kotlin
data class Driver(
    @DocumentId val id: String = "",
    val driver_id: String = "",
    val name: String = "",
    val contact: String = "",
    val license_number: String = "",
    val is_active: Boolean = true,
    @ServerTimestamp val created_at: Timestamp? = null,
    @ServerTimestamp val updated_at: Timestamp? = null
)
```

### Trip Schema
**Django Backend**:
```json
{
  "trip_id": "auto-generated-id",
  "driver_id": "driver-reference-id",
  "start_terminal": "terminal-reference-id",
  "destination_terminal": "terminal-reference-id",
  "passengers": 15,
  "start_time": "timestamp",
  "arrival_time": "timestamp",
  "status": "in_progress|completed|cancelled",
  "created_at": "timestamp",
  "updated_at": "timestamp"
}
```

**Android App Model**:
```kotlin
data class Trip(
    @DocumentId val id: String = "",
    val trip_id: String = "",
    val driver_id: String = "",
    val start_terminal: String = "",
    val destination_terminal: String = "",
    val passengers: Int = 0,
    val start_time: Timestamp? = null,
    val arrival_time: Timestamp? = null,
    val status: String = TripStatus.IN_PROGRESS.value,
    @ServerTimestamp val created_at: Timestamp? = null,
    @ServerTimestamp val updated_at: Timestamp? = null
)
```

## Cloudinary Integration

### Configuration
Both Django and Android use the same Cloudinary credentials:
- **Cloud Name**: `dquzz14x9`
- **API Key**: `217121185485512`
- **API Secret**: `nyL4eFp36DqRNIv4sfwibroYcMY`

### QR Code Format
**QR Code Data**: `terminal_id:3kkrj6sGsHt3SOe6GNby`
**Cloudinary URL**: `https://res.cloudinary.com/dquzz14x9/image/upload/v1755531771/qr_codes/terminal_3kkrj6sGsHt3SOe6GNby.png`

### Django QR Code Generation Process
1. Create terminal in Firebase
2. Generate QR code with format: `terminal_id:{terminal_id}`
3. Upload QR image to Cloudinary with public_id: `terminal_{terminal_id}`
4. Update terminal document with `qr_code` and `qr_code_url` fields

### Android QR Code Handling
1. **Scanning**: ML Kit scans QR codes and extracts the text
2. **Parsing**: Extract terminal_id from format `terminal_id:XXXXX`
3. **Lookup**: Find terminal by either:
   - Direct qr_code field match
   - Extract terminal_id and lookup by document ID
4. **Display**: Use CloudinaryService to generate optimized image URLs

## Key Synchronization Features

### 1. Data Sync Service
The `DataSyncService` class ensures data consistency:
- Validates all terminals have required fields
- Adds missing `terminal_id`, `qr_code`, and `is_active` fields
- Handles field mapping differences (e.g., `license` vs `license_number`)
- Provides validation for QR code format and Cloudinary URLs

### 2. QR Code Scanning Integration
```kotlin
// QR code format validation
fun validateQrCodeFormat(qrCode: String): Pair<Boolean, String?> {
    return if (qrCode.startsWith("terminal_id:")) {
        val terminalId = qrCode.substringAfter("terminal_id:")
        if (terminalId.isNotBlank()) {
            Pair(true, terminalId)
        } else {
            Pair(false, null)
        }
    } else {
        Pair(false, null)
    }
}
```

### 3. Cloudinary URL Processing
```kotlin
// Extract public ID from Cloudinary URL
fun extractPublicIdFromUrl(cloudinaryUrl: String): String? {
    val regex = Regex(".*/upload/(?:v\\d+/)?(.+?)(?:\\.[^.]+)?$")
    val matchResult = regex.find(cloudinaryUrl)
    return matchResult?.groupValues?.get(1)
}

// Generate optimized URLs
fun getQrCodeImageUrl(publicId: String, width: Int = 300, height: Int = 300): String {
    return "https://res.cloudinary.com/dquzz14x9/image/upload/w_$width,h_$height,c_fill,q_auto,f_auto/$publicId"
}
```

### 4. Debug and Testing Tools
- **Sync Debug Screen**: Test data synchronization and QR code functionality
- **Data validation**: Ensure all required fields are present
- **QR code testing**: Validate QR format and terminal lookup
- **Cloudinary validation**: Check URL format and accessibility

## Usage Instructions

### For Django Backend
1. Run `python manage.py populate_data --with-qr` to create terminals with QR codes
2. QR codes are automatically generated and uploaded to Cloudinary
3. Terminal documents include both `qr_code` (data) and `qr_code_url` (image) fields

### For Android App
1. Use the QR scanner to scan terminal QR codes
2. App automatically extracts terminal_id and looks up terminal data
3. QR code images are displayed using optimized Cloudinary URLs
4. Access Debug screen via "Debug" button on Start Trip screen

### Testing Synchronization
1. Open Android app and navigate to Debug screen
2. Run "Full Sync" to validate and fix any data inconsistencies
3. Use "Test QR" to validate QR code format and lookup functionality
4. Use "Test Cloudinary" to validate image URLs

## Troubleshooting

### Common Issues
1. **QR Code Not Found**: Ensure `qr_code` field exists and follows format `terminal_id:XXXXX`
2. **Image Not Loading**: Verify Cloudinary URL format and network connectivity
3. **Data Mismatch**: Run data sync to ensure all required fields are present

### Data Validation
The sync service automatically:
- Adds missing `terminal_id` fields
- Creates `qr_code` fields with proper format
- Ensures `is_active` field exists
- Maps `license` to `license_number` for drivers

## Security Considerations
- Cloudinary credentials are stored in environment files
- Firebase security rules should restrict write access
- QR codes contain only terminal IDs, no sensitive data

## Performance Optimizations
- Cloudinary URLs include optimization parameters (`q_auto`, `f_auto`)
- Image sizes are dynamically adjusted based on usage (thumbnails vs full size)
- Firebase queries use indexed fields for efficient lookups
