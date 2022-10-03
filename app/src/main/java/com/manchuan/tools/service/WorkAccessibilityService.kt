package com.manchuan.tools.service

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.CountDownTimer
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.manchuan.tools.R
import com.manchuan.tools.interfaces.ItemViewTouchListener
import com.manchuan.tools.model.ViewModelMain
import com.manchuan.tools.utils.Utils
import com.manchuan.tools.utils.Utils.isNull


/**
 * @功能:利用无障碍打开悬浮窗口 无局限性 任何界面可以显示
 * @User Lmy
 * @Creat 4/15/21 5:57 PM
 * @Compony 永远相信美好的事情即将发生
 */
class WorkAccessibilityService : AccessibilityService(), LifecycleOwner {
    private lateinit var windowManager: WindowManager
    private var floatRootView: View? = null//悬浮窗View
    private val mLifecycleRegistry = LifecycleRegistry(this)
    override fun onCreate() {
        super.onCreate()
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
        initObserve()
    }

    /**
     * 打开关闭的订阅
     */
    private fun initObserve() {
        ViewModelMain.isShowWindow.observe(this) {
            if (it) {
                showWindow()
            } else {
                if (!isNull(floatRootView)) {
                    if (!isNull(floatRootView?.windowToken)) {
                        if (!isNull(windowManager)) {
                            windowManager.removeView(floatRootView)
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    private fun showWindow() {
        // 设置LayoutParam
        // 获取WindowManager服务
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(outMetrics)
        val layoutParam = WindowManager.LayoutParams()
        layoutParam.apply {
            //显示的位置
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
                //刘海屏延伸到刘海里面
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            } else {
                type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            }
            flags = (WindowManager.LayoutParams.FLAG_FULLSCREEN
                    and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    and WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            format = PixelFormat.RGBA_8888
        }
        floatRootView = LayoutInflater.from(this).inflate(R.layout.activity_zen_mode, null)
        floatRootView?.setOnTouchListener(ItemViewTouchListener(layoutParam, windowManager))
        floatRootView?.setBackgroundColor(Color.BLACK)
        val zenTitle = floatRootView?.findViewById<TextView>(R.id.zen_title)
        windowManager.addView(floatRootView, layoutParam)
        zenTitle?.setTextColor(Color.WHITE)
        object : CountDownTimer(Utils.getTime(), 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                zenTitle?.text = timeConversion((millisUntilFinished / 1000).toInt())
            }
            override fun onFinish() {
                if (!isNull(floatRootView)) {
                    if (!isNull(floatRootView?.windowToken)) {
                        if (!isNull(windowManager)) {
                            windowManager.removeView(floatRootView)
                        }
                    }
                }
            }
        }.start()
    }

    fun timeConversion(time: Int): String {
        var hour = 0
        var minutes = 0
        var sencond = 0
        val temp = time % 3600
        if (time > 3600) {
            hour = time / 3600
            if (temp != 0) {
                if (temp > 60) {
                    minutes = temp / 60
                    if (temp % 60 != 0) {
                        sencond = temp % 60
                    }
                } else {
                    sencond = temp
                }
            }
        } else {
            minutes = time / 60
            if (time % 60 != 0) {
                sencond = time % 60
            }
        }
        return (if (hour < 10) "0$hour" else hour).toString() + ":" + (if (minutes < 10) "0$minutes" else minutes) + ":" + (if (sencond < 10) "0$sencond" else sencond)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    override fun getLifecycle(): Lifecycle = mLifecycleRegistry

    @Deprecated("Deprecated in Java")
    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
    }
}