package com.manchuan.tools.service

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import timber.log.Timber

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            val thisIntent = Intent(context, LockService::class.java) //启动服务
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(thisIntent)
            } else {
                context.startService(thisIntent)
            }
            try {
                var enabledServices = getEnabledServices(context)
                if (enabledServices === emptySet<Any>() as Set<*>) {
                    enabledServices = HashSet()
                }
                val toggledService =
                    ComponentName.unflattenFromString("com.manchuan.tools.service.WorkAccessibilityService") //添加服务
                enabledServices.plus(toggledService)
                val enabledServicesBuilder = StringBuilder()
                for (enabledService in enabledServices) {
                    enabledServicesBuilder.append(enabledService!!.flattenToString())
                    enabledServicesBuilder.append(':')
                }
                val enabledServicesBuilderLength = enabledServicesBuilder.length
                if (enabledServicesBuilderLength > 0) {
                    enabledServicesBuilder.deleteCharAt(enabledServicesBuilderLength - 1)
                }
                Settings.Secure.putString(
                    context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                    enabledServicesBuilder.toString()
                )
                Settings.Secure.putInt(
                    context.contentResolver,
                    Settings.Secure.ACCESSIBILITY_ENABLED, 1
                )
            } catch (error: Exception) {
                Timber.e(error)
            }
        }
    }

    fun getEnabledServices(context: Context): Set<ComponentName?> {
        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
            ?: return emptySet<ComponentName>()
        val enabledServices: MutableSet<ComponentName?> = HashSet()
        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)
        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            val enabledService = ComponentName.unflattenFromString(
                componentNameString
            )
            if (enabledService != null) {
                enabledServices.add(enabledService)
            }
        }
        return enabledServices
    }
}