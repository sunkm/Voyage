package com.manchuan.tools.model.hardware

import android.content.Context
import com.manchuan.tools.interfaces.HardwareFloat

fun provideHardwareFloat(context: Context) {
    return HardwareFloat().load(context)
}