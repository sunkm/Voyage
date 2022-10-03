package com.manchuan.tools.view

interface ITimer {
    fun setTotalTime(time: Long)
    fun start()
    fun pause()
    fun resume()
    fun reset()
}