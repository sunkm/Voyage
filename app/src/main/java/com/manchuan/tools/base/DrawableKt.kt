@file:JvmName("DrawableKt")

package com.manchuan.tools.base

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.content.ContextCompat

@SuppressLint("DiscouragedApi")
fun Context.getSystemColor(resName: String): Int {
    val id = resources.getIdentifier(resName, "color", "android")
    return ContextCompat.getColor(this, id)
}
