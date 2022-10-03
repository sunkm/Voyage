package com.manchuan.tools.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import com.manchuan.tools.interfaces.Wifip2pActionListener


/**
 * date：2018/2/24 on 11:10
 * description: 客户端监听连接服务端信息的变化，以回调的形式把信息传递给发送文件界面
 */
class Wifip2pReceiver(
    private val mWifiP2pManager: WifiP2pManager, private val mChannel: WifiP2pManager.Channel,
    private val mListener: Wifip2pActionListener,
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.e(TAG, "接收到广播： " + intent.action)
        val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
        when (intent.action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                mListener.wifiP2pEnabled(true)
            } else {
                mListener.wifiP2pEnabled(false)
            }
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> mWifiP2pManager.requestPeers(mChannel
            ) { peers -> mListener.onPeersInfo(peers.deviceList) }
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                val networkInfo =
                    intent.getParcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)
                if (networkInfo!!.isConnected) {
                    mWifiP2pManager.requestConnectionInfo(mChannel
                    ) { info -> mListener.onConnection(info) }
                } else {
                    mListener.onDisconnection()
                }
            }
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                val device =
                    intent.getParcelableExtra<WifiP2pDevice>(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)
                mListener.onDeviceInfo(device)
            }
            else -> {}
        }
    }

    companion object {
        const val TAG = "Wifip2pReceiver"
    }
}
