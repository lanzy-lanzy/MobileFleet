package com.ml.mobilefleet.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ml.mobilefleet.data.models.Terminal
import com.ml.mobilefleet.data.models.Trip
import com.ml.mobilefleet.data.models.TripStatus
import kotlinx.coroutines.tasks.await

/**
 * Repository class for Firebase Firestore operations
 * Handles all database operations for terminals and trips
 */
class FirebaseRepository {

    private val firestore: FirebaseFirestore = Firebase.firestore

    // Collection references
    private val terminalsCollection = firestore.collection("terminals")
    private val tripsCollection = firestore.collection("trips")

    /**
     * Fetch all terminals from Firebase
     */
    suspend fun getAllTerminals(): Result<List<Terminal>> {
        return try {
            val snapshot = terminalsCollection.get().await()
            val terminals = snapshot.documents.mapNotNull { document ->
                document.toObject(Terminal::class.java)?.copy(id = document.id)
            }
            Result.success(terminals)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Debug method to get all terminals with their QR codes
     */
    suspend fun getAllTerminalsDebug(): Result<List<Terminal>> {
        return try {
            Log.d("FirebaseRepository", "=== DEBUG: Fetching all terminals ===")
            val snapshot = terminalsCollection.get().await()
            Log.d("FirebaseRepository", "Found ${snapshot.documents.size} terminal documents")

            val terminals = snapshot.documents.mapNotNull { document ->
                val terminal = document.toObject(Terminal::class.java)?.copy(id = document.id)
                Log.d("FirebaseRepository", "Terminal: ${terminal?.name}, ID: ${terminal?.id}, QR: '${terminal?.qr_code}', QR_URL: '${terminal?.qr_code_url}'")
                terminal
            }
            Log.d("FirebaseRepository", "=== END DEBUG ===")
            Result.success(terminals)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error in getAllTerminalsDebug", e)
            Result.failure(e)
        }
    }

    /**
     * Get a specific terminal by ID
     */
    suspend fun getTerminalById(terminalId: String): Result<Terminal?> {
        return try {
            val snapshot = terminalsCollection.document(terminalId).get().await()
            val terminal = snapshot.toObject(Terminal::class.java)?.copy(id = snapshot.id)
            Result.success(terminal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Find terminal by QR code
     */
    suspend fun getTerminalByQrCode(qrCode: String): Result<Terminal?> {
        return try {
            Log.d("FirebaseRepository", "=== QR CODE SEARCH DEBUG ===")
            Log.d("FirebaseRepository", "Searching for terminal with QR code: '$qrCode'")
            Log.d("FirebaseRepository", "QR code length: ${qrCode.length}")
            Log.d("FirebaseRepository", "QR code bytes: ${qrCode.toByteArray().contentToString()}")

            // First try exact match
            Log.d("FirebaseRepository", "Executing query: terminals.whereEqualTo('qr_code', '$qrCode')")
            val snapshot = terminalsCollection
                .whereEqualTo("qr_code", qrCode)
                .get()
                .await()

            Log.d("FirebaseRepository", "Exact match query returned ${snapshot.documents.size} documents")

            // If we found documents, log them
            if (snapshot.documents.isNotEmpty()) {
                snapshot.documents.forEach { doc ->
                    val data = doc.data
                    Log.d("FirebaseRepository", "Found document ${doc.id}: qr_code='${data?.get("qr_code")}'")
                }
            }

            // Debug: Log all terminals to see what's actually stored
            val allTerminals = terminalsCollection.get().await()
            Log.d("FirebaseRepository", "Total terminals in database: ${allTerminals.documents.size}")
            allTerminals.documents.forEach { doc ->
                val data = doc.data
                val storedQrCode = data?.get("qr_code") as? String
                Log.d("FirebaseRepository", "Terminal ${doc.id}:")
                Log.d("FirebaseRepository", "  name='${data?.get("name")}'")
                Log.d("FirebaseRepository", "  terminal_id='${data?.get("terminal_id")}'")
                Log.d("FirebaseRepository", "  qr_code='$storedQrCode'")
                Log.d("FirebaseRepository", "  qr_code length: ${storedQrCode?.length ?: 0}")
                Log.d("FirebaseRepository", "  qr_code bytes: ${storedQrCode?.toByteArray()?.contentToString()}")
                Log.d("FirebaseRepository", "  qr_code equals scanned: ${storedQrCode == qrCode}")
                Log.d("FirebaseRepository", "  qr_code_url='${data?.get("qr_code_url")}'")
            }

            val terminal = if (snapshot.documents.isNotEmpty()) {
                val document = snapshot.documents.first()
                Log.d("FirebaseRepository", "Found terminal by exact match: ${document.id}")
                document.toObject(Terminal::class.java)?.copy(id = document.id)
            } else {
                Log.d("FirebaseRepository", "No exact match found, trying alternative methods...")

                // Try alternative search by extracting terminal_id from QR code format "terminal_id:XXXXX"
                val terminalId = extractTerminalIdFromQrCode(qrCode)
                if (terminalId != null) {
                    Log.d("FirebaseRepository", "Extracted terminal ID: '$terminalId'")
                    Log.d("FirebaseRepository", "Trying to find terminal by extracted ID...")
                    val terminalResult = getTerminalById(terminalId)
                    if (terminalResult.isSuccess && terminalResult.getOrNull() != null) {
                        Log.d("FirebaseRepository", "Found terminal by extracted ID: ${terminalResult.getOrNull()?.name}")
                        return terminalResult
                    } else {
                        Log.d("FirebaseRepository", "No terminal found with extracted ID: '$terminalId'")
                    }
                }

                // Try searching by terminal_id field directly
                Log.d("FirebaseRepository", "Trying to search by terminal_id field...")
                val terminalIdSnapshot = terminalsCollection
                    .whereEqualTo("terminal_id", qrCode)
                    .get()
                    .await()

                if (terminalIdSnapshot.documents.isNotEmpty()) {
                    val document = terminalIdSnapshot.documents.first()
                    Log.d("FirebaseRepository", "Found terminal by terminal_id field: ${document.id}")
                    document.toObject(Terminal::class.java)?.copy(id = document.id)
                } else {
                    Log.w("FirebaseRepository", "No terminal found for QR code: '$qrCode'")
                    null
                }
            }

            Log.d("FirebaseRepository", "=== END QR CODE SEARCH DEBUG ===")
            Result.success(terminal)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error searching for terminal", e)
            Result.failure(e)
        }
    }

    /**
     * Extract terminal ID from QR code format "terminal_id:XXXXX"
     */
    private fun extractTerminalIdFromQrCode(qrCode: String): String? {
        return if (qrCode.startsWith("terminal_id:")) {
            qrCode.substringAfter("terminal_id:")
        } else {
            null
        }
    }

    /**
     * Start a new trip
     */
    suspend fun startTrip(
        driverId: String,
        startTerminalId: String,
        destinationTerminalId: String,
        passengers: Int
    ): Result<String> {
        return try {
            val trip = Trip(
                driver_id = driverId,
                start_terminal = startTerminalId,
                destination_terminal = destinationTerminalId,
                passengers = passengers,
                start_time = Timestamp.now(),
                status = TripStatus.IN_PROGRESS.value
            )

            val documentRef = tripsCollection.add(trip).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Complete a trip
     */
    suspend fun completeTrip(tripId: String, arrivalTerminalId: String): Result<Unit> {
        return try {
            Log.d("FirebaseRepository", "Completing trip with ID: '$tripId'")
            Log.d("FirebaseRepository", "Arrival terminal ID: '$arrivalTerminalId'")

            val updates = mapOf(
                "arrival_time" to Timestamp.now(),
                "destination_terminal" to arrivalTerminalId,
                "status" to TripStatus.COMPLETED.value
            )

            Log.d("FirebaseRepository", "Updating trip document: trips/$tripId")
            tripsCollection.document(tripId).update(updates).await()
            Log.d("FirebaseRepository", "Trip completed successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error completing trip", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get current active trip for a driver
     */
    suspend fun getCurrentTripForDriver(driverId: String): Result<Trip?> {
        return try {
            val snapshot = tripsCollection
                .whereEqualTo("driver_id", driverId)
                .whereEqualTo("status", TripStatus.IN_PROGRESS.value)
                .get()
                .await()

            val trip = if (snapshot.documents.isNotEmpty()) {
                val document = snapshot.documents.first()
                document.toObject(Trip::class.java)?.copy(id = document.id)
            } else {
                null
            }
            Result.success(trip)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get terminal details with QR code URL for display purposes
     */
    suspend fun getTerminalWithQrUrl(terminalId: String): Result<Terminal?> {
        return try {
            val snapshot = terminalsCollection.document(terminalId).get().await()
            val terminal = snapshot.toObject(Terminal::class.java)?.copy(id = snapshot.id)
            Result.success(terminal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update passenger count for a trip
     */
    suspend fun updateTripPassengers(tripId: String, passengers: Int): Result<Unit> {
        return try {
            val updates = mapOf(
                "passengers" to passengers
            )

            tripsCollection.document(tripId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cancel a trip
     */
    suspend fun cancelTrip(tripId: String): Result<Unit> {
        return try {
            val updates = mapOf(
                "status" to TripStatus.CANCELLED.value
            )

            tripsCollection.document(tripId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
