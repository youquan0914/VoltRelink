package com.example.voltrelay.worker

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.voltrelay.data.linking.LinkingPreferences
import com.example.voltrelay.network.RetrofitClient
import kotlinx.coroutines.flow.first

class BatteryWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("BatteryWorker", "Starting battery check...")

        val batteryStatusIntent = applicationContext.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        ) ?: run {
            Log.e("BatteryWorker", "Could not get battery status intent")
            return Result.failure()
        }

        val level = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val percentage = if (scale > 0) (level * 100) / scale else -1

        val statusInt = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = statusInt == BatteryManager.BATTERY_STATUS_CHARGING ||
                        statusInt == BatteryManager.BATTERY_STATUS_FULL
        
        Log.d("BatteryWorker", "Battery Level: $percentage%, isCharging: $isCharging")

        if (percentage == -1) return Result.success()

        val prefs = LinkingPreferences(applicationContext)
        val lowThreshold = prefs.lowBatteryThreshold.first()
        val highThreshold = prefs.highBatteryThreshold.first()
        val lowEnabled = prefs.isLowBatteryEnabled.first()
        val highEnabled = prefs.isHighBatteryEnabled.first()
        val lastLevel = prefs.lastRecordedLevel.first()

        // Edge-trigger logic: Only trigger when crossing the threshold in the correct direction
        // High Trigger: Level was below threshold, now is at or above
        val isHighTrigger = highEnabled && percentage >= highThreshold && (lastLevel == -1 || lastLevel < highThreshold)
        
        // Low Trigger: Level was above threshold, now is at or below (and not charging)
        val isLowTrigger = lowEnabled && percentage <= lowThreshold && (lastLevel == -1 || lastLevel > lowThreshold) && !isCharging

        // Update the last known level regardless of trigger
        prefs.saveLastRecordedLevel(percentage)

        if (isHighTrigger || isLowTrigger) {
            val sendKey = prefs.serverChanSendKey.first()
            if (sendKey.isNullOrBlank()) {
                Log.w("BatteryWorker", "Alert triggered but no ServerChan SendKey found")
                return Result.success()
            }

            val customName = prefs.customDeviceName.first()
            val deviceDisplayName = if (!customName.isNullOrBlank()) customName else Build.MODEL
            val statusLabel = if (isHighTrigger) "FULL" else "LOW"

            Log.d("BatteryWorker", "Threshold met! Triggering ServerChan alert for $deviceDisplayName")
            return sendServerChanAlert(sendKey, deviceDisplayName, percentage, statusLabel)
        } else {
            Log.d("BatteryWorker", "No threshold crossing detected. (Last: $lastLevel, Current: $percentage)")
        }

        return Result.success()
    }

    private suspend fun sendServerChanAlert(sendKey: String, deviceName: String, level: Int, status: String): Result {
        return try {
            val title = "VoltRelink: $deviceName Battery $status"
            val description = "Device **$deviceName** battery is at **$level%**. Status: **$status**."
            val response = RetrofitClient.serverChanApiService.sendMessage(sendKey, title, description)
            if (response.isSuccessful && response.body()?.code == 0) {
                Log.d("BatteryWorker", "ServerChan alert sent successfully")
                Result.success()
            } else {
                val errorMsg = response.body()?.message ?: response.code().toString()
                Log.e("BatteryWorker", "ServerChan alert failed: $errorMsg")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e("BatteryWorker", "Error sending ServerChan alert", e)
            Result.retry()
        }
    }
}
