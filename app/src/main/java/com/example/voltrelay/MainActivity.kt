package com.example.voltrelay

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.voltrelay.battery.BatteryMonitoringService
import com.example.voltrelay.data.linking.ThemeMode
import com.example.voltrelay.navigation.DashboardKey
import com.example.voltrelay.navigation.RelayManagementKey
import com.example.voltrelay.navigation.SettingsKey
import com.example.voltrelay.ui.dashboard.BatteryViewModel
import com.example.voltrelay.ui.dashboard.DashboardScreen
import com.example.voltrelay.ui.settings.RelayManagementScreen
import com.example.voltrelay.ui.settings.SettingsScreen
import com.example.voltrelay.ui.settings.SettingsViewModel
import com.example.voltrelay.ui.theme.VoltRelinkTheme
import com.example.voltrelay.worker.scheduler.WorkScheduler

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Schedule fallback background monitoring
        WorkScheduler.schedulePeriodicBatteryCheck(this)

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val isActiveMonitoringEnabled by settingsViewModel.isActiveMonitoringEnabled.collectAsStateWithLifecycle()
            val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
            val isDynamicColorEnabled by settingsViewModel.isDynamicColorEnabled.collectAsStateWithLifecycle()
            val themeSeedColor by settingsViewModel.themeSeedColor.collectAsStateWithLifecycle()

            // Manage Foreground Service based on setting
            LaunchedEffect(isActiveMonitoringEnabled) {
                val serviceIntent = Intent(this@MainActivity, BatteryMonitoringService::class.java)
                if (isActiveMonitoringEnabled) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(serviceIntent)
                    } else {
                        startService(serviceIntent)
                    }
                } else {
                    stopService(serviceIntent)
                }
            }

            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            VoltRelinkTheme(
                darkTheme = darkTheme,
                dynamicColor = isDynamicColorEnabled,
                seedColor = themeSeedColor?.let { Color(it) }
            ) {
                val backStack = rememberNavBackStack(DashboardKey as NavKey)
                
                val windowAdaptiveInfo = currentWindowAdaptiveInfoV2()
                val directive = remember(windowAdaptiveInfo) {
                    calculatePaneScaffoldDirective(windowAdaptiveInfo)
                        .copy(horizontalPartitionSpacerSize = 0.dp)
                }
                val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(directive = directive)

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavDisplay(
                        backStack = backStack,
                        onBack = { backStack.removeLastOrNull() },
                        sceneStrategy = listDetailStrategy,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(300)) + slideInHorizontally { it / 3 })
                                .togetherWith(fadeOut(animationSpec = tween(300)) + slideOutHorizontally { -it / 3 })
                        },
                        popTransitionSpec = {
                            (fadeIn(animationSpec = tween(300)) + slideInHorizontally { -it / 3 })
                                .togetherWith(fadeOut(animationSpec = tween(300)) + slideOutHorizontally { it / 3 })
                        },
                        predictivePopTransitionSpec = {
                            ContentTransform(
                                targetContentEnter = fadeIn(),
                                initialContentExit = scaleOut(targetScale = 0.9f) + fadeOut()
                            )
                        },
                        entryDecorators = listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator()
                        ),
                        entryProvider = entryProvider {
                            entry<DashboardKey>(
                                metadata = ListDetailSceneStrategy.listPane(
                                    detailPlaceholder = {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                "Select settings to configure alerts",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                )
                            ) {
                                val batteryViewModel: BatteryViewModel = viewModel()
                                DashboardScreen(
                                    batteryViewModel = batteryViewModel,
                                    settingsViewModel = settingsViewModel,
                                    onNavigateToSettings = {
                                        backStack.add(SettingsKey)
                                    },
                                    onNavigateToRelayManagement = {
                                        backStack.add(RelayManagementKey)
                                    }
                                )
                            }
                            entry<RelayManagementKey>(
                                metadata = ListDetailSceneStrategy.detailPane()
                            ) {
                                RelayManagementScreen(
                                    viewModel = settingsViewModel,
                                    onBack = { backStack.removeLastOrNull() }
                                )
                            }
                            entry<SettingsKey>(
                                metadata = ListDetailSceneStrategy.detailPane()
                            ) {
                                SettingsScreen(
                                    viewModel = settingsViewModel,
                                    onBack = { backStack.removeLastOrNull() }
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
