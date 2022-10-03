package com.manchuan.tools.service

import android.annotation.SuppressLint
import android.graphics.PixelFormat
import android.os.Build
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import androidx.lifecycle.LifecycleService
import com.manchuan.tools.R
import com.manchuan.tools.interfaces.ItemViewTouchListener
import com.manchuan.tools.model.ViewModelMain
import com.manchuan.tools.utils.Utils

class SuspendwindowService : LifecycleService() {
    private lateinit var windowManager: WindowManager
    private var floatRootView: View? = null//悬浮窗View


    override fun onCreate() {
        super.onCreate()
        initObserve()
    }

    private fun initObserve() {
        ViewModelMain.apply {
            isVisible.observe(this@SuspendwindowService) {
                floatRootView?.visibility = if (it) View.VISIBLE else View.GONE
            }
            isShowSuspendWindow.observe(this@SuspendwindowService) {
                if (it) {
                    showWindow()
                } else {
                    if (!Utils.isNull(floatRootView)) {
                        if (!Utils.isNull(floatRootView?.windowToken)) {
                            if (!Utils.isNull(windowManager)) {
                                windowManager.removeView(floatRootView)
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    private fun showWindow() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(outMetrics)
        val layoutParam = WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
            format = PixelFormat.RGBA_8888
            flags =
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            //位置大小设置
            width = WRAP_CONTENT
            height = WRAP_CONTENT
            gravity = Gravity.LEFT or Gravity.TOP
            //设置剧中屏幕显示
            x = outMetrics.widthPixels / 2 - width / 2
            y = outMetrics.heightPixels / 2 - height / 2
        }
        // 新建悬浮窗控件
        floatRootView = LayoutInflater.from(this).inflate(R.layout.activity_main,null)
        floatRootView?.setOnTouchListener(ItemViewTouchListener(layoutParam, windowManager))
        // 将悬浮窗控件添加到WindowManager
        windowManager.addView(floatRootView, layoutParam)
    }
}