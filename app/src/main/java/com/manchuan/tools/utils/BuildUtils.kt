package com.manchuan.tools.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import timber.log.Timber

object BuildUtils {
    private const val TAG = "BuildUtils"
    val osVersion: String
        get() = Build.VERSION.RELEASE
    val id: String
        get() = Build.ID
    val product: String
        get() = Build.PRODUCT
    val device: String
        get() = Build.DEVICE
    val board: String
        get() = Build.BOARD
    val brand: String
        get() = Build.BRAND
    val bootLoader: String
        get() = Build.BOOTLOADER
    val hardware: String
        get() = Build.HARDWARE
    val fingerprint: String
        get() = Build.FINGERPRINT

    val model: String
        get() = Build.MODEL
    val name: String
        get() = Build.PRODUCT

    /**
     * 获取厂商
     *
     * @return e.g. HUAWEI
     */
    val manufacturer: String
        get() = Build.MANUFACTURER
    val sdkVersion: Int
        get() = Build.VERSION.SDK_INT

    @SuppressLint("HardwareIds")
    fun getUDID(context: Context): String {
        return try {
            Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e, "getUDID")
            "android_id_unknown"
        }
    }

    @get:SuppressLint("HardwareIds")
    val sN: String
        get() {
            var `val`: String? = null
            if (26 <= Build.VERSION.SDK_INT) {
                try {
                    `val` = Build.getSerial()
                } catch (ignore: Throwable) {
                }
            }
            if (TextUtils.isEmpty(`val`)) {
                try {
                    `val` = Build.SERIAL
                } catch (ignore: Throwable) {
                }
            }
            return if (TextUtils.isEmpty(`val`)) Build.UNKNOWN else `val`!!
        }
}