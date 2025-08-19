package com.ml.mobilefleet.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.ml.mobilefleet.data.models.Driver
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest

/**
 * Repository for driver authentication and session management
 */
class AuthRepository(private val context: Context) {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val driversCollection = firestore.collection("drivers")
    private val prefs: SharedPreferences = context.getSharedPreferences("driver_auth", Context.MODE_PRIVATE)
    
    companion object {
        private const val PREF_DRIVER_ID = "driver_id"
        private const val PREF_DRIVER_EMAIL = "driver_email"
        private const val PREF_IS_LOGGED_IN = "is_logged_in"
        private const val TAG = "AuthRepository"
    }
    
    /**
     * Login with email and password
     */
    suspend fun login(email: String, password: String): Result<Driver> {
        return try {
            Log.d(TAG, "Attempting login for email: $email")
            
            // Hash the password for comparison
            val hashedPassword = hashPassword(password)
            
            // Query driver by email
            val snapshot = driversCollection
                .whereEqualTo("email", email)
                .whereEqualTo("is_active", true)
                .limit(1)
                .get()
                .await()
            
            if (snapshot.isEmpty) {
                return Result.failure(Exception("Invalid email or password"))
            }
            
            val driverDoc = snapshot.documents.first()
            val driver = driverDoc.toObject<Driver>()?.copy(id = driverDoc.id)
            
            if (driver == null) {
                return Result.failure(Exception("Driver data not found"))
            }
            
            // For now, we'll use simple password comparison
            // In production, you should use proper password hashing (bcrypt, etc.)
            if (driver.password_hash.isNotEmpty() && driver.password_hash != hashedPassword) {
                return Result.failure(Exception("Invalid email or password"))
            }
            
            // Save login session
            saveLoginSession(driver)
            
            Log.d(TAG, "Login successful for driver: ${driver.driver_id}")
            Result.success(driver)
            
        } catch (e: Exception) {
            Log.e(TAG, "Login failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * Login with driver ID (alternative method)
     */
    suspend fun loginWithDriverId(driverId: String, password: String): Result<Driver> {
        return try {
            Log.d(TAG, "Attempting login for driver ID: $driverId")
            
            val hashedPassword = hashPassword(password)
            
            val snapshot = driversCollection
                .whereEqualTo("driver_id", driverId)
                .whereEqualTo("is_active", true)
                .limit(1)
                .get()
                .await()
            
            if (snapshot.isEmpty) {
                return Result.failure(Exception("Invalid driver ID or password"))
            }
            
            val driverDoc = snapshot.documents.first()
            val driver = driverDoc.toObject<Driver>()?.copy(id = driverDoc.id)
            
            if (driver == null) {
                return Result.failure(Exception("Driver data not found"))
            }
            
            if (driver.password_hash.isNotEmpty() && driver.password_hash != hashedPassword) {
                return Result.failure(Exception("Invalid driver ID or password"))
            }
            
            saveLoginSession(driver)
            
            Log.d(TAG, "Login successful for driver: ${driver.driver_id}")
            Result.success(driver)
            
        } catch (e: Exception) {
            Log.e(TAG, "Login with driver ID failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get current logged-in driver
     */
    suspend fun getCurrentDriver(): Result<Driver?> {
        return try {
            val driverId = prefs.getString(PREF_DRIVER_ID, null)
            val isLoggedIn = prefs.getBoolean(PREF_IS_LOGGED_IN, false)
            
            if (!isLoggedIn || driverId.isNullOrEmpty()) {
                return Result.success(null)
            }
            
            // Fetch fresh driver data from Firestore
            val snapshot = driversCollection
                .whereEqualTo("driver_id", driverId)
                .whereEqualTo("is_active", true)
                .limit(1)
                .get()
                .await()
            
            if (snapshot.isEmpty) {
                // Driver not found or inactive, clear session
                logout()
                return Result.success(null)
            }
            
            val driverDoc = snapshot.documents.first()
            val driver = driverDoc.toObject<Driver>()?.copy(id = driverDoc.id)
            
            Result.success(driver)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get current driver", e)
            Result.failure(e)
        }
    }
    
    /**
     * Logout current driver
     */
    fun logout() {
        prefs.edit()
            .remove(PREF_DRIVER_ID)
            .remove(PREF_DRIVER_EMAIL)
            .putBoolean(PREF_IS_LOGGED_IN, false)
            .apply()
        
        Log.d(TAG, "Driver logged out")
    }
    
    /**
     * Check if driver is logged in
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(PREF_IS_LOGGED_IN, false)
    }
    
    /**
     * Get stored driver ID
     */
    fun getStoredDriverId(): String? {
        return prefs.getString(PREF_DRIVER_ID, null)
    }
    
    /**
     * Update driver profile
     */
    suspend fun updateDriverProfile(driver: Driver): Result<Unit> {
        return try {
            driversCollection.document(driver.id)
                .set(driver)
                .await()
            
            // Update local session if this is the current driver
            val currentDriverId = getStoredDriverId()
            if (currentDriverId == driver.driver_id) {
                saveLoginSession(driver)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update driver profile", e)
            Result.failure(e)
        }
    }
    
    /**
     * Change driver password
     */
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            val driver = getCurrentDriver().getOrNull()
                ?: return Result.failure(Exception("No logged-in driver"))
            
            val currentHashedPassword = hashPassword(currentPassword)
            if (driver.password_hash != currentHashedPassword) {
                return Result.failure(Exception("Current password is incorrect"))
            }
            
            val newHashedPassword = hashPassword(newPassword)
            val updatedDriver = driver.copy(password_hash = newHashedPassword)
            
            updateDriverProfile(updatedDriver)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to change password", e)
            Result.failure(e)
        }
    }
    
    /**
     * Save login session to SharedPreferences
     */
    private fun saveLoginSession(driver: Driver) {
        prefs.edit()
            .putString(PREF_DRIVER_ID, driver.driver_id)
            .putString(PREF_DRIVER_EMAIL, driver.email)
            .putBoolean(PREF_IS_LOGGED_IN, true)
            .apply()
    }
    
    /**
     * Simple password hashing (use bcrypt in production)
     */
    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
