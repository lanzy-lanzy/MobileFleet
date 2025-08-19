package com.ml.mobilefleet.data.service

import android.content.Context
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import io.github.cdimascio.dotenv.dotenv
import java.io.File

/**
 * Service for handling Cloudinary operations
 * Manages QR code image URLs and other media operations
 */
class CloudinaryService(private val context: Context) {
    
    private var isInitialized = false
    
    init {
        initializeCloudinary()
    }
    
    /**
     * Initialize Cloudinary with credentials from .env file
     */
    private fun initializeCloudinary() {
        try {
            // Load environment variables
            val dotenv = dotenv {
                directory = "/android_asset"
                filename = "env" // Will look for assets/env file
            }
            
            val cloudName = dotenv["CLOUDINARY_CLOUD_NAME"]
            val apiKey = dotenv["CLOUDINARY_API_KEY"] 
            val apiSecret = dotenv["CLOUDINARY_API_SECRET"]
            
            if (cloudName != null && apiKey != null && apiSecret != null) {
                val config = mapOf(
                    "cloud_name" to cloudName,
                    "api_key" to apiKey,
                    "api_secret" to apiSecret
                )
                
                MediaManager.init(context, config)
                isInitialized = true
            } else {
                // Fallback to hardcoded values if .env not accessible
                initializeWithHardcodedValues()
            }
        } catch (e: Exception) {
            // Fallback to hardcoded values
            initializeWithHardcodedValues()
        }
    }
    
    /**
     * Fallback initialization with hardcoded Cloudinary credentials
     */
    private fun initializeWithHardcodedValues() {
        try {
            val config = mapOf(
                "cloud_name" to "dquzz14x9",
                "api_key" to "217121185485512",
                "api_secret" to "nyL4eFp36DqRNIv4sfwibroYcMY"
            )
            
            MediaManager.init(context, config)
            isInitialized = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Get optimized QR code image URL from Cloudinary
     */
    fun getQrCodeImageUrl(publicId: String, width: Int = 300, height: Int = 300): String {
        // Use direct URL construction for simplicity and reliability
        return "https://res.cloudinary.com/dquzz14x9/image/upload/w_$width,h_$height,c_fill,q_auto,f_auto/$publicId"
    }
    
    /**
     * Get thumbnail URL for QR code
     */
    fun getQrCodeThumbnailUrl(publicId: String): String {
        return getQrCodeImageUrl(publicId, 150, 150)
    }
    
    /**
     * Extract public ID from Cloudinary URL
     * Handles URLs like: https://res.cloudinary.com/dquzz14x9/image/upload/v1755531771/qr_codes/terminal_3kkrj6sGsHt3SOe6GNby.png
     */
    fun extractPublicIdFromUrl(cloudinaryUrl: String): String? {
        return try {
            // Extract public ID from URL like: https://res.cloudinary.com/cloud/image/upload/v123/folder/image.jpg
            val regex = Regex(".*/upload/(?:v\\d+/)?(.+?)(?:\\.[^.]+)?$")
            val matchResult = regex.find(cloudinaryUrl)
            matchResult?.groupValues?.get(1)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Check if Cloudinary is properly initialized
     */
    fun isReady(): Boolean = isInitialized
    
    /**
     * Upload QR code image (for future use if needed)
     */
    fun uploadQrCodeImage(
        imageFile: File,
        publicId: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isInitialized) {
            onError("Cloudinary not initialized")
            return
        }
        
        MediaManager.get()
            .upload(imageFile.absolutePath)
            .option("public_id", publicId)
            .option("folder", "qr_codes")
            .option("resource_type", "image")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    // Upload started
                }
                
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    // Upload progress
                }
                
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val secureUrl = resultData["secure_url"] as? String
                    if (secureUrl != null) {
                        onSuccess(secureUrl)
                    } else {
                        onError("Failed to get upload URL")
                    }
                }
                
                override fun onError(requestId: String, error: ErrorInfo) {
                    onError("Upload failed: ${error.description}")
                }
                
                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    // Upload rescheduled
                }
            })
            .dispatch()
    }
}
