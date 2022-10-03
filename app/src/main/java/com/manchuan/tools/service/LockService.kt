package com.manchuan.tools.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.DisplayMetrics
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.manchuan.tools.R
import timber.log.Timber

class LockService : AccessibilityService() {
    private lateinit var maskViewCore: LinearLayout

    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {
    }

    override fun onCreate() {
        super.onCreate()
        runCatching {
            createFullScreenView(applicationContext)
        }.onFailure {
            it.message?.let { it1 -> Timber.tag("无障碍服务错误").e(it1) }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onServiceConnected() {
        super.onServiceConnected()
        val accessibilityServiceInfo = AccessibilityServiceInfo()
        accessibilityServiceInfo.eventTypes = 32
        accessibilityServiceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_AUDIBLE
        serviceInfo = accessibilityServiceInfo
        createNotificationChannel()
        val builder = NotificationCompat.Builder(this, "100")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("禅定模式 - 无障碍服务已开启")
            .setStyle(
                NotificationCompat.InboxStyle()
                    .setBigContentTitle("禅定模式 - 无障碍服务已开启")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(100, builder.build())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun createFullScreenView(context: Context?) {
        val windowManager: WindowManager =
            context?.getSystemService(WINDOW_SERVICE) as WindowManager
        maskViewCore = LinearLayout(context)
        val fullScreenParams = WindowManager.LayoutParams()
        fullScreenParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        fullScreenParams.format = PixelFormat.TRANSLUCENT
        fullScreenParams.flags =
            fullScreenParams.flags or (WindowManager.LayoutParams.FLAG_FULLSCREEN
                    or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        fullScreenParams.gravity = Gravity.CENTER
        maskViewCore.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT)
        maskViewCore.setOnClickListener {
            Toast.makeText(context,"点击事件",Toast.LENGTH_SHORT).show()
        }
        windowManager.addView(maskViewCore, fullScreenParams)
        maskViewCore
    }

    private fun createMaskView(context: Context, windowManager: WindowManager): View {
        val layoutParams =
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                -3
            )
        if (Build.VERSION.SDK_INT >= 28) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                layoutParams.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
        }
        layoutParams.x = 0
        layoutParams.y = 0
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        (layoutParams as ViewGroup.LayoutParams).width = displayMetrics.widthPixels
        (layoutParams as ViewGroup.LayoutParams).height = displayMetrics.heightPixels
        val frameLayout = FrameLayout(context, null)
        frameLayout.layoutParams = WindowManager.LayoutParams(200, 200)
        frameLayout.setBackgroundColor(-14474461)
        val inflate: View =
            LayoutInflater.from(context).inflate(R.layout.activity_app, null as ViewGroup?)
        inflate.layoutParams = FrameLayout.LayoutParams(-1, -1)
        frameLayout.addView(inflate)
        windowManager.addView(frameLayout, layoutParams)
        return inflate
    }


    @SuppressLint("WrongConstant")
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "禅定模式无障碍服务"
            val descriptionText = "保活服务"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("100", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onInterrupt() {

    }
}