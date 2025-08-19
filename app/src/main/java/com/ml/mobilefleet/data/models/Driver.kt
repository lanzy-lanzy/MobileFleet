package com.ml.mobilefleet.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/**
 * Data class representing a Driver in the Firebase schema
 * Collection: drivers
 * Schema: { driver_id, name, contact, license_number, is_active, created_at, updated_at }
 */
data class Driver(
    @DocumentId
    val id: String = "",
    val driver_id: String = "",
    val name: String = "",
    val contact: String = "",
    val license_number: String = "",
    val is_active: Boolean = true,
    @ServerTimestamp
    val created_at: Timestamp? = null,
    @ServerTimestamp
    val updated_at: Timestamp? = null
) {
    // No-argument constructor for Firebase
    constructor() : this("", "", "", "", "", true, null, null)
}
