package com.manchuan.tools.utils

import android.content.Context
import android.os.Environment
import android.os.Build
import java.io.File

object StorageManager {
    private fun createAppFolder() {
        val appFolder =
            Environment.getExternalStorageDirectory().absolutePath + File.separator + "HaiYan"
        if (!File(appFolder).exists()) {
            File(appFolder).mkdirs()
        }
    }

    private fun createCacheFolder() {
        val appFolder =
            Environment.getExternalStorageDirectory().absolutePath + File.separator + "HaiYan" + File.separator + "cache"
        if (!File(appFolder).exists()) {
            File(appFolder).mkdirs()
        }
    }

    private fun createLogcatFolder(context: Context) {
        val logcatFolder = context.externalCacheDir!!.absolutePath + File.separator + "logs"
        if (!File(logcatFolder).exists()) {
            File(logcatFolder).mkdirs()
        }
    }

    fun createAllFolder(context: Context) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            createAppFolder()
            createCacheFolder()
            createLogcatFolder(context)
        }
    }
}