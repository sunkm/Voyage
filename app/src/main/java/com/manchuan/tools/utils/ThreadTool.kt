package com.manchuan.tools.utils

import android.os.Looper
import com.annimon.stream.function.Consumer
import kotlinx.coroutines.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 作者：By 15968
 * 日期：On 2021/10/24
 * 时间：At 20:29
 */
object ThreadTool {

    @OptIn(DelicateCoroutinesApi::class)
    fun executeNewTask(command: Runnable?): Job {
        return GlobalScope.launch(Dispatchers.IO) {
            command?.run()
        }
    }

    fun newScope(): CoroutineScope {
        return CoroutineScope(EmptyCoroutineContext)
    }

    fun cancelScope(scope: CoroutineScope) {
        try {
            scope.cancel()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun launch(scope: CoroutineScope, task: Runnable) {
        try {
            scope.launch(Dispatchers.IO) {
                task.run()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun runOnUI(task: Runnable) {
        GlobalScope.launch(Dispatchers.Main) {
            task.run()
        }
    }

    fun runOnUI(scope: CoroutineScope, task: Runnable) {
        try {
            scope.launch(Dispatchers.Main) {
                task.run()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun getStrOnUIThread(runnable: Consumer<UrlHolder>): String? {
        val urlHolder = UrlHolder()
        if (Thread.currentThread() === Looper.getMainLooper().thread) {
            runnable.accept(urlHolder)
            return urlHolder.url
        }
        val countDownLatch = CountDownLatch(1)
        runOnUI {
            runnable.accept(urlHolder)
            countDownLatch.countDown()
        }
        try {
            countDownLatch.await(5, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return urlHolder.url
    }

    class UrlHolder {
        var url: String? = null
    }
}