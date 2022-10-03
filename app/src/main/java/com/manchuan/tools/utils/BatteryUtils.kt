package com.manchuan.tools.utils

import android.annotation.SuppressLint
import android.content.Context

object BatteryUtils {

    @SuppressLint("PrivateApi")
    fun getBatteryCapacity(context: Context?): Double {
        val mPowerProfile: Any
        var batteryCapacity = 0.0
        val POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile"
        try {
            mPowerProfile = Class.forName(POWER_PROFILE_CLASS).getConstructor(Context::class.java)
                .newInstance(context)
            batteryCapacity = Class.forName(POWER_PROFILE_CLASS).getMethod("getBatteryCapacity")
                .invoke(mPowerProfile) as Double
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return batteryCapacity
    }

}