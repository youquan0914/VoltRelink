package com.example.voltrelay.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart

class BatteryMonitor(private val context: Context) {

    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

    fun observeBattery(): Flow<BatteryStatus> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    trySend(it.toBatteryStatus())
                }
            }
        }

        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }.onStart {
        // Emit initial value
        val initialIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        initialIntent?.let { emit(it.toBatteryStatus()) }
    }

    private fun Intent.toBatteryStatus(): BatteryStatus {
        val currentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val chargeCounter = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
        
        val chargeTimeRemaining = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            batteryManager.computeChargeTimeRemaining()
        } else {
            -1L
        }
        
        return BatteryStatus(
            level = getIntExtra(BatteryManager.EXTRA_LEVEL, -1),
            scale = getIntExtra(BatteryManager.EXTRA_SCALE, -1),
            status = getIntExtra(BatteryManager.EXTRA_STATUS, -1),
            health = getIntExtra(BatteryManager.EXTRA_HEALTH, -1),
            plugged = getIntExtra(BatteryManager.EXTRA_PLUGGED, -1),
            temperature = getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1),
            voltage = getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1),
            currentNow = currentNow,
            chargeCounter = chargeCounter,
            chargeTimeRemaining = chargeTimeRemaining,
            technology = getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
        )
    }
}
