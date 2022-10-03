package com.manchuan.tools.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Vibrator
import android.view.MenuItem
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import com.drake.statusbar.immersive
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.card.MaterialCardView
import com.manchuan.tools.R
import com.manchuan.tools.extensions.errorColor
import com.manchuan.tools.extensions.getColorByAttr
import com.manchuan.tools.utils.BaseActivity
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.white.progressview.CircleProgressView
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import java.math.BigDecimal
import kotlin.math.sqrt

class MetalDetectionActivity : BaseActivity() {
    private var a: SensorManager? = null
    private var alarmLim = 0.0
    private var _a_sensor_listener: SensorEventListener? = null

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.let { ViewPumpContextWrapper.wrap(it) })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_metal)
        initView(this)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "金属探测器"
        }
        a = getSystemService(SENSOR_SERVICE) as SensorManager
        _a_sensor_listener = object : SensorEventListener {
            override fun onSensorChanged(_param1: SensorEvent) {
                val _rotationMatrix = FloatArray(16)
                SensorManager.getRotationMatrixFromVector(_rotationMatrix, _param1.values)
                val _remappedRotationMatrix = FloatArray(16)
                SensorManager.remapCoordinateSystem(
                    _rotationMatrix,
                    SensorManager.AXIS_X,
                    SensorManager.AXIS_Z,
                    _remappedRotationMatrix
                )
                val _orientations = FloatArray(3)
                SensorManager.getOrientation(_remappedRotationMatrix, _orientations)
                for (_i in 0..2) {
                    _orientations[_i] = Math.toDegrees(
                        _orientations[_i].toDouble()
                    ).toFloat()
                }
                val _x = _orientations[0].toDouble()
                val _y = _orientations[1].toDouble()
                val _z = _orientations[2].toDouble()
            }

            override fun onAccuracyChanged(_param1: Sensor, _param2: Int) {}
        }
        a!!.registerListener(
            _a_sensor_listener,
            a!!.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR),
            SensorManager.SENSOR_DELAY_NORMAL
        )
        immersive(toolbar!!)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private var sensorManager: SensorManager? = null
    public override fun onPause() {
        super.onPause()
        sensorManager!!.unregisterListener(sensorEventListener)
    }

    public override fun onResume() {
        super.onResume()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager!!.registerListener(
            sensorEventListener, sensorManager!!.getDefaultSensor(
                Sensor.TYPE_MAGNETIC_FIELD
            ), SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    private val sensorEventListener: SensorEventListener = object : SensorEventListener {
        @SuppressLint("SetTextI18n")
        override fun onSensorChanged(sensorEvent: SensorEvent) {
            val rawTotal: Double //未处理的数据
            if (sensorEvent.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                //保持屏幕常亮
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                //分别计算三轴磁感应强度
                val X_lateral = sensorEvent.values[0]
                val Y_lateral = sensorEvent.values[1]
                val Z_lateral = sensorEvent.values[2]
                //Log.d(TAG,X_lateral + "");
                //计算出总磁感应强度
                rawTotal =
                    sqrt((X_lateral * X_lateral + Y_lateral * Y_lateral + Z_lateral * Z_lateral).toDouble())
                //初始化BigDecimal类
                val total = BigDecimal(rawTotal)
                val res = total.setScale(2, BigDecimal.ROUND_HALF_UP).toDouble()
                textview3!!.text = "$X_lateral μT"
                textview5!!.text = "$Y_lateral μT"
                textview7!!.text = "$Z_lateral μT"
                textview9!!.text = "$res μT"
                val alarmLimStr = "80"
                alarmLim = alarmLimStr.toDouble()
                if (res < alarmLim) {
                    textview1!!.setTextColor(getColor(R.color.textColor)) //设置文字颜色为黑色
                    textview1!!.text = "未探测到金属"
                    val progress = (res / alarmLim * 100).toInt() //计算进度
                    progressview!!.reachBarColor = getColorByAttr(com.google.android.material.R.attr.colorPrimary)
                    progressview!!.progress = progress //进度条
                    progressview!!.textColor = getColorByAttr(com.google.android.material.R.attr.colorPrimary)
                } else {
                    textview1!!.setTextColor(errorColor()) //红色
                    textview1!!.text = "探测到金属!"
                    progressview!!.reachBarColor = errorColor()
                    progressview!!.progress = 100 //进度条满
                    progressview!!.textColor = errorColor()
                    //震动
                    vibrate()
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    @SuppressLint("MissingPermission")
    private fun vibrate() {
        val vibrator = this.getSystemService(VIBRATOR_SERVICE) as Vibrator
        this.getSystemService(VIBRATOR_SERVICE) //获得 一个震动的服务
        vibrator.vibrate(100)
    }

    private var _coordinatorLayout: CoordinatorLayout? = null
    private var appBarLayout: AppBarLayout? = null
    private var toolbar: Toolbar? = null
    private var _linear: LinearLayout? = null
    private var sl: SmartRefreshLayout? = null
    private var vscroll1: NestedScrollView? = null
    private var linear1: LinearLayout? = null
    private var progressview: CircleProgressView? = null
    private var textview1: TextView? = null
    private var cardview1: MaterialCardView? = null
    private var linear2: LinearLayout? = null
    private var linear3: LinearLayout? = null
    private var textview2: TextView? = null
    private var textview3: TextView? = null
    private var linear4: LinearLayout? = null
    private var textview4: TextView? = null
    private var textview5: TextView? = null
    private var linear5: LinearLayout? = null
    private var textview6: TextView? = null
    private var textview7: TextView? = null
    private var linear6: LinearLayout? = null
    private var textview8: TextView? = null
    private var textview9: TextView? = null
    private fun initView(activity: Activity) {
        _coordinatorLayout = activity.findViewById(R.id._coordinatorLayout)
        appBarLayout = activity.findViewById(R.id.appBarLayout)
        toolbar = activity.findViewById(R.id.toolbar)
        _linear = activity.findViewById(R.id._linear)
        sl = activity.findViewById(R.id.sl)
        vscroll1 = activity.findViewById(R.id.vscroll1)
        linear1 = activity.findViewById(R.id.linear1)
        progressview = activity.findViewById(R.id.progressview)
        textview1 = activity.findViewById(R.id.textview1)
        cardview1 = activity.findViewById(R.id.cardview1)
        linear2 = activity.findViewById(R.id.linear2)
        linear3 = activity.findViewById(R.id.linear3)
        textview2 = activity.findViewById(R.id.textview2)
        textview3 = activity.findViewById(R.id.textview3)
        linear4 = activity.findViewById(R.id.linear4)
        textview4 = activity.findViewById(R.id.textview4)
        textview5 = activity.findViewById(R.id.textview5)
        linear5 = activity.findViewById(R.id.linear5)
        textview6 = activity.findViewById(R.id.textview6)
        textview7 = activity.findViewById(R.id.textview7)
        linear6 = activity.findViewById(R.id.linear6)
        textview8 = activity.findViewById(R.id.textview8)
        textview9 = activity.findViewById(R.id.textview9)
    }
}