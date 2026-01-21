package com.ml.mobilefleet.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ml.mobilefleet.ui.auth.AuthViewModel

private val GradientPrimary = listOf(
    Color(0xFF667eea),
    Color(0xFF764ba2)
)
private val GradientSuccess = listOf(
    Color(0xFF11998e),
    Color(0xFF38ef7d)
)
private val GradientInfo = listOf(
    Color(0xFF4facfe),
    Color(0xFF00f2fe)
)
private val GradientWarning = listOf(
    Color(0xFFFF6B6B),
    Color(0xFFFF8E53)
)

@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            content = content
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel? = null
) {
    var soundEnabled by remember { mutableStateOf(true) }
    var hapticEnabled by remember { mutableStateOf(true) }
    var autoScanEnabled by remember { mutableStateOf(false) }

    val authState by authViewModel?.authState?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8F9FF),
                        Color(0xFFE8ECFF),
                        Color.White
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            GlassCard {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(GradientPrimary)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E)
                    )

                    Text(
                        text = "App preferences and configuration",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            SettingsSection(
                title = "Audio & Feedback",
                icon = Icons.Default.Notifications,
                gradient = GradientInfo
            ) {
                SettingsToggleItem(
                    title = "Sound Announcements",
                    subtitle = "Enable voice announcements for trip events",
                    icon = Icons.Default.RecordVoiceOver,
                    checked = soundEnabled,
                    onCheckedChange = { soundEnabled = it },
                    iconColor = Color(0xFF667eea)
                )

                SettingsToggleItem(
                    title = "Haptic Feedback",
                    subtitle = "Enable vibration for QR scan success",
                    icon = Icons.Default.Vibration,
                    checked = hapticEnabled,
                    onCheckedChange = { hapticEnabled = it },
                    iconColor = Color(0xFFFF9800)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSection(
                title = "Scanner",
                icon = Icons.Default.QrCodeScanner,
                gradient = GradientSuccess
            ) {
                SettingsToggleItem(
                    title = "Auto-scan Mode",
                    subtitle = "Automatically scan QR codes when detected",
                    icon = Icons.Default.CameraAlt,
                    checked = autoScanEnabled,
                    onCheckedChange = { autoScanEnabled = it },
                    iconColor = Color(0xFF11998e)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSection(
                title = "About",
                icon = Icons.Default.Info,
                gradient = GradientPrimary
            ) {
                SettingsInfoItem(
                    title = "App Version",
                    subtitle = "1.0.0",
                    icon = Icons.Default.AppSettingsAlt,
                    iconColor = Color(0xFF667eea)
                )

                SettingsInfoItem(
                    title = "Driver ID",
                    subtitle = authState?.currentDriver?.driver_id ?: "Not logged in",
                    icon = Icons.Default.Badge,
                    iconColor = Color(0xFF11998e)
                )

                SettingsInfoItem(
                    title = "Last Sync",
                    subtitle = "Just now",
                    icon = Icons.Default.Sync,
                    iconColor = Color(0xFFFF9800)
                )
            }

            if (authViewModel != null && authState?.isAuthenticated == true) {
                Spacer(modifier = Modifier.height(16.dp))

                SettingsSection(
                    title = "Account",
                    icon = Icons.Default.AccountCircle,
                    gradient = GradientWarning
                ) {
                    SettingsLogoutItem(
                        title = "Sign Out",
                        subtitle = "Sign out of ${authState?.currentDriver?.name ?: "your account"}",
                        icon = Icons.AutoMirrored.Filled.Logout,
                        onClick = {
                            authViewModel.logout()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    gradient: List<Color>,
    content: @Composable ColumnScope.() -> Unit
) {
    GlassCard {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.linearGradient(gradient)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E)
                )
            }

            content()
        }
    }
}

@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    iconColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A2E)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF11998e)
            )
        )
    }
}

@Composable
private fun SettingsInfoItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A2E)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun SettingsLogoutItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFEBEE)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.linearGradient(GradientWarning)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFD32F2F)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFD32F2F).copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
