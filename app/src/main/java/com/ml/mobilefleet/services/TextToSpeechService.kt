package com.ml.mobilefleet.services

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

/**
 * TextToSpeech service for trip announcements
 * Provides a clean interface for speech synthesis with lifecycle management
 */
class TextToSpeechService(private val context: Context) {
    
    companion object {
        private const val TAG = "TextToSpeechService"
        private const val UTTERANCE_ID_PREFIX = "fleet_announcement_"
    }
    
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false
    private var utteranceCounter = 0
    
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()
    
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()
    
    /**
     * Initialize TextToSpeech engine
     */
    fun initialize() {
        if (textToSpeech != null) return
        
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.let { tts ->
                    // Set language to English (US)
                    val result = tts.setLanguage(Locale.US)
                    
                    if (result == TextToSpeech.LANG_MISSING_DATA || 
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.w(TAG, "Language not supported")
                    } else {
                        // Configure speech parameters for better clarity
                        tts.setSpeechRate(0.8f) // Slower for better clarity and understanding
                        tts.setPitch(1.0f) // Normal pitch
                        
                        // Set up utterance progress listener
                        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) {
                                _isSpeaking.value = true
                                Log.d(TAG, "Speech started: $utteranceId")
                            }
                            
                            override fun onDone(utteranceId: String?) {
                                _isSpeaking.value = false
                                Log.d(TAG, "Speech completed: $utteranceId")
                            }
                            
                            override fun onError(utteranceId: String?) {
                                _isSpeaking.value = false
                                Log.e(TAG, "Speech error: $utteranceId")
                            }
                        })
                        
                        isInitialized = true
                        _isReady.value = true
                        Log.d(TAG, "TextToSpeech initialized successfully")
                    }
                }
            } else {
                Log.e(TAG, "TextToSpeech initialization failed")
                _isReady.value = false
            }
        }
    }
    
    /**
     * Announce trip start
     */
    fun announceTripStart(
        startTerminalName: String,
        destinationTerminalName: String,
        passengerCount: Int
    ) {
        val message = "Starting trip to $destinationTerminalName. Proceed to destination."
        speak(message, "trip_start")
    }

    /**
     * Announce trip completion
     */
    fun announceTripCompletion(
        destinationTerminalName: String,
        passengerCount: Int
    ) {
        val message = "Arrived at destination $destinationTerminalName."
        speak(message, "trip_completion")
    }
    
    /**
     * Announce passenger count update
     */
    fun announcePassengerUpdate(newCount: Int) {
        val message = "Passenger count updated to $newCount"
        speak(message, "passenger_update")
    }
    
    /**
     * Announce QR code scan success
     */
    fun announceQrScanSuccess(terminalName: String) {
        val message = "Terminal $terminalName scanned successfully"
        speak(message, "qr_scan_success")
    }
    
    /**
     * Generic speak method
     */
    private fun speak(text: String, type: String) {
        if (!isInitialized || textToSpeech == null) {
            Log.w(TAG, "TextToSpeech not initialized")
            return
        }
        
        val utteranceId = "${UTTERANCE_ID_PREFIX}${type}_${++utteranceCounter}"
        
        textToSpeech?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH, // Replace any current speech
            null,
            utteranceId
        )
        
        Log.d(TAG, "Speaking: $text")
    }
    
    /**
     * Stop current speech
     */
    fun stop() {
        textToSpeech?.stop()
        _isSpeaking.value = false
    }
    
    /**
     * Check if TTS is currently speaking
     */
    fun isSpeaking(): Boolean {
        return textToSpeech?.isSpeaking == true
    }
    
    /**
     * Shutdown TextToSpeech engine
     */
    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isInitialized = false
        _isReady.value = false
        _isSpeaking.value = false
        Log.d(TAG, "TextToSpeech shutdown")
    }
}
