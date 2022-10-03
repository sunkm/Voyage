package com.lxj.androidktx.core

import android.app.Activity
import android.content.Context
import com.blankj.utilcode.util.ToastUtils

/**
 * 短吐司
 */
fun Context.tip(text: String?) {
    if (!text.isNullOrEmpty()) ToastUtils.showShort(text)
}

fun Activity.tip(text: String?) {
    if (!text.isNullOrEmpty()) ToastUtils.showShort(text)
}

fun String.tip() {
    if (this.isNotEmpty()) ToastUtils.showShort(this)
}

/**
 * 长吐司
 */
fun Context.toastLong(text: String?) {
    if (!text.isNullOrEmpty()) ToastUtils.showLong(text)
}