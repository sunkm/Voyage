package com.manchuan.tools.adapter.transformer

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

class CascadeTransformer : ViewPager2.PageTransformer {

    override fun transformPage(page: View, position: Float) {
        page.apply {
            when {
                position < -1 -> {
                    alpha = 0f
                    scaleX = 1.0f
                    scaleY = 1.0f
                }
                position <= 0 -> {
                    alpha = 1f
                    scaleX = 1.0f
                    scaleY = 1.0f
                    translationZ = 0f
                    translationX = 0f
                }
                position <= 1 -> {
                    alpha = 1 - position
                    translationX = -position * page.width
                    val scale = MIN_SCALE + (1 - MIN_SCALE) * (1 - abs(position))
                    scaleX = scale
                    scaleY = scale
                    translationZ = -position
                }
                else -> {
                    alpha = 0f
                }
            }
        }
    }
}