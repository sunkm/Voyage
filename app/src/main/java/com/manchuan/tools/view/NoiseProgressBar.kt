package com.manchuan.tools.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import com.lxj.androidktx.core.color
import com.manchuan.tools.R
import com.manchuan.tools.extensions.accentColor
import com.manchuan.tools.extensions.errorColor

/**
 * colorful arc progress bar
 * Created by shinelw on 12/4/15.
 */
class NoiseProgressBar : View {
    private val mWidth = 0
    private val mHeight = 0

    //直径
    private var diameter = 500
    private var bgRect: RectF? = null

    //圆心
    private var centerX = 0f
    private var centerY = 0f
    private var allArcPaint: Paint? = null
    private var progressPaint: Paint? = null
    private var vTextPaint: Paint? = null
    private var hintPaint: Paint? = null
    private var degreePaint: Paint? = null
    private var curSpeedPaint: Paint? = null
    private val startAngle = 135f
    private val sweepAngle = 270f
    private var currentAngle = 0f
    private var lastAngle = 0f
    private var colors = intArrayOf(Color.GREEN, Color.YELLOW, Color.RED, Color.RED)
    private var progressAnimator: ValueAnimator? = null
    private var maxValues = 60f
    private var curValues = 0f
    private var bgArcWidth = dipToPx(2f)
    private var progressWidth = dipToPx(10f)
    private var textSize = dipToPx(60f)
    private var hintSize = dipToPx(22f)
    private val curSpeedSize = dipToPx(13f)
    private val aniSpeed = 1000
    private val longdegree = dipToPx(13f)
    private val shortdegree = dipToPx(5f)
    private val DEGREE_PROGRESS_DISTANCE = dipToPx(8f)
    private val hintColor = "#448AFF"
    private val longDegreeColor = "#d2d2d2"
    private val shortDegreeColor = "#adadad"
    private val bgArcColor = "#111111"
    private var isShowCurrentSpeed = true
    private var hintString = "分贝"

    // sweepAngle / maxValues 的值
    private var k = 0f

    private var contexts: Context? = null

    constructor(context: Context?) : super(context) {
        this.contexts = context
        initView()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        this.contexts = context
        initView()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.contexts = context
        initView()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = 2 * longdegree + progressWidth + diameter + 2 * DEGREE_PROGRESS_DISTANCE
        val height = 2 * longdegree + progressWidth + diameter + 2 * DEGREE_PROGRESS_DISTANCE
        setMeasuredDimension(width, height)
    }

    private fun initView() {
        colors = intArrayOf(Color.GREEN, Color.YELLOW, context.errorColor(), Color.RED)
        diameter = 3 * screenWidth / 5
        //弧形的矩阵区域
        bgRect = RectF()
        bgRect!!.top = (longdegree + progressWidth / 2 + DEGREE_PROGRESS_DISTANCE).toFloat()
        bgRect!!.left = (longdegree + progressWidth / 2 + DEGREE_PROGRESS_DISTANCE).toFloat()
        bgRect!!.right =
            (diameter + (longdegree + progressWidth / 2 + DEGREE_PROGRESS_DISTANCE)).toFloat()
        bgRect!!.bottom =
            (diameter + (longdegree + progressWidth / 2 + DEGREE_PROGRESS_DISTANCE)).toFloat()

        //圆心
        centerX =
            ((2 * longdegree + progressWidth + diameter + 2 * DEGREE_PROGRESS_DISTANCE) / 2).toFloat()
        centerY =
            ((2 * longdegree + progressWidth + diameter + 2 * DEGREE_PROGRESS_DISTANCE) / 2).toFloat()

        //外部刻度线
        degreePaint = Paint()
        degreePaint!!.color = Color.parseColor(longDegreeColor)

        //整个弧形
        allArcPaint = Paint()
        allArcPaint!!.isAntiAlias = true
        allArcPaint!!.style = Paint.Style.STROKE
        allArcPaint!!.strokeWidth = bgArcWidth.toFloat()
        allArcPaint!!.color = Color.parseColor(bgArcColor)
        allArcPaint!!.strokeCap = Paint.Cap.ROUND

        //当前进度的弧形
        progressPaint = Paint()
        progressPaint!!.isAntiAlias = true
        progressPaint!!.style = Paint.Style.STROKE
        progressPaint!!.strokeCap = Paint.Cap.ROUND
        progressPaint!!.strokeWidth = progressWidth.toFloat()
        progressPaint!!.color = context.color(R.color.green)

        //当前速度显示文字
        vTextPaint = Paint()
        vTextPaint!!.textSize = textSize.toFloat()
        vTextPaint!!.color = context.accentColor()
        vTextPaint!!.typeface = Typeface.create("sans", Typeface.NORMAL)
        vTextPaint!!.textAlign = Paint.Align.CENTER

        //显示“km/h”文字
        hintPaint = Paint()
        hintPaint!!.textSize = hintSize.toFloat()
        hintPaint!!.color = context.accentColor()
        hintPaint!!.textAlign = Paint.Align.CENTER

        //显示“km/h”文字
        curSpeedPaint = Paint()
        curSpeedPaint!!.textSize = curSpeedSize.toFloat()
        curSpeedPaint!!.color = context.accentColor()
        curSpeedPaint!!.textAlign = Paint.Align.CENTER
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {

        //抗锯齿
        canvas.drawFilter = PaintFlagsDrawFilter(
            0,
            Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG
        )

        //画刻度线
        for (i in 0..39) {
            if (i in 16..24) {
                canvas.rotate(9f, centerX, centerY)
                continue
            }
            if (i % 5 == 0) {
                degreePaint!!.strokeWidth = dipToPx(2f).toFloat()
                degreePaint!!.color = Color.parseColor(longDegreeColor)
                canvas.drawLine(
                    centerX,
                    centerY - diameter / 2 - progressWidth / 2 - DEGREE_PROGRESS_DISTANCE,
                    centerX,
                    centerY - diameter / 2 - progressWidth / 2 - DEGREE_PROGRESS_DISTANCE - longdegree,
                    degreePaint!!
                )
            } else {
                degreePaint!!.strokeWidth = dipToPx(1.4f).toFloat()
                degreePaint!!.color = Color.parseColor(shortDegreeColor)
                canvas.drawLine(
                    centerX,
                    centerY - diameter / 2 - progressWidth / 2 - DEGREE_PROGRESS_DISTANCE - (longdegree - shortdegree) / 2,
                    centerX,
                    centerY - diameter / 2 - progressWidth / 2 - DEGREE_PROGRESS_DISTANCE - (longdegree - shortdegree) / 2 - shortdegree,
                    degreePaint!!
                )
            }
            canvas.rotate(9f, centerX, centerY)
        }

        //整个弧
        canvas.drawArc(bgRect!!, startAngle, sweepAngle, false, allArcPaint!!)

        //设置渐变色
        val sweepGradient = SweepGradient(centerX, centerY, colors, null)
        val matrix = Matrix()
        matrix.setRotate(130f, centerX, centerY)
        sweepGradient.setLocalMatrix(matrix)
        progressPaint!!.shader = sweepGradient

        //当前进度
        canvas.drawArc(bgRect!!, startAngle, currentAngle, false, progressPaint!!)
        if (isShowCurrentSpeed) {
            canvas.drawText(curValues.toInt().toString(), centerX, centerY + textSize / 3, vTextPaint!!)
        }
        canvas.drawText(hintString, centerX, centerY + 2 * textSize / 2, hintPaint!!)
        canvas.drawText("噪音分贝值", centerX, centerY - 2 * textSize / 3, curSpeedPaint!!)
        invalidate()
    }

    /**
     * 设置最大值
     * @param maxValues
     */
    fun setMaxValues(maxValues: Float) {
        this.maxValues = maxValues
        k = sweepAngle / maxValues
    }
    fun setMaxValues(maxValues: Int) {
        this.maxValues = maxValues.toFloat()
        k = sweepAngle / maxValues
    }

    /**
     * 设置当前值
     * @param currentValues
     */
    fun setCurrentValues(currentValues: Double) {
        var currentValues = currentValues
        if (currentValues > maxValues) {
            currentValues = maxValues.toDouble()
        }
        if (currentValues < 0) {
            currentValues = 0.0
        }
        curValues = currentValues.toFloat()
        lastAngle = currentAngle
        setAnimation(lastAngle, currentValues.toFloat() * k, aniSpeed)
    }

    fun setCurrentValues(currentValues: Float) {
        var currentValues = currentValues
        if (currentValues > maxValues) {
            currentValues = maxValues
        }
        if (currentValues < 0) {
            currentValues = 0f
        }
        curValues = currentValues
        lastAngle = currentAngle
        setAnimation(lastAngle, currentValues * k, aniSpeed)
    }


    fun setCurrentValues(currentValues: Int) {
        var currentValues = currentValues
        if (currentValues > maxValues) {
            currentValues = maxValues.toInt()
        }
        if (currentValues < 0) {
            currentValues = 0
        }
        curValues = currentValues.toFloat()
        lastAngle = currentAngle
        setAnimation(lastAngle, currentValues * k, aniSpeed)
    }

    /**
     * 设置整个圆弧宽度
     * @param bgArcWidth
     */
    fun setBgArcWidth(bgArcWidth: Int) {
        this.bgArcWidth = bgArcWidth
    }

    /**
     * 设置进度宽度
     * @param progressWidth
     */
    fun setProgressWidth(progressWidth: Int) {
        this.progressWidth = progressWidth
    }

    /**
     * 设置速度文字大小
     * @param textSize
     */
    fun setTextSize(textSize: Int) {
        this.textSize = textSize
    }

    /**
     * 设置单位文字大小
     * @param hintSize
     */
    fun setHintSize(hintSize: Int) {
        this.hintSize = hintSize
    }

    /**
     * 设置单位文字
     * @param hintString
     */
    fun setUnit(hintString: String) {
        this.hintString = hintString
        invalidate()
    }

    /**
     * 设置直径大小
     * @param diameter
     */
    fun setDiameter(diameter: Int) {
        this.diameter = dipToPx(diameter.toFloat())
    }

    fun setAniSpeed() {}

    /**
     * 为进度设置动画
     * @param last
     * @param current
     */
    private fun setAnimation(last: Float, current: Float, length: Int) {
        progressAnimator = ValueAnimator.ofFloat(last, current)
        progressAnimator?.duration = 100
        progressAnimator?.setTarget(currentAngle)
        progressAnimator?.addUpdateListener { animation ->
            currentAngle = animation.animatedValue as Float
        }
        progressAnimator?.start()
    }

    /**
     * dip 转换成px
     * @param dip
     * @return
     */
    private fun dipToPx(dip: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dip * density + 0.5f * if (dip >= 0) 1 else -1).toInt()
    }

    /**
     * 得到屏幕宽度
     * @return
     */
    private val screenWidth: Int
        private get() {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            return displayMetrics.widthPixels
        }

    fun setIsShowCurrentSpeed(isShowCurrentSpeed: Boolean) {
        this.isShowCurrentSpeed = isShowCurrentSpeed
    }

    /**
     * 初始加载页面时设置加载动画
     */
    fun setDefaultWithAnimator() {
        setAnimation(sweepAngle, currentAngle, 2000)
    }
}