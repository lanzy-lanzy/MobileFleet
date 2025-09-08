package com.ml.mobilefleet.ui.trip

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import kotlinx.coroutines.delay
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.ml.mobilefleet.ui.scanner.QRCodeScanner
import com.ml.mobilefleet.ui.components.TripCompletionModal
import com.ml.mobilefleet.services.TextToSpeechService

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CompleteTripScreen(
    viewModel: TripViewModel,
    onTripCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentTrip by viewModel.currentTrip.collectAsStateWithLifecycle()
    val terminalNames by viewModel.terminalNames.collectAsStateWithLifecycle()

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    var showScanner by remember { mutableStateOf(false) }

    // Initialize TTS service
    val textToSpeechService = remember { TextToSpeechService(context) }

    LaunchedEffect(Unit) {
        textToSpeechService.initialize()
        viewModel.setTextToSpeechService(textToSpeechService)
    }
    
    // Handle trip completion - delay to allow modal and speech to complete
    LaunchedEffect(uiState.tripCompleted) {
        if (uiState.tripCompleted) {
            // Wait for modal display time and speech to complete before navigating
            delay(12000) // 12 seconds to ensure modal and speech complete
            onTripCompleted()
        }
    }
    
    // Scroll state for smooth scrolling
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Redesigned Header - Clean and Professional
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Clean icon container
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "🏁",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }

                Text(
                    text = "Complete Trip",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Scan the destination terminal QR code",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // Redesigned Trip Details Card - Clean and Organized
        currentTrip?.let { trip ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Clean header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "🚌",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }

                        Text(
                            text = "Trip Information",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Clean Route Information
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // From Terminal - Clean Design
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "From",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = terminalNames[trip.start_terminal] ?: "Loading...",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Destination Terminal - Clean Design
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "To",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = terminalNames[trip.destination_terminal] ?: "Loading...",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Passenger Count - Clean Interactive Design
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Passengers",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${trip.passengers} ${if (trip.passengers == 1) "passenger" else "passengers"}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // Clean Passenger Controls
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    shadowElevation = 1.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        // Decrease Button
                                        Surface(
                                            onClick = {
                                                if (trip.passengers > 0) {
                                                    viewModel.updatePassengerCount(trip.passengers - 1)
                                                }
                                            },
                                            enabled = trip.passengers > 0,
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (trip.passengers > 0)
                                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                                            else
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "−",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (trip.passengers > 0)
                                                        MaterialTheme.colorScheme.error
                                                    else
                                                        MaterialTheme.colorScheme.outline
                                                )
                                            }
                                        }

                                        // Count Display
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = trip.passengers.toString(),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }

                                        // Increase Button
                                        Surface(
                                            onClick = {
                                                viewModel.updatePassengerCount(trip.passengers + 1)
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "+",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Clean Status Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = when (trip.status.lowercase()) {
                                    "in_progress" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    "completed" -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                                    else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                when (trip.status.lowercase()) {
                                                    "in_progress" -> MaterialTheme.colorScheme.primary
                                                    "completed" -> MaterialTheme.colorScheme.tertiary
                                                    else -> MaterialTheme.colorScheme.secondary
                                                }
                                            )
                                    )
                                    Text(
                                        text = trip.status.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = when (trip.status.lowercase()) {
                                            "in_progress" -> MaterialTheme.colorScheme.primary
                                            "completed" -> MaterialTheme.colorScheme.tertiary
                                            else -> MaterialTheme.colorScheme.secondary
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // QR Scanner Section
        if (showScanner && cameraPermissionState.status.isGranted) {
            // QR Code Scanner - Full height for better camera preview
            QRCodeScanner(
                onQRCodeScanned = { qrCode ->
                    showScanner = false
                    viewModel.onDestinationTerminalScanned(qrCode)
                },
                onError = { _ ->
                    showScanner = false
                    // Handle error
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp) // Fixed height for better camera preview
            )
        }

        if (!showScanner) {
            // Clean QR Scanner Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Clean QR icon
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "📱",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Scan QR Code",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Point your camera at the destination terminal QR code",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Clean scan button
                    Button(
                        onClick = {
                            if (cameraPermissionState.status.isGranted) {
                                showScanner = true
                            } else {
                                cameraPermissionState.launchPermissionRequest()
                            }
                        },
                        enabled = !uiState.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Start Scanning",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Bottom spacing for better scrolling
        Spacer(modifier = Modifier.height(16.dp))
    }

    // Trip Completion Modal
    TripCompletionModal(
        isVisible = uiState.showTripCompletionModal,
        destinationTerminalName = uiState.destinationTerminalName ?: "",
        passengerCount = currentTrip?.passengers ?: 0,
        startTime = uiState.tripStartTime ?: System.currentTimeMillis(),
        onDismiss = { viewModel.dismissTripCompletionModal() }
    )
}
