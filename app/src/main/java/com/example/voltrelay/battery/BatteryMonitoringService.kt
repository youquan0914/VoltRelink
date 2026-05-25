package com.example.voltrelay.battery

import android.app.*
import android.content.*
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.voltrelay.MainActivity
import com.example.voltrelay.R
import com.example.voltrelay.worker.BatteryWorker

class BatteryMonitoringService : Service() {

    private var lastCheckLevel = -1

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val percentage = if (scale > 0) (level * 100) / scale else -1

            if (percentage != -1 && percentage != lastCheckLevel) {
                lastCheckLevel = percentage
                Log.d("BatteryService", "Battery changed: $percentage%. Triggering check.")
                
                // Update persistent notification
                updateNotification(percentage)

                // Trigger the worker for threshold check and relay
                val workRequest = OneTimeWorkRequestBuilder<BatteryWorker>()
                    .addTag("ServiceBatteryTrigger")
                    .build()
                WorkManager.getInstance(context).enqueue(workRequest)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val initialNotification = createNotification(0)
        startForeground(NOTIFICATION_ID, initialNotification)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Battery Monitoring",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active battery monitoring status"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(percentage: Int): Notification {
        val pendingIntent = Intent(this, MainActivity::class.java).let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("VoltRelink Active")
            .setContentText("Monitoring battery... Currently at $percentage%")
            .setSmallIcon(R.mipmap.ic_launcher) // Fallback to launcher icon
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun updateNotification(percentage: Int) {
        val notification = createNotification(percentage)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val CHANNEL_ID = "battery_monitoring_channel"
        private const val NOTIFICATION_ID = 1001
    }
}
