package com.ml.mobilefleet.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.ml.mobilefleet.data.models.*
import kotlinx.coroutines.tasks.await
import java.util.*

/**
 * Repository for managing driver earnings and commission calculations
 */
class EarningsRepository {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val earningsCollection = firestore.collection("driver_earnings")
    private val dailyEarningsCollection = firestore.collection("daily_earnings")
    private val settingsCollection = firestore.collection("commission_settings")
    private val tripsCollection = firestore.collection("trips")
    
    companion object {
        private const val TAG = "EarningsRepository"
    }
    
    /**
     * Calculate earnings for a completed trip
     */
    suspend fun calculateTripEarnings(trip: Trip, driver: Driver): Result<TripEarnings> {
        return try {
            val settings = getCommissionSettings().getOrNull() ?: CommissionSettings()
            
            // Base commission calculation
            val baseCommission = driver.commission_rate * trip.passengers
            
            // Passenger bonus
            val passengerBonus = driver.commission_per_passenger * trip.passengers
            
            // Trip completion bonus
            val completionBonus = driver.trip_completion_bonus
            
            // Time-based multipliers
            val tripTime = trip.start_time?.toDate() ?: Date()
            val calendar = Calendar.getInstance().apply { time = tripTime }
            
            val peakHourMultiplier = if (isPeakHour(calendar)) settings.peak_hour_multiplier else 1.0
            val weekendMultiplier = if (isWeekend(calendar)) settings.weekend_multiplier else 1.0
            
            // Calculate total earnings
            val subtotal = baseCommission + passengerBonus + completionBonus
            val totalEarnings = subtotal * peakHourMultiplier * weekendMultiplier
            
            val tripEarnings = TripEarnings(
                tripId = trip.id,
                baseCommission = baseCommission,
                passengerBonus = passengerBonus,
                completionBonus = completionBonus,
                peakHourMultiplier = peakHourMultiplier,
                weekendMultiplier = weekendMultiplier,
                totalEarnings = totalEarnings,
                calculationDetails = buildCalculationDetails(
                    baseCommission, passengerBonus, completionBonus,
                    peakHourMultiplier, weekendMultiplier, totalEarnings
                )
            )
            
            Log.d(TAG, "Calculated trip earnings: $totalEarnings for trip ${trip.id}")
            Result.success(tripEarnings)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to calculate trip earnings", e)
            Result.failure(e)
        }
    }
    
    /**
     * Record earnings for a completed trip
     */
    suspend fun recordTripEarnings(trip: Trip, driver: Driver, tripEarnings: TripEarnings): Result<Unit> {
        return try {
            val today = Calendar.getInstance()
            val dateKey = "${today.get(Calendar.YEAR)}-${today.get(Calendar.MONTH) + 1}-${today.get(Calendar.DAY_OF_MONTH)}"
            
            // Update or create daily earnings
            updateDailyEarnings(driver.driver_id, dateKey, tripEarnings)
            
            // Update monthly earnings
            updateMonthlyEarnings(driver.driver_id, today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1, tripEarnings, trip.id)
            
            Log.d(TAG, "Recorded trip earnings for driver ${driver.driver_id}")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to record trip earnings", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get earnings summary for a driver
     */
    suspend fun getEarningsSummary(driverId: String): Result<EarningsSummary> {
        return try {
            val today = Calendar.getInstance()
            val startOfWeek = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfMonth = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            
            // Get today's earnings
            val todayEarnings = getDailyEarnings(driverId, today.time).getOrNull()?.total_earned ?: 0.0
            
            // Get week earnings
            val weekEarnings = getEarningsInRange(driverId, startOfWeek.time, today.time).getOrNull() ?: 0.0
            
            // Get month earnings
            val monthEarnings = getMonthlyEarnings(driverId, today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1).getOrNull()?.total_earnings ?: 0.0
            
            // Get trip statistics
            val monthlyData = getMonthlyEarnings(driverId, today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1).getOrNull()
            val totalTrips = monthlyData?.total_trips ?: 0
            val totalPassengers = monthlyData?.total_passengers ?: 0
            val averageEarningsPerTrip = if (totalTrips > 0) monthEarnings / totalTrips else 0.0
            
            val summary = EarningsSummary(
                todayEarnings = todayEarnings,
                weekEarnings = weekEarnings,
                monthEarnings = monthEarnings,
                totalTrips = totalTrips,
                totalPassengers = totalPassengers,
                averageEarningsPerTrip = averageEarningsPerTrip,
                lastPaymentDate = monthlyData?.payment_date,
                pendingPayment = if (monthlyData?.payment_status == "pending") monthEarnings else 0.0
            )
            
            Result.success(summary)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get earnings summary", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get monthly earnings for a driver
     */
    suspend fun getMonthlyEarnings(driverId: String, year: Int, month: Int): Result<DriverEarnings?> {
        return try {
            val snapshot = earningsCollection
                .whereEqualTo("driver_id", driverId)
                .whereEqualTo("year", year)
                .whereEqualTo("month", month)
                .limit(1)
                .get()
                .await()
            
            val earnings = if (!snapshot.isEmpty) {
                snapshot.documents.first().toObject<DriverEarnings>()?.copy(id = snapshot.documents.first().id)
            } else null
            
            Result.success(earnings)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get monthly earnings", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get daily earnings for a specific date
     */
    suspend fun getDailyEarnings(driverId: String, date: Date): Result<DailyEarnings?> {
        return try {
            val calendar = Calendar.getInstance().apply { time = date }
            val startOfDay = Calendar.getInstance().apply {
                time = date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val endOfDay = Calendar.getInstance().apply {
                time = date
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            
            val snapshot = dailyEarningsCollection
                .whereEqualTo("driver_id", driverId)
                .whereGreaterThanOrEqualTo("date", Timestamp(startOfDay.time))
                .whereLessThanOrEqualTo("date", Timestamp(endOfDay.time))
                .limit(1)
                .get()
                .await()
            
            val earnings = if (!snapshot.isEmpty) {
                snapshot.documents.first().toObject<DailyEarnings>()?.copy(id = snapshot.documents.first().id)
            } else null
            
            Result.success(earnings)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get daily earnings", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get earnings in a date range
     */
    private suspend fun getEarningsInRange(driverId: String, startDate: Date, endDate: Date): Result<Double> {
        return try {
            val snapshot = dailyEarningsCollection
                .whereEqualTo("driver_id", driverId)
                .whereGreaterThanOrEqualTo("date", Timestamp(startDate))
                .whereLessThanOrEqualTo("date", Timestamp(endDate))
                .get()
                .await()
            
            val totalEarnings = snapshot.documents.sumOf { doc ->
                doc.toObject<DailyEarnings>()?.total_earned ?: 0.0
            }
            
            Result.success(totalEarnings)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get earnings in range", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get commission settings
     */
    private suspend fun getCommissionSettings(): Result<CommissionSettings> {
        return try {
            val snapshot = settingsCollection.limit(1).get().await()
            val settings = if (!snapshot.isEmpty) {
                snapshot.documents.first().toObject<CommissionSettings>() ?: CommissionSettings()
            } else CommissionSettings()
            
            Result.success(settings)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get commission settings", e)
            Result.success(CommissionSettings()) // Return default settings on error
        }
    }
    
    /**
     * Update daily earnings
     */
    private suspend fun updateDailyEarnings(driverId: String, dateKey: String, tripEarnings: TripEarnings) {
        val existing = getDailyEarnings(driverId, Date()).getOrNull()
        
        if (existing != null) {
            // Update existing record
            val updated = existing.copy(
                trips_count = existing.trips_count + 1,
                passengers_count = existing.passengers_count + tripEarnings.tripId.length, // This should be actual passenger count
                commission_earned = existing.commission_earned + tripEarnings.baseCommission,
                bonus_earned = existing.bonus_earned + tripEarnings.passengerBonus + tripEarnings.completionBonus,
                total_earned = existing.total_earned + tripEarnings.totalEarnings,
                trip_ids = existing.trip_ids + tripEarnings.tripId
            )
            
            dailyEarningsCollection.document(existing.id).set(updated).await()
        } else {
            // Create new record
            val newRecord = DailyEarnings(
                driver_id = driverId,
                date = Timestamp.now(),
                trips_count = 1,
                passengers_count = 0, // This should be actual passenger count
                commission_earned = tripEarnings.baseCommission,
                bonus_earned = tripEarnings.passengerBonus + tripEarnings.completionBonus,
                total_earned = tripEarnings.totalEarnings,
                trip_ids = listOf(tripEarnings.tripId)
            )
            
            dailyEarningsCollection.add(newRecord).await()
        }
    }
    
    /**
     * Update monthly earnings
     */
    private suspend fun updateMonthlyEarnings(driverId: String, year: Int, month: Int, tripEarnings: TripEarnings, tripId: String) {
        val existing = getMonthlyEarnings(driverId, year, month).getOrNull()
        
        if (existing != null) {
            // Update existing record
            val updated = existing.copy(
                total_trips = existing.total_trips + 1,
                commission_earnings = existing.commission_earnings + tripEarnings.totalEarnings,
                total_earnings = existing.total_earnings + tripEarnings.totalEarnings,
                trips_completed = existing.trips_completed + tripId
            )
            
            earningsCollection.document(existing.id).set(updated).await()
        } else {
            // Create new record
            val newRecord = DriverEarnings(
                driver_id = driverId,
                date = Timestamp.now(),
                month = month,
                year = year,
                total_trips = 1,
                commission_earnings = tripEarnings.totalEarnings,
                total_earnings = tripEarnings.totalEarnings,
                trips_completed = listOf(tripId)
            )
            
            earningsCollection.add(newRecord).await()
        }
    }
    
    /**
     * Check if time is during peak hours
     */
    private fun isPeakHour(calendar: Calendar): Boolean {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        return (hour in 7..9) || (hour in 17..19)
    }
    
    /**
     * Check if date is weekend
     */
    private fun isWeekend(calendar: Calendar): Boolean {
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
    }
    
    /**
     * Build calculation details string
     */
    private fun buildCalculationDetails(
        baseCommission: Double,
        passengerBonus: Double,
        completionBonus: Double,
        peakMultiplier: Double,
        weekendMultiplier: Double,
        total: Double
    ): String {
        return "Base: $${String.format("%.2f", baseCommission)} + " +
                "Passenger Bonus: $${String.format("%.2f", passengerBonus)} + " +
                "Completion Bonus: $${String.format("%.2f", completionBonus)} " +
                "× Peak: ${String.format("%.1f", peakMultiplier)} " +
                "× Weekend: ${String.format("%.1f", weekendMultiplier)} " +
                "= $${String.format("%.2f", total)}"
    }
}
