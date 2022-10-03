package com.manchuan.tools.interfaces

import android.content.Context
import android.net.TrafficStats
import com.drake.interval.Interval
import com.drake.net.utils.scopeNet
import com.dylanc.longan.dp
import com.dylanc.longan.roundCorners
import com.dylanc.longan.toast
import com.lzf.easyfloat.EasyFloat
import com.lzf.easyfloat.anim.DefaultAnimator
import com.lzf.easyfloat.enums.ShowPattern
import com.manchuan.tools.R
import com.manchuan.tools.databinding.HardwareFloatBinding
import com.manchuan.tools.extensions.loge
import com.manchuan.tools.utils.BatteryUtils
import com.manchuan.tools.utils.HardwareInfoUtils
import com.manchuan.tools.utils.SystemProperties
import org.koin.core.component.KoinComponent
import java.text.NumberFormat
import java.util.concurrent.TimeUnit

interface HardwareFloatInterface {

    fun load(context: Context)

    fun loadNetworkFloat(context: Context)

}

class HardwarePresenter(private val repo: HardwareFloatInterface) {

    fun loadContext(context: Context) = repo.load(context)
    fun loadNetworkFloat(context: Context) = repo.loadNetworkFloat(context)
}

class HardwareFloat : HardwareFloatInterface, KoinComponent {

    override fun load(context: Context) =
        EasyFloat.with(context).setLayout(R.layout.hardware_float) { view ->
            val binding = HardwareFloatBinding.bind(view)
            view.roundCorners = 18.dp
            Interval(2, TimeUnit.SECONDS).subscribe {
                scopeNet {
                    val stringBuilder = StringBuilder()
                    stringBuilder.append("CPU使用率:${HardwareInfoUtils.getCPUUsage()}%")
                    stringBuilder.append(
                        "\n当前可用运行内存:${
                            HardwareInfoUtils.getAvailMemory(context).toFloat() / 1000.0
                        }GB"
                    )
                    stringBuilder.append("\nCPU平台:${SystemProperties.getProperty("ro.board.platform")}")
                    stringBuilder.append("\n电池容量:${BatteryUtils.getBatteryCapacity(context)}mAh")
                    binding.info.text = stringBuilder
                }.catch {
                    context.toast(it.toString())
                }
            }.start()
        }
            .setShowPattern(ShowPattern.ALL_TIME)
            .setAnimator(DefaultAnimator())
            .setDragEnable(true)
            .hasEditText(false)
            .show()

    override fun loadNetworkFloat(context: Context) =
        EasyFloat.with(context).setLayout(R.layout.hardware_float) { view ->
            val binding = HardwareFloatBinding.bind(view)
            view.roundCorners = 18.dp
            mLastUP = TrafficStats.getTotalTxBytes();
            mLastDOWN = TrafficStats.getTotalRxBytes();
            Interval(1, TimeUnit.SECONDS).subscribe {
                scopeNet {
                    getRate()
                    val stringBuilder = StringBuilder()
                    stringBuilder.append("上行速度:${lUP}/s")
                    stringBuilder.append("\n下行速度:$lDOWN/s")
                    binding.info.text = stringBuilder
                    mLastUP = TrafficStats.getTotalTxBytes();
                    mLastDOWN = TrafficStats.getTotalRxBytes();
                }.catch {
                    loge(it.toString())
                    context.toast(it.toString())
                }
            }.start()
        }
            .setShowPattern(ShowPattern.ALL_TIME)
            .setAnimator(DefaultAnimator())
            .setDragEnable(true)
            .hasEditText(false)
            .show()

    private var mCurrentUP: Long = 0 //当前手机的上行流量

    private var mCurrentDOWN: Long = 0 //当前手机的下行流量

    private var mLastUP: Long = 0 //上次手机的上行流量

    private var mLastDOWN: Long = 0 //上次手机的下行流量


    // 进行换算过后的上传下载速率
    private var lUP = ""
    private var lDOWN = ""

    private fun getRate() {
        mCurrentUP = TrafficStats.getTotalTxBytes() - mLastUP
        mCurrentDOWN = TrafficStats.getTotalRxBytes() - mLastDOWN
        // 对上传速率进行换算
        if (mCurrentUP >= 1000000) {
            // 字节换算成M,设置精确到小数点后1位
            val numberFormat: NumberFormat = NumberFormat.getInstance()
            numberFormat.maximumFractionDigits = 1
            lUP = numberFormat.format(mCurrentUP.toFloat() / 1000000) + " MB"
        } else if (mCurrentUP >= 1000) {
            // 字节换算成K,设置精确到小数点后1位
            val numberFormat: NumberFormat = NumberFormat.getInstance()
            numberFormat.maximumFractionDigits = 1
            lUP = numberFormat.format(mCurrentUP.toFloat() / 1000) + " KB"
        } else {
            // 直接显示字节
            lUP = "$mCurrentUP B"
        }

        // 对下载速率进行换算
        if (mCurrentDOWN >= 1000000) {
            // 字节换算成M,设置精确到小数点后1位
            val numberFormat: NumberFormat = NumberFormat.getInstance()
            numberFormat.maximumFractionDigits = 1
            lDOWN = numberFormat.format(mCurrentDOWN.toFloat() / 1000000) + " MB"
        } else if (mCurrentDOWN >= 1000) {
            // 字节换算成K,设置精确到小数点后1位
            val numberFormat: NumberFormat = NumberFormat.getInstance()
            numberFormat.maximumFractionDigits = 1
            lDOWN = numberFormat.format(mCurrentDOWN.toFloat() / 1000) + " KB"
        } else {
            // 直接显示字节
            lDOWN = "$mCurrentDOWN B"
        }
        //当前上行流量，用来存储显示
        val now_up: String = lUP
        //当前下行流量，存储显示
        val now_down: String = lDOWN
    }
}