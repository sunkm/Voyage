package com.manchuan.tools.api

import android.annotation.SuppressLint
import android.content.Context
import com.manchuan.tools.api.Bean

class Bean {
    @JvmName("setContext1")
    fun setContext(context: Context) {
        this.context = context
    }

    var context: Context? = null
        get() = Companion.context

    companion object {
        @SuppressLint("StaticFieldLeak")
        var context: Context? = null
    }
}