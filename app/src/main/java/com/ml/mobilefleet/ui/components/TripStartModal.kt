package com.ml.mobilefleet.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

/**
 * Trip Start Modal with animations and visual effects
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripStartModal(
    isVisible: Boolean,
    startTerminalName: String,
    destinationTerminalName: String,
    passengerCount: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    minDisplayTime: Long = 8000L // Minimum time to display modal (8 seconds for speech completion)
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
    
    // Pulsing animation for success icon
    val pulseScale by animateFloatAsState(
        targetValue = if (isVisible) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Gradient colors
    val gradientColors = listOf(
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
    )
    
    LaunchedEffect(Unit) {
        isVisible = true
        // Auto-dismiss after specified time to allow TTS to complete
        delay(minDisplayTime)
        onDismiss()
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .scale(scale)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(gradientColors)
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Success Icon with pulse animation
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Trip Started",
                    modifier = Modifier
                        .size(64.dp)
                        .scale(pulseScale),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                // Title
                Text(
                    text = "🚌 Trip Started Successfully!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                // Trip Details Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // From Terminal
                        TripDetailRow(
                            icon = Icons.Default.LocationOn,
                            label = "From",
                            value = startTerminalName,
                            iconTint = MaterialTheme.colorScheme.primary
                        )
                        
                        // To Terminal
                        TripDetailRow(
                            icon = Icons.Default.ArrowForward,
                            label = "To",
                            value = destinationTerminalName,
                            iconTint = MaterialTheme.colorScheme.secondary
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
                
                // Motivational Message
                Text(
                    text = "Have a safe journey! 🛡️",
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
                        text = "🔊 Announcing trip details...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Dismiss Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
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
