package com.ml.mobilefleet.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

/** Trip Start Modal with animations and visual effects */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripStartModal(
        isVisible: Boolean,
        startTerminalName: String,
        destinationTerminalName: String,
        passengerCount: Int,
        onDismiss: () -> Unit,
        modifier: Modifier = Modifier,
        minDisplayTime: Long =
                8000L // Minimum time to display modal (8 seconds for speech completion)
) {
    if (isVisible) {
        Dialog(
                onDismissRequest = onDismiss,
                properties =
                        DialogProperties(
                                dismissOnBackPress = true,
                                dismissOnClickOutside = true,
                                usePlatformDefaultWidth = false
                        )
        ) {
            TripStartModalContent(
                    startTerminalName = startTerminalName,
                    destinationTerminalName = destinationTerminalName,
                    passengerCount = passengerCount,
                    onDismiss = onDismiss,
                    minDisplayTime = minDisplayTime,
                    modifier = modifier
            )
        }
    }
}

@Composable
private fun TripStartModalContent(
        startTerminalName: String,
        destinationTerminalName: String,
        passengerCount: Int,
        onDismiss: () -> Unit,
        minDisplayTime: Long,
        modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    // Animation states
    val scale by
            animateFloatAsState(
                    targetValue = if (isVisible) 1f else 0.8f,
                    animationSpec =
                            spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                            ),
                    label = "scale"
            )

    val alpha by
            animateFloatAsState(
                    targetValue = if (isVisible) 1f else 0f,
                    animationSpec = tween(400),
                    label = "alpha"
            )

    // Bouncing animation for success icon
    val iconOffset by
            animateFloatAsState(
                    targetValue = if (isVisible) 0f else 30f,
                    animationSpec =
                            spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                            ),
                    label = "icon_offset"
            )

    // Pulsing animation for success icon
    val pulseScale by
            animateFloatAsState(
                    targetValue = if (isVisible) 1.1f else 1f,
                    animationSpec =
                            infiniteRepeatable(
                                    animation = tween(1200),
                                    repeatMode = RepeatMode.Reverse
                            ),
                    label = "pulse"
            )

    // Gradient colors - enhanced
    val gradientColors =
            listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
            )

    LaunchedEffect(Unit) {
        isVisible = true
        // Auto-dismiss after specified time to allow TTS to complete
        delay(minDisplayTime)
        onDismiss()
    }

    Box(
            modifier = modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
    ) {
        Card(
                modifier =
                        Modifier.fillMaxWidth(0.88f).scale(scale).alpha(alpha).wrapContentHeight(),
                shape = RoundedCornerShape(32.dp),
                colors =
                        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 24.dp)
        ) {
            Column(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .background(brush = Brush.verticalGradient(gradientColors))
                                    .padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Success Icon with bounce and pulse animation
                Box(
                        modifier = Modifier.offset(y = iconOffset.dp),
                        contentAlignment = Alignment.Center
                ) {
                    // Background glow circle
                    Box(
                            modifier =
                                    Modifier.size(100.dp)
                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                            .background(
                                                    MaterialTheme.colorScheme.primary.copy(
                                                            alpha = 0.15f
                                                    )
                                            )
                    )

                    Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Trip Started",
                            modifier = Modifier.size(72.dp).scale(pulseScale),
                            tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Title with better visual hierarchy
                Text(
                        text = "Trip Started!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                )

                Text(
                        text = "Your journey is now active",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                )

                // Trip Details Card - Enhanced design
                Card(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)),
                        colors =
                                CardDefaults.cardColors(
                                        containerColor =
                                                MaterialTheme.colorScheme.primaryContainer.copy(
                                                        alpha = 0.4f
                                                )
                                ),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // From Terminal
                        TripDetailRow(
                                icon = Icons.Default.LocationOn,
                                label = "From",
                                value = startTerminalName,
                                iconTint = MaterialTheme.colorScheme.primary
                        )

                        // Divider
                        Box(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .height(1.dp)
                                                .background(
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                                .copy(alpha = 0.1f)
                                                )
                        )

                        // To Terminal
                        TripDetailRow(
                                icon = Icons.Default.NavigateNext,
                                label = "To",
                                value = destinationTerminalName,
                                iconTint = MaterialTheme.colorScheme.secondary
                        )

                        // Divider
                        Box(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .height(1.dp)
                                                .background(
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                                .copy(alpha = 0.1f)
                                                )
                        )

                        // Passenger Count
                        TripDetailRow(
                                icon = Icons.Default.Person,
                                label = "Passengers",
                                value = "$passengerCount",
                                iconTint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                // Motivational Message with better styling
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                                CardDefaults.cardColors(
                                        containerColor =
                                                MaterialTheme.colorScheme.tertiary.copy(
                                                        alpha = 0.15f
                                                )
                                ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                            text = "Safe travels on your journey!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(16.dp)
                    )
                }

                // Speaking Indicator with animation
                Row(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .background(
                                                MaterialTheme.colorScheme.secondary.copy(
                                                        alpha = 0.1f
                                                ),
                                                RoundedCornerShape(12.dp)
                                        )
                                        .padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    // Animated dots
                    repeat(3) { index ->
                        val dotAlpha by
                                animateFloatAsState(
                                        targetValue = if (isVisible) 1f else 0.3f,
                                        animationSpec =
                                                infiniteRepeatable(
                                                        animation =
                                                                tween(
                                                                        1000,
                                                                        delayMillis = index * 200
                                                                ),
                                                        repeatMode = RepeatMode.Reverse
                                                ),
                                        label = "dot_$index"
                                )

                        Box(
                                modifier =
                                        Modifier.size(6.dp)
                                                .clip(androidx.compose.foundation.shape.CircleShape)
                                                .background(
                                                        MaterialTheme.colorScheme.secondary.copy(
                                                                alpha = dotAlpha
                                                        )
                                                )
                        )

                        if (index < 2) {
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                            text = "Announcing trip details",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold
                    )
                }

                // Dismiss Button - Enhanced
                Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                ),
                        shape = RoundedCornerShape(16.dp),
                        elevation =
                                ButtonDefaults.buttonElevation(
                                        defaultElevation = 12.dp,
                                        pressedElevation = 20.dp
                                )
                ) {
                    Text(
                            text = "Continue",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun TripDetailRow(
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        label: String,
        value: String,
        iconTint: Color,
        modifier: Modifier = Modifier
) {
    Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
            )
            Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
            )
        }
    }
}
