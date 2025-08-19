package com.ml.mobilefleet.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun FleetAnimation(
    modifier: Modifier = Modifier,
    isAnimating: Boolean = true
) {
    // Animation for vehicle movement
    val infiniteTransition = rememberInfiniteTransition(label = "fleet_animation")
    
    val vehicle1Position by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 500f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vehicle1_position"
    )
    
    val vehicle2Position by infiniteTransition.animateFloat(
        initialValue = -150f,
        targetValue = 550f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vehicle2_position"
    )
    
    val vehicle3Position by infiniteTransition.animateFloat(
        initialValue = -200f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vehicle3_position"
    )
    
    // Colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        // Draw road
        drawRoad(canvasWidth, canvasHeight, surfaceColor)
        
        if (isAnimating) {
            // Draw vehicles
            drawVehicle(
                position = Offset(vehicle1Position, canvasHeight * 0.4f),
                color = primaryColor,
                size = Size(60f, 30f)
            )
            
            drawVehicle(
                position = Offset(vehicle2Position, canvasHeight * 0.55f),
                color = secondaryColor,
                size = Size(70f, 35f)
            )
            
            drawVehicle(
                position = Offset(vehicle3Position, canvasHeight * 0.7f),
                color = tertiaryColor,
                size = Size(55f, 28f)
            )
        }
    }
}

private fun DrawScope.drawRoad(
    canvasWidth: Float,
    canvasHeight: Float,
    surfaceColor: Color
) {
    // Road surface
    drawRect(
        color = surfaceColor.copy(alpha = 0.1f),
        topLeft = Offset(0f, canvasHeight * 0.35f),
        size = Size(canvasWidth, canvasHeight * 0.4f)
    )
    
    // Lane markings
    val laneMarkingWidth = 40f
    val laneMarkingHeight = 4f
    val spacing = 80f
    
    for (i in 0 until (canvasWidth / spacing).toInt() + 2) {
        val x = i * spacing - 20f
        
        // Center lane marking
        drawRect(
            color = Color.White.copy(alpha = 0.6f),
            topLeft = Offset(x, canvasHeight * 0.53f),
            size = Size(laneMarkingWidth, laneMarkingHeight)
        )
    }
}

private fun DrawScope.drawVehicle(
    position: Offset,
    color: Color,
    size: Size
) {
    // Vehicle body
    val vehicleRect = RoundRect(
        left = position.x,
        top = position.y,
        right = position.x + size.width,
        bottom = position.y + size.height,
        radiusX = 8f,
        radiusY = 8f
    )
    
    val path = Path().apply {
        addRoundRect(vehicleRect)
    }
    
    drawPath(
        path = path,
        color = color
    )
    
    // Vehicle cab
    val cabRect = RoundRect(
        left = position.x,
        top = position.y - 10f,
        right = position.x + size.width * 0.4f,
        bottom = position.y,
        radiusX = 6f,
        radiusY = 6f
    )
    
    val cabPath = Path().apply {
        addRoundRect(cabRect)
    }
    
    drawPath(
        path = cabPath,
        color = color
    )
    
    // Wheels
    drawCircle(
        color = Color.Black,
        radius = 6f,
        center = Offset(position.x + size.width * 0.2f, position.y + size.height + 4f)
    )
    
    drawCircle(
        color = Color.Black,
        radius = 6f,
        center = Offset(position.x + size.width * 0.8f, position.y + size.height + 4f)
    )
    
    // Windshield
    drawRect(
        color = Color.White.copy(alpha = 0.7f),
        topLeft = Offset(position.x + 4f, position.y - 8f),
        size = Size(size.width * 0.3f, 6f)
    )
}
