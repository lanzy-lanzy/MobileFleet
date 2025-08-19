package com.ml.mobilefleet.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ml.mobilefleet.data.service.CloudinaryService

/**
 * Composable for displaying QR code images from Cloudinary
 */
@Composable
fun QRCodeImage(
    qrUrl: String,
    terminalName: String,
    modifier: Modifier = Modifier,
    size: Int = 200,
    showLabel: Boolean = true
) {
    val context = LocalContext.current
    val cloudinaryService = remember { CloudinaryService(context) }
    
    var imageUrl by remember(qrUrl) { 
        mutableStateOf<String?>(null) 
    }
    
    // Process the QR URL to get the optimized Cloudinary URL
    LaunchedEffect(qrUrl) {
        if (qrUrl.isNotBlank()) {
            imageUrl = if (qrUrl.contains("cloudinary.com")) {
                // If it's already a Cloudinary URL, extract public ID and optimize
                val publicId = cloudinaryService.extractPublicIdFromUrl(qrUrl)
                if (publicId != null) {
                    cloudinaryService.getQrCodeImageUrl(publicId, size, size)
                } else {
                    qrUrl // Use original URL if extraction fails
                }
            } else {
                // If it's a public ID, generate Cloudinary URL
                cloudinaryService.getQrCodeImageUrl(qrUrl, size, size)
            }
        }
    }
    
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (showLabel) {
                Text(
                    text = terminalName,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            }
            
            Box(
                modifier = Modifier.size(size.dp),
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "QR Code for $terminalName",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    // Loading placeholder
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "Loading QR Code...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Thumbnail version of QR Code Image
 */
@Composable
fun QRCodeThumbnail(
    qrUrl: String,
    terminalName: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val cloudinaryService = remember { CloudinaryService(context) }
    
    var thumbnailUrl by remember(qrUrl) { 
        mutableStateOf<String?>(null) 
    }
    
    LaunchedEffect(qrUrl) {
        if (qrUrl.isNotBlank()) {
            thumbnailUrl = if (qrUrl.contains("cloudinary.com")) {
                val publicId = cloudinaryService.extractPublicIdFromUrl(qrUrl)
                if (publicId != null) {
                    cloudinaryService.getQrCodeThumbnailUrl(publicId)
                } else {
                    qrUrl
                }
            } else {
                cloudinaryService.getQrCodeThumbnailUrl(qrUrl)
            }
        }
    }
    
    Card(
        modifier = modifier.then(
            if (onClick != null) {
                Modifier.clickable { onClick() }
            } else {
                Modifier
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(60.dp),
                contentAlignment = Alignment.Center
            ) {
                if (thumbnailUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "QR Code thumbnail for $terminalName",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 1.dp
                    )
                }
            }
            
            Text(
                text = terminalName,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}
