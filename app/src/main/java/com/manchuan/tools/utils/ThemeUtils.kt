package com.manchuan.tools.utils

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object ThemeUtils {
    var mContext: Context? = null
    fun init(context: Context?) {
        mContext = context
    }
}