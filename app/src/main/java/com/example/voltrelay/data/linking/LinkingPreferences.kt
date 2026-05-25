package com.example.voltrelay.data.linking

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "linking_prefs")

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

class LinkingPreferences(private val context: Context) {
    private val CUSTOM_DEVICE_NAME = stringPreferencesKey("custom_device_name")
    private val SERVER_CHAN_SEND_KEY = stringPreferencesKey("server_chan_send_key")
    
    // Threshold Keys
    private val LOW_BATTERY_THRESHOLD = intPreferencesKey("low_battery_threshold")
    private val HIGH_BATTERY_THRESHOLD = intPreferencesKey("high_battery_threshold")
    private val LOW_BATTERY_ENABLED = booleanPreferencesKey("low_battery_enabled")
    private val HIGH_BATTERY_ENABLED = booleanPreferencesKey("high_battery_enabled")
    private val ACTIVE_MONITORING_ENABLED = booleanPreferencesKey("active_monitoring_enabled")
    private val LAST_RECORDED_LEVEL = intPreferencesKey("last_recorded_level")

    // Theme Keys
    private val THEME_MODE = stringPreferencesKey("theme_mode")
    private val DYNAMIC_COLOR_ENABLED = booleanPreferencesKey("dynamic_color_enabled")
    private val THEME_SEED_COLOR = longPreferencesKey("theme_seed_color")

    val customDeviceName: Flow<String?> = context.dataStore.data.map { it[CUSTOM_DEVICE_NAME] }
    val serverChanSendKey: Flow<String?> = context.dataStore.data.map { it[SERVER_CHAN_SEND_KEY] }

    // Threshold Flows
    val lowBatteryThreshold: Flow<Int> = context.dataStore.data.map { it[LOW_BATTERY_THRESHOLD] ?: 20 }
    val highBatteryThreshold: Flow<Int> = context.dataStore.data.map { it[HIGH_BATTERY_THRESHOLD] ?: 100 }
    val isLowBatteryEnabled: Flow<Boolean> = context.dataStore.data.map { it[LOW_BATTERY_ENABLED] ?: true }
    val isHighBatteryEnabled: Flow<Boolean> = context.dataStore.data.map { it[HIGH_BATTERY_ENABLED] ?: true }
    val isActiveMonitoringEnabled: Flow<Boolean> = context.dataStore.data.map { it[ACTIVE_MONITORING_ENABLED] ?: false }
    val lastRecordedLevel: Flow<Int> = context.dataStore.data.map { it[LAST_RECORDED_LEVEL] ?: -1 }

    // Theme Flows
    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val modeStr = preferences[THEME_MODE]
        try {
            if (modeStr != null) ThemeMode.valueOf(modeStr) else ThemeMode.SYSTEM
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }
    val isDynamicColorEnabled: Flow<Boolean> = context.dataStore.data.map { it[DYNAMIC_COLOR_ENABLED] ?: true }
    val themeSeedColor: Flow<Long?> = context.dataStore.data.map { it[THEME_SEED_COLOR] }

    suspend fun saveCustomDeviceName(name: String) {
        context.dataStore.edit { it[CUSTOM_DEVICE_NAME] = name }
    }

    suspend fun saveServerChanSendKey(sendKey: String) {
        context.dataStore.edit { it[SERVER_CHAN_SEND_KEY] = sendKey }
    }

    // Threshold Savers
    suspend fun saveLowBatteryThreshold(threshold: Int) {
        context.dataStore.edit { it[LOW_BATTERY_THRESHOLD] = threshold }
    }

    suspend fun saveHighBatteryThreshold(threshold: Int) {
        context.dataStore.edit { it[HIGH_BATTERY_THRESHOLD] = threshold }
    }

    suspend fun saveLowBatteryEnabled(enabled: Boolean) {
        context.dataStore.edit { it[LOW_BATTERY_ENABLED] = enabled }
    }

    suspend fun saveHighBatteryEnabled(enabled: Boolean) {
        context.dataStore.edit { it[HIGH_BATTERY_ENABLED] = enabled }
    }

    suspend fun saveActiveMonitoringEnabled(enabled: Boolean) {
        context.dataStore.edit { it[ACTIVE_MONITORING_ENABLED] = enabled }
    }

    suspend fun saveLastRecordedLevel(level: Int) {
        context.dataStore.edit { it[LAST_RECORDED_LEVEL] = level }
    }

    // Theme Savers
    suspend fun saveThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[THEME_MODE] = mode.name }
    }

    suspend fun saveDynamicColorEnabled(enabled: Boolean) {
        context.dataStore.edit { it[DYNAMIC_COLOR_ENABLED] = enabled }
    }

    suspend fun saveThemeSeedColor(color: Long) {
        context.dataStore.edit { it[THEME_SEED_COLOR] = color }
    }
}
