package com.example.voltrelay.ui.dashboard

import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voltrelay.battery.BatteryStatus
import com.example.voltrelay.ui.settings.SettingsViewModel
import com.example.voltrelay.ui.theme.BatteryGreen
import com.example.voltrelay.ui.theme.BatteryRed
import com.example.voltrelay.ui.theme.VoltRelinkTheme
import com.example.voltrelay.ui.theme.VoltYellow
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    batteryViewModel: BatteryViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToRelayManagement: () -> Unit = {}
) {
    val batteryStatus by batteryViewModel.batteryStatus.collectAsStateWithLifecycle()
    val sendKey by settingsViewModel.serverChanSendKey.collectAsStateWithLifecycle()
    val customName by settingsViewModel.customDeviceName.collectAsStateWithLifecycle()
    val highThreshold by settingsViewModel.highBatteryThreshold.collectAsStateWithLifecycle()

    DashboardContent(
        batteryStatus = batteryStatus,
        serviceActive = !sendKey.isNullOrBlank(),
        deviceDisplayName = customName ?: Build.MODEL,
        highThreshold = highThreshold,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToRelayManagement = onNavigateToRelayManagement
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    batteryStatus: BatteryStatus,
    serviceActive: Boolean,
    deviceDisplayName: String,
    highThreshold: Int,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToRelayManagement: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("VoltRelink", fontWeight = FontWeight.Black) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BatteryStatusCard(batteryStatus)
            
            InfoGrid(batteryStatus)
            
            ChargingSpeedCard(batteryStatus, highThreshold)
            
            RelayStatusCard(
                serviceActive = serviceActive,
                deviceDisplayName = deviceDisplayName,
                onSetupClick = onNavigateToRelayManagement
            )
        }
    }
}

@Composable
fun BatteryStatusCard(status: BatteryStatus) {
    val animatedProgress by animateFloatAsState(
        targetValue = status.percentage / 100f,
        label = "BatteryProgress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(200.dp),
                        strokeWidth = 14.dp,
                        color = when {
                            status.percentage > 20 -> BatteryGreen
                            else -> BatteryRed
                        },
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${status.percentage}%",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (status.isCharging) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Rounded.FlashOn,
                                    contentDescription = null,
                                    tint = VoltYellow,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    "CHARGING",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoGrid(status: BatteryStatus) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        InfoCard(
            modifier = Modifier.weight(1f),
            label = "Health",
            value = status.healthString,
            icon = Icons.Rounded.Favorite,
            color = MaterialTheme.colorScheme.tertiaryContainer
        )
        InfoCard(
            modifier = Modifier.weight(1f),
            label = "Temperature",
            value = "${status.temperature / 10f}°C",
            icon = Icons.Rounded.Thermostat,
            color = MaterialTheme.colorScheme.secondaryContainer
        )
    }
}

@Composable
fun ChargingSpeedCard(status: BatteryStatus, highThreshold: Int) {
    if (status.isCharging) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Rounded.Bolt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            "Charging Speed",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f W (%d mA)", status.powerInWatts, status.currentInMa),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Limit Set", style = MaterialTheme.typography.labelSmall)
                        Text("$highThreshold%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    
                    val effectiveRemaining = status.getEffectiveChargeTimeRemaining(highThreshold)
                    
                    if (effectiveRemaining > 0) {
                        val hours = TimeUnit.MILLISECONDS.toHours(effectiveRemaining)
                        val minutes = TimeUnit.MILLISECONDS.toMinutes(effectiveRemaining) % 60
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Time to Target", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Time to Target", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) "Calculating..." else "Not supported",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RelayStatusCard(
    serviceActive: Boolean,
    deviceDisplayName: String,
    onSetupClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Relay Service",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (serviceActive) "Active for: $deviceDisplayName" else "ServerChan not configured",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Button(
                onClick = onSetupClick,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(if (serviceActive) "Manage" else "Setup")
            }
        }
    }
}


@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun DashboardPreview() {
    VoltRelinkTheme {
        DashboardContent(
            batteryStatus = BatteryStatus(
                level = 85,
                scale = 100,
                status = 2, // Charging
                health = 2, // Good
                temperature = 320, // 32.0 C
                voltage = 4000, // 4V
                currentNow = 2500000, // 2.5A / 2500mA
                chargeTimeRemaining = 3600000 // 1 hour
            ),
            serviceActive = true,
            deviceDisplayName = "My Pixel 7",
            highThreshold = 95
        )
    }
}
