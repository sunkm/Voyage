package com.manchuan.tools.activity

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.kongzue.dialogx.dialogs.PopTip
import com.manchuan.tools.databinding.ActivityZenModeBinding
import com.manchuan.tools.interceptor.PermissionInterceptor
import com.manchuan.tools.model.ViewModelMain
import com.manchuan.tools.utils.Utils
import com.manchuan.tools.utils.ZenModeReceiver
import com.manchuan.tools.view.customViewGroup
import com.robinhood.ticker.TickerUtils
import com.robinhood.ticker.TickerView
import org.apache.commons.lang3.StringUtils
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method


class ZenModeActivity : AppCompatActivity() {
    private lateinit var zenModeBinding: ActivityZenModeBinding
    private lateinit var dpm: DevicePolicyManager
    private lateinit var deviceAdmin: ComponentName
    private fun isActiveAdmin(): Boolean = dpm.isAdminActive(deviceAdmin)

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        zenModeBinding = ActivityZenModeBinding.inflate(layoutInflater)
        deviceAdmin = ComponentName(this, ZenModeReceiver::class.java)
        setContentView(zenModeBinding.root)
        dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        zenModeBinding.zenTime.setCharacterLists(TickerUtils.provideNumberList())
        zenModeBinding.zenTime.textSize = 80F
        zenModeBinding.zenTime.animationDuration = 700
        zenModeBinding.zenTime.setPreferredScrollingDirection(TickerView.ScrollingDirection.ANY)
        zenModeBinding.zenTime.setCharacterLists(TickerUtils.provideNumberList())
        zenModeBinding.zenTime.animationInterpolator = OvershootInterpolator()
        zenModeBinding.zenTime.gravity = Gravity.CENTER
        zenModeBinding.zenTime.text = "00 时 59 分"
        zenModeBinding.zenTime.setOnClickListener {
            val picker = MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H)
                .setTitleText("设定时间")
                .setHour(StringUtils.substringBefore(zenModeBinding.zenTime.text, " 时").toInt())
                .setMinute(StringUtils.substringBetween(zenModeBinding.zenTime.text, "时 ", " 分")
                    .toInt()).build()
            picker.show(supportFragmentManager, "ZenModeTime")
            picker.addOnPositiveButtonClickListener {
                // call back code
                zenModeBinding.zenTime.text = "${picker.hour} 时 ${picker.minute} 分"
            }
        }
        zenModeBinding.zenStart.setOnClickListener {
            if (zenModeBinding.zenTime.text.toString() == "0 时 0 分") {
                PopTip.show("禅定时间不可为0时0分")
            } else {
                XXPermissions.with(this).permission(Permission.SYSTEM_ALERT_WINDOW)
                    .interceptor(PermissionInterceptor()).request { permissions, all ->
                        if (all) {
                            val strings = zenModeBinding.zenTime.text.toString()
                            if (zenModeBinding.zenTime.text.toString().contains("0 时")) {
                                Utils.setTime((StringUtils.substringBetween(strings, "0 时 ", " 分")
                                    .toInt() * 60 * 1000).toLong())
                            } else {
                                Utils.setTime((StringUtils.substringBefore(strings, " 时")
                                    .toInt() * 60 * 60 * 1000).toLong() + (StringUtils.substringBetween(
                                    strings,
                                    "时 ",
                                    " 分").toInt() * 60 * 1000).toLong())
                            }
                            Utils.checkAccessibilityPermission(this) {
                                ViewModelMain.isShowWindow.postValue(true)
                            }
                        }
                    }
            }
        }
    }

    var view: customViewGroup? = null
    var manager: WindowManager? = null
    var innerReceiver: com.manchuan.tools.service.ZenModeReceiver? = null
    private lateinit var wmParams: WindowManager.LayoutParams

    //禁止下拉
    private fun prohibitDropDown() {
        manager = applicationContext.getSystemService(WINDOW_SERVICE) as WindowManager
        view = customViewGroup(this)
        manager!!.addView(view, params)
    }

    private //设置window type 下面变量2002是在屏幕区域显示，2003则可以显示在状态栏之上
//设置可以显示在状态栏上
//设置悬浮窗口长宽数据
    val params: WindowManager.LayoutParams
        get() {
            wmParams = WindowManager.LayoutParams()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                wmParams.type = WindowManager.LayoutParams.TYPE_PHONE
            }
            wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or  // 这是为了使通知能够接收触摸事件
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or  // 绘制状态栏
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            wmParams.gravity = Gravity.TOP
            wmParams.width = WindowManager.LayoutParams.MATCH_PARENT
            wmParams.height = WindowManager.LayoutParams.MATCH_PARENT
            wmParams.format = PixelFormat.TRANSPARENT
            return wmParams
        }

    override fun onDestroy() {
        super.onDestroy()
        if (innerReceiver != null) {
            // 解除广播
            unregisterReceiver(innerReceiver)
        }
    }

    private fun collapseNow() {
        if (collapseNotificationHandler == null) {
            collapseNotificationHandler = Handler(Looper.getMainLooper())
        }
        if (!currentFocus && !isPaused) {
            collapseNotificationHandler!!.postDelayed(object : Runnable {
                @SuppressLint("WrongConstant")
                override fun run() {
                    val statusBarService: Any = getSystemService("statusbar")
                    var statusBarManager: Class<*>? = null
                    try {
                        statusBarManager = Class.forName("android.app.StatusBarManager")
                    } catch (e: ClassNotFoundException) {
                        e.printStackTrace()
                    }
                    var collapseStatusBar: Method? = null
                    try {
                        collapseStatusBar = statusBarManager!!.getMethod("collapsePanels")
                    } catch (e: NoSuchMethodException) {
                    }
                    collapseStatusBar!!.isAccessible = true
                    try {
                        collapseStatusBar.invoke(statusBarService)
                    } catch (e: IllegalArgumentException) {
                        e.printStackTrace()
                    } catch (e: IllegalAccessException) {
                        e.printStackTrace()
                    } catch (e: InvocationTargetException) {
                        e.printStackTrace()
                    }
                    if (!currentFocus && !isPaused) {
                        collapseNotificationHandler!!.postDelayed(this, 10L)
                    }
                }
            }, 10L)
        }
    }

    override fun onPause() {
        isPaused = false
        super.onPause()
    }

    private var collapseNotificationHandler: Handler? = null
    private var currentFocus = false
    private var isPaused = false

}