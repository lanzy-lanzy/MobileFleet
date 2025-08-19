package com.ml.mobilefleet.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Trip Completion Modal with celebration effects
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripCompletionModal(
    isVisible: Boolean,
    destinationTerminalName: String,
    passengerCount: Int,
    startTime: Long,
    endTime: Long = System.currentTimeMillis(),
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    minDisplayTime: Long = 12000L // Minimum time to display modal (12 seconds for speech completion and celebration)
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            TripCompletionModalContent(
                destinationTerminalName = destinationTerminalName,
                passengerCount = passengerCount,
                startTime = startTime,
                endTime = endTime,
                onDismiss = onDismiss,
                minDisplayTime = minDisplayTime,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun TripCompletionModalContent(
    destinationTerminalName: String,
    passengerCount: Int,
    startTime: Long,
    endTime: Long,
    onDismiss: () -> Unit,
    minDisplayTime: Long,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    
    // Animation states
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(300),
        label = "alpha"
    )
    
    // Celebration animations
    val celebrationScale by animateFloatAsState(
        targetValue = if (showConfetti) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "celebration"
    )
    
    val rotation by animateFloatAsState(
        targetValue = if (showConfetti) 360f else 0f,
        animationSpec = tween(1000),
        label = "rotation"
    )
    
    // Calculate trip duration
    val durationMinutes = ((endTime - startTime) / 60000).toInt()
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    // Gradient colors for celebration
    val celebrationGradient = listOf(
        Color(0xFFFFD700), // Gold
        Color(0xFFFF6B6B), // Coral
        Color(0xFF4ECDC4), // Turquoise
        Color(0xFF45B7D1)  // Blue
    )
    
    LaunchedEffect(Unit) {
        isVisible = true
        delay(500)
        showConfetti = true
        // Auto-dismiss after specified time to allow TTS to complete and celebration to be enjoyed
        delay(minDisplayTime)
        onDismiss()
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        // Confetti effect background
        if (showConfetti) {
            repeat(8) { index ->
                ConfettiParticle(
                    delay = index * 100L,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .scale(scale)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.radialGradient(
                            colors = celebrationGradient.map { it.copy(alpha = 0.1f) },
                            radius = 800f
                        )
                    )
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Celebration Icon with animations
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    // Background circle with gradient
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .scale(celebrationScale)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                                    )
                                )
                            )
                    )
                    
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Trip Completed",
                        modifier = Modifier
                            .size(72.dp)
                            .scale(celebrationScale),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    // Rotating stars around the icon
                    repeat(4) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                                .offset(
                                    x = (40 * cos(Math.toRadians((rotation + index * 90).toDouble()))).dp,
                                    y = (40 * sin(Math.toRadians((rotation + index * 90).toDouble()))).dp
                                ),
                            tint = Color(0xFFFFD700)
                        )
                    }
                }
                
                // Celebration Title
                Text(
                    text = "🎉 Trip Completed! 🎉",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Excellent work, driver!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
                
                // Trip Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Trip Summary",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        // Destination
                        TripSummaryRow(
                            icon = Icons.Default.LocationOn,
                            label = "Destination",
                            value = destinationTerminalName,
                            iconTint = MaterialTheme.colorScheme.primary
                        )
                        
                        // Passengers
                        TripSummaryRow(
                            icon = Icons.Default.Person,
                            label = "Final Passengers",
                            value = "$passengerCount",
                            iconTint = MaterialTheme.colorScheme.secondary
                        )

                        // Duration
                        TripSummaryRow(
                            icon = Icons.Default.Info,
                            label = "Duration",
                            value = "${durationMinutes} minutes",
                            iconTint = MaterialTheme.colorScheme.tertiary
                        )
                        
                        // Completion Time
                        TripSummaryRow(
                            icon = Icons.Default.CheckCircle,
                            label = "Completed at",
                            value = timeFormatter.format(Date(endTime)),
                            iconTint = Color(0xFF4CAF50)
                        )
                    }
                }
                
                // Motivational Message
                Text(
                    text = "Thank you for your safe driving! 🚌✨",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )

                // Speaking Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔊 Announcing trip completion...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Continue",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TripSummaryRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(28.dp)
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ConfettiParticle(
    delay: Long,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    val offsetY by animateFloatAsState(
        targetValue = if (isVisible) 1000f else -100f,
        animationSpec = tween(2000, delayMillis = delay.toInt()),
        label = "confetti_fall"
    )
    
    val rotation by animateFloatAsState(
        targetValue = if (isVisible) 720f else 0f,
        animationSpec = tween(2000, delayMillis = delay.toInt()),
        label = "confetti_rotation"
    )
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delay)
        isVisible = true
    }
    
    Box(
        modifier = modifier
            .offset(
                x = (50..350).random().dp,
                y = offsetY.dp
            )
            .size(8.dp)
            .rotate(rotation)
            .clip(CircleShape)
            .background(
                listOf(
                    Color(0xFFFFD700),
                    Color(0xFFFF6B6B),
                    Color(0xFF4ECDC4),
                    Color(0xFF45B7D1),
                    Color(0xFFFF9FF3)
                ).random()
            )
    )
}
