package com.manchuan.tools.activity.transfer

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Rect
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.kongzue.dialogx.dialogs.PopTip
import com.manchuan.tools.R
import com.manchuan.tools.databinding.ActivityReceiveBinding
import com.manchuan.tools.service.Wifip2pService
import com.manchuan.tools.service.Wifip2pService.MyBinder
import com.manchuan.tools.socket.ReceiveSocket
import com.manchuan.tools.utils.BuildUtils
import moe.feng.common.stepperview.VerticalStepperItemView
import timber.log.Timber
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method


class ReceiveActivity : BaseActivity(), ReceiveSocket.ProgressReceiveListener {
    private val binding by lazy {
        ActivityReceiveBinding.inflate(layoutInflater)
    }
    private lateinit var mBinder: MyBinder
    private val mSteppers = arrayOfNulls<VerticalStepperItemView>(3)
    private lateinit var mIntent: Intent
    private var step: Int = 0

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder) {
            //调用服务里面的方法进行绑定
            mBinder = service as MyBinder
            mBinder.initListener(this@ReceiveActivity)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            //服务断开重新绑定
            bindService(mIntent, this, Context.BIND_AUTO_CREATE)
        }
    }

    private lateinit var p2pConfig: WifiP2pConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        mSteppers[0] = binding.stepper0
        mSteppers[1] = binding.stepper1
        mSteppers[2] = binding.stepper2
        VerticalStepperItemView.bindSteppers(*mSteppers)
        binding.title.setFactory {
            TextView(ContextThemeWrapper(this, R.style.TextView_SansSerif)).apply {
                setTextAppearance(com.google.android.material.R.style.TextAppearance_AppCompat_Display1)
                gravity = Gravity.CENTER
            }
        }
        binding.subtitle.setFactory {
            TextView(ContextThemeWrapper(this, R.style.TextView_SansSerif)).apply {
                setTextAppearance(com.google.android.material.R.style.TextAppearance_AppCompat_Medium)
                gravity = Gravity.CENTER
            }
        }
        mIntent = Intent(this, Wifip2pService::class.java)
        startService(mIntent)
        bindService(mIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        binding.title.setCurrentText("接收文件")
        binding.subtitle.setCurrentText("准备开始")
        val rectangle = Rect()
        window.decorView.getWindowVisibleDisplayFrame(rectangle)
        //高度为rectangle.top-0仍为rectangle.top
        //高度为rectangle.top-0仍为rectangle.top
        val inAnimator: Animation = AnimationUtils.loadAnimation(this,
            android.R.anim.fade_in)
        val out: Animation = AnimationUtils.loadAnimation(this,
            android.R.anim.fade_out)
        binding.title.inAnimation = inAnimator
        binding.title.outAnimation = out
        binding.subtitle.inAnimation = inAnimator
        binding.subtitle.outAnimation = out
        binding.progress.layoutParams =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, rectangle.top)
        binding.buttonNext0.setOnClickListener {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS));
        }
        binding.location.setOnClickListener {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
        binding.button4.setOnClickListener {
            if (step == 0) {
                step = 1
                binding.steps.visibility = View.GONE
                binding.title.setText("创建连接")
                binding.subtitle.setText("第一步:设置设备名称")
                binding.devices.visibility = View.VISIBLE
                binding.deviceName.setText(BuildUtils.model)
                binding.button4.text = "创建连接"
            } else if (step == 1 && binding.button4.text.toString() == "创建连接") {
                if (binding.deviceName.text.toString().isEmpty()) {
                    PopTip.show("设备名不能为空")
                } else {
                    mWifiP2pManager?.apply {
                        setDeviceName(binding.deviceName.text.toString())
                    }
                    createGroup()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching {
            removeGroup()
        }
        clear()
    }

    private fun removeGroup() {
        mWifiP2pManager!!.removeGroup(mChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
            }

            override fun onFailure(reason: Int) {
            }
        })
    }

    private fun setDeviceName(deviceName: String) {
        try {
            val paramTypes0 = WifiP2pManager.Channel::class.java
            val paramTypes1 = String::class.java
            val paramTypes2 = WifiP2pManager.ActionListener::class.java

            val setDeviceName: Method = mWifiP2pManager!!.javaClass.getMethod("setDeviceName",
                paramTypes0,
                paramTypes1,
                paramTypes2)
            setDeviceName.isAccessible = true
            setDeviceName.invoke(mWifiP2pManager, mChannel,
                deviceName,
                object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        Log.i("局域网快传", "setDeviceName succeeded")
                    }

                    override fun onFailure(reason: Int) {
                        Log.i("局域网快传", "setDeviceName failed")
                    }
                })
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }


    @SuppressLint("MissingPermission")
    private fun createGroup() {
        mChannel?.let {
            mWifiP2pManager!!.createGroup(it, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Timber.e("TAG", "创建群组成功")
                    Toast.makeText(this@ReceiveActivity, "创建群组成功", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(reason: Int) {
                    Timber.e("TAG",
                        "创建群组失败: $reason")
                    Toast.makeText(this@ReceiveActivity,
                        "创建群组失败,请移除已有的组群或者连接同一WIFI重试",
                        Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    override fun onSatrt() {
        binding.progress.show()
        binding.progress.isIndeterminate = false
        binding.title.setText("传输中")
        binding.subtitle.setText("请勿离开页面")
    }

    override fun onProgressChanged(file: File?, progress: Int) {
        binding.progress.setProgressCompat(progress,true)
    }

    override fun onFinished(file: File?) {
        binding.progress.hide()
        binding.progress.isIndeterminate = true
        binding.title.setText("传输完成")
        binding.subtitle.setText("欢迎使用")
    }

    override fun onFaliure(file: File?) {
        binding.progress.hide()
        binding.progress.isIndeterminate = true
        binding.title.setText("传输失败")
        binding.subtitle.setText("请检查网络及设备")
    }

    private fun clear() {
        unbindService(serviceConnection)
        stopService(mIntent)
    }

}