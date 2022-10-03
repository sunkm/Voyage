package com.lxj.androidktx.luban

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Responsible for starting compress and managing active and cached resources.
 */
internal class Engine(
    private val srcImg: InputStreamProvider,
    private val tagImg: File,
    private val focusAlpha: Boolean
) {
    private var srcWidth: Int
    private var srcHeight: Int

    init {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        options.inSampleSize = 1
        BitmapFactory.decodeStream(srcImg.open(), null, options)
        srcWidth = options.outWidth
        srcHeight = options.outHeight
    }

    private fun computeSize(): Int {
        srcWidth = if (srcWidth % 2 == 1) srcWidth + 1 else srcWidth
        srcHeight = if (srcHeight % 2 == 1) srcHeight + 1 else srcHeight
        val longSide = Math.max(srcWidth, srcHeight)
        val shortSide = Math.min(srcWidth, srcHeight)
        val scale = shortSide.toFloat() / longSide
        return if (scale <= 1 && scale > 0.5625) {
            if (longSide < 1664) {
                1
            } else if (longSide < 4990) {
                2
            } else if (longSide > 4990 && longSide < 10240) {
                4
            } else {
                if (longSide / 1280 == 0) 1 else longSide / 1280
            }
        } else if (scale <= 0.5625 && scale > 0.5) {
            if (longSide / 1280 == 0) 1 else longSide / 1280
        } else {
            Math.ceil(longSide / (1280.0 / scale)).toInt()
        }
    }

    private fun rotatingImage(bitmap: Bitmap?, angle: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(bitmap!!, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    @Throws(IOException::class)
    fun compress(): File {
        val options = BitmapFactory.Options()
        options.inSampleSize = computeSize()
        var tagBitmap = BitmapFactory.decodeStream(srcImg.open(), null, options)
        val stream = ByteArrayOutputStream()
        if (Checker.SINGLE.isJPG(srcImg.open())) {
            tagBitmap = rotatingImage(
                tagBitmap, Checker.SINGLE.getOrientation(
                    srcImg.open()
                )
            )
        }
        tagBitmap!!.compress(
            if (focusAlpha) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG,
            60,
            stream
        )
        tagBitmap.recycle()
        val fos = FileOutputStream(tagImg)
        fos.write(stream.toByteArray())
        fos.flush()
        fos.close()
        stream.close()
        return tagImg
    }
}