package com.manchuan.tools.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnticipateInterpolator
import android.view.animation.OvershootInterpolator
import com.manchuan.tools.R
import com.manchuan.tools.view.IStopwatch.StopwatchListener

class GMStopwatch : AbsTimeView, IStopwatch {
    private val mVerticalOffset = dp2px(16f)
    private var mColorDialEnd = 0
    private var mColorDialStart = 0
    private var mColorAxle = 0
    private var mColorUnCheckedDial = 0
    private var mColorShadow = 0
    private var mColorHand = 0
    private var mColorInnerDial = 0
    private var mColorBorder = 0
    private var mAxleRadius = 0
    private var mHandWidth = 0
    private var mInnerDialWidth = 0
    private var mOuterDialWidth = 0
    private var mInnerDialLength = 0
    private var mOuterDialLength = 0
    private var mBorderLineWidth = 0
    private var mAxlePaint: Paint? = null
    private var mBorderLinePaint: Paint? = null
    private var mHandPaint: Paint? = null
    private var mInnerDialPaint: Paint? = null
    private var mOuterDialRadius = 0f
    private var mInnerDialRadius = 0f
    private val mDash = FloatArray(2)
    private var mTimeDegree = 0f
    private var mInitBorderPath: Path? = null
    private var mInitBorderSmallPath: Path? = null
    private val mDstBorderPath: Path? = Path()
    private val mBorderPath = Path()
    private val mBorderSmallPath: Path? = Path()
    private var mBorderRadius = 0f
    private val mPathMeasure = PathMeasure()
    private val mMatrix = Matrix()
    private var mBase: Long = 0
    private val mDialColors = IntArray(3)
    private val mDialColorPositions = floatArrayOf(0.5f, 0.75f, 1f)
    private var mSweepGradient: SweepGradient? = null
    private var mElapsedTime: Long = 0
    private var mPauseTime: Long = 0
    private var mHandOffsetDegree = 0f
    private var mHandAlpha = 255
    private var mHandScale = 1f
    private var mDialAlpha = 255
    private var mInnerDialAlpha = 255
    private var mAxleAlpha = 255
    private var mSmallBorderAlpha = 255
    private var mInitOuterDialRadius = 0f
    private var mInitInnerDialRadius = 0f
    private var mBorderLength = 0f
    private var mBounds: RectF? = null
    private var mBorderSmallScale = 1f
    private var mRecordAnim: ValueAnimator? = null
    private var mStopwatchListener: StopwatchListener? = null
    private var mRecordTime: Long = 0

    constructor(context: Context?) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.GMStopwatch)
        mColorAxle = array.getColor(R.styleable.GMStopwatch_stopwatchAxleColor, -0xcb6d1a)
        mColorUnCheckedDial =
            array.getColor(R.styleable.GMStopwatch_stopwatchUnCheckedDialColor, -0x737374)
        mColorShadow = array.getColor(R.styleable.GMStopwatch_stopwatchShadowColor, 0x30000000)
        mColorHand = array.getColor(R.styleable.GMStopwatch_stopwatchHandColor, -0xcb6d1a)
        mColorBorder = array.getColor(R.styleable.GMStopwatch_stopwatchBorderColor, -0x737374)
        mColorDialStart = array.getColor(R.styleable.GMStopwatch_stopwatchDialStartColor, -0xf8ab01)
        mColorDialEnd = array.getColor(R.styleable.GMStopwatch_stopwatchDialEndColor, -0xbaa201)
        mColorInnerDial = array.getColor(R.styleable.GMStopwatch_stopwatchInnerDialColor, -0x737374)
        mAxleRadius =
            array.getDimensionPixelSize(R.styleable.GMStopwatch_stopwatchAxleRadius, dp2px(3f))
        mHandWidth =
            array.getDimensionPixelSize(R.styleable.GMStopwatch_stopwatchHandWidth, dp2px(1f))
        mOuterDialWidth =
            array.getDimensionPixelSize(R.styleable.GMStopwatch_stopwatchOuterDialWidth, dp2px(1f))
        mInnerDialWidth =
            array.getDimensionPixelSize(R.styleable.GMStopwatch_stopwatchInnerDialWidth, dp2px(1f))
        mOuterDialLength =
            array.getDimensionPixelSize(R.styleable.GMStopwatch_stopwatchOuterDialLength, dp2px(7f))
        mInnerDialLength =
            array.getDimensionPixelSize(R.styleable.GMStopwatch_stopwatchInnerDialLength,
                dp2px(10.5f))
        mBorderLineWidth =
            array.getDimensionPixelSize(R.styleable.GMStopwatch_stopwatchBorderLineWidth, dp2px(1f))
        array.recycle()
        updateDialColors()
        initPaint()
        initBorderPath()
    }

    private fun updateDialColors() {
        mDialColors[0] = mColorUnCheckedDial
        mDialColors[1] = mColorDialStart
        mDialColors[2] = mColorDialEnd
    }

    private fun initPaint() {
        mBorderLinePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
        mBorderLinePaint!!.style = Paint.Style.STROKE
        mBorderLinePaint!!.color = mColorBorder
        mBorderLinePaint!!.strokeWidth = mBorderLineWidth.toFloat()
        mAxlePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
        mAxlePaint!!.color = mColorAxle
        mAxlePaint!!.style = Paint.Style.FILL
        mAxlePaint!!.setShadowLayer(mShadowRadius, 0f, mShadowOffset, mColorShadow)
        mHandPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
        mHandPaint!!.color = mColorHand
        mHandPaint!!.style = Paint.Style.STROKE
        mHandPaint!!.strokeCap = Paint.Cap.ROUND
        mHandPaint!!.strokeWidth = mHandWidth.toFloat()
        mInnerDialPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
        mInnerDialPaint!!.color = mColorInnerDial
        mInnerDialPaint!!.style = Paint.Style.STROKE
        mInnerDialPaint!!.strokeWidth = mInnerDialLength.toFloat()
        mSweepGradient = SweepGradient(0F, 0F, mDialColors, mDialColorPositions)
        mDialPaint!!.strokeWidth = mOuterDialLength.toFloat()
        mDialPaint!!.shader = mSweepGradient
    }

    override fun onTimeChanged() {
        val time = stopwatchTime
        var degree = time * (360f / (60 * 1000))
        degree %= 360f
        setTimeDegree(degree)
        if (mStopwatchListener != null) mStopwatchListener!!.onUpdate(stopwatchTime)
    }

    private fun setBase() {
        setBase(true)
    }

    private fun setBase(resetElapse: Boolean) {
        mBase = SystemClock.elapsedRealtime() - mElapsedTime
        if (resetElapse) mElapsedTime = 0
    }

    private fun getElapsedTime(): Long {
        return SystemClock.elapsedRealtime() - mBase
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val initOuterBorder = mBoundRadius * 0.76f
        mInitOuterDialRadius = initOuterBorder - mOuterDialLength * 0.5f
        setOuterDialRadius(mInitOuterDialRadius)
        mInitInnerDialRadius = initOuterBorder - mOuterDialLength - mInnerDialLength * 0.5f
        setInnerDialRadius(mInitInnerDialRadius)
        val linearGradient = LinearGradient(
            0F,
            -initOuterBorder,
            0F,
            initOuterBorder,
            mColorDialStart,
            mColorDialEnd,
            Shader.TileMode.CLAMP)
        mMatrix.reset()
        mMatrix.setRotate(45f)
        linearGradient.setLocalMatrix(mMatrix)
        setBorderRadius(0.8f * mBoundRadius)
    }

    private fun initBorderPath() {
        mInitBorderPath = parseSVG(BORDER_PATH_DATA1)
        mInitBorderSmallPath = parseSVG(BORDER_PATH_DATA2)
    }

    private fun setInnerDialRadius(radius: Float) {
        if (mInnerDialRadius != radius) {
            mInnerDialRadius = radius
            updateInnerDialPathEffect()
        }
    }

    private fun updateInnerDialPathEffect() {
        val unitDial = (2 * Math.PI * mInnerDialRadius / 12).toFloat()
        mDash[0] = mInnerDialWidth.toFloat()
        mDash[1] = unitDial - mInnerDialWidth
        mInnerDialPaint!!.pathEffect = DashPathEffect(mDash, 0F)
    }

    private fun setOuterDialRadius(radius: Float) {
        if (mOuterDialRadius != radius) {
            mOuterDialRadius = radius
            updateOuterDialPathEffect()
        }
    }

    private fun updateOuterDialPathEffect() {
        val unitDial = (2 * Math.PI * mOuterDialRadius / 240).toFloat()
        mDash[0] = mOuterDialWidth.toFloat()
        mDash[1] = unitDial - mOuterDialWidth
        val effect = DashPathEffect(mDash, 0F)
        mDialPaint!!.pathEffect = effect
    }

    private fun setBorderRadius(radius: Float) {
        if (mBorderRadius != radius) {
            mBorderRadius = radius
            mMatrix.reset()
            val scale = mBorderRadius / 402f
            mMatrix.preScale(scale, scale)
            mInitBorderPath!!.transform(mMatrix, mBorderPath)
            mInitBorderSmallPath!!.transform(mMatrix, mBorderSmallPath)
            mPathMeasure.setPath(mBorderPath, false)
            mBorderLength = mPathMeasure.length
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.translate(mYOffset, mYOffset + mVerticalOffset)
        drawBorder(canvas)
        drawDials(canvas)
        drawHand(canvas)
        drawAxle(canvas)
    }

    private fun drawBorder(canvas: Canvas) {
        if (mDstBorderPath != null) {
            canvas.drawPath(mDstBorderPath, mBorderLinePaint!!)
        }
        if (mBorderSmallPath != null) {
            canvas.save()
            if (ViewConfig.STOPWATCH_IS_TOUCHABLE) canvas.scale(mBorderSmallScale,
                mBorderSmallScale)
            mBorderLinePaint!!.alpha = mSmallBorderAlpha
            canvas.drawPath(mBorderSmallPath, mBorderLinePaint!!)
            mBorderLinePaint!!.alpha = 255
            canvas.restore()
        }
    }

    private fun drawDials(canvas: Canvas) {
        mMatrix.reset()
        mMatrix.preRotate(mTimeDegree - 90)
        mSweepGradient!!.setLocalMatrix(mMatrix)
        mDialPaint!!.shader = mSweepGradient
        mDialPaint!!.alpha = mDialAlpha
        mInnerDialPaint!!.alpha = mInnerDialAlpha
        canvas.drawCircle(0f, 0f, mOuterDialRadius, mDialPaint!!)
        canvas.drawCircle(0f, 0f, mInnerDialRadius, mInnerDialPaint!!)
    }

    private fun drawHand(canvas: Canvas) {
        val bound = mOuterDialRadius + mOuterDialLength * 0.5f
        canvas.save()
        canvas.scale(mHandScale, mHandScale)
        val degree = mTimeDegree - 90 + mHandOffsetDegree
        setPaintShadow(degree, mHandPaint!!, mColorShadow)
        canvas.rotate(degree)
        mHandPaint!!.alpha = mHandAlpha
        canvas.drawLine(-bound * 0.2f, 0f, bound, 0f, mHandPaint!!)
        canvas.restore()
    }

    private fun drawAxle(canvas: Canvas) {
        mAxlePaint!!.alpha = mAxleAlpha
        canvas.drawCircle(0f, 0f, mAxleRadius.toFloat(), mAxlePaint!!)
    }

    override fun setElapsedTime(millis: Long) {
        if (mElapsedTime != millis) {
            mElapsedTime = millis
            var degree = millis * (360f / (60 * 1000))
            degree %= 360f
            setTimeDegree(degree)
            if (mStopwatchListener != null) mStopwatchListener!!.onUpdate(millis)
        }
    }

    override fun start() {
        if (!isStarted) {
            setBase()
            isStarted = true
        }
    }

    override fun pause() {
        if (isRunning) {
            isPaused = true
            mPauseTime = SystemClock.elapsedRealtime()
        }
    }

    override fun resume() {
        if (isPaused) {
            val pauseElapseTime = SystemClock.elapsedRealtime() - mPauseTime
            mBase += pauseElapseTime
            isPaused = false
        }
    }

    override fun reset() {
        isStarted = false
        setTimeDegree(0f)
        mBase = 0
        if (mStopwatchListener != null) mStopwatchListener!!.onUpdate(stopwatchTime)
    }

    private fun setTimeDegree(degree: Float) {
        if (mTimeDegree != degree) {
            mTimeDegree = degree
            invalidate()
        }
    }

    override fun onInitShowAnimState() {
        mAxleAlpha = 0
        mDialAlpha = 0
        mHandAlpha = 0
        mInnerDialAlpha = 0
        mSmallBorderAlpha = 0
    }

    override val showAnimSet: AnimatorSet
        protected get() {
            val animSet = AnimatorSet()
            val anim1_1 = ValueAnimator.ofFloat(-30f, 0f)
            anim1_1.duration = 300
            anim1_1.interpolator = AccelerateDecelerateInterpolator()
            anim1_1.addUpdateListener { animation: ValueAnimator ->
                mHandOffsetDegree = animation.animatedValue as Float
            }
            val anim1_2 = ValueAnimator.ofInt(0, 255)
            anim1_2.duration = 300
            anim1_2.addUpdateListener { animation: ValueAnimator ->
                mHandAlpha = animation.animatedValue as Int
            }
            val anim1_3 = ValueAnimator.ofFloat(0.2f, 1f)
            anim1_3.duration = 600
            anim1_3.interpolator = OvershootInterpolator(1.5f)
            anim1_3.addUpdateListener { animation: ValueAnimator ->
                mHandScale = animation.animatedValue as Float
                invalidate()
            }
            val anim2_1 = ValueAnimator.ofInt(0, 255)
            anim2_1.duration = 300
            anim2_1.addUpdateListener { animation: ValueAnimator ->
                mDialAlpha = animation.animatedValue as Int
            }
            val anim2_2 = ValueAnimator.ofFloat(0.2f, 1f)
            anim2_2.duration = 600
            anim2_2.interpolator = OvershootInterpolator(0.7f)
            anim2_2.addUpdateListener { animation: ValueAnimator -> setOuterDialRadius(animation.animatedValue as Float * mInitOuterDialRadius) }
            val anim3_1 = ValueAnimator.ofInt(0, 255)
            anim3_1.duration = 150
            anim3_1.addUpdateListener { animation: ValueAnimator ->
                mAxleAlpha = animation.animatedValue as Int
            }
            val anim4_1 = ValueAnimator.ofFloat(1f, 0f)
            anim4_1.duration = 600
            anim4_1.addUpdateListener { animation: ValueAnimator ->
                mDstBorderPath!!.reset()
                mPathMeasure.getSegment(animation.animatedValue as Float * mBorderLength,
                    mBorderLength,
                    mDstBorderPath,
                    true)
            }
            val anim4_2 = ValueAnimator.ofInt(0, 255)
            anim4_2.startDelay = 360
            anim4_2.duration = 240
            anim4_2.addUpdateListener { animation: ValueAnimator ->
                mSmallBorderAlpha = animation.animatedValue as Int
            }
            val anim5_1 = ValueAnimator.ofInt(0, 255)
            anim5_1.startDelay = 150
            anim5_1.duration = 300
            anim5_1.addUpdateListener { animation: ValueAnimator ->
                mInnerDialAlpha = animation.animatedValue as Int
            }
            val anim5_2 = ValueAnimator.ofFloat(0.2f, 1f)
            anim5_2.startDelay = 150
            anim5_2.duration = 600
            anim5_2.interpolator = OvershootInterpolator(1.5f)
            anim5_2.addUpdateListener { animation: ValueAnimator ->
                setInnerDialRadius(animation.animatedValue as Float * mInitInnerDialRadius)
                invalidate()
            }
            animSet.playTogether(anim1_1, anim1_2, anim1_3,
                anim2_1, anim2_2, anim3_1, anim4_1,
                anim4_2, anim5_1, anim5_2)
            return animSet
        }
    override val hideAnimSet: AnimatorSet
        protected get() {
            val animSet = AnimatorSet()
            val anim1_1 = ValueAnimator.ofFloat(0f, -30f)
            anim1_1.startDelay = 60
            anim1_1.duration = 300
            anim1_1.interpolator = AccelerateDecelerateInterpolator()
            anim1_1.addUpdateListener { animation: ValueAnimator ->
                mHandOffsetDegree = animation.animatedValue as Float
            }
            val anim1_2 = ValueAnimator.ofInt(255, 0)
            anim1_2.startDelay = 60
            anim1_2.duration = 300
            anim1_2.addUpdateListener { animation: ValueAnimator ->
                mHandAlpha = animation.animatedValue as Int
            }
            val anim1_3 = ValueAnimator.ofFloat(1f, 0.64f)
            anim1_3.startDelay = 90
            anim1_3.duration = 240
            anim1_3.interpolator = AnticipateInterpolator()
            anim1_3.addUpdateListener { animation: ValueAnimator ->
                mHandScale = animation.animatedValue as Float
            }
            val anim2_1 = ValueAnimator.ofInt(255, 0)
            anim2_1.startDelay = 210
            anim2_1.duration = 240
            anim2_1.addUpdateListener { animation: ValueAnimator ->
                mDialAlpha = animation.animatedValue as Int
            }
            val anim2_2 = ValueAnimator.ofFloat(1f, 0.2f)
            anim2_2.startDelay = 120
            anim2_2.duration = 330
            anim2_2.interpolator = AnticipateInterpolator()
            anim2_2.addUpdateListener { animation: ValueAnimator -> setOuterDialRadius(animation.animatedValue as Float * mInitOuterDialRadius) }
            val anim3_1 = ValueAnimator.ofInt(255, 0)
            anim3_1.startDelay = 300
            anim3_1.duration = 150
            anim3_1.addUpdateListener { animation: ValueAnimator ->
                mAxleAlpha = animation.animatedValue as Int
            }
            val anim4_1 = ValueAnimator.ofFloat(0f, 1f)
            anim4_1.duration = 450
            anim4_1.addUpdateListener { animation: ValueAnimator ->
                mDstBorderPath!!.reset()
                mPathMeasure.getSegment(animation.animatedValue as Float * mBorderLength,
                    mBorderLength,
                    mDstBorderPath,
                    true)
                invalidate()
            }
            val anim4_2 = ValueAnimator.ofInt(255, 0)
            anim4_2.duration = 150
            anim4_2.addUpdateListener { animation: ValueAnimator ->
                mSmallBorderAlpha = animation.animatedValue as Int
            }
            val anim5_1 = ValueAnimator.ofInt(255, 0)
            anim5_1.startDelay = 60
            anim5_1.duration = 240
            anim5_1.addUpdateListener { animation: ValueAnimator ->
                mInnerDialAlpha = animation.animatedValue as Int
            }
            val anim5_2 = ValueAnimator.ofFloat(1f, 0.2f)
            anim5_2.startDelay = 60
            anim5_2.duration = 240
            anim5_2.interpolator = AnticipateInterpolator()
            anim5_2.addUpdateListener { animation: ValueAnimator -> setInnerDialRadius(animation.animatedValue as Float * mInitInnerDialRadius) }
            animSet.playTogether(anim1_1, anim1_2, anim1_3,
                anim2_1, anim2_2, anim3_1, anim4_1,
                anim4_2, anim5_1, anim5_2)
            return animSet
        }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (ViewConfig.STOPWATCH_IS_TOUCHABLE) {
                if (mBounds == null) {
                    mBounds = RectF()
                    mBorderSmallPath!!.computeBounds(mBounds!!, false)
                }
                val x = event.x - width / 2f
                val y = event.y - height / 2f - mVerticalOffset
                if (mBounds!!.contains(x, y)) {
                    startRecordAnimation()
                    return true
                }
            }
            return super.onTouchEvent(event)
        }
        return super.onTouchEvent(event)
    }

    private fun startRecordAnimation() {
        if (mRecordAnim == null) {
            mRecordAnim = ValueAnimator.ofFloat(1f, 0.93f, 1f)
            mRecordAnim!!.duration = 100
            mRecordAnim!!.addUpdateListener { animation: ValueAnimator ->
                mBorderSmallScale = animation.animatedValue as Float
                invalidate()
            }
            mRecordAnim!!.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    mRecordTime = stopwatchTime
                }

                override fun onAnimationEnd(animation: Animator) {
                    if (mStopwatchListener != null) mStopwatchListener!!.onRecord(mRecordTime)
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
        }
        mRecordAnim!!.start()
    }

    override fun record() {
        if (ViewConfig.STOPWATCH_IS_TOUCHABLE) startRecordAnimation() else if (mStopwatchListener != null) mStopwatchListener!!.onRecord(
            stopwatchTime)
    }

    override val stopwatchTime: Long
        get() {
            if (isPaused) {
                return mPauseTime - mBase
            }
            return if (!isStarted) 0 else getElapsedTime()
        }

    override fun setStopwatchWatcher(listener: StopwatchListener?) {
        mStopwatchListener = listener
    }

    companion object {
        private const val BORDER_PATH_DATA1 = "M333.2-224.6C376.6-160.4,402-83,402,0.3" +
                "c0,222-180,402-402,402s-402-180-402-402c0-212.6,165.1-386.7,374.1-401c0-0.5-0.1-25.3-0.1-74.5" +
                "h-14.4c-15.9,0-28.9-12.9-28.9-28.9c0-15.9,12.9-28.9,28.9-28.9h86.6c15.9,0,28.9,12.9,28.9,28.9" +
                "c0,15.9-12.9,28.9-28.9,28.9H29.7v72.2c67.5,7.3,130.4,29,184.6,63.2"
        private const val BORDER_PATH_DATA2 = "M330.2-383.2l-54.5,58.1c-10,10.7-9.4,27.4,1.3,37.4" +
                "s27.6,9.4,37.6-1.3l54.5-58.1c10-10.7,9.4-27.4-1.3-37.4S340.2-393.9,330.2-383.2z"
    }
}