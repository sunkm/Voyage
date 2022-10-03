package com.manchuan.tools.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.manchuan.tools.utils.RandomColorBalance.getActionColor
import com.manchuan.tools.utils.RandomColorBalance.getColorRGB

class SecondSketchFilter : ImageFilerName() {
    var value = 2
    fun getSimpleSketch(paramBitmap: Bitmap): Bitmap {
        var paramBitmap = paramBitmap
        System.currentTimeMillis()
        val i3 = paramBitmap.width
        val i4 = paramBitmap.height
        var k = i3 * i4
        val arrayOfInt2 = IntArray(k)
        paramBitmap.getPixels(arrayOfInt2, 0, i3, 0, 0, i3, i4)
        var i = 0
        var j: Int
        var m: Int
        var n: Int
        while (i < i4) {
            j = 0
            while (j < i3) {
                m = i * i3 + j
                n = 255 - (Color.red(arrayOfInt2[m]) * 28 + Color.green(
                    arrayOfInt2[m]
                ) * 151 + Color.blue(arrayOfInt2[m]) * 77 shr 8)
                arrayOfInt2[m] = Color.rgb(n, n, n)
                j += 1
            }
            i += 1
        }
        val arrayOfInt1 = IntArray(k)
        val localMinBlurValue = MinBlurValue()
        localMinBlurValue.minBlurVal = value
        System.currentTimeMillis()
        k = 0
        i = k
        while (k < i4) {
            m = 0
            while (m < i3) {
                n = -1
                var i1: Int
                j = -1
                while (n <= 1) {
                    val i5 = k + n
                    i1 = j
                    if (i5 >= 0) {
                        i1 = j
                        if (i5 < i4) {
                            i1 = -localMinBlurValue.minBlurVal
                            while (true) {
                                var i2 = localMinBlurValue.minBlurVal
                                if (i1 > 0) {
                                    i1 = j
                                    break
                                }
                                val i6 = m + i1
                                i2 = j
                                if (i6 >= 0) {
                                    i2 = j
                                    if (i6 < i3) {
                                        i2 = getActionColor(j, arrayOfInt2[i6 + i5 * i3])
                                    }
                                }
                                i1 += 1
                                j = i2
                            }
                        }
                    }
                    n += 1
                    j = i1
                }
                arrayOfInt1[i] = j
                m += 1
                i += 1
            }
            k += 1
        }
        paramBitmap.getPixels(arrayOfInt2, 0, i3, 0, 0, i3, i4)
        simpleRGB(arrayOfInt2, i3, i4)
        getColorRGB(arrayOfInt2, arrayOfInt1, i3, i4)
        paramBitmap = Bitmap.createBitmap(i3, i4, Bitmap.Config.ARGB_8888)
        paramBitmap.setPixels(arrayOfInt1, 0, i3, 0, 0, i3, i4)
        System.gc()
        return paramBitmap
    }

    fun getSimpleSketchValue(paramInt: Int) {
        value = paramInt
    }

    companion object {
        fun simpleRGB(paramArrayOfInt: IntArray, paramInt1: Int, paramInt2: Int) {
            var i = 0
            while (i < paramInt2) {
                var j = 0
                while (j < paramInt1) {
                    val k = i * paramInt1 + j
                    val m = Color.red(paramArrayOfInt[k]) * 28 + Color.green(
                        paramArrayOfInt[k]
                    ) * 151 + Color.blue(paramArrayOfInt[k]) * 77 shr 8
                    paramArrayOfInt[k] = Color.rgb(m, m, m)
                    j += 1
                }
                i += 1
            }
        }
    }
}