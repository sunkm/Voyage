/*
 * Copyright (C) 2013 4th Line GmbH, Switzerland
 *
 * The contents of this file are subject to the terms of either the GNU
 * Lesser General Public License Version 2 or later ("LGPL") or the
 * Common Development and Distribution License Version 1 or later
 * ("CDDL") (collectively, the "License"). You may not use this file
 * except in compliance with the License. See LICENSE.txt for more
 * information.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package org.fourthline.cling.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.MulticastLock
import android.net.wifi.WifiManager.WifiLock
import android.os.Parcelable
import org.fourthline.cling.UpnpServiceConfiguration
import org.fourthline.cling.model.ModelUtil
import org.fourthline.cling.protocol.ProtocolFactory
import org.fourthline.cling.transport.Router
import org.fourthline.cling.transport.RouterException
import org.fourthline.cling.transport.RouterImpl
import org.seamless.util.Exceptions
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Monitors all network connectivity changes, switching the router accordingly.
 *
 * @author Michael Pujos
 * @author Christian Bauer
 */
open class AndroidRouter(
    configuration: UpnpServiceConfiguration?,
    protocolFactory: ProtocolFactory?,
    private val context: Context
) : RouterImpl(configuration, protocolFactory) {
    private val wifiManager: WifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private var multicastLock: MulticastLock? = null
    protected var wifiLock: WifiLock? = null
    var networkInfo: NetworkInfo?
        protected set
    protected var broadcastReceiver: BroadcastReceiver? = null

    init {
        networkInfo = NetworkUtils.getConnectedNetworkInfo(context)

        // Only register for network connectivity changes if we are not running on emulator
        if (!ModelUtil.ANDROID_EMULATOR) {
            broadcastReceiver = createConnectivityBroadcastReceiver()
            context.registerReceiver(broadcastReceiver,
                IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"))
        }
    }

    private fun createConnectivityBroadcastReceiver(): BroadcastReceiver {
        return ConnectivityBroadcastReceiver()
    }

    override fun getLockTimeoutMillis(): Int {
        return 15000
    }

    @Throws(RouterException::class)
    override fun shutdown() {
        super.shutdown()
        unregisterBroadcastReceiver()
    }

    @Throws(RouterException::class)
    override fun enable(): Boolean {
        lock(writeLock)
        return try {
            var enabled: Boolean
            if (super.enable().also { enabled = it }) {
                // Enable multicast on the WiFi network interface,
                // requires android.permission.CHANGE_WIFI_MULTICAST_STATE
                if (isWifi) {
                    setWiFiMulticastLock(true)
                    setWifiLock(true)
                }
            }
            enabled
        } finally {
            unlock(writeLock)
        }
    }

    @Throws(RouterException::class)
    override fun disable(): Boolean {
        lock(writeLock)
        return try {
            // Disable multicast on WiFi network interface,
            // requires android.permission.CHANGE_WIFI_MULTICAST_STATE
            if (isWifi) {
                setWiFiMulticastLock(false)
                setWifiLock(false)
            }
            super.disable()
        } finally {
            unlock(writeLock)
        }
    }

    val isMobile: Boolean
        get() = NetworkUtils.isMobile(networkInfo)
    val isWifi: Boolean
        get() = NetworkUtils.isWifi(networkInfo)
    val isEthernet: Boolean
        get() = NetworkUtils.isEthernet(networkInfo)

    fun enableWiFi(): Boolean {
        log.info("Enabling WiFi...")
        return try {
            wifiManager.setWifiEnabled(true)
        } catch (t: Throwable) {
            // workaround (HTC One X, 4.0.3)
            //java.lang.SecurityException: Permission Denial: writing com.android.providers.settings.SettingsProvider
            // uri content://settings/system from pid=4691, uid=10226 requires android.permission.WRITE_SETTINGS
            //	at android.os.Parcel.readException(Parcel.java:1332)
            //	at android.os.Parcel.readException(Parcel.java:1286)
            //	at android.net.wifi.IWifiManager$Stub$Proxy.setWifiEnabled(IWifiManager.java:1115)
            //	at android.net.wifi.WifiManager.setWifiEnabled(WifiManager.java:946)
            log.log(Level.WARNING, "SetWifiEnabled failed", t)
            false
        }
    }

    fun unregisterBroadcastReceiver() {
        if (broadcastReceiver != null) {
            context.unregisterReceiver(broadcastReceiver)
            broadcastReceiver = null
        }
    }

    fun setWiFiMulticastLock(enable: Boolean) {
        if (multicastLock == null) {
            multicastLock = wifiManager.createMulticastLock(javaClass.simpleName)
        }
        if (enable) {
            if (multicastLock!!.isHeld) {
                log.warning("WiFi multicast lock already acquired")
            } else {
                log.info("WiFi multicast lock acquired")
                multicastLock!!.acquire()
            }
        } else {
            if (multicastLock!!.isHeld) {
                log.info("WiFi multicast lock released")
                multicastLock!!.release()
            } else {
                log.warning("WiFi multicast lock already released")
            }
        }
    }

    protected fun setWifiLock(enable: Boolean) {
        if (wifiLock == null) {
            wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF,
                javaClass.simpleName)
        }
        if (enable) {
            if (wifiLock!!.isHeld) {
                log.warning("WiFi lock already acquired")
            } else {
                log.info("WiFi lock acquired")
                wifiLock!!.acquire()
            }
        } else {
            if (wifiLock!!.isHeld) {
                log.info("WiFi lock released")
                wifiLock!!.release()
            } else {
                log.warning("WiFi lock already released")
            }
        }
    }

    /**
     * Can be overriden by subclasses to do additional work.
     *
     * @param oldNetwork `null` when first called by constructor.
     */
    @Throws(RouterException::class)
    protected fun onNetworkTypeChange(oldNetwork: NetworkInfo?, newNetwork: NetworkInfo?) {
        log.info(String.format("Network type changed %s => %s",
            if (oldNetwork == null) "" else oldNetwork.typeName,
            if (newNetwork == null) "NONE" else newNetwork.typeName))
        if (disable()) {
            log.info(String.format(
                "Disabled router on network type change (old network: %s)",
                if (oldNetwork == null) "NONE" else oldNetwork.typeName
            ))
        }
        networkInfo = newNetwork
        if (enable()) {
            // Can return false (via earlier InitializationException thrown by NetworkAddressFactory) if
            // no bindable network address found!
            log.info(String.format(
                "Enabled router on network type change (new network: %s)",
                if (newNetwork == null) "NONE" else newNetwork.typeName
            ))
        }
    }

    /**
     * Handles errors when network has been switched, during reception of
     * network switch broadcast. Logs a warning by default, override to
     * change this behavior.
     */
    protected fun handleRouterExceptionOnNetworkTypeChange(ex: RouterException) {
        val cause = Exceptions.unwrap(ex)
        if (cause is InterruptedException) {
            log.log(Level.INFO, "Router was interrupted: $ex", cause)
        } else {
            log.log(Level.WARNING, "Router error on network change: $ex", ex)
        }
    }

    internal inner class ConnectivityBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != ConnectivityManager.CONNECTIVITY_ACTION) return
            displayIntentInfo(intent)
            var newNetworkInfo = NetworkUtils.getConnectedNetworkInfo(context)

            // When Android switches WiFI => MOBILE, sometimes we may have a short transition
            // with no network: WIFI => NONE, NONE => MOBILE
            // The code below attempts to make it look like a single WIFI => MOBILE
            // transition, retrying up to 3 times getting the current network.
            //
            // Note: this can block the UI thread for up to 3s
            if (networkInfo != null && newNetworkInfo == null) {
                for (i in 1..3) {
                    try {
                        Thread.sleep(1000)
                    } catch (e: InterruptedException) {
                        return
                    }
                    log.warning(String.format(
                        "%s => NONE network transition, waiting for new network... retry #%d",
                        networkInfo!!.typeName, i
                    ))
                    newNetworkInfo = NetworkUtils.getConnectedNetworkInfo(context)
                    if (newNetworkInfo != null) break
                }
            }
            if (isSameNetworkType(networkInfo, newNetworkInfo)) {
                log.info("No actual network change... ignoring event!")
            } else {
                try {
                    onNetworkTypeChange(networkInfo, newNetworkInfo)
                } catch (ex: RouterException) {
                    handleRouterExceptionOnNetworkTypeChange(ex)
                }
            }
        }

        fun isSameNetworkType(network1: NetworkInfo?, network2: NetworkInfo?): Boolean {
            if (network1 == null && network2 == null) return true
            return if (network1 == null || network2 == null) false else network1.type == network2.type
        }

        fun displayIntentInfo(intent: Intent) {
            val noConnectivity =
                intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)
            val reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON)
            val isFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false)
            val currentNetworkInfo =
                intent.getParcelableExtra<Parcelable>(ConnectivityManager.EXTRA_NETWORK_INFO) as NetworkInfo?
            val otherNetworkInfo =
                intent.getParcelableExtra<Parcelable>(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO) as NetworkInfo?
            log.info("Connectivity change detected...")
            log.info("EXTRA_NO_CONNECTIVITY: $noConnectivity")
            log.info("EXTRA_REASON: $reason")
            log.info("EXTRA_IS_FAILOVER: $isFailover")
            log.info("EXTRA_NETWORK_INFO: " + (currentNetworkInfo ?: "none"))
            log.info("EXTRA_OTHER_NETWORK_INFO: " + (otherNetworkInfo ?: "none"))
            log.info("EXTRA_EXTRA_INFO: " + intent.getStringExtra(ConnectivityManager.EXTRA_EXTRA_INFO))
        }
    }

    companion object {
        private val log = Logger.getLogger(Router::class.java.name)
    }
}