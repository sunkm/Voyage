package com.luoye.fpic.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

/**
 * Created by zyw on 2017/8/17.
 */
object ConvertUtils {
    /**
     * 核心，文本转成图片
     * @param bitmap 原图片
     * @param text 文本
     * @param fontSize 文字大小
     * @return
     */
    fun getTextBitmap(bitmap: Bitmap?, backColor: Int, text: String, fontSize: Int): Bitmap {
        requireNotNull(bitmap) { "Bitmap cannot be null." }
        val picWidth = bitmap.width
        val picHeight = bitmap.height
        val back = Bitmap.createBitmap(
            if (bitmap.width % fontSize == 0) bitmap.width else bitmap.width / fontSize * fontSize,
            if (bitmap.height % fontSize == 0) bitmap.height else bitmap.height / fontSize * fontSize,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(back)
        canvas.drawColor(backColor)
        var idx = 0
        var y = 0
        while (y < picHeight) {
            var x = 0
            while (x < picWidth) {
                val colors = getPixels(bitmap, x, y, fontSize, fontSize)
                val paint = Paint()
                paint.isAntiAlias = true
                paint.color = getAverage(colors)
                paint.textSize = fontSize.toFloat()
                val fontMetrics = paint.fontMetrics
                val padding =
                    if (y == 0) fontSize + fontMetrics.ascent else (fontSize + fontMetrics.ascent) * 2
                canvas.drawText(text[idx++].toString(), x.toFloat(), y - padding, paint)
                if (idx == text.length) {
                    idx = 0
                }
                x += fontSize
            }
            y += fontSize
        }
        return back
    }

    /**
     * 转成像素图片
     * @param bitmap 原图片
     * @param blockSize 块大小
     * @return
     */
    fun getBlockBitmap(bitmap: Bitmap?, blockSize: Int): Bitmap {
        requireNotNull(bitmap) { "Bitmap cannot be null." }
        val picWidth = bitmap.width
        val picHeight = bitmap.height
        val back = Bitmap.createBitmap(
            if (bitmap.width % blockSize == 0) bitmap.width else bitmap.width / blockSize * blockSize,
            if (bitmap.height % blockSize == 0) bitmap.height else bitmap.height / blockSize * blockSize,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(back)
        canvas.drawColor(0xfff)
        var y = 0
        while (y < picHeight) {
            var x = 0
            while (x < picWidth) {
                val colors = getPixels(bitmap, x, y, blockSize, blockSize)
                val paint = Paint()
                paint.isAntiAlias = true
                paint.color = getAverage(colors)
                paint.style = Paint.Style.FILL
                val left = x
                val top = y
                val right = x + blockSize
                val bottom = y + blockSize
                canvas.drawRect(
                    left.toFloat(),
                    top.toFloat(),
                    right.toFloat(),
                    bottom.toFloat(),
                    paint
                )
                x += blockSize
            }
            y += blockSize
        }
        return back
    }

    /**
     * 获取某一块的所有像素的颜色
     * @param bitmap
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     */
    private fun getPixels(bitmap: Bitmap, x: Int, y: Int, w: Int, h: Int): IntArray {
        val colors = IntArray(w * h)
        var idx = 0
        var i = y
        while (i < h + y && i < bitmap.height) {
            var j = x
            while (j < w + x && j < bitmap.width) {
                val color = bitmap.getPixel(j, i)
                colors[idx++] = color
                j++
            }
            i++
        }
        return colors
    }

    /**
     * 求取多个颜色的平均值
     * @param colors
     * @return
     */
    private fun getAverage(colors: IntArray): Int {
        //int alpha=0;
        var red = 0
        var green = 0
        var blue = 0
        for (color in colors) {
            red += color and 0xff0000 shr 16
            green += color and 0xff00 shr 8
            blue += color and 0x0000ff
        }
        val len = colors.size.toFloat()
        //alpha=Math.round(alpha/len);
        red = Math.round(red / len)
        green = Math.round(green / len)
        blue = Math.round(blue / len)
        return Color.argb(0xff, red, green, blue)
    }

    private fun log(log: String) {
        println("-------->Utils:$log")
    }
}