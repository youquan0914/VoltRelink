package com.example.voltrelay.ui.settings

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.voltrelay.data.linking.LinkingPreferences
import com.example.voltrelay.data.linking.ThemeMode
import com.example.voltrelay.network.RetrofitClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val linkingPreferences = LinkingPreferences(application)

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    val customDeviceName: StateFlow<String?> = linkingPreferences.customDeviceName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val serverChanSendKey: StateFlow<String?> = linkingPreferences.serverChanSendKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Threshold States
    val lowBatteryThreshold: StateFlow<Int> = linkingPreferences.lowBatteryThreshold
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 20)
    val highBatteryThreshold: StateFlow<Int> = linkingPreferences.highBatteryThreshold
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 100)
    val isLowBatteryEnabled: StateFlow<Boolean> = linkingPreferences.isLowBatteryEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val isHighBatteryEnabled: StateFlow<Boolean> = linkingPreferences.isHighBatteryEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val isActiveMonitoringEnabled: StateFlow<Boolean> = linkingPreferences.isActiveMonitoringEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Theme States
    val themeMode: StateFlow<ThemeMode> = linkingPreferences.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)
    val isDynamicColorEnabled: StateFlow<Boolean> = linkingPreferences.isDynamicColorEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val themeSeedColor: StateFlow<Long?> = linkingPreferences.themeSeedColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateCustomDeviceName(name: String) {
        viewModelScope.launch {
            linkingPreferences.saveCustomDeviceName(name)
            _uiEvent.emit(UiEvent.ShowSnackbar("Device name updated"))
        }
    }

    fun updateServerChanSendKey(sendKey: String) {
        viewModelScope.launch {
            linkingPreferences.saveServerChanSendKey(sendKey)
            _uiEvent.emit(UiEvent.ShowSnackbar("ServerChan key updated"))
        }
    }

    // Threshold Updaters
    fun updateLowBatteryThreshold(threshold: Int) {
        viewModelScope.launch { linkingPreferences.saveLowBatteryThreshold(threshold) }
    }

    fun updateHighBatteryThreshold(threshold: Int) {
        viewModelScope.launch { linkingPreferences.saveHighBatteryThreshold(threshold) }
    }

    fun updateLowBatteryEnabled(enabled: Boolean) {
        viewModelScope.launch { linkingPreferences.saveLowBatteryEnabled(enabled) }
    }

    fun updateHighBatteryEnabled(enabled: Boolean) {
        viewModelScope.launch { linkingPreferences.saveHighBatteryEnabled(enabled) }
    }

    fun updateActiveMonitoringEnabled(enabled: Boolean) {
        viewModelScope.launch { linkingPreferences.saveActiveMonitoringEnabled(enabled) }
    }

    // Theme Updaters
    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch { linkingPreferences.saveThemeMode(mode) }
    }

    fun updateDynamicColorEnabled(enabled: Boolean) {
        viewModelScope.launch { linkingPreferences.saveDynamicColorEnabled(enabled) }
    }

    fun updateThemeSeedColor(color: Long) {
        viewModelScope.launch { linkingPreferences.saveThemeSeedColor(color) }
    }

    fun sendTestNotification() {
        viewModelScope.launch {
            try {
                val sendKey = serverChanSendKey.value
                val deviceName = customDeviceName.value ?: Build.MODEL
                
                if (sendKey.isNullOrBlank()) {
                    _uiEvent.emit(UiEvent.ShowSnackbar("ServerChan SendKey is missing!"))
                    return@launch
                }
                
                val response = RetrofitClient.serverChanApiService.sendMessage(
                    sendKey = sendKey,
                    title = "VoltRelink Test",
                    description = "This is a test notification from **$deviceName**."
                )
                
                if (response.isSuccessful && response.body()?.code == 0) {
                    _uiEvent.emit(UiEvent.ShowSnackbar("ServerChan test success!"))
                } else {
                    _uiEvent.emit(UiEvent.ShowSnackbar("ServerChan failed: ${response.body()?.message ?: response.code()}"))
                }
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Error: ${e.localizedMessage}"))
            }
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
    }
}
