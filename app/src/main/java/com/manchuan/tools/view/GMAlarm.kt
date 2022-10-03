package com.manchuan.tools.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnticipateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import com.manchuan.tools.R
import java.util.*

/**
 * @author Felix.Liang
 */
class GMAlarm @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    AbsTimeView(context, attrs) {
    private var mAttached = false
    private var mAxlePaint: Paint? = null
    private var mSecondHandPaint: Paint? = null
    private var mMinuteHandPaint: Paint? = null
    private var mHourHandPaint: Paint? = null
    private var mSubDialPaint: Paint? = null
    private var mScaleTextPaint: Paint? = null
    private val mColorAxle: Int
    private val mColorSecondHand: Int
    private val mColorMinuteHand: Int
    private val mColorHourHand: Int
    private val mColorShadow: Int
    private val mColorSubDial: Int
    private val mColorDialStart: Int
    private val mColorDialEnd: Int
    private val mColorUncheckedDial: Int
    private val mDialWidth: Float
    private val mDialLength: Float
    private val mAxleRadius: Float
    private var mSecondHandLength = 0f
    private val mSecondHandWidth: Float
    private var mMinuteHandLength = 0f
    private val mMinuteHandWidth: Float
    private var mHourHandLength = 0f
    private val mHourHandWidth: Float
    private val mSubDialLength: Float
    private val mSubDialWidth: Float
    private val mScaleTextColor: Int
    private val mScaleTextSize: Int
    private var mDialRadius = 0f
    private var mSubDialRadius = 0f
    private val mScaleTextMargin: Int
    private var mSecondDegree = 0f
    private var mMinuteDegree = 0f
    private var mHourDegree = 0f
    private val mDialColors = IntArray(3)
    private val mDialColorPositions = floatArrayOf(0.5f, 0.75f, 1f)
    private val mDash = FloatArray(2)
    private var mSweepGradient: SweepGradient? = null
    private val mTimeZone: String?
    private var mTime: Calendar? = null
    private val mMatrix = Matrix()
    private var mHourOffsetDegree = 0f
    private var mMinuteOffsetDegree = 0f
    private var mSecondOffsetDegree = 0f
    private var mHourAlpha = 255
    private var mMinuteAlpha = 255
    private var mSecondAlpha = 255
    private var mAxleAlpha = 255
    private var mScaleTextAlpha = 255
    private var mDialAlpha = 255
    private var mSubDialAlpha = 255
    private var mHourScale = 1f
    private var mMinuteScale = 1f
    private var mSecondScale = 1f
    private var mInitDialRadius = 0f
    private var mInitSubDialRadius = 0f
    private val mIntentReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            onTimeChanged()
        }
    }

    private fun updateDialColors() {
        mDialColors[0] = mColorUncheckedDial
        mDialColors[1] = mColorDialStart
        mDialColors[2] = mColorDialEnd
    }

    private fun initPaints() {
        mDialPaint!!.strokeWidth = mDialLength
        mAxlePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
        mAxlePaint!!.color = mColorAxle
        mAxlePaint!!.style = Paint.Style.FILL
        mAxlePaint!!.setShadowLayer(mShadowRadius, 0f, mShadowOffset, mColorShadow)
        mSecondHandPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
        mSecondHandPaint!!.color = mColorSecondHand
        mSecondHandPaint!!.style = Paint.Style.STROKE
        mSecondHandPaint!!.strokeCap = Paint.Cap.ROUND
        mSecondHandPaint!!.strokeWidth = mSecondHandWidth
        mMinuteHandPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
        mMinuteHandPaint!!.color = mColorMinuteHand
        mMinuteHandPaint!!.style = Paint.Style.STROKE
        mMinuteHandPaint!!.strokeCap = Paint.Cap.ROUND
        mMinuteHandPaint!!.strokeWidth = mMinuteHandWidth
        mHourHandPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
        mHourHandPaint!!.color = mColorHourHand
        mHourHandPaint!!.style = Paint.Style.STROKE
        mHourHandPaint!!.strokeCap = Paint.Cap.ROUND
        mHourHandPaint!!.strokeWidth = mHourHandWidth
        mSubDialPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
        mSubDialPaint!!.style = Paint.Style.STROKE
        mSubDialPaint!!.strokeWidth = mSubDialLength
        mSubDialPaint!!.color = mColorSubDial
        mSweepGradient = SweepGradient(0F, 0F, mDialColors, mDialColorPositions)
        mDialPaint!!.shader = mSweepGradient
        mScaleTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
        (mScaleTextPaint as TextPaint).textAlign = Paint.Align.CENTER
        (mScaleTextPaint as TextPaint).textSize = mScaleTextSize.toFloat()
        (mScaleTextPaint as TextPaint).color = mScaleTextColor
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!mAttached) {
            registerReceiver()
            createTime(mTimeZone)
        }
    }

    private fun createTime(timeZone: String?) {
        mTime = if (timeZone != null) {
            Calendar.getInstance(TimeZone.getTimeZone(timeZone))
        } else {
            Calendar.getInstance()
        }
    }

    private fun registerReceiver() {
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_TIME_TICK)
        filter.addAction(Intent.ACTION_TIME_CHANGED)
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED)
        context.registerReceiver(mIntentReceiver, filter, null, handler)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (mAttached) {
            mAttached = false
            unregisterReceiver()
        }
    }

    private fun unregisterReceiver() {
        context.unregisterReceiver(mIntentReceiver)
    }

    override fun onTimeChanged() {
        mTime!!.timeInMillis = System.currentTimeMillis()
        val millis = mTime!![Calendar.MILLISECOND].toLong()
        val second = mTime!![Calendar.SECOND] + millis / 1000f
        val minute = mTime!![Calendar.MINUTE].toFloat()
        val hour = mTime!![Calendar.HOUR].toFloat()
        mSecondDegree = second * 6
        mSecondDegree %= 360f
        mMinuteDegree = minute * 6
        mMinuteDegree %= 360f
        mHourDegree = (hour + minute / 60f) * 30
        mHourDegree %= 360f
        updateView()
    }

    private fun updateView() {
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mInitDialRadius = mBoundRadius / 1.08f - mDialLength * 0.5f
        setDialRadius(mInitDialRadius)
        mInitSubDialRadius = mBoundRadius / 1.08f - mSubDialLength * 0.5f
        setSubDialRadius(mInitSubDialRadius)
    }

    private fun setDialRadius(radius: Float) {
        var radius = radius
        if (mDialRadius != radius) {
            mDialRadius = radius
            radius += mDialLength * 0.5f
            mSecondHandLength = radius
            mMinuteHandLength = radius * 0.73f
            mHourHandLength = radius * 0.55f
            updateDialPaint()
        }
    }

    fun setSubDialRadius(radius: Float) {
        if (mSubDialRadius != radius) {
            mSubDialRadius = radius
            updateSubDialPaint()
        }
    }

    private fun updateDialPaint() {
        val unitDial = (2 * Math.PI * mDialRadius / 240).toFloat()
        mDash[0] = mDialWidth
        mDash[1] = unitDial - mDialWidth
        mDialPaint!!.pathEffect = DashPathEffect(mDash, 0F)
        mDialPaint!!.color = -0x1000000
    }

    private fun updateSubDialPaint() {
        val unitDial = (2 * Math.PI * mSubDialRadius / 12).toFloat()
        mDash[0] = mSubDialWidth
        mDash[1] = unitDial - mSubDialWidth
        mSubDialPaint!!.pathEffect = DashPathEffect(mDash, 0F)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.translate(mXOffset, mYOffset)
        drawDials(canvas)
        drawScales(canvas)
        drawHands(canvas)
    }

    private fun drawScales(canvas: Canvas) {
        canvas.save()
        canvas.translate(0f, dp2px(6f).toFloat())
        mScaleTextPaint!!.alpha = mScaleTextAlpha
        val r = mDialRadius - mSubDialLength - mScaleTextMargin
        for (i in 0..11) {
            val x = (r * Math.sin(Math.toRadians((i * 30).toDouble()))).toFloat()
            val y = -(r * Math.cos(Math.toRadians((i * 30).toDouble()))).toFloat()
            val scale = if (i == 0) 12 else i
            canvas.drawText(scale.toString(), x, y, mScaleTextPaint!!)
        }
        canvas.restore()
    }

    private fun drawDials(canvas: Canvas) {
        mMatrix.reset()
        mMatrix.preRotate(mSecondDegree - 90)
        mSweepGradient!!.setLocalMatrix(mMatrix)
        mDialPaint!!.shader = mSweepGradient
        mDialPaint!!.alpha = mDialAlpha
        mSubDialPaint!!.alpha = mSubDialAlpha
        canvas.drawCircle(0f, 0f, mDialRadius, mDialPaint!!)
        canvas.drawCircle(0f, 0f, mSubDialRadius, mSubDialPaint!!)
    }

    private fun drawHands(canvas: Canvas) {
        drawHourHand(canvas)
        drawMinuteHand(canvas)
        drawSecondHand(canvas)
        drawAxle(canvas)
    }

    private fun drawSecondHand(canvas: Canvas) {
        canvas.save()
        canvas.scale(mSecondScale, mSecondScale)
        mSecondHandPaint!!.alpha = mSecondAlpha
        val degree = mSecondDegree - 90 + mSecondOffsetDegree
        setPaintShadow(degree, mSecondHandPaint!!, mColorShadow)
        canvas.rotate(degree)
        canvas.drawLine(0f, 0f, mSecondHandLength, 0f, mSecondHandPaint!!)
        canvas.restore()
    }

    private fun drawMinuteHand(canvas: Canvas) {
        canvas.save()
        canvas.scale(mMinuteScale, mMinuteScale)
        mMinuteHandPaint!!.alpha = mMinuteAlpha
        val degree = mMinuteDegree - 90 + mMinuteOffsetDegree
        setPaintShadow(degree, mMinuteHandPaint!!, mColorShadow)
        canvas.rotate(degree)
        canvas.drawLine(0f, 0f, mMinuteHandLength, 0f, mMinuteHandPaint!!)
        canvas.restore()
    }

    private fun drawHourHand(canvas: Canvas) {
        canvas.save()
        canvas.scale(mHourScale, mHourScale)
        mHourHandPaint!!.alpha = mHourAlpha
        val degree = mHourDegree - 90 + mHourOffsetDegree
        setPaintShadow(degree, mHourHandPaint!!, mColorShadow)
        canvas.rotate(degree)
        canvas.drawLine(0f, 0f, mHourHandLength, 0f, mHourHandPaint!!)
        canvas.restore()
    }

    private fun drawAxle(canvas: Canvas) {
        mAxlePaint!!.alpha = mAxleAlpha
        canvas.drawCircle(0f, 0f, mAxleRadius, mAxlePaint!!)
    }

    override fun onInitShowAnimState() {
        mMinuteAlpha = 0
        mHourAlpha = 0
        mSecondAlpha = 0
        mDialAlpha = 0
        mSubDialAlpha = 0
        mScaleTextAlpha = 0
    }

    override val showAnimSet: AnimatorSet
        protected get() {
            val animSet = AnimatorSet()
            val anim1_1 = ValueAnimator.ofFloat(-30f, 0f)
            anim1_1.duration = 300
            anim1_1.interpolator = AccelerateDecelerateInterpolator()
            anim1_1.addUpdateListener { animation ->
                mHourOffsetDegree = animation.animatedValue as Float
            }
            val anim1_2 = ValueAnimator.ofInt(0, 255)
            anim1_2.duration = 300
            anim1_2.addUpdateListener { animation -> mHourAlpha = animation.animatedValue as Int }
            val anim1_3 = ValueAnimator.ofFloat(0.2f, 1f)
            anim1_3.duration = 450
            anim1_3.interpolator = OvershootInterpolator(1.5f)
            anim1_3.addUpdateListener { animation ->
                mHourScale = animation.animatedValue as Float
                invalidate()
            }
            val anim2_1 = ValueAnimator.ofFloat(-30f, 0f)
            anim2_1.startDelay = 90
            anim2_1.duration = 300
            anim2_1.interpolator = AccelerateDecelerateInterpolator()
            anim2_1.addUpdateListener { animation ->
                mMinuteOffsetDegree = animation.animatedValue as Float
            }
            val anim2_2 = ValueAnimator.ofInt(0, 255)
            anim2_2.startDelay = 90
            anim2_2.duration = 300
            anim2_2.addUpdateListener { animation -> mMinuteAlpha = animation.animatedValue as Int }
            val anim2_3 = ValueAnimator.ofFloat(0.2f, 1f)
            anim2_3.duration = 390
            anim2_3.interpolator = OvershootInterpolator(1.5f)
            anim2_3.addUpdateListener { animation ->
                mMinuteScale = animation.animatedValue as Float
                invalidate()
            }
            val anim3_1 = ValueAnimator.ofFloat(-30f, 0f)
            anim3_1.startDelay = 150
            anim3_1.duration = 300
            anim3_1.addUpdateListener { animation ->
                mSecondOffsetDegree = animation.animatedValue as Float
            }
            val anim3_2 = ValueAnimator.ofInt(0, 255)
            anim3_2.startDelay = 150
            anim3_2.duration = 300
            anim3_2.addUpdateListener { animation -> mSecondAlpha = animation.animatedValue as Int }
            val anim3_3 = ValueAnimator.ofFloat(0.2f, 1f)
            anim3_3.startDelay = 150
            anim3_3.duration = 330
            anim3_3.addUpdateListener { animation ->
                mSecondScale = animation.animatedValue as Float
                invalidate()
            }
            val anim4_1 = ValueAnimator.ofInt(0, 255)
            anim4_1.duration = 150
            anim4_1.addUpdateListener { animation -> mAxleAlpha = animation.animatedValue as Int }
            val anim5_1 = ValueAnimator.ofInt(0, 255)
            anim5_1.startDelay = 450
            anim5_1.duration = 300
            anim5_1.addUpdateListener { animation ->
                mScaleTextAlpha = animation.animatedValue as Int
                invalidate()
            }
            val anim6_1 = ValueAnimator.ofInt(0, 255)
            anim6_1.duration = 300
            anim6_1.addUpdateListener { animation -> mDialAlpha = animation.animatedValue as Int }
            val anim6_2 = ValueAnimator.ofFloat(0.2f, 1f)
            anim6_2.duration = 450
            anim6_2.interpolator = OvershootInterpolator(1.5f)
            anim6_2.addUpdateListener { animation -> setDialRadius(animation.animatedValue as Float * mInitDialRadius) }
            val anim7_1 = ValueAnimator.ofInt(0, 255)
            anim7_1.startDelay = 150
            anim7_1.duration = 300
            anim7_1.addUpdateListener { animation ->
                mSubDialAlpha = animation.animatedValue as Int
            }
            val anim7_2 = ValueAnimator.ofFloat(0.2f, 1f)
            anim7_2.duration = 450
            anim7_2.startDelay = 150
            anim7_2.interpolator = OvershootInterpolator()
            anim7_2.addUpdateListener { animation ->
                setSubDialRadius(animation.animatedValue as Float * mInitSubDialRadius)
                invalidate()
            }
            animSet.playTogether(anim1_1, anim1_2, anim1_3,
                anim2_1, anim2_2, anim2_3,
                anim3_1, anim3_2, anim3_3,
                anim4_1, anim5_1, anim6_1,
                anim6_2, anim7_1, anim7_2
            )
            return animSet
        }
    override val hideAnimSet: AnimatorSet
        protected get() {
            val animSet = AnimatorSet()
            val anim1_1 = ValueAnimator.ofFloat(0f, -30f)
            anim1_1.duration = 240
            anim1_1.interpolator = AccelerateDecelerateInterpolator()
            anim1_1.addUpdateListener { animation ->
                mHourOffsetDegree = animation.animatedValue as Float
                invalidate()
            }
            val anim1_2 = ValueAnimator.ofInt(255, 0)
            anim1_2.duration = 240
            anim1_2.addUpdateListener { animation -> mHourAlpha = animation.animatedValue as Int }
            val anim1_3 = ValueAnimator.ofFloat(1f, 1.04f)
            anim1_3.startDelay = 180
            anim1_3.duration = 60
            anim1_3.addUpdateListener { animation -> mHourScale = animation.animatedValue as Float }
            val anim2_1 = ValueAnimator.ofFloat(0f, -30f)
            anim2_1.startDelay = 60
            anim2_1.duration = 240
            anim2_1.interpolator = AccelerateDecelerateInterpolator()
            anim2_1.addUpdateListener { animation ->
                mMinuteOffsetDegree = animation.animatedValue as Float
                invalidate()
            }
            val anim2_2 = ValueAnimator.ofInt(255, 0)
            anim2_2.startDelay = 60
            anim2_2.duration = 240
            anim2_2.addUpdateListener { animation -> mMinuteAlpha = animation.animatedValue as Int }
            val anim2_3 = ValueAnimator.ofFloat(1f, 1.04f, 0.945f)
            anim2_3.startDelay = 60
            anim2_3.duration = 120
            anim2_3.addUpdateListener { animation ->
                mMinuteScale = animation.animatedValue as Float
            }
            val anim3_1 = ValueAnimator.ofFloat(0f, -30f)
            anim3_1.startDelay = 120
            anim3_1.duration = 240
            anim3_1.interpolator = AccelerateDecelerateInterpolator()
            anim3_1.addUpdateListener { animation ->
                mSecondOffsetDegree = animation.animatedValue as Float
                invalidate()
            }
            val anim3_2 = ValueAnimator.ofInt(255, 0)
            anim3_2.startDelay = 120
            anim3_2.duration = 240
            anim3_2.addUpdateListener { animation -> mSecondAlpha = animation.animatedValue as Int }
            val anim3_3 = ValueAnimator.ofFloat(1f, 1.04f, 0.654f)
            anim3_3.startDelay = 120
            anim3_3.duration = 240
            anim3_3.interpolator = DecelerateInterpolator()
            anim3_3.addUpdateListener { animation ->
                mSecondScale = animation.animatedValue as Float
            }
            val anim4_1 = ValueAnimator.ofInt(255, 0)
            anim4_1.startDelay = 390
            anim4_1.duration = 150
            anim4_1.addUpdateListener { animation ->
                mAxleAlpha = animation.animatedValue as Int
                invalidate()
            }
            anim4_1.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    mAxlePaint!!.setShadowLayer(dp2px(2f).toFloat(),
                        dp2px(2f).toFloat(),
                        dp2px(2f).toFloat(),
                        mColorShadow)
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
            val anim5_1 = ValueAnimator.ofInt(255, 0)
            anim5_1.duration = 150
            anim5_1.addUpdateListener { animation ->
                mScaleTextAlpha = animation.animatedValue as Int
                invalidate()
            }
            val anim6_1 = ValueAnimator.ofInt(255, 0)
            anim6_1.startDelay = 180
            anim6_1.duration = 300
            anim6_1.addUpdateListener { animation -> mDialAlpha = animation.animatedValue as Int }
            val anim6_2 = ValueAnimator.ofFloat(1f, 0.2f)
            anim6_2.startDelay = 180
            anim6_2.duration = 360
            anim6_2.interpolator = AnticipateInterpolator()
            anim6_2.addUpdateListener { animation ->
                setDialRadius(mInitDialRadius * animation.animatedValue as Float)
                invalidate()
            }
            val anim7_1 = ValueAnimator.ofInt(255, 0)
            anim7_1.startDelay = 90
            anim7_1.duration = 300
            anim7_1.addUpdateListener { animation ->
                mSubDialAlpha = animation.animatedValue as Int
            }
            val anim7_2 = ValueAnimator.ofFloat(1f, 0.2f)
            anim7_2.startDelay = 90
            anim7_2.duration = 300
            anim7_2.interpolator = AnticipateInterpolator()
            anim7_2.addUpdateListener { animation ->
                setSubDialRadius(mInitSubDialRadius * animation.animatedValue as Float)
                invalidate()
            }
            animSet.playTogether(anim1_1, anim1_2, anim1_3,
                anim2_1, anim2_2, anim2_3,
                anim3_1, anim3_2, anim3_3,
                anim4_1, anim5_1, anim6_1,
                anim6_2, anim7_1, anim7_2)
            return animSet
        }

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.GMAlarm)
        mTimeZone = array.getString(R.styleable.GMAlarm_alarmTimeZone)
        mDialWidth =
            array.getDimensionPixelSize(R.styleable.GMAlarm_alarmDialWidth, dp2px(1f)).toFloat()
        mDialLength =
            array.getDimensionPixelSize(R.styleable.GMAlarm_alarmDialLength, dp2px(8f)).toFloat()
        mSubDialWidth =
            array.getDimensionPixelSize(R.styleable.GMAlarm_alarmSubDialWidth, dp2px(1.4f))
                .toFloat()
        mSubDialLength =
            array.getDimensionPixelSize(R.styleable.GMAlarm_alarmSubDialLength, dp2px(12f))
                .toFloat()
        mAxleRadius =
            array.getDimensionPixelSize(R.styleable.GMAlarm_alarmAxleRadius, dp2px(5f)).toFloat()
        mColorAxle = array.getColor(R.styleable.GMAlarm_alarmAxleColor, -0xcb6d1a)
        mColorSecondHand = array.getColor(R.styleable.GMAlarm_alarmSecondHandColor, -0xcb6d1a)
        mColorMinuteHand = array.getColor(R.styleable.GMAlarm_alarmMinuteHandColor, -0x34000000)
        mColorHourHand = array.getColor(R.styleable.GMAlarm_alarmHourHandColor, -0x34000000)
        mColorShadow = array.getColor(R.styleable.GMAlarm_alarmShadowColor, 0x30000000)
        mColorSubDial = array.getColor(R.styleable.GMAlarm_alarmSubDialColor, -0xb5b5b6)
        mSecondHandWidth =
            array.getColor(R.styleable.GMAlarm_alarmSecondHandWidth, dp2px(1f)).toFloat()
        mMinuteHandWidth =
            array.getColor(R.styleable.GMAlarm_alarmMinuteHandWidth, dp2px(3.8f)).toFloat()
        mHourHandWidth =
            array.getColor(R.styleable.GMAlarm_alarmSecondHandWidth, dp2px(4.5f)).toFloat()
        mColorDialStart = array.getColor(R.styleable.GMAlarm_alarmDialStartColor, -0xac7101)
        mColorDialEnd = array.getColor(R.styleable.GMAlarm_alarmDialEndColor, -0xf8ab01)
        mColorUncheckedDial = array.getColor(R.styleable.GMAlarm_alarmDialUncheckedColor, -0x737374)
        mScaleTextSize =
            array.getDimensionPixelSize(R.styleable.GMAlarm_alarmScaleTextSize, sp2px(16f))
        mScaleTextColor =
            array.getDimensionPixelSize(R.styleable.GMAlarm_alarmScaleTextColor, -0xb5b5b6)
        mScaleTextMargin =
            array.getDimensionPixelSize(R.styleable.GMAlarm_alarmScaleTextMargin, dp2px(8f))
        updateDialColors()
        array.recycle()
        isStarted = true
        initPaints()
    }
}