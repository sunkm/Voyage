package com.manchuan.tools.genshin.html

import android.graphics.drawable.Drawable
import android.text.Html
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.dylanc.longan.dp
import com.lxj.androidktx.AndroidKTX.context
import com.manchuan.tools.R

class ImageGetter: Html.ImageGetter {
    private val padding = 10
    override fun getDrawable(p0: String?): Drawable {

        return if(p0!=null){
            val drawable = Glide.with(context).asDrawable().load(p0).submit().get()
            val screenWidth = (context.resources.displayMetrics.widthPixels - (padding *2).dp.toInt()).toFloat()

            val scaleValue = screenWidth/drawable.intrinsicWidth
            var drawableHeight = 0


            drawableHeight = if(drawable.intrinsicWidth>=screenWidth){
                (scaleValue * drawable.intrinsicHeight).toInt()
            }else{
                (drawable.intrinsicHeight*scaleValue).toInt()
            }

            drawable.setBounds(0,0,screenWidth.toInt(),drawableHeight)
            drawable
        }else{
            ContextCompat.getDrawable(context, R.drawable.icon_klee)!!
        }


    }
}