package com.manchuan.tools.activity.transfer

import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.view.View
import android.view.Window
import com.dylanc.longan.roundCorners
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.manchuan.tools.interfaces.Wifip2pActionListener
import com.manchuan.tools.service.Wifip2pReceiver
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import rikka.material.app.MaterialActivity
import timber.log.Timber


open class BaseActivity : MaterialActivity(), Wifip2pActionListener {
    var mWifiP2pManager: WifiP2pManager? = null
    var mChannel: WifiP2pManager.Channel? = null
    var mWifip2pReceiver: Wifip2pReceiver? = null
    var mWifiP2pInfo: WifiP2pInfo? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        val root = findViewById<View>(R.id.content)
        root.transitionName = "root"
        root.roundCorners = 24F
        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        window.sharedElementEnterTransition = buildContainerTransform(true)
        window.sharedElementReturnTransition = buildContainerTransform(false)
        window.allowEnterTransitionOverlap = true
        window.sharedElementsUseOverlay = false
        super.onCreate(savedInstanceState)
        //注册WifiP2pManager
        mWifiP2pManager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager?
        mChannel = mWifiP2pManager?.initialize(this, mainLooper, this)
        //注册广播
        mWifip2pReceiver = mWifiP2pManager?.let { Wifip2pReceiver(it, mChannel!!, this) }
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        registerReceiver(mWifip2pReceiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        //注销广播
        unregisterReceiver(mWifip2pReceiver)
        mWifip2pReceiver = null
    }


    private fun buildContainerTransform(entering: Boolean): MaterialContainerTransform {
        val transform = MaterialContainerTransform(this, entering)
        transform.setAllContainerColors(
            MaterialColors.getColor(findViewById(R.id.content), com.google.android.material.R.attr.colorSurface)
        )
        transform.addTarget(R.id.content)
        transform.duration = 700
        return transform
    }

    override fun wifiP2pEnabled(enabled: Boolean) {
        Timber.tag(TAG).e("传输通道是否可用：%s", enabled)
    }

    override fun onConnection(wifiP2pInfo: WifiP2pInfo?) {
        if (wifiP2pInfo != null) {
            mWifiP2pInfo = wifiP2pInfo
            Timber.tag(TAG).e("WifiP2pInfo:%s", wifiP2pInfo)
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onDisconnection() {
        Timber.tag(TAG).e("连接断开")
    }

    override fun onDeviceInfo(wifiP2pDevice: WifiP2pDevice?) {

    }

    @SuppressLint("BinaryOperationInTimber")
    override fun onPeersInfo(wifiP2pDeviceList: Collection<WifiP2pDevice?>?) {
        if (wifiP2pDeviceList != null) {
            for (device in wifiP2pDeviceList) {
                if (device != null) {
                    Timber.tag(TAG)
                        .e("%s%s", "连接的设备信息：" + device.deviceName.toString() + "--------", device.deviceAddress)
                }
            }
        }
    }

    /**

    fun onDeviceInfo(wifiP2pDevice: WifiP2pDevice) {
    Log.e(TAG, "当前的的设备名称" + wifiP2pDevice.deviceName)
    }
    fun onPeersInfo(wifiP2pDeviceList: Collection<WifiP2pDevice?>) {
    for (device in wifiP2pDeviceList) {
    Log.e(TAG,
    "连接的设备信息：" + device.deviceName.toString() + "--------" + device.deviceAddress)
    }
    }
     **/

    override fun onChannelDisconnected() {}

    companion object {
        private const val TAG = "BaseActivity"
    }
}
