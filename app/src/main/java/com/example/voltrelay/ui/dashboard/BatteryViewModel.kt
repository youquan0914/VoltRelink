package com.example.voltrelay.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.voltrelay.battery.BatteryMonitor
import com.example.voltrelay.battery.BatteryStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class BatteryViewModel(application: Application) : AndroidViewModel(application) {
    private val batteryMonitor = BatteryMonitor(application)

    val batteryStatus: StateFlow<BatteryStatus> = batteryMonitor.observeBattery()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BatteryStatus()
        )
}
