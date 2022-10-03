package com.manchuan.tools.activity

import android.animation.LayoutTransition
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.graphics.Color
import android.graphics.Typeface
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.WindowManager
import android.view.animation.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.vectordrawable.graphics.drawable.ArgbEvaluator
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.NetworkUtils.OnNetworkStatusChangedListener
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.TimeUtils
import com.drake.interval.Interval
import com.drake.net.utils.scope
import com.dylanc.longan.sp
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar
import com.lxj.androidktx.core.post
import com.manchuan.tools.R
import com.manchuan.tools.databinding.ActivityTimeBinding
import com.manchuan.tools.model.ViewModelTime
import com.manchuan.tools.utils.SettingsLoader
import com.robinhood.ticker.TickerUtils
import com.robinhood.ticker.TickerView
import rikka.material.app.MaterialActivity
import rikka.material.preference.MaterialSwitchPreference
import rikka.preference.SimpleMenuPreference
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class TimeActivity : MaterialActivity() {

    private val netWorkListener: OnNetworkStatusChangedListener? = null
    private var filter: IntentFilter? = null
    private var intents: Intent? = null
    private var batteryStatus: Intent? = null
    private val binding by lazy {
        ActivityTimeBinding.inflate(layoutInflater)
    }
    private lateinit var interval: Interval

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        ImmersionBar.with(this).fullScreen(true).hideBar(BarHide.FLAG_HIDE_BAR)
            .keyboardEnable(false).init()
        initView()
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        timeLays.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        intents = intent
        filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        batteryStatus = registerReceiver(null, filter)
        battery.setOnClickListener { view: View? -> battery.performLongClick() }
        NetworkUtils.registerNetworkStatusChangedListener(netWorkListener)
        filter = IntentFilter()
        filter!!.addAction(Intent.ACTION_BATTERY_CHANGED)
        val sp = SPUtils.getInstance(MODE_PRIVATE)
        initObserve()
        time_view.setCharacterLists(TickerUtils.provideNumberList())
        time_view.textSize = 128.sp
        time_view.typeface = Typeface.createFromAsset(assets, "fonts/FiraCode-Regular.ttf")
        time_view.animationDuration = 1000L
        time_view.animationInterpolator = SettingsLoader.interpolator
        time_view.gravity = Gravity.CENTER
        time_view.setPreferredScrollingDirection(TickerView.ScrollingDirection.ANY)
        date_view.setCharacterLists(TickerUtils.provideNumberList())
        date_view.textSize = 18.sp
        date_view.animationDuration = 1000L
        date_view.typeface = Typeface.createFromAsset(assets, "fonts/FiraCode-Regular.ttf")
        date_view.animationInterpolator = SettingsLoader.interpolator
        date_view.gravity = Gravity.CENTER_VERTICAL
        date_view.setPreferredScrollingDirection(TickerView.ScrollingDirection.ANY)
        supportFragmentManager.beginTransaction().replace(R.id.settings, TimeFragment()).commit()
        post {
            when {
                sp.getString("darkMode") == "yes" -> {
                    startBackGroundAnimation(time_lay, Color.WHITE, Color.BLACK)
                    changeTickerColor(Color.WHITE, Color.BLACK)
                }
                sp.getString("darkMode") == "no" -> {
                    startBackGroundAnimation(time_lay, Color.BLACK, Color.WHITE)
                    changeTickerColor(Color.BLACK, Color.WHITE)
                }
                else -> {
                    sp.put("darkMode", "yes")
                    startBackGroundAnimation(time_lay, Color.WHITE, Color.BLACK)
                    changeTickerColor(Color.WHITE, Color.BLACK)
                }
            }
        }
        switch_mode.setOnClickListener { view: View? ->
            Handler(mainLooper).post {
                if (sp.getString("darkMode") == "yes") {
                    //PopTip.show("夜间模式:" + sp.getString("darkMode"));
                    sp.put("darkMode", "no")
                    startBackGroundAnimation(time_lay, Color.BLACK, Color.WHITE)
                    changeTickerColor(Color.BLACK, Color.WHITE)
                } else if (sp.getString("darkMode") == "no") {
                    //PopTip.show("夜间模式:" + sp.getString("darkMode"));
                    sp.put("darkMode", "yes")
                    startBackGroundAnimation(time_lay, Color.WHITE, Color.BLACK)
                    changeTickerColor(Color.WHITE, Color.BLACK)
                }
            }
        }
        settings.setOnClickListener {
            binding.drawerLayout.open()
        }
        interval = Interval(1, TimeUnit.SECONDS)
        interval.subscribe {
            val sdf_date = SimpleDateFormat("dd MMMM", Locale.CHINA)
            val date = sdf_date.format(Date())
            @SuppressLint("SimpleDateFormat") val sdf_time24 = SimpleDateFormat("HH:mm:ss")
            val time24 = sdf_time24.format(Date())
            @SuppressLint("SimpleDateFormat") val sdf_week_of_month = SimpleDateFormat("EE d MMMM")
            val week_of_month = sdf_week_of_month.format(Date())
            time_view.text = time24
            date_view.text = TimeUtils.getChineseWeek(Date()) + " " + date
            time_view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            val batteryManager = getSystemService(BATTERY_SERVICE) as BatteryManager
            val batteryCapacity =
                batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            battery.tooltipText = "当前电量：$batteryCapacity%"
            when (batteryManager.isCharging) {
                true -> {
                    when {
                        batteryCapacity <= 20 -> {
                            battery.setImageResource(R.drawable.twotone_battery_charging_20_black_24dp)
                        }
                        batteryCapacity <= 30 -> {
                            battery.setImageResource(R.drawable.twotone_battery_charging_30_black_24dp)
                        }
                        batteryCapacity <= 50 -> {
                            battery.setImageResource(R.drawable.twotone_battery_charging_50_black_24dp)
                        }
                        batteryCapacity <= 60 -> {
                            battery.setImageResource(R.drawable.twotone_battery_charging_60_black_24dp)
                        }
                        batteryCapacity <= 80 -> {
                            battery.setImageResource(R.drawable.twotone_battery_charging_80_black_24dp)
                        }
                        batteryCapacity <= 90 -> {
                            battery.setImageResource(R.drawable.twotone_battery_charging_90_black_24dp)
                        }
                        batteryCapacity == 100 -> {
                            battery.setImageResource(R.drawable.twotone_battery_charging_full_black_24dp)
                        }
                    }
                }
                false -> {
                    when {
                        batteryCapacity <= 20 -> {
                            battery.setImageResource(R.drawable.twotone_battery_20_black_24dp)
                        }
                        batteryCapacity <= 30 -> {
                            battery.setImageResource(R.drawable.twotone_battery_30_black_24dp)
                        }
                        batteryCapacity <= 50 -> {
                            battery.setImageResource(R.drawable.twotone_battery_50_black_24dp)
                        }
                        batteryCapacity <= 60 -> {
                            battery.setImageResource(R.drawable.twotone_battery_60_black_24dp)
                        }
                        batteryCapacity <= 80 -> {
                            battery.setImageResource(R.drawable.twotone_battery_80_black_24dp)
                        }
                        batteryCapacity <= 90 -> {
                            battery.setImageResource(R.drawable.twotone_battery_90_black_24dp)
                        }
                        batteryCapacity >= 100 -> {
                            battery.setImageResource(R.drawable.twotone_battery_full_black_24dp)
                        }
                    }
                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        interval.cancel()
        NetworkUtils.unregisterNetworkStatusChangedListener(netWorkListener)
    }

    private var fiveG: ImageView? = null


    private fun startBackGroundAnimation(bgView: View?, startColor: Int, endColor: Int) {
        //创建动画,这里的关键就是使用ArgbEvaluator, 后面2个参数就是 开始的颜色,和结束的颜色.
        scope {
            @SuppressLint("RestrictedApi") val colorAnimator =
                ValueAnimator.ofObject(ArgbEvaluator(), startColor, endColor)
            colorAnimator.addUpdateListener { animation ->
                val color = animation.animatedValue as Int //之后就可以得到动画的颜色了
                bgView!!.setBackgroundColor(color)
            }
            colorAnimator.duration = 600
            colorAnimator.start()
        }
    }

    private fun changeTickerColor(startColor: Int, endColor: Int) {
        @SuppressLint("RestrictedApi") val colorAnimator =
            ValueAnimator.ofObject(ArgbEvaluator(), endColor, startColor)
        colorAnimator.addUpdateListener { animation ->
            val color = animation.animatedValue as Int //之后就可以得到动画的颜色了
            //bgView.setBackgroundColor(color);//设置一下, 就可以看到效果.
            time_view.textColor = color
            date_view.textColor = color
            //textView.setTextColor();
            fiveG?.setColorFilter(color)
            battery.setColorFilter(color)
            switch_mode.setColorFilter(color)
            settings.setColorFilter(color)
        }
        colorAnimator.duration = 600
        colorAnimator.start()
    }

    private lateinit var time_lay: RelativeLayout
    private lateinit var switch_mode: ImageView
    private lateinit var time_view: TickerView
    private lateinit var settings: ImageView
    private lateinit var date_view: TickerView
    private lateinit var battery: ImageView
    private lateinit var timeLays: FrameLayout
    private fun initView() {
        time_lay = binding.timeLay
        switch_mode = binding.switchMode
        time_view = binding.timeView
        date_view = binding.dateView
        settings = binding.timeFullscreenExit
        battery = binding.battery
        fiveG = binding.fiveG
        timeLays = binding.timeLays
    }

    class TimeFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener,
        Preference.OnPreferenceChangeListener {
        override fun onPreferenceChange(p1: Preference, p2: Any): Boolean {
            return false
        }

        override fun onSharedPreferenceChanged(p1: SharedPreferences, p2: String) {}
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesMode = MODE_PRIVATE
            setPreferencesFromResource(R.xml.time_settings, rootKey)
            val themePreference = findPreference<Preference>("time_style") as SimpleMenuPreference
            val bright =
                findPreference<MaterialSwitchPreference>("bright_key") as MaterialSwitchPreference
            val brightTime =
                findPreference<EditTextPreference>("bright_time_key") as EditTextPreference
            themePreference.summary = themePreference.entry
            brightTime.isVisible = !bright.isChecked
            bright.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference, newValue ->
                    brightTime.isVisible = bright.isChecked
                    true
                }
            themePreference.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference, newValue ->
                    val indexOfValue = themePreference.findIndexOfValue(newValue.toString())
                    themePreference.summary =
                        if (indexOfValue >= 0) themePreference.entries[indexOfValue] else null
                    val interpolator = when (newValue.toString()) {
                        "linear" -> LinearInterpolator()
                        "reduce" -> DecelerateInterpolator()
                        "speed_down" -> AccelerateDecelerateInterpolator()
                        "speed" -> AccelerateInterpolator()
                        "stage" -> BounceInterpolator()
                        "back_forth" -> AnticipateInterpolator()
                        else -> {
                            LinearInterpolator()
                        }
                    }
                    ViewModelTime.interpolator.postValue(interpolator)
                    true
                }
        }
    }

    private fun initObserve() {
        ViewModelTime.interpolator.observe(this) {
            time_view.animationInterpolator = it
            date_view.animationInterpolator = it
        }
    }

}