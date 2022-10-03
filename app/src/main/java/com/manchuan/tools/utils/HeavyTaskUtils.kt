package com.manchuan.tools.utils

import kotlinx.coroutines.Job
import java.util.concurrent.CancellationException

object HeavyTaskUtils {
    private const val TAG = "HeavyTaskUtil"
    private val playPosFixWhiteList = arrayOf("m3u8.htv009.com", "127.0.0.1", ":11111/")

    //这里的代码是拿的AsyncTask的源码，作用是创建合理可用的线程池容量
    @JvmStatic
    fun executeNewTask(command: Runnable?) {
        ThreadTool.executeNewTask(command)
    }

    fun launch(command: Runnable?): Job {
        return ThreadTool.executeNewTask(command)
    }

    fun cancel(job: Job?) {
        try {
            if (job != null && !job.isCancelled) {
                job.cancel(CancellationException())
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}