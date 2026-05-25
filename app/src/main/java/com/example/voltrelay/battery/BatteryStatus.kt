package com.example.voltrelay.battery

import android.os.BatteryManager

data class BatteryStatus(
    val level: Int = 0,
    val scale: Int = 100,
    val status: Int = BatteryManager.BATTERY_STATUS_UNKNOWN,
    val health: Int = BatteryManager.BATTERY_HEALTH_UNKNOWN,
    val plugged: Int = 0,
    val temperature: Int = 0,
    val voltage: Int = 0, // In mV
    val currentNow: Int = 0, // In microamperes (uA)
    val chargeCounter: Int = 0, // In microampere-hours (uAh)
    val chargeTimeRemaining: Long = -1, // In milliseconds
    val technology: String? = null
) {
    val percentage: Int
        get() = if (scale > 0) (level * 100) / scale else 0

    val isCharging: Boolean
        get() = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

    val healthString: String
        get() = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Unspecified Failure"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }

    val currentInMa: Int
        get() = Math.abs(currentNow / 1000)

    val powerInWatts: Double
        get() = (voltage.toDouble() / 1000.0) * (currentInMa.toDouble() / 1000.0)

    /**
     * Optimized estimation for time to full charge.
     * Uses system value if available, otherwise falls back to a linear estimation
     * based on current capacity and charging current.
     */
    fun getEffectiveChargeTimeRemaining(targetThreshold: Int = 100): Long {
        // 1. Use system value if valid and target is 100
        if (chargeTimeRemaining > 0 && targetThreshold >= 100) {
            return chargeTimeRemaining
        }

        // 2. Fallback estimation if charging
        val absoluteCurrent = Math.abs(currentNow).toDouble()
        if (isCharging && absoluteCurrent > 0 && level > 0 && level < targetThreshold) {
            try {
                // Total capacity estimation: current_uAh / (current_level / scale)
                val totalCapacityUAh = (chargeCounter.toDouble() / level.toDouble()) * scale.toDouble()
                val targetCapacityUAh = (totalCapacityUAh * targetThreshold.toDouble()) / scale.toDouble()
                val remainingCapacityUAh = targetCapacityUAh - chargeCounter.toDouble()
                
                if (remainingCapacityUAh > 0) {
                    // Time (hours) = capacity (uAh) / current (uA)
                    val hours = remainingCapacityUAh / absoluteCurrent
                    var millis = (hours * 3600000.0).toLong()
                    
                    // Adjustment for lithium battery charging curve (slows down after 80%)
                    if (level > 80) {
                        millis = (millis * 1.5).toLong() 
                    } else if (targetThreshold > 80) {
                        val part1 = (80 - level).toDouble() / (targetThreshold - level).toDouble()
                        val part2 = 1.0 - part1
                        millis = (millis * part1 + millis * 1.5 * part2).toLong()
                    }
                    
                    return millis
                }
            } catch (e: Exception) {
                // Fallback to -1
            }
        }

        return -1L
    }
}
