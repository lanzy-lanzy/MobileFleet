package com.ml.mobilefleet.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/**
 * Driver earnings tracking model
 */
data class DriverEarnings(
    @DocumentId
    val id: String = "",
    val driver_id: String = "",
    val date: Timestamp? = null,
    val month: Int = 0, // 1-12
    val year: Int = 0,
    val total_trips: Int = 0,
    val total_passengers: Int = 0,
    val base_salary: Double = 0.0,
    val commission_earnings: Double = 0.0,
    val bonus_earnings: Double = 0.0,
    val total_earnings: Double = 0.0,
    val trips_completed: List<String> = emptyList(), // Trip IDs for reference
    val payment_status: String = "pending", // pending, paid, processing
    val payment_date: Timestamp? = null,
    val notes: String = "",
    @ServerTimestamp
    val created_at: Timestamp? = null,
    @ServerTimestamp
    val updated_at: Timestamp? = null
) {
    constructor() : this("", "", null, 0, 0, 0, 0, 0.0, 0.0, 0.0, 0.0, emptyList(), "pending", null, "", null, null)
}

/**
 * Daily earnings summary
 */
data class DailyEarnings(
    @DocumentId
    val id: String = "",
    val driver_id: String = "",
    val date: Timestamp? = null,
    val trips_count: Int = 0,
    val passengers_count: Int = 0,
    val commission_earned: Double = 0.0,
    val bonus_earned: Double = 0.0,
    val total_earned: Double = 0.0,
    val trip_ids: List<String> = emptyList(),
    @ServerTimestamp
    val created_at: Timestamp? = null
) {
    constructor() : this("", "", null, 0, 0, 0.0, 0.0, 0.0, emptyList(), null)
}

/**
 * Commission calculation settings
 */
data class CommissionSettings(
    @DocumentId
    val id: String = "",
    val base_commission_rate: Double = 0.05, // 5% default
    val passenger_bonus: Double = 2.0, // $2 per passenger
    val trip_completion_bonus: Double = 5.0, // $5 per completed trip
    val monthly_trip_bonus_threshold: Int = 100, // Bonus after 100 trips
    val monthly_trip_bonus_amount: Double = 200.0, // $200 bonus
    val peak_hour_multiplier: Double = 1.5, // 1.5x during peak hours
    val peak_hours: List<String> = listOf("07:00-09:00", "17:00-19:00"),
    val weekend_multiplier: Double = 1.2, // 1.2x on weekends
    @ServerTimestamp
    val updated_at: Timestamp? = null
) {
    constructor() : this("", 0.05, 2.0, 5.0, 100, 200.0, 1.5, listOf("07:00-09:00", "17:00-19:00"), 1.2, null)
}

/**
 * Authentication state
 */
data class AuthState(
    val isAuthenticated: Boolean = false,
    val currentDriver: Driver? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Earnings summary for UI display
 */
data class EarningsSummary(
    val todayEarnings: Double = 0.0,
    val weekEarnings: Double = 0.0,
    val monthEarnings: Double = 0.0,
    val totalTrips: Int = 0,
    val totalPassengers: Int = 0,
    val averageEarningsPerTrip: Double = 0.0,
    val lastPaymentDate: Timestamp? = null,
    val pendingPayment: Double = 0.0
)

/**
 * Trip earnings calculation result
 */
data class TripEarnings(
    val tripId: String = "",
    val baseCommission: Double = 0.0,
    val passengerBonus: Double = 0.0,
    val completionBonus: Double = 0.0,
    val peakHourMultiplier: Double = 1.0,
    val weekendMultiplier: Double = 1.0,
    val totalEarnings: Double = 0.0,
    val calculationDetails: String = ""
)
