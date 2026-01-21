package com.ml.mobilefleet.ui.navigation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ml.mobilefleet.data.models.Trip

private val GradientPrimary = listOf(
    Color(0xFF667eea),
    Color(0xFF764ba2)
)
private val GradientSuccess = listOf(
    Color(0xFF11998e),
    Color(0xFF38ef7d)
)
private val GradientWarning = listOf(
    Color(0xFFFF6B6B),
    Color(0xFFFF8E53)
)
private val GradientInfo = listOf(
    Color(0xFF4facfe),
    Color(0xFF00f2fe)
)

enum class BottomNavDestination(
    val route: String,
    val icon: ImageVector,
    val label: String,
    val gradient: List<Color>
) {
    START_TRIP("start_trip", Icons.Default.PlayArrow, "Start Trip", GradientSuccess),
    COMPLETE_TRIP("complete_trip", Icons.Default.CheckCircle, "Complete Trip", GradientWarning),
    TRIP_HISTORY("trip_history", Icons.Default.History, "History", GradientInfo),
    SETTINGS("settings", Icons.Default.Settings, "Settings", GradientPrimary)
}

@Composable
fun SmartBottomNavigationBar(
    currentDestination: String,
    currentTrip: Trip?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val hasActiveTrip = currentTrip != null

    NavigationBar(
        modifier = modifier,
        containerColor = Color.White,
        contentColor = Color.White,
        tonalElevation = 0.dp
    ) {
        BottomNavDestination.values().forEach { destination ->
            val isSelected = currentDestination == destination.route
            val isEnabled = when (destination) {
                BottomNavDestination.START_TRIP -> !hasActiveTrip
                BottomNavDestination.COMPLETE_TRIP -> hasActiveTrip
                BottomNavDestination.TRIP_HISTORY -> true
                BottomNavDestination.SETTINGS -> true
            }

            val isCompleteTripActive = destination == BottomNavDestination.COMPLETE_TRIP && hasActiveTrip

            NavigationBarItem(
                icon = {
                    NavigationIcon(
                        icon = destination.icon,
                        isSelected = isSelected,
                        isEnabled = isEnabled,
                        hasActiveTrip = hasActiveTrip,
                        destination = destination
                    )
                },
                label = {
                    NavigationLabel(
                        text = destination.label,
                        isSelected = isSelected,
                        isEnabled = isEnabled,
                        hasActiveTrip = hasActiveTrip,
                        destination = destination
                    )
                },
                selected = isSelected,
                onClick = {
                    if (isEnabled) {
                        onNavigate(destination.route)
                    }
                },
                enabled = isEnabled,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = if (isCompleteTripActive) Color(0xFFFF6B6B) else Color(0xFF667eea),
                    unselectedIconColor = if (isEnabled) Color.Gray else Color.Gray.copy(alpha = 0.3f),
                    selectedTextColor = if (isCompleteTripActive) Color(0xFFFF6B6B) else Color(0xFF667eea),
                    unselectedTextColor = if (isEnabled) Color.Gray else Color.Gray.copy(alpha = 0.3f),
                    indicatorColor = if (isCompleteTripActive) {
                        Color(0xFFFFEBEE)
                    } else {
                        Color(0xFFE8F0FF)
                    }
                )
            )
        }
    }
}

@Composable
private fun NavigationIcon(
    icon: ImageVector,
    isSelected: Boolean,
    isEnabled: Boolean,
    hasActiveTrip: Boolean,
    destination: BottomNavDestination
) {
    val isCompleteTripActive = destination == BottomNavDestination.COMPLETE_TRIP && hasActiveTrip

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(40.dp)
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            if (isCompleteTripActive) GradientWarning else destination.gradient
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = destination.label,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isEnabled) Color.Gray.copy(alpha = 0.1f) else Color.Transparent
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = destination.label,
                    tint = if (isEnabled) Color.Gray else Color.Gray.copy(alpha = 0.3f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        if (destination == BottomNavDestination.COMPLETE_TRIP && hasActiveTrip && !isSelected) {
            Badge(
                modifier = Modifier.offset(x = 24.dp, y = (-8).dp),
                containerColor = Color(0xFFFF6B6B)
            ) {
                Text(
                    text = "!",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun NavigationLabel(
    text: String,
    isSelected: Boolean,
    isEnabled: Boolean,
    hasActiveTrip: Boolean,
    destination: BottomNavDestination
) {
    val displayText = when {
        destination == BottomNavDestination.COMPLETE_TRIP && hasActiveTrip -> "Complete Trip"
        destination == BottomNavDestination.START_TRIP && !isEnabled -> "Trip Active"
        else -> text
    }

    Text(
        text = displayText,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        color = when {
            !isEnabled -> Color.Gray.copy(alpha = 0.5f)
            destination == BottomNavDestination.COMPLETE_TRIP && hasActiveTrip -> Color(0xFFFF6B6B)
            isSelected -> Color(0xFF667eea)
            else -> Color.Gray
        }
    )
}
