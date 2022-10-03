package com.manchuan.tools.activity

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaRecorder
import android.os.*
import android.view.KeyEvent
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.drake.statusbar.immersive
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.manchuan.tools.R
import com.manchuan.tools.interceptor.PermissionInterceptor
import com.manchuan.tools.view.NoiseProgressBar
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import timber.log.Timber
import java.io.File
import kotlin.math.floor
import kotlin.math.log10

class NoiseMeasurementActivity : AppCompatActivity() {
    private var isRun = true
    private val file: File? = null
    private var noise_progress: NoiseProgressBar? = null
    private var noise_text: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_noise)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        noise_progress = findViewById(R.id.noise_progress)
        noise_text = findViewById(R.id.noise_text)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "噪音测量"
        }
        immersive(toolbar)
        noise_progress!!.setMaxValues(150f)
        noise_progress!!.setCurrentValues(0)
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.let { ViewPumpContextWrapper.wrap(it) })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        methodRequiresPermission()
    }

    private fun methodRequiresPermission() {
        XXPermissions.with(this).permission(Permission.RECORD_AUDIO)
            .interceptor(PermissionInterceptor()).request { permissions, all ->
                if (all) {
                    isRun = true
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        filePath =
                            if (fileIsExists(this@NoiseMeasurementActivity.externalCacheDir!!.path + "/null.cache")) {
                                runCatching {
                                    File(this@NoiseMeasurementActivity.externalCacheDir!!.path + "/null.cache").createNewFile()
                                }
                                this@NoiseMeasurementActivity.externalCacheDir!!.path + "/null.cache"
                            } else {
                                this@NoiseMeasurementActivity.externalCacheDir!!.path + "/null.cache"
                            }
                    } else {
                        runCatching {
                            filePath =
                                if (fileIsExists(Environment.getExternalStorageDirectory().absolutePath + "/HaiYan/null")) {
                                    File(Environment.getExternalStorageDirectory().absolutePath + "/HaiYan/null").createNewFile()
                                    Environment.getExternalStorageDirectory().absolutePath + "/HaiYan/null"
                                } else {
                                    Environment.getExternalStorageDirectory().absolutePath + "/HaiYan/null"
                                }
                        }
                    }
                    startRecord()
                }
            }
    }

    @SuppressLint("HandlerLeak")
    private val handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val dbCount = msg.obj.toString().toDouble()
            val db = floor(dbCount).toInt()
            if (msg.what == 1) {
                if (dbCount <= 30) {
                    noise_text!!.text = "静谧之地，宜学习看书"
                    noise_progress!!.setCurrentValues(db)
                } else if (dbCount > 30 && dbCount < 50) {
                    noise_text!!.text = "环境正常"
                    noise_progress!!.setCurrentValues(db)
                } else if (dbCount in 50.0..70.0) {
                    noise_text!!.text = "聒噪的环境"
                    noise_progress!!.setCurrentValues(db)
                } else if (dbCount > 70 && dbCount < 100) {
                    noise_text!!.text = "喧嚣的环境，建议远离"
                    noise_progress!!.setCurrentValues(db)
                } else if (dbCount >= 100) {
                    noise_text!!.text = "过度喧嚣的环境，建议马上远离"
                    noise_progress!!.setCurrentValues(db)
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            isRun = false
            finish()
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onPause() {
        super.onPause()
        isRun = false
        runCatching {
            stopRecord()
        }
        //new File("/storage/emulated/0/SoundMeter/temp.amr").delete();
    }

    override fun onDestroy() {
        super.onDestroy()
        isRun = false
        runCatching {
            stopRecord()
        }
        //new File("/storage/emulated/0/SoundMeter/temp.amr").delete();
    }

    private val TAG = "MediaRecord"
    private lateinit var mMediaRecorder: MediaRecorder
    private var filePath: String? = null
    private val MAX_LENGTH = 600000
    private var startTime: Long = 0
    private var endTime: Long = 0
    private fun startRecord() {
        mMediaRecorder = MediaRecorder()
        runCatching {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mMediaRecorder.setOutputFile(filePath)
            mMediaRecorder.setMaxDuration(MAX_LENGTH)
            mMediaRecorder.prepare()
            mMediaRecorder.start()
        }
        startTime = System.currentTimeMillis()
        updateMicStatus()
    }

    private fun fileIsExists(strFile: String?): Boolean {
        try {
            val f = strFile?.let { File(it) }
            if (f != null) {
                if (!f.exists()) {
                    return true
                }
            }
        } catch (e: Exception) {
            return true
        }
        return false
    }

    /**
     * 停止录音
     *
     */
    fun stopRecord() {
        endTime = System.currentTimeMillis()
        Timber.tag("ACTION_END").i("endTime%s", endTime)
        mMediaRecorder.stop()
        mMediaRecorder.reset()
        mMediaRecorder.release()
        Timber.tag("ACTION_LENGTH").i("Time%s", (endTime - startTime))
    }

    private val mUpdateMicStatusTimer = Runnable { updateMicStatus() }

    // 获得两次调用该方法时间内的最大声压值
    private val maxAmplitude: Float
        get() = try {
            mMediaRecorder.maxAmplitude.toFloat() // 获得两次调用该方法时间内的最大声压值
        } catch (e: IllegalArgumentException) {
            0.0f
        }

    /**
     * 更新话筒状态
     *
     */
    private val BASE = 1
    private fun updateMicStatus() {
        runCatching {
            val ratio = maxAmplitude.toDouble() / BASE.toDouble()
            var db = 0.0 // 分贝
            if (ratio > 1) db = log10(ratio) * 20.0
            val message = Message.obtain()
            message.what = 1
            message.obj = db
            handler.sendMessage(message)
            Timber.tag(TAG).d("分贝值：%s", db)
            handler.postDelayed(mUpdateMicStatusTimer, 500)
        }
    }
}