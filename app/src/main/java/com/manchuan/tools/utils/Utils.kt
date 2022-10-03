package com.manchuan.tools.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.manchuan.tools.service.WorkAccessibilityService
import timber.log.Timber
import kotlin.properties.Delegates

object Utils {
    const val REQUEST_FLOAT_CODE = 1001

    /**
     * 跳转到设置页面申请打开无障碍辅助功能
     */
    private fun accessibilityToSettingPage(context: Context) {
        //开启辅助功能页面
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            e.printStackTrace()
        }
    }

    /**
     * 判断Service是否开启
     *
     */
    fun isServiceRunning(context: Context, ServiceName: String): Boolean {
        if (TextUtils.isEmpty(ServiceName)) {
            return false
        }
        val myManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningService =
            myManager.getRunningServices(1000) as ArrayList<ActivityManager.RunningServiceInfo>
        for (i in runningService.indices) {
            if (runningService[i].service.className == ServiceName) {
                return true
            }
        }
        return false
    }

    fun setTime(time: Long) {
        this.times = time
    }

    fun getTime(): Long {
        return this.times
    }

    var times by Delegates.notNull<Long>()

    /**
     * 判断悬浮窗权限权限
     */
    @SuppressLint("ObsoleteSdkInt")
    private fun commonROMPermissionCheck(context: Context?): Boolean {
        var result = true
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                val clazz: Class<*> = Settings::class.java
                val canDrawOverlays =
                    clazz.getDeclaredMethod("canDrawOverlays", Context::class.java)
                result = canDrawOverlays.invoke(null, context) as Boolean
            } catch (e: Exception) {
                Timber.tag("ServiceUtils").e(Log.getStackTraceString(e))
            }
        }
        return result
    }

    /**
     * 检查悬浮窗权限是否开启
     */
    fun checkSuspendedWindowPermission(context: Activity, block: () -> Unit) {
        if (commonROMPermissionCheck(context)) {
            block()
        } else {
            Toast.makeText(context, "请开启悬浮窗权限", Toast.LENGTH_SHORT).show()
            context.startActivityForResult(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:${context.packageName}")
            }, REQUEST_FLOAT_CODE)
        }
    }

    /**
     * 检查无障碍服务权限是否开启
     */
    fun checkAccessibilityPermission(context: Activity, block: () -> Unit) {
        if (WorkAccessibilityService::class.java.canonicalName?.let {
                isServiceRunning(
                    context,
                    it
                )
            } == true) {
            block()
        } else {
            accessibilityToSettingPage(context)
        }
    }

    fun isNull(any: Any?): Boolean = any == null

}