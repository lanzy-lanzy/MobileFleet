package com.ml.mobilefleet.data.service

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ml.mobilefleet.data.models.Terminal
import com.ml.mobilefleet.data.models.Driver
import com.ml.mobilefleet.data.models.Trip
import kotlinx.coroutines.tasks.await

/**
 * Service for synchronizing data between Django backend and Android app
 * Ensures data consistency and handles schema validation
 */
class DataSyncService {
    
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val tag = "DataSyncService"
    
    /**
     * Validate and sync terminal data
     * Ensures all terminals have required fields for Android app compatibility
     */
    suspend fun validateAndSyncTerminals(): Result<List<Terminal>> {
        return try {
            Log.d(tag, "Starting terminal validation and sync...")
            
            val snapshot = firestore.collection("terminals").get().await()
            val validatedTerminals = mutableListOf<Terminal>()
            var updatedCount = 0
            
            for (document in snapshot.documents) {
                val data = document.data ?: continue
                val terminalId = document.id
                
                Log.d(tag, "Processing terminal: $terminalId")
                
                // Check for missing fields and create update map
                val updates = mutableMapOf<String, Any>()
                
                // Ensure terminal_id field exists
                if (!data.containsKey("terminal_id") || data["terminal_id"] == null) {
                    updates["terminal_id"] = terminalId
                    Log.d(tag, "Adding missing terminal_id for $terminalId")
                }
                
                // Ensure qr_code field exists with proper format
                val qrCode = data["qr_code"] as? String
                if (qrCode.isNullOrBlank()) {
                    val qrData = "terminal_id:$terminalId"
                    updates["qr_code"] = qrData
                    Log.d(tag, "Adding missing qr_code for $terminalId: $qrData")
                }
                
                // Ensure is_active field exists
                if (!data.containsKey("is_active")) {
                    updates["is_active"] = true
                    Log.d(tag, "Adding missing is_active for $terminalId")
                }
                
                // Apply updates if needed
                if (updates.isNotEmpty()) {
                    document.reference.update(updates).await()
                    updatedCount++
                    Log.d(tag, "Updated terminal $terminalId with ${updates.keys}")
                }
                
                // Convert to Terminal object
                val terminal = document.toObject(Terminal::class.java)?.copy(id = terminalId)
                if (terminal != null) {
                    validatedTerminals.add(terminal)
                }
            }
            
            Log.d(tag, "Terminal sync completed. Updated $updatedCount terminals, validated ${validatedTerminals.size} total")
            Result.success(validatedTerminals)
            
        } catch (e: Exception) {
            Log.e(tag, "Error during terminal validation and sync", e)
            Result.failure(e)
        }
    }
    
    /**
     * Validate and sync driver data
     */
    suspend fun validateAndSyncDrivers(): Result<List<Driver>> {
        return try {
            Log.d(tag, "Starting driver validation and sync...")
            
            val snapshot = firestore.collection("drivers").get().await()
            val validatedDrivers = mutableListOf<Driver>()
            var updatedCount = 0
            
            for (document in snapshot.documents) {
                val data = document.data ?: continue
                val driverId = document.id
                
                Log.d(tag, "Processing driver: $driverId")
                
                val updates = mutableMapOf<String, Any>()
                
                // Ensure driver_id field exists
                if (!data.containsKey("driver_id") || data["driver_id"] == null) {
                    updates["driver_id"] = driverId
                    Log.d(tag, "Adding missing driver_id for $driverId")
                }
                
                // Ensure is_active field exists
                if (!data.containsKey("is_active")) {
                    updates["is_active"] = true
                    Log.d(tag, "Adding missing is_active for $driverId")
                }
                
                // Handle license vs license_number field mapping
                val license = data["license"] as? String
                val licenseNumber = data["license_number"] as? String
                
                if (licenseNumber.isNullOrBlank() && !license.isNullOrBlank()) {
                    updates["license_number"] = license
                    Log.d(tag, "Copying license to license_number for $driverId")
                }
                
                // Apply updates if needed
                if (updates.isNotEmpty()) {
                    document.reference.update(updates).await()
                    updatedCount++
                    Log.d(tag, "Updated driver $driverId with ${updates.keys}")
                }
                
                // Convert to Driver object
                val driver = document.toObject(Driver::class.java)?.copy(id = driverId)
                if (driver != null) {
                    validatedDrivers.add(driver)
                }
            }
            
            Log.d(tag, "Driver sync completed. Updated $updatedCount drivers, validated ${validatedDrivers.size} total")
            Result.success(validatedDrivers)
            
        } catch (e: Exception) {
            Log.e(tag, "Error during driver validation and sync", e)
            Result.failure(e)
        }
    }
    
    /**
     * Validate and sync trip data
     */
    suspend fun validateAndSyncTrips(): Result<List<Trip>> {
        return try {
            Log.d(tag, "Starting trip validation and sync...")
            
            val snapshot = firestore.collection("trips").get().await()
            val validatedTrips = mutableListOf<Trip>()
            var updatedCount = 0
            
            for (document in snapshot.documents) {
                val data = document.data ?: continue
                val tripId = document.id
                
                Log.d(tag, "Processing trip: $tripId")
                
                val updates = mutableMapOf<String, Any>()
                
                // Ensure trip_id field exists
                if (!data.containsKey("trip_id") || data["trip_id"] == null) {
                    updates["trip_id"] = tripId
                    Log.d(tag, "Adding missing trip_id for $tripId")
                }
                
                // Apply updates if needed
                if (updates.isNotEmpty()) {
                    document.reference.update(updates).await()
                    updatedCount++
                    Log.d(tag, "Updated trip $tripId with ${updates.keys}")
                }
                
                // Convert to Trip object
                val trip = document.toObject(Trip::class.java)?.copy(id = tripId)
                if (trip != null) {
                    validatedTrips.add(trip)
                }
            }
            
            Log.d(tag, "Trip sync completed. Updated $updatedCount trips, validated ${validatedTrips.size} total")
            Result.success(validatedTrips)
            
        } catch (e: Exception) {
            Log.e(tag, "Error during trip validation and sync", e)
            Result.failure(e)
        }
    }
    
    /**
     * Perform complete data synchronization
     */
    suspend fun performFullSync(): Result<String> {
        return try {
            Log.d(tag, "Starting full data synchronization...")
            
            val terminalResult = validateAndSyncTerminals()
            val driverResult = validateAndSyncDrivers()
            val tripResult = validateAndSyncTrips()
            
            val summary = buildString {
                appendLine("Data Synchronization Complete:")
                
                if (terminalResult.isSuccess) {
                    appendLine("✅ Terminals: ${terminalResult.getOrNull()?.size ?: 0} validated")
                } else {
                    appendLine("❌ Terminals: Sync failed - ${terminalResult.exceptionOrNull()?.message}")
                }
                
                if (driverResult.isSuccess) {
                    appendLine("✅ Drivers: ${driverResult.getOrNull()?.size ?: 0} validated")
                } else {
                    appendLine("❌ Drivers: Sync failed - ${driverResult.exceptionOrNull()?.message}")
                }
                
                if (tripResult.isSuccess) {
                    appendLine("✅ Trips: ${tripResult.getOrNull()?.size ?: 0} validated")
                } else {
                    appendLine("❌ Trips: Sync failed - ${tripResult.exceptionOrNull()?.message}")
                }
            }
            
            Log.d(tag, summary)
            Result.success(summary)
            
        } catch (e: Exception) {
            Log.e(tag, "Error during full sync", e)
            Result.failure(e)
        }
    }
    
    /**
     * Validate QR code format and extract terminal ID
     */
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
    
    /**
     * Check Cloudinary URL format
     */
    fun validateCloudinaryUrl(url: String): Boolean {
        return url.contains("cloudinary.com") && url.contains("/upload/")
    }
}
