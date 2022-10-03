package com.manchuan.tools.utils

import android.content.Context
import android.graphics.*
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur

fun Bitmap.zoom(newHeight: Float, newWidth: Float): Bitmap {
    val matrix = Matrix()
    val scaleWidth = newWidth / width
    val scaleHeight = newHeight / height
    matrix.postScale(scaleWidth, scaleHeight)
    return Bitmap.createBitmap(
        this, 0, 0, width,
        height, matrix, true
    )
}

fun Bitmap.blur(context: Context, radius: Float, ty: Float): Bitmap {
    val bitmap = Bitmap.createScaledBitmap(
        this,
        (width / ty).toInt(), (height / ty).toInt(), false
    ) //先缩放图片，增加模糊速度
    val rs = RenderScript.create(context)
    val input = Allocation.createFromBitmap(
        rs, bitmap, Allocation.MipmapControl.MIPMAP_NONE,
        Allocation.USAGE_SCRIPT
    )
    val output = Allocation.createTyped(rs, input.type)
    val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
    script.setRadius(25F.coerceAtLeast(radius))
    script.setInput(input)
    script.forEach(output)
    output.copyTo(bitmap)
    rs.destroy()
    return bitmap
}

fun Bitmap.brightness(): Float {
    val bmp = zoom(3F, 3F) //转3*3大小的位图
    val pixel = bmp.getPixel(1, 1) //取中间位置的像素
    val r = (pixel shr 16 and 0xff) / 255.0f
    val g = (pixel shr 8 and 0xff) / 255.0f
    val b = (pixel and 0xff) / 255.0f
    return 0.299f * r + 0.587f * g + 0.114f * b //计算灰阶
}

fun Bitmap.drawColor(color: Int): Bitmap {
    val newBit = Bitmap.createBitmap(this)
    val canvas = Canvas(newBit)
    canvas.drawColor(color)
    return newBit
}

fun Bitmap.handleImageEffect(saturation: Float): Bitmap {
    val saturationMatrix = ColorMatrix()
    saturationMatrix.setSaturation(saturation)
    val imageMatrix = ColorMatrix()
    imageMatrix.postConcat(saturationMatrix)
    val paint = Paint()
    paint.colorFilter = ColorMatrixColorFilter(imageMatrix)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawBitmap(this, 0F, 0F, paint)
    return bitmap
}

fun Bitmap.mesh(floats: FloatArray): Bitmap {
    val fArr2 = FloatArray(72)
    var i = 0
    while (i <= 5) {
        var i2 = 0
        var i3 = 5
        while (i2 <= i3) {
            val i4 = i * 12 + i2 * 2
            val i5 = i4 + 1
            fArr2[i4] = floats[i4] * width.toFloat()
            fArr2[i5] = floats[i5] * height.toFloat()
            i2++
            i3 = 5
        }
        i++
    }
    val newBit = Bitmap.createBitmap(this)
    val canvas = Canvas(newBit)
    canvas.drawBitmapMesh(newBit, 5, 5, fArr2, 0, null, 0, null)
    return newBit
}

fun processBitmap(context: Context, bitmap: Bitmap): Bitmap {
    val floats = floatArrayOf(-0.2351f,
        -0.0967f,
        0.2135f,
        -0.1414f,
        0.9221f,
        -0.0908f,
        0.9221f,
        -0.0685f,
        1.3027f,
        0.0253f,
        1.2351f,
        0.1786f,
        -0.3768f,
        0.1851f,
        0.2f,
        0.2f,
        0.6615f,
        0.3146f,
        0.9543f,
        0.0f,
        0.6969f,
        0.1911f,
        1.0f,
        0.2f,
        0.0f,
        0.4f,
        0.2f,
        0.4f,
        0.0776f,
        0.2318f,
        0.6f,
        0.4f,
        0.6615f,
        0.3851f,
        1.0f,
        0.4f,
        0.0f,
        0.6f,
        0.1291f,
        0.6f,
        0.4f,
        0.6f,
        0.4f,
        0.4304f,
        0.4264f,
        0.5792f,
        1.2029f,
        0.8188f,
        -0.1192f,
        1.0f,
        0.6f,
        0.8f,
        0.4264f,
        0.8104f,
        0.6f,
        0.8f,
        0.8f,
        0.8f,
        1.0f,
        0.8f,
        0.0f,
        1.0f,
        0.0776f,
        1.0283f,
        0.4f,
        1.0f,
        0.6f,
        1.0f,
        0.8f,
        1.0f,
        1.1868f,
        1.0283f)
    val tmp = bitmap.zoom(150f, (bitmap.height * 150 / bitmap.width).toFloat())
        .blur(context, 25F, 1F)
        .mesh(floats)
        .zoom(1000F, 1000F)
        .mesh(floats)
        .blur(context, 12F, 1F)
        .handleImageEffect(1.8f)
    val float = tmp.brightness()
    return when {
        float > 0.8 ->
            tmp.drawColor(Color.parseColor("#50000000"))
        float < 0.2 ->
            tmp.drawColor(Color.parseColor("#50FFFFFF"))
        else ->
            tmp
    }
}

