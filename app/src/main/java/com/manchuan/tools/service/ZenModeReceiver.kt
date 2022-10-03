package com.manchuan.tools.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.manchuan.tools.activity.MainActivity


class ZenModeReceiver : BroadcastReceiver() {
    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onReceive(arg0: Context, arg1: Intent) {
        val action = arg1.action
        if (action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {                                           //按下Home键会发送ACTION_CLOSE_SYSTEM_DIALOGS的广播
            val reason = arg1.getStringExtra(SYSTEM_DIALOG_REASON_KEY)
            if (reason != null) {
                if (reason == SYSTEM_DIALOG_REASON_HOME_KEY) {
                    val intent = Intent(arg0, MainActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP //点击home键无延时，且不会产生新的activity
                    val pendingIntent: PendingIntent = PendingIntent.getActivity(
                        arg0, 0,
                        intent, 0
                    )
                    try {
                        pendingIntent.send()
                    } catch (e: PendingIntent.CanceledException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        //开机自启动
        if (arg1.action == ACTION) {
            val mainActivityIntent = Intent(
                arg0,
                MainActivity::class.java
            )
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            arg0.startActivity(mainActivityIntent)
        }
    }

    companion object {
        //开机自启动
        const val ACTION = "android.intent.action.BOOT_COMPLENTED"
        const val SYSTEM_DIALOG_REASON_KEY = "reason"
        const val SYSTEM_DIALOG_REASON_HOME_KEY = "homekey"
    }
}