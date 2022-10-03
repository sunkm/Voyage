package com.manchuan.tools.extensions

import timber.log.Timber

inline val <reified T> T.TAG: String
    get() = T::class.java.simpleName.let {
        if (it.isBlank()) throw IllegalStateException("tag is empty")
        if (it.length > 23) it.substring(0, 23) else it
    }

inline fun <reified T> T.logv(message: String, throwable: Throwable? = null) =
    logv(TAG, message, throwable)

inline fun <reified T> T.logi(message: String, throwable: Throwable? = null) =
    logi(TAG, message, throwable)

inline fun <reified T> T.logw(message: String, throwable: Throwable? = null) =
    logw(TAG, message, throwable)

inline fun <reified T> T.logd(message: String, throwable: Throwable? = null) =
    logd(TAG, message, throwable)

inline fun <reified T> T.loge(message: Any?, throwable: Any? = null) =
    loge(TAG, message.toString(), throwable as Throwable?)

inline fun <reified T> T.logv(tag: String, message: String, throwable: Throwable? = null) =
    Timber.tag(tag).v(throwable, message)

inline fun <reified T> T.logi(tag: String, message: String, throwable: Throwable? = null) =
    Timber.tag(tag).i(throwable, message)

inline fun <reified T> T.logw(tag: String, message: String, throwable: Throwable? = null) =
    Timber.tag(tag).w(throwable, message)

inline fun <reified T> T.logd(tag: String, message: String, throwable: Throwable? = null) =
    Timber.tag(tag).d(throwable, message)

inline fun <reified T> T.loge(tag: String, message: Any?, throwable: Throwable? = null) =
    Timber.tag(tag).e(throwable, message.toString())