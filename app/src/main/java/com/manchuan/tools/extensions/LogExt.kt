package com.manchuan.tools.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.mcxiaoke.koi.KoiConfig
import com.mcxiaoke.koi.threadName
import timber.log.Timber


fun Throwable.stackTraceString(): String = Log.getStackTraceString(this)

private fun logMessageWithThreadName(message: String): String
        = "$message [thread:${threadName()}]"

fun Context.logv(message: String) {
    logv(javaClass.simpleName, message)
}

fun Context.logd(message: String) {
    logd(javaClass.simpleName, message)
}

fun Context.logi(message: String) {
    logi(javaClass.simpleName, message)
}

fun Context.logw(message: String) {
    logw(javaClass.simpleName, message)
}

fun Context.loge(message: String) {
    loge(javaClass.simpleName, message)
}

fun Context.logf(message: String) {
    logf(javaClass.simpleName, message)
}

fun logv(tag: String, message: String, exception: Throwable? = null) {
    if (KoiConfig.logLevel <= Log.VERBOSE) {
        Timber.tag(tag).v(exception, logMessageWithThreadName(message))
    }
}

fun logd(tag: String, message: String, exception: Throwable? = null) {
    if (KoiConfig.logLevel <= Log.DEBUG) {
        Timber.tag(tag).d(exception, logMessageWithThreadName(message))
    }
}

fun logi(tag: String, message: String, exception: Throwable? = null) {
    if (KoiConfig.logLevel <= Log.INFO) {
        Timber.tag(tag).i(exception, logMessageWithThreadName(message))
    }
}

fun logw(tag: String, message: String, exception: Throwable? = null) {
    if (KoiConfig.logLevel <= Log.WARN) {
        Timber.tag(tag).w(exception, logMessageWithThreadName(message))
    }
}

fun loge(tag: String, message: String, exception: Throwable? = null) {
    if (KoiConfig.logLevel <= Log.ERROR) {
        Timber.tag(tag).e(exception, logMessageWithThreadName(message))
    }
}

fun logf(tag: String, message: String, exception: Throwable? = null) {
    if (KoiConfig.logLevel <= Log.ERROR) {
        Timber.tag(tag).wtf(exception, logMessageWithThreadName(message))
    }
}

fun logw(tag: String, exception: Throwable?) {
    if (KoiConfig.logLevel <= Log.WARN) {
        Timber.tag(tag).w(exception, logMessageWithThreadName("warn"))
    }
}

fun logf(tag: String, exception: Throwable?) {
    if (KoiConfig.logLevel <= Log.ERROR) {
        Timber.tag(tag).wtf(exception, logMessageWithThreadName("wtf"))
    }
}

inline fun Context.logv(lazyMessage: () -> Any?) {
    logv(javaClass.simpleName, lazyMessage)
}

inline fun Context.logd(lazyMessage: () -> Any?) {
    logd(javaClass.simpleName, lazyMessage)
}

inline fun Context.logi(lazyMessage: () -> Any?) {
    logi(javaClass.simpleName, lazyMessage)
}

inline fun Context.logw(lazyMessage: () -> Any?) {
    logw(javaClass.simpleName, lazyMessage)
}

fun Context.loge(lazyMessage: () -> Any?) {
    loge(javaClass.simpleName, lazyMessage)
}

inline fun Context.logf(lazyMessage: () -> Any?) {
    logf(javaClass.simpleName, lazyMessage)
}

@SuppressLint("ThrowableNotAtBeginning", "BinaryOperationInTimber")
inline fun logv(tag: String, lazyMessage: () -> Any?, exception: Throwable? = null) {
    if (KoiConfig.logLevel <= Log.VERBOSE) {
        Timber.tag(tag).v((lazyMessage()?.toString() ?: "null") + " [T:${threadName()}]", exception)
    }
}

inline fun logd(tag: String, lazyMessage: () -> Any?, exception: Throwable? = null) {
    if (KoiConfig.logLevel <= Log.DEBUG) {
        Log.d(tag, (lazyMessage()?.toString() ?: "null") + " [T:${threadName()}]", exception)
    }
}

inline fun logi(tag: String, lazyMessage: () -> Any?, exception: Throwable? = null) {
    if (KoiConfig.logLevel <= Log.INFO) {
        Log.i(tag, (lazyMessage()?.toString() ?: "null") + " [T:${threadName()}]", exception)
    }
}

inline fun logw(tag: String, lazyMessage: () -> Any?, exception: Throwable? = null) {
    if (KoiConfig.logLevel <= Log.WARN) {
        Log.w(tag, (lazyMessage()?.toString() ?: "null") + " [T:${threadName()}]", exception)
    }
}

inline fun loge(tag: String, lazyMessage: () -> Any?, exception: Throwable? = null) {
    if (KoiConfig.logLevel <= Log.ERROR) {
        Log.e(tag, (lazyMessage()?.toString() ?: "null") + " [T:${threadName()}]", exception)
    }
}

inline fun logf(tag: String, lazyMessage: () -> Any?, exception: Throwable? = null) {
    if (KoiConfig.logLevel <= Log.ERROR) {
        Log.wtf(tag, (lazyMessage()?.toString() ?: "null") + " [T:${threadName()}]", exception)
    }
}