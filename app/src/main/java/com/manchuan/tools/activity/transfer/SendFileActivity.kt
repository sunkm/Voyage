package com.manchuan.tools.activity.transfer

import ando.file.core.FileUri
import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.drake.interval.Interval
import com.github.dhaval2404.imagepicker.ImagePicker
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.R
import com.manchuan.tools.bean.FileBean
import com.manchuan.tools.databinding.ActivitySendFileBinding
import com.manchuan.tools.extensions.setTextWithAnimation
import com.manchuan.tools.model.DeviceNameTag
import com.manchuan.tools.utils.Md5Util.getMd5
import moe.feng.common.stepperview.VerticalStepperItemView
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit


class SendFileActivity : BaseActivity() {
    private val mSteppers = arrayOfNulls<VerticalStepperItemView>(3)
    private val binding by lazy {
        ActivitySendFileBinding.inflate(layoutInflater)
    }

    private var isSearching: Boolean = false
    private val mListDeviceName: ArrayList<DeviceNameTag> = ArrayList()
    private val mListDevice: ArrayList<WifiP2pDevice> = ArrayList()
    private lateinit var interval: Interval

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        mSteppers[0] = binding.stepper0
        mSteppers[1] = binding.stepper1
        mSteppers[2] = binding.stepper2
        binding.container.layoutTransition.enableTransitionType(LayoutTransition.CHANGE_APPEARING)
        binding.rootLay.layoutTransition.enableTransitionType(LayoutTransition.DISAPPEARING)
        VerticalStepperItemView.bindSteppers(*mSteppers)
        binding.subtitle.setTextWithAnimation("准备开始")
        val rectangle = Rect()
        window.decorView.getWindowVisibleDisplayFrame(rectangle)
        binding.progress.layoutParams =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, rectangle.top)
        binding.buttonNext0.setOnClickListener {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS));
        }
        binding.location.setOnClickListener {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
        binding.button4.setOnClickListener {
            when {
                !isSearching && binding.button4.text.toString() == "开始传输" -> {
                    ImagePicker.with(this).galleryOnly().galleryMimeTypes(arrayOf("*/*"))
                        .createIntent {
                            startForProfileImageResult.launch(it)
                        }
                }
                !isSearching -> {
                    binding.steps.visibility = View.GONE
                    binding.title.setTextWithAnimation("搜索设备")
                    connectServer()
                    isSearching = true
                }
                else -> {
                    PopTip.show("正在搜索中，请勿重复点击")
                }
            }
        }
    }

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data
            if (resultCode == Activity.RESULT_OK) {
                //Image Uri will not be null for RESULT_OK
                val uri: Uri? = data?.data
                if (uri != null) {
                    val path: String? = FileUri.getPathByUri(uri)
                    val file = path?.let { it1 -> File(it1) }
                    if (file != null) {
                        if (!file.exists() || mWifiP2pInfo == null) {
                            Toast.makeText(this@SendFileActivity, "文件路径找不到", Toast.LENGTH_SHORT)
                                .show()
                            return@registerForActivityResult
                        }
                    }
                    val md5 = getMd5(file)
                    val fileBean = file?.let { it1 -> FileBean(it1.path, file.length(), md5) }
                    val hostAddress = mWifiP2pInfo!!.groupOwnerAddress.hostAddress
                    SendTask(this@SendFileActivity, fileBean).execute(hostAddress)
                }
            } else if (resultCode == ImagePicker.RESULT_ERROR) {
                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
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
        return (if (minutes < 10) "0$minutes" else minutes).toString() + "分" + (if (sencond < 10) "0$sencond" else sencond) + "秒"
    }

    @SuppressLint("MissingPermission")
    private fun connectServer() {
        mWifiP2pManager!!.discoverPeers(mChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Timber.tag("WIFI P2P").e("搜索设备成功")
                binding.devices.visibility = View.VISIBLE
                binding.progress.show()
                interval = Interval(0, 1, TimeUnit.SECONDS, 300).subscribe {
                    binding.subtitle.setTextWithAnimation("搜索中 剩余时间:${timeConversion(it.toInt())}")
                }.finish {
                    isSearching = false
                    binding.title.setTextWithAnimation("搜索超时")
                    binding.subtitle.setTextWithAnimation("请重新搜索")
                }.start()
            }

            override fun onFailure(reasonCode: Int) {
                Timber.tag("WIFI P2P").e("搜索设备失败")
                binding.devices.visibility = View.VISIBLE
                binding.subtitle.setTextWithAnimation("搜索失败")
                binding.progress.hide()
                isSearching = false
            }
        })
    }

    @SuppressLint("MissingPermission")
    private fun connect(wifiP2pDevice: WifiP2pDevice?) {
        val config = WifiP2pConfig()
        if (wifiP2pDevice != null) {
            config.deviceAddress = wifiP2pDevice.deviceAddress
            config.wps.setup = WpsInfo.PBC
            mWifiP2pManager!!.connect(mChannel, config, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Timber.tag("WIFI P2P").e("连接成功")
                    TipDialog.show("连接成功", WaitDialog.TYPE.SUCCESS)
                    binding.devices.visibility = View.INVISIBLE
                    binding.button4.text = "开始传输"
                }

                override fun onFailure(reason: Int) {
                    Timber.tag("WIFI P2P").e("连接失败")
                    TipDialog.show("连接失败", WaitDialog.TYPE.ERROR)
                }
            })
        }
    }

    override fun onPeersInfo(wifiP2pDeviceList: Collection<WifiP2pDevice?>?) {
        super.onPeersInfo(wifiP2pDeviceList)
        if (wifiP2pDeviceList != null) {
            for (device in wifiP2pDeviceList) {
                if (!mListDeviceName.contains(DeviceNameTag(device!!.deviceName,
                        device.deviceAddress)) && !mListDevice.contains(device)
                ) {
                    mListDeviceName.add(DeviceNameTag(device.deviceName, device.deviceAddress))
                    mListDevice.add(device)
                }
            }
        }
        showDeviceInfo()
        binding.subtitle.setText("共搜索到${mListDevice.size}个设备")
        binding.progress.hide()
        interval.cancel()
        isSearching = false
    }

    private fun showDeviceInfo() {
        binding.devices.linear().setup {
            setAnimation(AnimationType.ALPHA)
            addType<DeviceNameTag>(R.layout.wifi_info)
            onBind {
                findView<TextView>(R.id.name).text = getModel<DeviceNameTag>().name
                findView<TextView>(R.id.mac).text = getModel<DeviceNameTag>().mac
            }
            onClick(R.id.itemView) {
                val wifiP2pDevice = mListDevice[modelPosition]
                connect(wifiP2pDevice)
            }
        }.models = mListDeviceName
    }

}