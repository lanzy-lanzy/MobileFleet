package com.ml.mobilefleet.ui.trip

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.ml.mobilefleet.data.models.Terminal
import com.ml.mobilefleet.ui.scanner.QRCodeScanner
import com.ml.mobilefleet.ui.components.QRCodeImage
import com.ml.mobilefleet.ui.components.TripStartModal
import com.ml.mobilefleet.services.TextToSpeechService
import com.ml.mobilefleet.services.HapticFeedbackService

// Custom composables for enhanced UI
@Composable
private fun StepIndicator(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isCompleted: Boolean = false,
    isActive: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Step number circle
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (isCompleted) MaterialTheme.colorScheme.primary
                    else if (isActive) containerColor
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
                .border(
                    width = if (isActive) 2.dp else 0.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Step content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = if (isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StartTripScreen(
    viewModel: TripViewModel,
    onTripStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val terminals by viewModel.terminals.collectAsStateWithLifecycle()

    // Initialize services
    val textToSpeechService = remember { TextToSpeechService(context) }

    // Initialize TTS service
    LaunchedEffect(Unit) {
        textToSpeechService.initialize()
        viewModel.setTextToSpeechService(textToSpeechService)
    }
    val currentTrip by viewModel.currentTrip.collectAsStateWithLifecycle()

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    var showScanner by remember { mutableStateOf(false) }
    var passengers by remember { mutableStateOf("") }
    var selectedDestination by remember { mutableStateOf<Terminal?>(null) }
    var expanded by remember { mutableStateOf(false) }

    // Check if there's already an active trip
    LaunchedEffect(currentTrip) {
        if (currentTrip != null) {
            onTripStarted()
        }
    }

    // Handle trip started
    LaunchedEffect(uiState.tripStarted) {
        if (uiState.tripStarted) {
            onTripStarted()
        }
    }

    // Determine current step
    val currentStep = when {
        uiState.startTerminal == null -> 1
        !uiState.showPassengerInput -> 1
        passengers.isBlank() || selectedDestination == null -> 2
        else -> 3
    }
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Background gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface
                        ),
                        startY = 0f,
                        endY = 800f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Modern Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    MaterialTheme.colorScheme.secondary
                                ),
                                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
                            )
                        )
                        .padding(32.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Floating icon
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .shadow(
                                    elevation = 16.dp,
                                    shape = CircleShape,
                                    ambientColor = Color.Black.copy(alpha = 0.3f)
                                )
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBus,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Title section
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Start Your Journey",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Follow the steps below to begin your trip",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.9f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            if (showScanner && cameraPermissionState.status.isGranted) {
                // Modern QR Code Scanner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        QRCodeScanner(
                            onQRCodeScanned = { qrCode ->
                                showScanner = false
                                viewModel.onStartTerminalScanned(qrCode)
                            },
                            onError = { _ ->
                                showScanner = false
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Scanner overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(250.dp)
                                    .border(
                                        width = 3.dp,
                                        color = Color.White,
                                        shape = RoundedCornerShape(24.dp)
                                    )
                            )
                        }

                        // Close button
                        IconButton(
                            onClick = { showScanner = false },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.5f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close scanner",
                                tint = Color.White
                            )
                        }
                    }
                }
            } else {
                // Step 1: QR Code Scanning
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Step indicator
                        StepIndicator(
                            title = "Scan Terminal QR Code",
                            subtitle = "Point your camera at the terminal QR code",
                            icon = Icons.Default.CameraAlt,
                            isActive = currentStep == 1,
                            isCompleted = uiState.startTerminal != null,
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )

                        AnimatedVisibility(
                            visible = uiState.startTerminal != null,
                            enter = slideInVertically() + fadeIn(),
                            exit = slideOutVertically() + fadeOut()
                        ) {
                            uiState.startTerminal?.let { terminal ->
                                // Success state - Terminal scanned (Clean, minimal design)
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        // Success icon - smaller and cleaner
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        // Terminal info - horizontal layout
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "Terminal Scanned Successfully!",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.LocationOn,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = terminal.name,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = uiState.startTerminal == null,
                            enter = slideInVertically() + fadeIn(),
                            exit = slideOutVertically() + fadeOut()
                        ) {
                            // Scan prompt state
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                // Animated QR placeholder
                                val infiniteTransition = rememberInfiniteTransition(label = "qr_animation")
                                val animatedAlpha by infiniteTransition.animateFloat(
                                    initialValue = 0.3f,
                                    targetValue = 0.8f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(2000),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "alpha_animation"
                                )

                                Box(
                                    modifier = Modifier
                                        .size(140.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = animatedAlpha)
                                        )
                                        .border(
                                            width = 2.dp,
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(24.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = animatedAlpha)
                                    )
                                }

                                Text(
                                    text = "Tap to scan terminal QR code",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )

                                // Modern scan button
                                Button(
                                    onClick = {
                                        if (cameraPermissionState.status.isGranted) {
                                            showScanner = true
                                        } else {
                                            cameraPermissionState.launchPermissionRequest()
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(64.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 8.dp,
                                        pressedElevation = 16.dp
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = null,
                                            modifier = Modifier.size(28.dp),
                                            tint = Color.White
                                        )
                                        Text(
                                            text = "Start Scanning",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Step 2: Passenger Input Section
            AnimatedVisibility(
                visible = uiState.showPassengerInput,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Step indicator
                        StepIndicator(
                            title = "Enter Passenger Count",
                            subtitle = "How many passengers are boarding?",
                            icon = Icons.Default.Person,
                            isActive = currentStep == 2,
                            isCompleted = passengers.isNotBlank() && passengers.toIntOrNull() != null,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )

                        // Passenger input with modern styling
                        OutlinedTextField(
                            value = passengers,
                            onValueChange = { newValue ->
                                // Only allow numbers
                                if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                    passengers = newValue
                                }
                            },
                            label = {
                                Text(
                                    "Passenger Count",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            placeholder = {
                                Text(
                                    "Enter number of passengers",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            trailingIcon = {
                                if (passengers.isNotBlank() && passengers.toIntOrNull() != null) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }

            // Step 3: Destination Selection
            AnimatedVisibility(
                visible = uiState.showPassengerInput && passengers.isNotBlank(),
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Step indicator
                        StepIndicator(
                            title = "Select Destination",
                            subtitle = "Choose your destination terminal",
                            icon = Icons.Default.LocationOn,
                            isActive = currentStep >= 2,
                            isCompleted = selectedDestination != null,
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )

                        // Modern dropdown
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedDestination?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = {
                                    Text(
                                        "Destination Terminal",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                placeholder = {
                                    Text(
                                        "Choose destination",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                terminals.forEach { terminal ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.LocationOn,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(
                                                    text = terminal.name,
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedDestination = terminal
                                            expanded = false
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Start Trip Button - Always visible when all steps are complete
            AnimatedVisibility(
                visible = uiState.showPassengerInput &&
                         passengers.isNotBlank() &&
                         selectedDestination != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Trip summary card - Clean and minimal
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Trip Summary",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Route display - cleaner layout
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // From terminal
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        text = "From",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = uiState.startTerminal?.name ?: "Unknown",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // Arrow - smaller and subtle
                                Icon(
                                    imageVector = Icons.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )

                                // To terminal
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "To",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = selectedDestination?.name ?: "Unknown",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Passenger count - subtle styling
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "$passengers passengers",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Enhanced Start Trip Button
                    Button(
                        onClick = {
                            val passengerCount = passengers.toIntOrNull()
                            if (passengerCount != null && selectedDestination != null) {
                                viewModel.startTrip(selectedDestination!!.id, passengerCount)
                            }
                        },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 16.dp,
                            pressedElevation = 24.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    strokeWidth = 4.dp,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.DirectionsBus,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                    tint = Color.White
                                )
                            }
                            Text(
                                text = if (uiState.isLoading) "Starting Your Trip..." else "Start Trip",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Error Message with modern styling
            uiState.errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Bottom spacing
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Trip Start Modal - positioned as overlay
        TripStartModal(
            isVisible = uiState.showTripStartModal,
            startTerminalName = uiState.startTerminal?.name ?: "",
            destinationTerminalName = uiState.destinationTerminalName ?: "",
            passengerCount = passengers.toIntOrNull() ?: 0,
            onDismiss = viewModel::dismissTripStartModal
        )
    }
}
