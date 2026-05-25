package com.example.voltrelay.worker.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.voltrelay.worker.BatteryWorker

class PowerConnectionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("PowerReceiver", "Received battery event: $action")
        
        // Trigger a one-time worker to check the state immediately
        val workRequest = OneTimeWorkRequestBuilder<BatteryWorker>()
            .addTag("ImmediateBatteryCheck")
            .build()
            
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
