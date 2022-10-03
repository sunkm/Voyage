package com.manchuan.tools.view

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import com.manchuan.tools.R

class GMTimer @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    AbsTimeView(context, attrs), ITimer {
    private var mTotalTime: Long = -1
    private val mDividerLength: Float
    private var mInnerRadius = 0f
    private var mOuterRadius = 0f
    private var mDialRadius = 0f
    private val mDstOuterPath = Path()
    private val mDstInnerPath = Path()
    private val mDstTrianglePath = Path()
    private var mOuterPath: Path? = null
    private var mInnerPath: Path? = null
    private var mBorderLinePaint: Paint? = null
    private var mNumberTextPaint: Paint? = null
    private var mScaleTextPaint: Paint? = null
    private val mColorBorder: Int
    private val mColorDialStart: Int
    private val mColorDialEnd: Int
    private val mBorderLineWidth: Int
    private var mBase: Long = 0
    private val mNumberTextSize: Int
    private val mColorNumberText: Int
    private val mScaleTextSize: Int
    private val mColorScaleText: Int
    private var mTimeText: String? = null
    private val mTextVerticalOffset: Int
    private val mTextPadding: Int
    private var mCheckedDegree = 0f
    private val mHourScale = getString(R.string.hour)
    private val mMinScale = getString(R.string.minute)
    private val mSecScale = getString(R.string.second)
    private val mDash = FloatArray(2)
    private val mDialWidth: Int
    private val mDialLength: Int
    private var mRemainingTime: Long = -1
    private var mTrianglePath: Path? = null
    private var mBorderFillPaint: Paint? = null
    private var mPauseTime: Long = 0
    private val mMatrix = Matrix()
    private var mOuterScale = 1f
    private var mInnerScale = 1f
    private var mNumberTextScale = 1f
    private var mInnerAlpha = 255
    private var mOuterAlpha = 255
    private var mScaleTextAlpha = 255
    private var mNumberTextAlpha = 255
    private var mDialAlpha = 255
    private var mOffsetDegree = 0f
    private fun initPaint() {
        mBorderLinePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
        mBorderLinePaint!!.style = Paint.Style.STROKE
        mBorderLinePaint!!.color = mColorBorder
        mBorderLinePaint!!.strokeWidth = mBorderLineWidth.toFloat()
        mBorderFillPaint = Paint()
        mBorderFillPaint!!.set(mBorderLinePaint)
        mBorderFillPaint!!.style = Paint.Style.FILL
        mNumberTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        (mNumberTextPaint as TextPaint).color = mColorNumberText
        (mNumberTextPaint as TextPaint).textSize = mNumberTextSize.toFloat()
        (mNumberTextPaint as TextPaint).textAlign = Paint.Align.CENTER
        mScaleTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        (mScaleTextPaint as TextPaint).color = mColorScaleText
        (mScaleTextPaint as TextPaint).textSize = mScaleTextSize.toFloat()
        (mScaleTextPaint as TextPaint).textAlign = Paint.Align.CENTER
        mDialPaint!!.color = mColorDialStart
        mDialPaint!!.strokeWidth = mDialLength.toFloat()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mOuterRadius = mBoundRadius * 0.92f
        mInnerRadius = mBoundRadius * 0.76f
        val initDialRadius = (mOuterRadius + mInnerRadius) * 0.5f
        val linearGradient = LinearGradient(
            0F,
            -initDialRadius,
            0F,
            initDialRadius,
            mColorDialStart,
            mColorDialEnd,
            Shader.TileMode.CLAMP)
        mMatrix.reset()
        mMatrix.setRotate(45f)
        linearGradient.setLocalMatrix(mMatrix)
        mDialPaint!!.shader = linearGradient
        setDialRadius(initDialRadius)
        initPath()
    }

    private fun initPath() {
        initInnerPath()
        initOuterPath()
        initTrianglePath()
    }

    private fun initTrianglePath() {
        val tri = Path()
        tri.moveTo(-dp2px(4f).toFloat(), -mOuterRadius)
        tri.rLineTo(dp2px(8f).toFloat(), 0f)
        tri.lineTo(0f, -mOuterRadius + dp2px(5f))
        tri.lineTo(-dp2px(4f).toFloat(), -mOuterRadius)
        tri.close()
        if (mTrianglePath == null) {
            mTrianglePath = Path()
            for (i in 0..3) {
                mMatrix.reset()
                mMatrix.preRotate((i * 90).toFloat())
                tri.transform(mMatrix)
                mTrianglePath!!.addPath(tri)
            }
        }
    }

    fun setDialRadius(radius: Float) {
        if (mDialRadius != radius) {
            mDialRadius = radius
            updateDialPathEffect()
        }
    }

    private fun updateDialPathEffect() {
        val unitDial = (2 * Math.PI * mDialRadius / 240).toFloat()
        mDash[0] = mDialWidth.toFloat()
        mDash[1] = unitDial - mDialWidth
        mDialPaint!!.pathEffect = DashPathEffect(mDash, 0F)
    }

    private fun initInnerPath() {
        if (mInnerPath == null) {
            mInnerPath = Path()
            mInnerPath!!.addOval(-mInnerRadius,
                -mInnerRadius,
                mInnerRadius,
                mInnerRadius,
                Path.Direction.CCW)
            val temp = Path()
            temp.moveTo(0f, -mInnerRadius - dp2px(1f))
            temp.rLineTo(0f, -mDividerLength)
            mInnerPath!!.addPath(temp)
            temp.reset()
            temp.moveTo(mInnerRadius + dp2px(1f), 0f)
            temp.rLineTo(mDividerLength, 0f)
            mInnerPath!!.addPath(temp)
            temp.reset()
            temp.moveTo(0f, mInnerRadius + dp2px(1f))
            temp.rLineTo(0f, mDividerLength)
            mInnerPath!!.addPath(temp)
            temp.reset()
            temp.moveTo(-mInnerRadius - dp2px(1f), 0f)
            temp.rLineTo(-mDividerLength, 0f)
            mInnerPath!!.addPath(temp)
        }
    }

    private fun initOuterPath() {
        if (mOuterPath == null) {
            mOuterPath = Path()
            var start = 5f
            for (i in 0..3) {
                mOuterPath!!.addArc(-mOuterRadius,
                    -mOuterRadius,
                    mOuterRadius,
                    mOuterRadius,
                    start,
                    80f)
                start += 90f
            }
        }
    }

    override fun onTimeChanged() {
        setRemainingTime(Math.max(0, mTotalTime - elapsedTime))
        if (mRemainingTime == 0L) {
            isStarted = false
        }
    }

    private fun setRemainingTime(millis: Long) {
        if (millis != mRemainingTime) {
            mRemainingTime = millis
            onRemainingTimeChanged()
        }
    }

    @Synchronized
    private fun onRemainingTimeChanged() {
        updateText(mRemainingTime)
        updateCheckedDial(mRemainingTime)
        invalidate()
    }

    private fun updateText(millis: Long) {
        val time = TimeFormatter.getFormatTime(millis)
        setTimeText(time)
    }

    private fun updateCheckedDial(millis: Long) {
        mCheckedDegree = Math.min(360f, calculateDegreeWithMillis(millis))
        invalidate()
    }

    private fun calculateDegreeWithMillis(millis: Long): Float {
        val degree: Float
        val dpm: Float //degree per millisecond
        if (millis <= 1000 * 60) {
            dpm = 90f / (1000 * 60)
            degree = millis * dpm
        } else if (millis <= 1000 * 60 * 10) {
            dpm = 90f / (1000 * 60 * 9)
            degree = (millis - 1000 * 60) * dpm + 90
        } else if (millis <= 1000 * 60 * 30) {
            dpm = 90f / (1000 * 60 * 20)
            degree = (millis - 1000 * 60 * 10) * dpm + 180
        } else if (millis <= 1000 * 60 * 60) {
            dpm = 90f / (1000 * 60 * 30)
            degree = (millis - 1000 * 60 * 30) * dpm + 270
        } else {
            dpm = 360f / (1000 * 60 * 60)
            degree = (millis - 1000 * 60 * 60) * dpm + 360
        }
        return degree
    }

    fun setTimeText(text: String?) {
        mTimeText = text
        invalidate()
    }

    fun setBase() {
        mBase = SystemClock.elapsedRealtime()
    }

    private val elapsedTime: Long
        private get() = SystemClock.elapsedRealtime() - mBase

    override fun onDraw(canvas: Canvas) {
        canvas.translate(mXOffset, mYOffset)
        drawBorder(canvas)
        drawDial(canvas)
        drawNumberText(canvas)
        drawScaleText(canvas)
    }

    private fun drawBorder(canvas: Canvas) {
        mMatrix.reset()
        mMatrix.preScale(mInnerScale, mInnerScale)
        mInnerPath!!.transform(mMatrix, mDstInnerPath)
        mBorderLinePaint!!.alpha = mInnerAlpha
        canvas.drawPath(mDstInnerPath, mBorderLinePaint!!)
        mMatrix.reset()
        mMatrix.preScale(mOuterScale, mOuterScale)
        mOuterPath!!.transform(mMatrix, mDstOuterPath)
        mTrianglePath!!.transform(mMatrix, mDstTrianglePath)
        mBorderLinePaint!!.alpha = mOuterAlpha
        mBorderFillPaint!!.alpha = mOuterAlpha
        canvas.drawPath(mDstOuterPath, mBorderLinePaint!!)
        canvas.drawPath(mDstTrianglePath, mBorderFillPaint!!)
    }

    private fun drawDial(canvas: Canvas) {
        canvas.save()
        canvas.rotate(mOffsetDegree)
        mDialPaint!!.alpha = mDialAlpha
        canvas.drawArc(-mDialRadius,
            -mDialRadius,
            mDialRadius,
            mDialRadius,
            -90f,
            mCheckedDegree,
            false,
            mDialPaint!!)
        canvas.restore()
    }

    private fun drawNumberText(canvas: Canvas) {
        if (!TextUtils.isEmpty(mTimeText)) {
            mNumberTextPaint!!.alpha = mNumberTextAlpha
            canvas.save()
            canvas.scale(mNumberTextScale, mNumberTextScale)
            canvas.drawText(mTimeText!!, 0f, dp2px(8f).toFloat(), mNumberTextPaint!!)
            canvas.restore()
        }
    }

    private fun drawScaleText(canvas: Canvas) {
        val y = mTextPadding + mTextVerticalOffset + mScaleTextSize
        mScaleTextPaint!!.alpha = mScaleTextAlpha
        canvas.drawText(mHourScale, -dp2px(56f).toFloat(), y.toFloat(), mScaleTextPaint!!)
        canvas.drawText(mMinScale, 0f, y.toFloat(), mScaleTextPaint!!)
        canvas.drawText(mSecScale, dp2px(56f).toFloat(), y.toFloat(), mScaleTextPaint!!)
    }

    override fun setTotalTime(millis: Long) {
        if (mTotalTime != millis) {
            mTotalTime = millis
            setRemainingTime(millis)
            onRemainingTimeChanged()
        }
    }

    override fun start() {
        if (mTotalTime != 0L) {
            setBase()
            isStarted = true
        }
    }

    override fun pause() {
        if (mTotalTime != 0L) {
            isPaused = true
            mPauseTime = SystemClock.elapsedRealtime()
        }
    }

    override fun resume() {
        if (mTotalTime != 0L) {
            val pauseElapseTime = SystemClock.elapsedRealtime() - mPauseTime
            mBase += pauseElapseTime
            isPaused = false
        }
    }

    override fun reset() {
        isStarted = false
        setTotalTime(0)
    }

    override fun onInitShowAnimState() {
        mScaleTextAlpha = 0
        mNumberTextAlpha = 0
        mDialAlpha = 0
        mOuterAlpha = 0
        mInnerAlpha = 0
    }

    override val showAnimSet: AnimatorSet
        protected get() {
            val animSet = AnimatorSet()
            val anim1_1 = ValueAnimator.ofInt(0, 255)
            anim1_1.duration = 300
            anim1_1.addUpdateListener { animation -> mOuterAlpha = animation.animatedValue as Int }
            val anim1_2 = ValueAnimator.ofFloat(0.3f, 1f)
            anim1_2.duration = 540
            anim1_2.interpolator = OvershootInterpolator(1.5f)
            anim1_2.addUpdateListener { animation ->
                mOuterScale = animation.animatedValue as Float
                invalidate()
            }
            val anim2_1 = ValueAnimator.ofInt(0, 255)
            anim2_1.startDelay = 120
            anim2_1.duration = 300
            anim2_1.addUpdateListener { animation -> mInnerAlpha = animation.animatedValue as Int }
            val anim2_2 = ValueAnimator.ofFloat(0.3f, 1f)
            anim2_2.startDelay = 120
            anim2_2.duration = 540
            anim2_2.interpolator = OvershootInterpolator(1.5f)
            anim2_2.addUpdateListener { animation ->
                mInnerScale = animation.animatedValue as Float
                invalidate()
            }
            val anim3_1 = ValueAnimator.ofInt(0, 255)
            anim3_1.startDelay = 450
            anim3_1.duration = 150
            anim3_1.addUpdateListener { animation ->
                mScaleTextAlpha = animation.animatedValue as Int
                invalidate()
            }
            val anim4_1 = ValueAnimator.ofFloat(0.5f, 1f)
            anim4_1.startDelay = 450
            anim4_1.duration = 210
            anim4_1.addUpdateListener { animation ->
                mNumberTextScale = animation.animatedValue as Float
                invalidate()
            }
            val anim4_2 = ValueAnimator.ofInt(0, 255)
            anim4_2.startDelay = 450
            anim4_2.duration = 150
            anim4_2.addUpdateListener { animation ->
                mNumberTextAlpha = animation.animatedValue as Int
            }
            val anim5_1 = ValueAnimator.ofFloat(-45f, 0f)
            anim5_1.startDelay = 300
            anim5_1.duration = 300
            anim5_1.addUpdateListener { animation ->
                mOffsetDegree = animation.animatedValue as Float
            }
            val anim5_2 = ValueAnimator.ofInt(0, 255)
            anim5_2.startDelay = 300
            anim5_2.duration = 300
            anim5_2.interpolator = AccelerateDecelerateInterpolator()
            anim5_2.addUpdateListener { animation ->
                mDialAlpha = animation.animatedValue as Int
                invalidate()
            }
            animSet.playTogether(anim1_1,
                anim1_2,
                anim2_1,
                anim2_2,
                anim3_1,
                anim4_1,
                anim4_2,
                anim5_1,
                anim5_2)
            return animSet
        }
    override val hideAnimSet: AnimatorSet
        protected get() {
            val animSet = AnimatorSet()
            val anim1_1 = ValueAnimator.ofInt(255, 0)
            anim1_1.startDelay = 90
            anim1_1.duration = 360
            anim1_1.addUpdateListener { animation -> mOuterAlpha = animation.animatedValue as Int }
            val anim1_2 = ValueAnimator.ofFloat(1f, 0f)
            anim1_2.startDelay = 90
            anim1_2.duration = 360
            anim1_2.addUpdateListener { animation ->
                mOuterScale = animation.animatedValue as Float
            }
            val anim2_1 = ValueAnimator.ofInt(255, 0)
            anim2_1.duration = 450
            anim2_1.interpolator = AccelerateDecelerateInterpolator()
            anim2_1.addUpdateListener { animation ->
                mInnerAlpha = animation.animatedValue as Int
                invalidate()
            }
            val anim2_2 = ValueAnimator.ofFloat(1f, 0f)
            anim2_2.duration = 450
            anim2_2.interpolator = AccelerateDecelerateInterpolator()
            anim2_2.addUpdateListener { animation ->
                mInnerScale = animation.animatedValue as Float
            }
            val anim3_1 = ValueAnimator.ofInt(255, 0)
            anim3_1.duration = 150
            anim3_1.addUpdateListener { animation ->
                mScaleTextAlpha = animation.animatedValue as Int
            }
            val anim4_1 = ValueAnimator.ofInt(255, 0)
            anim4_1.duration = 150
            anim4_1.interpolator = AccelerateDecelerateInterpolator()
            anim4_1.addUpdateListener { animation ->
                mNumberTextAlpha = animation.animatedValue as Int
            }
            val anim4_2 = ValueAnimator.ofFloat(1f, 0.5f)
            anim4_2.duration = 210
            anim4_2.interpolator = AccelerateDecelerateInterpolator()
            anim4_2.addUpdateListener { animation ->
                mNumberTextScale = animation.animatedValue as Float
            }
            val anim5_1 = ValueAnimator.ofFloat(0f, -45f)
            anim5_1.duration = 300
            anim5_1.interpolator = AccelerateDecelerateInterpolator()
            anim5_1.addUpdateListener { animation ->
                mOffsetDegree = animation.animatedValue as Float
            }
            val anim5_2 = ValueAnimator.ofInt(255, 0)
            anim5_2.duration = 240
            anim5_2.addUpdateListener { animation -> mDialAlpha = animation.animatedValue as Int }
            animSet.playTogether(anim1_1,
                anim1_2,
                anim2_1,
                anim2_2,
                anim3_1,
                anim4_1,
                anim4_2,
                anim5_1,
                anim5_2)
            return animSet
        }

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.GMTimer)
        mDividerLength =
            array.getDimensionPixelSize(R.styleable.GMTimer_timerDividerLength, dp2px(12f))
                .toFloat()
        mBorderLineWidth =
            array.getDimensionPixelSize(R.styleable.GMTimer_timerBorderLineWidth, dp2px(0.8f))
        mColorBorder = array.getColor(R.styleable.GMTimer_timerBorderColor, -0x737374)
        mColorDialStart = array.getColor(R.styleable.GMTimer_timerDialStartColor, -0xf8ab01)
        mColorDialEnd = array.getColor(R.styleable.GMTimer_timerDialEndColor, -0xbaa201)
        mColorNumberText = array.getColor(R.styleable.GMTimer_timerNumberTextColor, -0x34000000)
        mNumberTextSize =
            array.getDimensionPixelSize(R.styleable.GMTimer_timerNumberTextSize, sp2px(40f))
        mScaleTextSize =
            array.getDimensionPixelSize(R.styleable.GMTimer_timerScaleTextSize, sp2px(11f))
        mColorScaleText = array.getColor(R.styleable.GMTimer_timerScaleTextColor, -0x747784)
        mTextVerticalOffset =
            array.getDimensionPixelSize(R.styleable.GMTimer_timerTextVerticalOffset, dp2px(10f))
        mTextPadding = array.getDimensionPixelSize(R.styleable.GMTimer_timerTextPadding, dp2px(6f))
        mDialWidth = array.getDimensionPixelSize(R.styleable.GMTimer_timerDialWidth, dp2px(0.8f))
        mDialLength = array.getDimensionPixelSize(R.styleable.GMTimer_timerDialLength, dp2px(7f))
        array.recycle()
        initPaint()
        setTotalTime(0)
    }
}