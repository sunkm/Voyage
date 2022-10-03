package com.manchuan.tools.view

import android.content.Context
import android.graphics.Canvas
import android.view.View
import androidx.annotation.RawRes
import com.manchuan.tools.drawable.SvgDrawable

class SvgView(context: Context?) : View(context) {
    var drawable: SvgDrawable? = null
    override fun onDraw(canvas: Canvas?) {
        drawable?.draw(canvas)
    }

    fun setSvg(@RawRes resId: Int) {
        drawable = SvgDrawable(context, resId)
        drawable!!.setScale(SvgDrawable.getDefaultScale(context))
        invalidate()
    }
}
