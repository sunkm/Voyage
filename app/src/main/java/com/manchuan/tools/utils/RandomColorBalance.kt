package com.manchuan.tools.utils

import android.graphics.Color
import java.util.*

object RandomColorBalance {
    init {
        Random()
    }

    @JvmStatic
    fun getActionColor(paramInt1: Int, paramInt2: Int): Int {
        var paramInt1 = paramInt1
        var paramInt2 = paramInt2
        val i2 = paramInt1 shr 24 and 0xFF
        val i5 = paramInt2 shr 16 and 0xFF
        val i4 = paramInt2 shr 8 and 0xFF
        val i3 = paramInt2 and 0xFF
        val i1 = (paramInt1 shr 16 and 0xFF).coerceAtMost(i5)
        val m = (paramInt1 shr 8 and 0xFF).coerceAtMost(i4)
        val n = (paramInt1 and 0xFF).coerceAtMost(i3)
        var k = i2
        var j = m
        var i = n
        paramInt1 = i1
        if (i2 != 255) {
            k = i2 * 255 / 255
            paramInt2 = (255 - k) * (paramInt2 shr 24 and 0xFF) / 255
            paramInt1 = getActionMask((i1 * k + i5 * paramInt2) / 255)
            j = getActionMask((m * k + i4 * paramInt2) / 255)
            i = getActionMask((n * k + i3 * paramInt2) / 255)
            k = getActionMask(k + paramInt2)
        }
        return paramInt1 shl 16 or (k shl 24) or (j shl 8) or i
    }

    private fun getActionMask(paramInt: Int): Int {
        if (paramInt < 0) {
            return 0
        }
        return if (paramInt > 255) {
            255
        } else paramInt
    }

    @JvmStatic
    fun getColorRGB(
        paramArrayOfInt1: IntArray,
        paramArrayOfInt2: IntArray,
        paramInt1: Int,
        paramInt2: Int
    ) {
        var i = 0
        while (i < paramInt1) {
            var j = 0
            while (j < paramInt2) {
                val k = j * paramInt1 + i
                var i2 = paramArrayOfInt1[k]
                var i1 = paramArrayOfInt2[k]
                val m = Color.red(i2)
                val n = Color.green(i2)
                i2 = Color.blue(i2)
                val i3 = Color.red(i1)
                val i4 = Color.green(i1)
                i1 = Color.blue(i1)
                paramArrayOfInt2[k] = Color.argb(
                    255,
                    getRandColorInt(m, i3),
                    getRandColorInt(n, i4),
                    getRandColorInt(i2, i1)
                )
                j += 1
            }
            i += 1
        }
    }

    private fun getRandColorInt(paramInt1: Int, paramInt2: Int): Int {
        var paramInt1 = paramInt1
        paramInt1 += paramInt1 * paramInt2 / (256 - paramInt2)
        return if (paramInt1 > 255) {
            255
        } else paramInt1
    }
}