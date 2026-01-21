package com.ml.mobilefleet.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

private val GradientSuccess = listOf(
    Color(0xFF11998e),
    Color(0xFF38ef7d)
)
private val GradientCelebration = listOf(
    Color(0xFFFFD700),
    Color(0xFFFF6B6B),
    Color(0xFF4ECDC4),
    Color(0xFF45B7D1)
)

@Composable
fun TripCompletionModal(
    isVisible: Boolean,
    destinationTerminalName: String,
    passengerCount: Int,
    startTime: Long,
    endTime: Long = System.currentTimeMillis(),
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    minDisplayTime: Long = 12000L
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

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

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

    val durationMinutes = ((endTime - startTime) / 60000).toInt()
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    LaunchedEffect(Unit) {
        isVisible = true
        delay(500)
        showConfetti = true
        delay(minDisplayTime)
        onDismiss()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
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
                .fillMaxWidth(0.92f)
                .scale(scale),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFE8F5E9).copy(alpha = 0.5f),
                                Color.White,
                                Color(0xFFFFF3E0).copy(alpha = 0.3f)
                            )
                        )
                    )
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .scale(celebrationScale)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF11998e).copy(alpha = 0.2f),
                                        Color(0xFF38ef7d).copy(alpha = 0.1f)
                                    )
                                )
                            )
                    )

                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier
                            .size(72.dp)
                            .scale(celebrationScale),
                        tint = Color(0xFFFFD700)
                    )

                    repeat(4) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .offset(
                                    x = (50 * cos(Math.toRadians((rotation + index * 90).toDouble()))).dp,
                                    y = (50 * sin(Math.toRadians((rotation + index * 90).toDouble()))).dp
                                ),
                            tint = Color(0xFFFFD700)
                        )
                    }
                }

                Text(
                    text = "Trip Completed!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Great job, driver! 🚌✨",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF11998e),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.linearGradient(GradientSuccess)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Destination",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.Gray
                                )
                                Text(
                                    text = destinationTerminalName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1A1A2E)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.linearGradient(
                                            if (passengerCount > 0) GradientSuccess
                                            else listOf(Color.Gray.copy(alpha = 0.3f), Color.Gray.copy(alpha = 0.2f))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Passengers",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.Gray
                                )
                                Text(
                                    text = if (passengerCount > 0) passengerCount.toString() else "--",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (passengerCount > 0) Color(0xFF1A1A2E) else Color.Gray
                                )
                            }
                        }

                        Divider(color = Color.Gray.copy(alpha = 0.2f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE8F0FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = Color(0xFF667eea),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Duration",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "${durationMinutes} min",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1A1A2E)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFF3E0)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = Color(0xFFFF9800),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Completed",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                                Text(
                                    text = timeFormatter.format(Date(endTime)),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1A1A2E)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF11998e)
                    )
                    Text(
                        text = "Announcing trip completion...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF11998e),
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF11998e)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 16.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
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
            .size(10.dp)
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
