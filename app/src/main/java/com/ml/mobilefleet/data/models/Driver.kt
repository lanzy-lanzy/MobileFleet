package com.ml.mobilefleet.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/**
 * Data class representing a Driver in the Firebase schema
 * Extended for authentication and salary management
 */
data class Driver(
    @DocumentId
    val id: String = "",
    val driver_id: String = "",
    val name: String = "",
    val contact: String = "",
    val license_number: String = "",
    val is_active: Boolean = true,
    // Authentication fields
    val email: String = "",
    val password_hash: String = "", // For local auth (if not using Firebase Auth)
    val phone_number: String = "",
    val profile_image_url: String = "",
    val address: String = "",
    val emergency_contact: String = "",
    val emergency_phone: String = "",
    val hire_date: Timestamp? = null,
    val status: String = "active", // active, inactive, suspended
    // Salary and commission fields
    val salary_type: String = "commission", // fixed, commission, hybrid
    val base_salary: Double = 0.0, // Monthly base salary (if applicable)
    val commission_rate: Double = 0.05, // Commission percentage per trip (5% default)
    val commission_per_passenger: Double = 2.0, // Fixed amount per passenger ($2 default)
    val trip_completion_bonus: Double = 5.0, // Bonus per completed trip ($5 default)
    @ServerTimestamp
    val created_at: Timestamp? = null,
    @ServerTimestamp
    val updated_at: Timestamp? = null
) {
    // No-argument constructor for Firebase
    constructor() : this("", "", "", "", "", true, "", "", "", "", "", "", "", null, "active", "commission", 0.0, 0.05, 2.0, 5.0, null, null)
}
