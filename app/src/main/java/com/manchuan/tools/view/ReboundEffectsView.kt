package com.manchuan.tools.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout

class ReboundEffectsView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(
    context!!, attrs, defStyleAttr
) {
    private var mPrinceView // 太子View
            : View? = null
    private var mInitTop = 0
    private var mInitBottom // 太子View初始时上下坐标位置(相对父View,即当前ReboundEffectsView)
            = 0
    private var isEndwiseSlide // 是否纵向滑动
            = false
    private var mVariableY // 手指上下滑动Y坐标变化前的Y坐标值
            = 0f

    /**
     * Touch事件
     */
    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (null != mPrinceView) {
            when (e.action) {
                MotionEvent.ACTION_DOWN -> onActionDown(e)
                MotionEvent.ACTION_MOVE -> return onActionMove(e)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> onActionUp(e) // 当ACTION_UP一样处理
            }
        }
        return super.onTouchEvent(e)
    }

    /**
     * 手指按下事件
     */
    private fun onActionDown(e: MotionEvent) {
        mVariableY = e.y
        /**
         * 保存mPrinceView的初始上下高度位置
         */
        mInitTop = mPrinceView!!.top
        mInitBottom = mPrinceView!!.bottom
    }

    /**
     * 手指滑动事件
     */
    private fun onActionMove(e: MotionEvent): Boolean {
        val nowY = e.y
        val diff = (nowY - mVariableY) / 2
        if (Math.abs(diff) > 0) { // 上下滑动
            // 移动太子View的上下位置
            mPrinceView!!.layout(
                mPrinceView!!.left, mPrinceView!!.top + diff.toInt(), mPrinceView!!.right,
                mPrinceView!!.bottom + diff.toInt()
            )
            mVariableY = nowY
            isEndwiseSlide = true
            return true // 消费touch事件
        }
        return super.onTouchEvent(e)
    }

    /**
     * 手指释放事件
     */
    private fun onActionUp(e: MotionEvent) {
        if (isEndwiseSlide) { // 是否为纵向滑动事件
            // 是纵向滑动事件，需要给太子View重置位置
            resetPrinceView()
            isEndwiseSlide = false
        }
    }

    /**
     * 回弹，重置太子View初始的位置
     */
    private fun resetPrinceView() {
        val ta = TranslateAnimation(0F, 0F, (mPrinceView!!.top - mInitTop).toFloat(), 0F)
        ta.duration = 600
        mPrinceView!!.startAnimation(ta)
        mPrinceView!!.layout(mPrinceView!!.left, mInitTop, mPrinceView!!.right, mInitBottom)
    }

    /**
     * XML布局完成加载
     */
    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount > 0) {
            mPrinceView = getChildAt(0) // 获得子View，太子View
        }
    }

    init {
        this.isClickable = true
    }
}