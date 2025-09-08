package com.ml.mobilefleet.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.ml.mobilefleet.data.models.Driver
import kotlinx.coroutines.tasks.await

/**
 * Repository for driver authentication and session management using Firebase Auth
 */
class AuthRepository(private val context: Context) {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val driversCollection = firestore.collection("drivers")
    private val prefs: SharedPreferences = context.getSharedPreferences("driver_auth", Context.MODE_PRIVATE)

    companion object {
        private const val PREF_DRIVER_ID = "driver_id"
        private const val PREF_DRIVER_EMAIL = "driver_email"
        private const val PREF_AUTH_UID = "auth_uid"
        private const val PREF_IS_LOGGED_IN = "is_logged_in"
        private const val TAG = "AuthRepository"
    }
    
    /**
     * Login with email and password using Firebase Authentication
     */
    suspend fun login(email: String, password: String): Result<Driver> {
        return try {
            Log.d(TAG, "Attempting Firebase Auth login for email: $email")

            // Authenticate with Firebase Auth
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser == null) {
                return Result.failure(Exception("Authentication failed"))
            }

            Log.d(TAG, "Firebase Auth successful, fetching driver data for UID: ${firebaseUser.uid}")

            // Query driver by auth_uid
            val snapshot = driversCollection
                .whereEqualTo("auth_uid", firebaseUser.uid)
                .whereEqualTo("is_active", true)
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) {
                // Sign out from Firebase Auth if no driver record found
                firebaseAuth.signOut()
                return Result.failure(Exception("Driver account not found or inactive"))
            }

            val driverDoc = snapshot.documents.first()
            val driver = driverDoc.toObject<Driver>()?.copy(id = driverDoc.id)

            if (driver == null) {
                firebaseAuth.signOut()
                return Result.failure(Exception("Driver data not found"))
            }

            // Save login session
            saveLoginSession(driver, firebaseUser.uid)

            Log.d(TAG, "Login successful for driver: ${driver.driver_id}")
            Result.success(driver)

        } catch (e: Exception) {
            Log.e(TAG, "Login failed", e)
            Result.failure(e)
        }
    }
    

    
    /**
     * Get current logged-in driver using Firebase Auth
     */
    suspend fun getCurrentDriver(): Result<Driver?> {
        return try {
            val firebaseUser = firebaseAuth.currentUser

            if (firebaseUser == null) {
                // No Firebase Auth user, clear local session
                logout()
                return Result.success(null)
            }

            Log.d(TAG, "Getting current driver for Firebase UID: ${firebaseUser.uid}")

            // Fetch fresh driver data from Firestore using auth_uid
            val snapshot = driversCollection
                .whereEqualTo("auth_uid", firebaseUser.uid)
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

            if (driver != null) {
                // Update local session with latest data
                saveLoginSession(driver, firebaseUser.uid)
            }

            Result.success(driver)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get current driver", e)
            Result.failure(e)
        }
    }
    
    /**
     * Logout current driver from Firebase Auth and clear local session
     */
    fun logout() {
        firebaseAuth.signOut()
        prefs.edit()
            .clear()
            .apply()
        Log.d(TAG, "Driver logged out from Firebase Auth and local session cleared")
    }
    
    /**
     * Check if driver is logged in
     */
    fun isLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null && prefs.getBoolean(PREF_IS_LOGGED_IN, false)
    }

    /**
     * Get stored driver ID
     */
    fun getStoredDriverId(): String? {
        return prefs.getString(PREF_DRIVER_ID, null)
    }

    /**
     * Get current Firebase Auth UID
     */
    fun getCurrentAuthUid(): String? {
        return firebaseAuth.currentUser?.uid
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
                val authUid = getCurrentAuthUid()
                if (authUid != null) {
                    saveLoginSession(driver, authUid)
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update driver profile", e)
            Result.failure(e)
        }
    }
    
    /**
     * Save login session to SharedPreferences
     */
    private fun saveLoginSession(driver: Driver, authUid: String) {
        prefs.edit()
            .putString(PREF_DRIVER_ID, driver.driver_id)
            .putString(PREF_DRIVER_EMAIL, driver.email)
            .putString(PREF_AUTH_UID, authUid)
            .putBoolean(PREF_IS_LOGGED_IN, true)
            .apply()
        Log.d(TAG, "Login session saved for driver: ${driver.driver_id}, Auth UID: $authUid")
    }
}
