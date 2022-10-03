package com.manchuan.tools.view

import androidx.viewpager.widget.ViewPager
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

class NoScrollViewPager : ViewPager {
    private var noScroll = true

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
    }

    constructor(context: Context?) : super(context!!) {}

    //调用此方法 参数为false 即可禁止滑动
    fun setNoScroll(noScroll: Boolean) {
        this.noScroll = noScroll
    }

    override fun scrollTo(x: Int, y: Int) {
//        if(noScroll){  //加上判断无法用 setCurrentItem 方法切换
        super.scrollTo(x, y)
        //        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(arg0: MotionEvent): Boolean {
        return if (!noScroll) false else super.onTouchEvent(arg0)
    }

    override fun onInterceptTouchEvent(arg0: MotionEvent): Boolean {
        return if (!noScroll) false else super.onInterceptTouchEvent(arg0)
    }

    override fun setCurrentItem(item: Int, smoothScroll: Boolean) {
        super.setCurrentItem(item, smoothScroll)
    }

    override fun setCurrentItem(item: Int) {
        super.setCurrentItem(item)
    }
}