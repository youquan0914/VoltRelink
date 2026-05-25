package com.example.voltrelay.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voltrelay.data.linking.ThemeMode
import com.example.voltrelay.ui.theme.VoltRelinkTheme
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val lowThreshold by viewModel.lowBatteryThreshold.collectAsStateWithLifecycle()
    val highThreshold by viewModel.highBatteryThreshold.collectAsStateWithLifecycle()
    val lowEnabled by viewModel.isLowBatteryEnabled.collectAsStateWithLifecycle()
    val highEnabled by viewModel.isHighBatteryEnabled.collectAsStateWithLifecycle()
    val activeMonitoring by viewModel.isActiveMonitoringEnabled.collectAsStateWithLifecycle()
    
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val isDynamicEnabled by viewModel.isDynamicColorEnabled.collectAsStateWithLifecycle()
    val themeSeedColor by viewModel.themeSeedColor.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is SettingsViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    SettingsContent(
        lowThreshold = lowThreshold,
        highThreshold = highThreshold,
        lowEnabled = lowEnabled,
        highEnabled = highEnabled,
        activeMonitoring = activeMonitoring,
        themeMode = themeMode,
        isDynamicEnabled = isDynamicEnabled,
        themeSeedColor = themeSeedColor,
        snackbarHostState = snackbarHostState,
        onUpdateLowThreshold = { viewModel.updateLowBatteryThreshold(it) },
        onUpdateHighThreshold = { viewModel.updateHighBatteryThreshold(it) },
        onUpdateLowEnabled = { viewModel.updateLowBatteryEnabled(it) },
        onUpdateHighEnabled = { viewModel.updateHighBatteryEnabled(it) },
        onUpdateActiveMonitoring = { viewModel.updateActiveMonitoringEnabled(it) },
        onUpdateThemeMode = { viewModel.updateThemeMode(it) },
        onUpdateDynamicEnabled = { viewModel.updateDynamicColorEnabled(it) },
        onUpdateThemeSeedColor = { viewModel.updateThemeSeedColor(it) },
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    lowThreshold: Int,
    highThreshold: Int,
    lowEnabled: Boolean,
    highEnabled: Boolean,
    activeMonitoring: Boolean,
    themeMode: ThemeMode,
    isDynamicEnabled: Boolean,
    themeSeedColor: Long?,
    snackbarHostState: SnackbarHostState,
    onUpdateLowThreshold: (Int) -> Unit,
    onUpdateHighThreshold: (Int) -> Unit,
    onUpdateLowEnabled: (Boolean) -> Unit,
    onUpdateHighEnabled: (Boolean) -> Unit,
    onUpdateActiveMonitoring: (Boolean) -> Unit,
    onUpdateThemeMode: (ThemeMode) -> Unit,
    onUpdateDynamicEnabled: (Boolean) -> Unit,
    onUpdateThemeSeedColor: (Long) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Theme Customization
            SectionHeader("Appearance")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Theme Mode
                    Text("Theme Mode", style = MaterialTheme.typography.labelLarge)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeModeButton(ThemeMode.SYSTEM, "System", themeMode == ThemeMode.SYSTEM, Modifier.weight(1f)) { onUpdateThemeMode(it) }
                        ThemeModeButton(ThemeMode.LIGHT, "Light", themeMode == ThemeMode.LIGHT, Modifier.weight(1f)) { onUpdateThemeMode(it) }
                        ThemeModeButton(ThemeMode.DARK, "Dark", themeMode == ThemeMode.DARK, Modifier.weight(1f)) { onUpdateThemeMode(it) }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // Dynamic Color Toggle
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Dynamic Color", fontWeight = FontWeight.SemiBold)
                            Text("Use wallpaper colors (Android 12+)", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(checked = isDynamicEnabled, onCheckedChange = onUpdateDynamicEnabled)
                    }

                    // Preset Color Seeds
                    Text("Preset Colors", style = MaterialTheme.typography.labelLarge)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val presets = listOf(
                            Color(0xFF6750A4), // Purple
                            Color(0xFF006C4C), // Green
                            Color(0xFF7D5260), // Red/Pink
                            Color(0xFF0061A4), // Blue
                            Color(0xFFFFB300)  // Orange/Yellow
                        )
                        
                        presets.forEach { color ->
                            val colorLong = color.toArgb().toLong() and 0xFFFFFFFFL
                            val isSelected = !isDynamicEnabled && themeSeedColor == colorLong
                            
                            ColorSeedCircle(color, isSelected) { 
                                onUpdateDynamicEnabled(false)
                                onUpdateThemeSeedColor(colorLong) 
                            }
                        }
                    }
                }
            }

            // Foreground Service Toggle
            SectionHeader("Monitoring Mode")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.NotificationsActive, null, tint = if (activeMonitoring) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Active Monitoring", fontWeight = FontWeight.SemiBold)
                            Text("Real-time threshold tracking", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(checked = activeMonitoring, onCheckedChange = onUpdateActiveMonitoring)
                    }
                }
            }

            // Notification Thresholds
            SectionHeader("Notification Thresholds")
            ThresholdSetting(
                title = "Low Battery Alert",
                enabled = lowEnabled,
                onEnabledChange = onUpdateLowEnabled,
                value = lowThreshold,
                onValueChange = onUpdateLowThreshold,
                range = 1f..50f,
                icon = Icons.Rounded.BatteryAlert
            )
            ThresholdSetting(
                title = "Full Battery Alert",
                enabled = highEnabled,
                onEnabledChange = onUpdateHighEnabled,
                value = highThreshold,
                onValueChange = onUpdateHighThreshold,
                range = 51f..100f,
                icon = Icons.Rounded.BatteryFull
            )
        }
    }
}

@Composable
fun ThemeModeButton(mode: ThemeMode, label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: (ThemeMode) -> Unit) {
    OutlinedButton(
        onClick = { onClick(mode) },
        modifier = modifier,
        colors = if (selected) ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else ButtonDefaults.outlinedButtonColors()
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun ColorSeedCircle(color: Color, selected: Boolean, onClick: (Color) -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(color)
            .clickable { onClick(color) }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    VoltRelinkTheme {
        SettingsContent(
            lowThreshold = 20,
            highThreshold = 100,
            lowEnabled = true,
            highEnabled = true,
            activeMonitoring = true,
            themeMode = ThemeMode.SYSTEM,
            isDynamicEnabled = false,
            themeSeedColor = 0xFF6750A4L,
            snackbarHostState = SnackbarHostState(),
            onUpdateLowThreshold = {},
            onUpdateHighThreshold = {},
            onUpdateLowEnabled = {},
            onUpdateHighEnabled = {},
            onUpdateActiveMonitoring = {},
            onUpdateThemeMode = {},
            onUpdateDynamicEnabled = {},
            onUpdateThemeSeedColor = {},
            onBack = {}
        )
    }
}
