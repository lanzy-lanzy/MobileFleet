package com.ml.mobilefleet.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ml.mobilefleet.data.models.Trip

/**
 * Navigation destinations for bottom navigation
 */
enum class BottomNavDestination(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    START_TRIP("start_trip", Icons.Default.PlayArrow, "Start Trip"),
    COMPLETE_TRIP("complete_trip", Icons.Default.CheckCircle, "Complete Trip"),
    TRIP_HISTORY("trip_history", Icons.Default.History, "History"),
    SETTINGS("settings", Icons.Default.Settings, "Settings")
}

/**
 * Smart bottom navigation bar that adapts based on trip status
 */
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
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 8.dp
    ) {
        BottomNavDestination.values().forEach { destination ->
            val isSelected = currentDestination == destination.route
            val isEnabled = when (destination) {
                BottomNavDestination.START_TRIP -> !hasActiveTrip // Disabled when trip is active
                BottomNavDestination.COMPLETE_TRIP -> hasActiveTrip // Only enabled when trip is active
                BottomNavDestination.TRIP_HISTORY -> true // Always enabled
                BottomNavDestination.SETTINGS -> true // Always enabled
            }
            
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
                    selectedIconColor = getIconColor(destination, isSelected, hasActiveTrip),
                    unselectedIconColor = if (isEnabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    },
                    selectedTextColor = getTextColor(destination, isSelected, hasActiveTrip),
                    unselectedTextColor = if (isEnabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    },
                    indicatorColor = getIndicatorColor(destination, hasActiveTrip)
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
    Box {
        Icon(
            imageVector = icon,
            contentDescription = destination.label,
            modifier = Modifier.size(24.dp)
        )
        
        // Add indicator for required action
        if (destination == BottomNavDestination.COMPLETE_TRIP && hasActiveTrip && !isSelected) {
            Badge(
                modifier = Modifier.offset(x = 8.dp, y = (-8).dp),
                containerColor = MaterialTheme.colorScheme.error
            ) {
                Text(
                    text = "!",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
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
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
    )
}

@Composable
private fun getIconColor(
    destination: BottomNavDestination,
    isSelected: Boolean,
    hasActiveTrip: Boolean
): Color {
    return when {
        destination == BottomNavDestination.COMPLETE_TRIP && hasActiveTrip -> {
            if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.error
        }
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
private fun getTextColor(
    destination: BottomNavDestination,
    isSelected: Boolean,
    hasActiveTrip: Boolean
): Color {
    return when {
        destination == BottomNavDestination.COMPLETE_TRIP && hasActiveTrip -> {
            if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.error
        }
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
private fun getIndicatorColor(
    destination: BottomNavDestination,
    hasActiveTrip: Boolean
): Color {
    return when {
        destination == BottomNavDestination.COMPLETE_TRIP && hasActiveTrip -> {
            MaterialTheme.colorScheme.errorContainer
        }
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
}
