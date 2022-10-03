package com.manchuan.tools.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration

@SuppressLint("StaticFieldLeak")
object UiModeUtils {
    @SuppressLint("StaticFieldLeak")
    private var mContext: Context? = null
    @JvmStatic
    fun initialize(context: Context?) {
        mContext = context
    }

    val isDarkMode: Boolean
        get() {
            var isDark = false
            when (mContext!!.resources.configuration.uiMode) {
                Configuration.UI_MODE_NIGHT_NO -> isDark = false
                Configuration.UI_MODE_NIGHT_YES -> isDark = true
            }
            return isDark
        }
}