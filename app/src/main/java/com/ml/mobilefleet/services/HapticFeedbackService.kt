package com.ml.mobilefleet.services

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresApi

/**
 * Haptic feedback service for enhanced user experience
 * Provides different vibration patterns for various trip events
 */
class HapticFeedbackService(private val context: Context) {
    
    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
    
    /**
     * Success haptic feedback for trip start
     */
    fun tripStartSuccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 100, 50, 100)
            val amplitudes = intArrayOf(0, 255, 0, 255)
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 100, 50, 100), -1)
        }
    }
    
    /**
     * Celebration haptic feedback for trip completion
     */
    fun tripCompletionSuccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 50, 50, 100, 50, 150, 50, 200)
            val amplitudes = intArrayOf(0, 128, 0, 180, 0, 220, 0, 255)
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 50, 50, 100, 50, 150, 50, 200), -1)
        }
    }
    
    /**
     * Light haptic feedback for QR scan success
     */
    fun qrScanSuccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }
    
    /**
     * Gentle haptic feedback for passenger count changes
     */
    fun passengerCountChange() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(30, 100))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(30)
        }
    }
    
    /**
     * Error haptic feedback
     */
    fun error() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 200, 100, 200)
            val amplitudes = intArrayOf(0, 255, 0, 255)
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 200, 100, 200), -1)
        }
    }
    
    /**
     * Check if device supports vibration
     */
    fun hasVibrator(): Boolean {
        return vibrator.hasVibrator()
    }
}
