package com.google.android.material.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.manchuan.tools.R


abstract class MaterialSearchLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    // *********************************************************************************************
    @NavigationIconCompat
    @get:NavigationIconCompat
    var navigationIconCompat: Int = 0
        @SuppressLint("PrivateResource")
        set(@NavigationIconCompat navigationIconCompat) {
            field = navigationIconCompat

            when (navigationIconCompat) {
                NavigationIconCompat.NONE -> {
                    setNavigationIcon(0)
                }
                NavigationIconCompat.ARROW -> {
                    setNavigationIcon(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.search_ic_outline_arrow_back_24
                        )
                    )
                }
                NavigationIconCompat.SEARCH -> {
                    setNavigationIcon(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.search_ic_outline_search_24
                        )
                    )
                }
            }
        }

    // *********************************************************************************************
    abstract fun setNavigationIcon(@DrawableRes resId: Int)

    abstract fun setNavigationIcon(drawable: Drawable?)

    abstract fun setNavigationContentDescription(@StringRes resId: Int)

    abstract fun setNavigationContentDescription(description: CharSequence?)

    abstract fun setNavigationOnClickListener(listener: OnClickListener)

    abstract fun setNavigationElevation(elevation: Float)

    abstract fun setNavigationBackgroundColor(@ColorInt color: Int)

}