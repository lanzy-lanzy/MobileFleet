package com.ml.mobilefleet.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/**
 * Data class representing a Terminal in the Firebase schema
 * Collection: terminals
 * Schema: { terminal_id, name, qr_code, latitude, longitude, qr_code_url, is_active, created_at, updated_at }
 */
data class Terminal(
    @DocumentId
    val id: String = "",
    val terminal_id: String = "",
    val name: String = "",
    val qr_code: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val qr_code_url: String = "",
    val is_active: Boolean = true,
    @ServerTimestamp
    val created_at: Timestamp? = null,
    @ServerTimestamp
    val updated_at: Timestamp? = null
) {
    // No-argument constructor for Firebase
    constructor() : this("", "", "", "", 0.0, 0.0, "", true, null, null)
}
