package com.manchuan.tools.service

import android.app.IntentService
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.annotation.Nullable
import com.manchuan.tools.socket.ReceiveSocket


class Wifip2pService : IntentService("Wifip2pService") {
    private var mReceiveSocket: ReceiveSocket? = null
    @Deprecated("Deprecated in Java", ReplaceWith("MyBinder()"))
    @Nullable
    override fun onBind(intent: Intent?): IBinder {
        return MyBinder()
    }

    inner class MyBinder : Binder() {
        fun initListener(listener: ReceiveSocket.ProgressReceiveListener?) {
            mReceiveSocket?.setOnProgressReceiveListener(listener)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, "服务启动了")
    }

    @Deprecated("Deprecated in Java")
    override fun onHandleIntent(intent: Intent?) {
        mReceiveSocket = ReceiveSocket()
        mReceiveSocket!!.createServerSocket()
        Log.e(TAG, "传输完毕")
    }

    @Deprecated("Deprecated in Java")
    override fun onDestroy() {
        super.onDestroy()
        mReceiveSocket?.clean()
    }

    companion object {
        private const val TAG = "Wifip2pService"
    }
}
