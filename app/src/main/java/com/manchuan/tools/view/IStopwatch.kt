package com.manchuan.tools.view

import com.manchuan.tools.view.IStopwatch.StopwatchListener

interface IStopwatch {
    fun setElapsedTime(millis: Long)
    fun start()
    fun pause()
    fun resume()
    fun reset()
    fun record()
    val stopwatchTime: Long

    interface StopwatchListener {
        fun onRecord(recordTime: Long)
        fun onUpdate(stopwatchTime: Long)
    }

    fun setStopwatchWatcher(listener: StopwatchListener?)
}