package com.ml.mobilefleet.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/**
 * Data class representing a Trip in the Firebase schema
 * Collection: trips
 * Schema: { trip_id, driver_id, start_terminal, destination_terminal, passengers, start_time, arrival_time, status, created_at, updated_at }
 */
data class Trip(
    @DocumentId
    val id: String = "",
    val trip_id: String = "",
    val driver_id: String = "",
    val start_terminal: String = "",
    val destination_terminal: String = "",
    val passengers: Int = 0,
    val start_time: Timestamp? = null,
    val arrival_time: Timestamp? = null,
    val status: String = TripStatus.IN_PROGRESS.value,
    @ServerTimestamp
    val created_at: Timestamp? = null,
    @ServerTimestamp
    val updated_at: Timestamp? = null
) {
    // No-argument constructor for Firebase
    constructor() : this("", "", "", "", "", 0, null, null, TripStatus.IN_PROGRESS.value, null, null)
}

/**
 * Enum class for Trip status values
 */
enum class TripStatus(val value: String) {
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    CANCELLED("cancelled")
}
