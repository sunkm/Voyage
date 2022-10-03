package com.manchuan.tools.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.content.Context
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import com.manchuan.tools.utils.SizeTransformer.dp2px
import com.manchuan.tools.utils.SizeTransformer.sp2px
import com.manchuan.tools.utils.SvgPathParser.parsePath
import java.text.ParseException

/**
 * @author Felix.Liang
 */
abstract class AbsTimeView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {
    protected var mShadowRadius = dp2px(1f).toFloat()
    protected var mShadowOffset = dp2px(2.5f).toFloat()
    private var mVisible = false
    var isRunning = false
        private set
    private var mStarted = false
    private var mPaused = false
    protected var mXOffset = 0f
    protected var mYOffset = 0f
    protected var mDialPaint: Paint? = null
    protected var mSubLinePaint: Paint? = null
    protected var mBoundRadius = 0f
    protected var mAnimatedCallback: AnimatedCallback? = null
    private var mShowAnimSet: AnimatorSet? = null
    private var mHideAnimSet: AnimatorSet? = null
    private fun initBasePaint() {
        mDialPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
        mDialPaint!!.style = Paint.Style.STROKE
        mSubLinePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
        mSubLinePaint!!.style = Paint.Style.STROKE
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mXOffset = w / 2f
        mYOffset = h / 2f
        mBoundRadius = w.coerceAtMost(h) * 0.5f
    }

    protected fun dp2px(dpValue: Float): Int {
        return dp2px(context, dpValue)
    }

    protected fun sp2px(spValue: Float): Int {
        return sp2px(context, spValue)
    }

    protected fun getString(resId: Int): String {
        return context.getString(resId)
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        mVisible = visibility == VISIBLE
        updateRunning()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mVisible = false
        if (mShowAnimSet != null) mShowAnimSet!!.removeAllListeners()
        if (mHideAnimSet != null) mHideAnimSet!!.removeAllListeners()
    }

    private fun updateRunning() {
        val running = mStarted && mVisible && !mPaused
        if (isRunning != running) {
            isRunning = running
            if (running) {
                postDelayed(mTicker, UPDATE_DELAY)
            } else {
                removeCallbacks(mTicker)
            }
        }
    }

    var isStarted: Boolean
        get() = mStarted
        protected set(started) {
            if (mStarted != started) {
                mStarted = started
                if (!mStarted) {
                    mPaused = false
                }
                updateRunning()
            }
        }
    var isPaused: Boolean
        get() = mPaused
        protected set(paused) {
            if (mPaused != paused) {
                mPaused = paused
                updateRunning()
            }
        }
    private val mTicker: Runnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                onTimeChanged()
                postDelayed(this, UPDATE_DELAY)
            }
        }
    }

    protected abstract fun onTimeChanged()
    protected fun parseSVG(data: String?): Path? {
        return try {
            parsePath(data)
        } catch (e: ParseException) {
            null
        }
    }

    fun setAnimatedCallback(callback: AnimatedCallback?) {
        mAnimatedCallback = callback
    }

    /**
     * Start an animation to show current time widget
     */
    fun animatedShow() {
        if (mShowAnimSet == null) {
            mShowAnimSet = showAnimSet
            if (mShowAnimSet != null) {
                mShowAnimSet!!.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {
                        if (mAnimatedCallback != null) mAnimatedCallback!!.onStart()
                        setLayerType(LAYER_TYPE_HARDWARE, null)
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        if (mAnimatedCallback != null) mAnimatedCallback!!.onEnd()
                        setLayerType(LAYER_TYPE_SOFTWARE, null)
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        setLayerType(LAYER_TYPE_SOFTWARE, null)
                    }

                    override fun onAnimationRepeat(animation: Animator) {}
                })
            }
        }
        if (mShowAnimSet != null && mShowAnimSet!!.isRunning) mShowAnimSet!!.cancel()
        if (mShowAnimSet != null && !mShowAnimSet!!.isStarted) {
            onInitShowAnimState()
            mShowAnimSet!!.start()
        }
    }

    protected open fun onInitShowAnimState() {}

    /**
     * Start an animation to hide current time widget
     */
    fun animatedHide() {
        if (mShowAnimSet != null && mShowAnimSet!!.isRunning) mShowAnimSet!!.cancel()
        if (ViewConfig.SHOW_HIDE_ANIMATION) {
            if (mHideAnimSet == null) {
                mHideAnimSet = hideAnimSet
                if (mHideAnimSet != null) {
                    mHideAnimSet!!.addListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {
                            if (mAnimatedCallback != null) mAnimatedCallback!!.onStart()
                        }

                        override fun onAnimationEnd(animation: Animator) {
                            if (mAnimatedCallback != null) mAnimatedCallback!!.onEnd()
                        }

                        override fun onAnimationCancel(animation: Animator) {}
                        override fun onAnimationRepeat(animation: Animator) {}
                    })
                }
            }
            if (mHideAnimSet != null) {
                if (mHideAnimSet!!.isRunning) mHideAnimSet!!.cancel()
                mHideAnimSet!!.start()
            }
        } else {
            if (mAnimatedCallback != null) mAnimatedCallback!!.onEnd()
        }
    }

    protected open val showAnimSet: AnimatorSet?
        protected get() = AnimatorSet()
    protected open val hideAnimSet: AnimatorSet?
        protected get() = AnimatorSet()

    interface AnimatedCallback {
        fun onStart()
        fun onEnd()
    }

    protected fun setPaintShadow(degree: Float, paint: Paint, color: Int) {
        val rad = Math.toRadians(degree.toDouble()).toFloat()
        paint.setShadowLayer(mShadowRadius,
            (mShadowOffset * Math.sin(rad.toDouble())).toFloat(),
            (mShadowOffset * Math.cos(rad.toDouble())).toFloat(),
            color)
    }

    companion object {
        private const val UPDATE_DELAY: Long = 30
    }

    init {
        initBasePaint()
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }
}