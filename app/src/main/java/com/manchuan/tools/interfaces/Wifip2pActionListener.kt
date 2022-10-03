package com.manchuan.tools.interfaces

import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager


/**
 * date：2018/2/24 on 16:41
 * description: 监听广播回调信息
 */
interface Wifip2pActionListener : WifiP2pManager.ChannelListener {
    fun wifiP2pEnabled(enabled: Boolean)
    fun onConnection(wifiP2pInfo: WifiP2pInfo?)
    fun onDisconnection()
    fun onDeviceInfo(wifiP2pDevice: WifiP2pDevice?)
    fun onPeersInfo(wifiP2pDeviceList: Collection<WifiP2pDevice?>?)
}