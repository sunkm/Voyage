package com.manchuan.tools.utils

import android.net.TrafficStats
import java.math.BigDecimal

object GetUpAndDownloadSpeed {
    private var mCurrentTotalUp: Long = 0       //当前手机总的上行流量
    private var mCurrentTotalDown: Long = 0     //当前手机总的下行流量
    private var mLastTotalUp: Long = 0          //上次手机总的上行流量
    private var mLastTotalDown: Long = 0        //上次手机总的下行流量
    private var lastTimeStampTotalUp: Long = 0
    private var lastTimeStampTotalDown: Long = 0

    private var mCurrentUp: Long = 0            //当前手机指定进程的上行流量
    private var mCurrentDown: Long = 0         //当前手机指定进程的下行流量
    private var mLastUp: Long = 0               //上次手机指定进程的上行流量
    private var mLastDown: Long = 0             //上次手机指定进程的下行流量
    private var lastTimeStampUp: Long = 0       //上行时间戳
    private var lastTimeStampDown: Long = 0     //下行时间戳

    //换算后的上下行速度
    private var totalUpSpeed: Float = 0.00F
    private var totalDownSpeed: Float = 0.00F

    private var upSpeed: Float = 0.00F
    private var downSpeed: Float = 0.00F

    private var defaultPointAmount = 2          //保留小数的位数，默认为2位

    //获取总的上行速度
    fun getTotalUpSpeed(): Float {                    //refreshTime : 刷新时间
        val currentTotalTxBytes = TrafficStats.getTotalTxBytes()
        val nowTimeStampTotalUp = System.currentTimeMillis()
        mCurrentTotalUp = currentTotalTxBytes - mLastTotalUp

        //计算上传速度
        totalUpSpeed =
            BigDecimal((mCurrentTotalUp / 1024) / ((nowTimeStampTotalUp - lastTimeStampTotalUp) * 1.0)).setScale(
                defaultPointAmount,
                BigDecimal.ROUND_HALF_UP
            ).toFloat() //单位： KB/s

        //保存当前的流量总和和上次的时间戳
        mLastTotalUp = currentTotalTxBytes
        lastTimeStampTotalUp = nowTimeStampTotalUp

        return totalUpSpeed
    }

    //获取总的下行速度
    fun getTotalDownloadSpeed(): Float {
        val currentTotalRxBytes = TrafficStats.getTotalRxBytes()
        val nowTimeStampTotalDown = System.currentTimeMillis()
        mCurrentTotalDown = currentTotalRxBytes - mLastTotalDown

        //计算下行速度
        totalDownSpeed =
            BigDecimal((mCurrentTotalDown / 1024) / ((nowTimeStampTotalDown - lastTimeStampTotalDown) * 1.0)).setScale(
                defaultPointAmount,
                BigDecimal.ROUND_HALF_UP
            ).toFloat() //单位： KB/s

        //保存当前的流量总和和上次的时间戳
        mLastTotalDown = currentTotalRxBytes
        lastTimeStampTotalDown = nowTimeStampTotalDown

        return totalDownSpeed
    }

    //获取指定进程的上行速度
    fun getUpSpeedByUid(uid: Int): Float {
        val currentTxBytes = TrafficStats.getUidTxBytes(uid)
        val nowTimeStampUp = System.currentTimeMillis()
        mCurrentUp = currentTxBytes - mLastUp

        //计算上传速度
        upSpeed =
            BigDecimal((mCurrentUp / 1024) * 1000 / ((nowTimeStampUp - lastTimeStampUp) * 1.0)).setScale(
                defaultPointAmount,
                BigDecimal.ROUND_HALF_UP
            ).toFloat() //单位： KB/s
        //保存当前的流量总和和上次的时间戳
        mLastUp = currentTxBytes
        lastTimeStampUp = nowTimeStampUp

        return upSpeed
    }

    //获取总的下行速度
    fun getDownloadSpeedByUid(uid: Int): Float {
        val currentRxBytes = TrafficStats.getUidRxBytes(uid)
        val nowTimeStampDown = System.currentTimeMillis()
        mCurrentDown = currentRxBytes - mLastDown

        //计算下行速度
        //downSpeed = numberFormat.format((mCurrentDown/1024)*1000  /  ((nowTimeStampDown-lastTimeStampDown).toFloat()) )   //单元为 KB/s
        downSpeed =
            BigDecimal((mCurrentDown / 1024) * 1000 / ((nowTimeStampDown - lastTimeStampDown) * 1.0)).setScale(
                defaultPointAmount,
                BigDecimal.ROUND_HALF_UP
            ).toFloat() //单位： KB/s

        //保存当前的流量总和和上次的时间戳
        mLastDown = currentRxBytes
        lastTimeStampDown = nowTimeStampDown

        return downSpeed
    }
}

