package com.lxj.androidktx.util

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import com.lxj.androidktx.AndroidKTX

object UriHelper {
    fun setIntentDataAndType(
        intent: Intent,
        type: String?,
        uri: Uri?,
        writeAble: Boolean,
    ) {
        if (Build.VERSION.SDK_INT >= 24) {
            intent.setDataAndType(uri, type)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (writeAble) {
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
        } else {
            intent.setDataAndType(uri, type)
        }
    }

    fun setIntentData(
        intent: Intent,
        uri: Uri?,
        writeAble: Boolean,
    ) {
        if (Build.VERSION.SDK_INT >= 24) {
            intent.data = uri
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (writeAble) {
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
        } else {
            intent.data = uri
        }
    }

    /**
     * 申请uri的操作权限
     * @param intent
     * @param uri
     * @param writeAble
     */
    fun grantPermissions(intent: Intent, uri: Uri?, writeAble: Boolean) {
        var flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
        if (writeAble) {
            flag = flag or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        }
        intent.addFlags(flag)
        val resInfoList = AndroidKTX.context.packageManager.queryIntentActivities(intent,
            PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            AndroidKTX.context.grantUriPermission(packageName, uri, flag)
        }
    }
}