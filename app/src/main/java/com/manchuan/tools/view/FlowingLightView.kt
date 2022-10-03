package com.manchuan.tools.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.AttributeSet
import com.flaviofaria.kenburnsview.KenBurnsView
import com.flaviofaria.kenburnsview.RandomTransitionGenerator
import com.manchuan.tools.utils.*

/**
 * 如果这段代码能够正常工作，那么请记住作者是Simon。
 * 如果不能正常工作，那我也不知道是谁写的。
 */
class FlowingLightView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : KenBurnsView(context, attrs, defStyle) {
    init {
        val randomTransitionGenerator = RandomTransitionGenerator()
        randomTransitionGenerator.setTransitionDuration(3400)
        setTransitionGenerator(randomTransitionGenerator)
    }
    fun setFlowingLight(bitmap: Bitmap) {
        val floats = floatArrayOf(-0.2351f, -0.0967f, 0.2135f, -0.1414f, 0.9221f, -0.0908f, 0.9221f, -0.0685f, 1.3027f, 0.0253f, 1.2351f, 0.1786f, -0.3768f, 0.1851f, 0.2f, 0.2f, 0.6615f, 0.3146f, 0.9543f, 0.0f, 0.6969f, 0.1911f, 1.0f, 0.2f, 0.0f, 0.4f, 0.2f, 0.4f, 0.0776f, 0.2318f, 0.6f, 0.4f, 0.6615f, 0.3851f, 1.0f, 0.4f, 0.0f, 0.6f, 0.1291f, 0.6f, 0.4f, 0.6f, 0.4f, 0.4304f, 0.4264f, 0.5792f, 1.2029f, 0.8188f, -0.1192f, 1.0f, 0.6f, 0.8f, 0.4264f, 0.8104f, 0.6f, 0.8f, 0.8f, 0.8f, 1.0f, 0.8f, 0.0f, 1.0f, 0.0776f, 1.0283f, 0.4f, 1.0f, 0.6f, 1.0f, 0.8f, 1.0f, 1.1868f, 1.0283f)
        var tmp = bitmap.zoom(150f, (bitmap.height * 150 / bitmap.width).toFloat())
            .blur(context, 25F, 1.2F)
            .mesh(floats)
            .zoom(1200F, 1200F)
            .mesh(floats)
            .blur(context, 18F, 1.2F)
            .handleImageEffect(2f)
        val float = tmp.brightness()
        when {
            float > 0.8 -> {//判断图片大体颜色是深色还是浅色
                tmp = tmp.drawColor(Color.parseColor("#50000000"))
                setImageBitmap(tmp)//浅色就加入黑色遮罩
            }
            float < 0.2 -> {
                tmp = tmp.drawColor(Color.parseColor("#50FFFFFF"))
                setImageBitmap(tmp)//深色就加入白色遮罩
            }
            else -> {
                setImageBitmap(tmp)
            }
        }
    }
}