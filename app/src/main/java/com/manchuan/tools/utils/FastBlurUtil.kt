package com.manchuan.tools.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.*
import java.net.URL
import kotlin.math.abs

/**
 * @author pedra
 */
object FastBlurUtil {
    /**
     *
     */
    /**
     * 得到本地或者网络上的bitmap url - 网络或者本地图片的绝对路径,比如:
     * A.网络路径: url="http://blog.foreverlove.us/girl2.png" ;
     * B.本地路径:url="file://mnt/sdcard/photo/image.png";
     * C.支持的图片格式 ,png, jpg,bmp,gif等等
     *
     * @param url
     * @return
     */
    private var IO_BUFFER_SIZE = 2 * 1024
    fun getUrlBitmap(url: String?, scaleRatio: Int): Bitmap? {
        var scaleRatio = scaleRatio
        val blurRadius = 8
        if (scaleRatio <= 0) {
            scaleRatio = 10
        }
        var originBitmap: Bitmap? = null
        var `in`: InputStream? = null
        var out: BufferedOutputStream? = null
        return try {
            `in` = BufferedInputStream(URL(url).openStream(), IO_BUFFER_SIZE)
            val dataStream = ByteArrayOutputStream()
            out = BufferedOutputStream(dataStream, IO_BUFFER_SIZE)
            copy(`in`, out)
            out.flush()
            val data = dataStream.toByteArray()
            originBitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            val scaledBitmap = Bitmap.createScaledBitmap(
                originBitmap,
                originBitmap.width / scaleRatio,
                originBitmap.height / scaleRatio,
                false
            )
            doBlur(scaledBitmap, blurRadius, true)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    @Throws(IOException::class)
    private fun copy(`in`: InputStream, out: OutputStream) {
        val b = ByteArray(IO_BUFFER_SIZE)
        var read: Int
        while (`in`.read(b).also { read = it } != -1) {
            out.write(b, 0, read)
        }
    }

    //    把本地图片毛玻璃化
    fun toBlur(originBitmap: Bitmap, scaleRatio: Int): Bitmap? {
        var scaleRatio = scaleRatio
        val blurRadius = 8
        if (scaleRatio <= 0) {
            scaleRatio = 10
        }
        val scaledBitmap = Bitmap.createScaledBitmap(
            originBitmap,
            originBitmap.width / scaleRatio,
            originBitmap.height / scaleRatio,
            false
        )
        return doBlur(scaledBitmap, blurRadius, true)
    }

    private fun doBlur(sentBitmap: Bitmap, radius: Int, canReuseInBitmap: Boolean): Bitmap? {
        val bitmap: Bitmap = if (canReuseInBitmap) {
            sentBitmap
        } else {
            sentBitmap.copy(sentBitmap.config, true)
        }
        if (radius < 1) {
            return null
        }
        val w = bitmap.width
        val h = bitmap.height
        val pix = IntArray(w * h)
        bitmap.getPixels(pix, 0, w, 0, 0, w, h)
        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1
        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)
        var rsum: Int
        var gsum: Int
        var bsum: Int
        var x: Int
        var y: Int
        var i: Int
        var p: Int
        var yp: Int
        var yi: Int
        val vmin = IntArray(w.coerceAtLeast(h))
        var divsum = div + 1 shr 1
        divsum *= divsum
        val dv = IntArray(256 * divsum)
        i = 0
        while (i < 256 * divsum) {
            dv[i] = i / divsum
            i++
        }
        yi = 0
        var yw: Int = yi
        val stack = Array(div) { IntArray(3) }
        var stackpointer: Int
        var stackstart: Int
        var sir: IntArray
        var rbs: Int
        val r1 = radius + 1
        var routsum: Int
        var goutsum: Int
        var boutsum: Int
        var rinsum: Int
        var ginsum: Int
        var binsum: Int
        y = 0
        while (y < h) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            i = -radius
            while (i <= radius) {
                p = pix[yi + wm.coerceAtMost(i.coerceAtLeast(0))]
                sir = stack[i + radius]
                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff
                rbs = r1 - abs(i)
                rsum += sir[0] * rbs
                gsum += sir[1] * rbs
                bsum += sir[2] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                i++
            }
            stackpointer = radius
            x = 0
            while (x < w) {
                r[yi] = dv[rsum]
                g[yi] = dv[gsum]
                b[yi] = dv[bsum]
                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum
                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]
                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]
                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm)
                }
                p = pix[yw + vmin[x]]
                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
                rsum += rinsum
                gsum += ginsum
                bsum += binsum
                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer % div]
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]
                yi++
                x++
            }
            yw += w
            y++
        }
        x = 0
        while (x < w) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            yp = -radius * w
            i = -radius
            while (i <= radius) {
                yi = 0.coerceAtLeast(yp) + x
                sir = stack[i + radius]
                sir[0] = r[yi]
                sir[1] = g[yi]
                sir[2] = b[yi]
                rbs = r1 - abs(i)
                rsum += r[yi] * rbs
                gsum += g[yi] * rbs
                bsum += b[yi] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                if (i < hm) {
                    yp += w
                }
                i++
            }
            yi = x
            stackpointer = radius
            y = 0
            while (y < h) {

                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] =
                    -0x1000000 and pix[yi] or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]
                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum
                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]
                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]
                if (x == 0) {
                    vmin[y] = (y + r1).coerceAtMost(hm) * w
                }
                p = x + vmin[y]
                sir[0] = r[p]
                sir[1] = g[p]
                sir[2] = b[p]
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
                rsum += rinsum
                gsum += ginsum
                bsum += binsum
                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer]
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]
                yi += w
                y++
            }
            x++
        }
        bitmap.setPixels(pix, 0, w, 0, 0, w, h)
        return bitmap
    }
}