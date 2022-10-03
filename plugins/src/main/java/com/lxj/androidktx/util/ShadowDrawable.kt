package com.lxj.androidktx.util

import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.graphics.drawable.DrawableCompat
import kotlin.math.ceil

class ShadowDrawable(
    innerDrawable: Drawable?, shadowColor: Int, radius: Float,
    shadowSize: Float, maxShadowSize: Float,
) : Drawable(), Drawable.Callback {
    private val mInsetShadow // extra shadow to avoid gaps between card and shadow
            : Int
    private val mPaint: Paint
    private val mCornerShadowPaint: Paint
    private val mEdgeShadowPaint: Paint
    private val mCardBounds: RectF
    private var mCornerRadius = 1f
    private var mCornerShadowPath: Path? = null

    // actual value set by developer
    private var mRawMaxShadowSize = 0f

    // multiplied value to account for shadow offset
    private var mShadowSize = 0f

    // actual value set by developer
    private var mRawShadowSize = 0f
    val color: ColorStateList? = null
    private var mDirty = true
    private val mShadowStartColor: Int
    private val mShadowEndColor: Int
    private var mAddPaddingForCorners = true

    /**
     * If shadow size is set to a value above max shadow, we print a warning
     */
    private var mPrintedShadowClipWarning = false
    private var mDrawable: Drawable? = null

    constructor(
        innerDrawable: Drawable?, radius: Float,
        shadowSize: Float,
    ) : this(innerDrawable, 0, radius, shadowSize, shadowSize) {
    }

    fun setWrappedDrawable(drawable: Drawable?) {
        if (mDrawable != null) {
            mDrawable!!.callback = null as Callback?
        }
        mDrawable = drawable
        if (drawable != null) {
            drawable.callback = this
        }
    }
    //    private void setBackground(ColorStateList color) {
    //        mBackground = (color == null) ?  ColorStateList.valueOf(Color.TRANSPARENT) : color;
    //        mPaint.setColor(mBackground.getColorForState(getState(), mBackground.getDefaultColor()));
    //    }
    /**
     * Casts the value to an even integer.
     */
    private fun toEven(value: Float): Int {
        val i = (value + .5f).toInt()
        return if (i % 2 == 1) {
            i - 1
        } else i
    }

    fun setAddPaddingForCorners(addPaddingForCorners: Boolean) {
        mAddPaddingForCorners = addPaddingForCorners
        invalidateSelf()
    }

    override fun setAlpha(alpha: Int) {
        mDrawable!!.alpha = alpha
        mPaint.alpha = alpha
        mCornerShadowPaint.alpha = alpha
        mEdgeShadowPaint.alpha = alpha
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        //        this.mDrawable.setBounds(bounds);
        mDirty = true
    }

    override fun setChangingConfigurations(configs: Int) {
        mDrawable!!.changingConfigurations = configs
    }

    override fun getChangingConfigurations(): Int {
        return mDrawable!!.changingConfigurations
    }

    private fun setShadowSize(shadowSize: Float, maxShadowSize: Float) {
        var shadowSize = shadowSize
        var maxShadowSize = maxShadowSize
        require(shadowSize >= 0f) {
            ("Invalid shadow size $shadowSize. Must be >= 0")
        }
        require(maxShadowSize >= 0f) {
            ("Invalid max shadow size $maxShadowSize. Must be >= 0")
        }
        shadowSize = toEven(shadowSize).toFloat()
        maxShadowSize = toEven(maxShadowSize).toFloat()
        if (shadowSize > maxShadowSize) {
            shadowSize = maxShadowSize
            if (!mPrintedShadowClipWarning) {
                mPrintedShadowClipWarning = true
            }
        }
        if (mRawShadowSize == shadowSize && mRawMaxShadowSize == maxShadowSize) {
            return
        }
        mRawShadowSize = shadowSize
        mRawMaxShadowSize = maxShadowSize
        mShadowSize = (shadowSize * SHADOW_MULTIPLIER + mInsetShadow + .5f).toInt().toFloat()
        mDirty = true
        invalidateSelf()
    }

    override fun getPadding(padding: Rect): Boolean {
        val vOffset = ceil(calculateVerticalPadding(mRawMaxShadowSize,
            mCornerRadius,
            mAddPaddingForCorners).toDouble()).toInt()
        val hOffset = ceil(calculateHorizontalPadding(mRawMaxShadowSize,
            mCornerRadius,
            mAddPaddingForCorners).toDouble()).toInt()
        padding[hOffset, vOffset, hOffset] = vOffset
        return true
    }

    override fun onStateChange(stateSet: IntArray): Boolean {
        val newColor = color!!.getColorForState(stateSet, color.defaultColor)
        if (mPaint.color == newColor) {
            return false
        }
        mPaint.color = newColor
        mDirty = true
        invalidateSelf()
        return true
    }

    override fun isStateful(): Boolean {
        return color != null && color.isStateful || mDrawable!!.isStateful
    }

    override fun setState(stateSet: IntArray): Boolean {
        return mDrawable!!.setState(stateSet)
    }

    override fun getState(): IntArray {
        return mDrawable!!.state
    }

    override fun setColorFilter(cf: ColorFilter?) {
        mDrawable!!.colorFilter = cf
        mPaint.colorFilter = cf
    }

    override fun jumpToCurrentState() {
        DrawableCompat.jumpToCurrentState(mDrawable!!)
    }

    override fun getCurrent(): Drawable {
        return mDrawable!!.current
    }

    override fun setVisible(visible: Boolean, restart: Boolean): Boolean {
        return super.setVisible(visible, restart) || mDrawable!!.setVisible(visible, restart)
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun getIntrinsicWidth(): Int {
        val rect = Rect()
        getPadding(rect)
        return mDrawable!!.intrinsicWidth - rect.width()
    }

    override fun getIntrinsicHeight(): Int {
        val rect = Rect()
        getPadding(rect)
        return mDrawable!!.intrinsicHeight - rect.height()
    }

    override fun getMinimumWidth(): Int {
        val rect = Rect()
        getPadding(rect)
        return mDrawable!!.minimumWidth - rect.width()
    }

    override fun getMinimumHeight(): Int {
        val rect = Rect()
        getPadding(rect)
        return mDrawable!!.minimumHeight - rect.height()
    }

    override fun getTransparentRegion(): Region? {
        return mDrawable!!.transparentRegion
    }

    override fun draw(canvas: Canvas) {
        if (mDirty) {
            buildComponents(bounds)
            mDirty = false
        }
        //        canvas.translate(0, mRawShadowSize / 2);
        drawShadow(canvas)
        //        canvas.translate(0, -mRawShadowSize / 2);
        drawRoundRect(canvas, mCardBounds, mCornerRadius, mPaint)
        mDrawable!!.draw(canvas)
    }

    private fun drawShadow(canvas: Canvas) {
        val edgeShadowTop = -mCornerRadius - mShadowSize
        val inset = mCornerRadius + mInsetShadow + mRawShadowSize / 2
        val drawHorizontalEdges = mCardBounds.width() - 2 * inset > 0
        val drawVerticalEdges = mCardBounds.height() - 2 * inset > 0
        // LT
        var saved = canvas.save()
        canvas.translate(mCardBounds.left + inset, mCardBounds.top + inset)
        canvas.drawPath(mCornerShadowPath!!, mCornerShadowPaint)
        if (drawHorizontalEdges) {
            canvas.drawRect(0f,
                edgeShadowTop,
                mCardBounds.width() - 2 * inset,
                -mCornerRadius,
                mEdgeShadowPaint)
        }
        canvas.restoreToCount(saved)
        // RB
        saved = canvas.save()
        canvas.translate(mCardBounds.right - inset, mCardBounds.bottom - inset)
        canvas.rotate(180f)
        canvas.drawPath(mCornerShadowPath!!, mCornerShadowPaint)
        if (drawHorizontalEdges) {
            canvas.drawRect(0f,
                edgeShadowTop,
                mCardBounds.width() - 2 * inset,
                -mCornerRadius + mShadowSize,
                mEdgeShadowPaint)
        }
        canvas.restoreToCount(saved)
        // LB
        saved = canvas.save()
        canvas.translate(mCardBounds.left + inset, mCardBounds.bottom - inset)
        canvas.rotate(270f)
        canvas.drawPath(mCornerShadowPath!!, mCornerShadowPaint)
        if (drawVerticalEdges) {
            canvas.drawRect(0f,
                edgeShadowTop,
                mCardBounds.height() - 2 * inset,
                -mCornerRadius,
                mEdgeShadowPaint)
        }
        canvas.restoreToCount(saved)
        // RT
        saved = canvas.save()
        canvas.translate(mCardBounds.right - inset, mCardBounds.top + inset)
        canvas.rotate(90f)
        canvas.drawPath(mCornerShadowPath!!, mCornerShadowPaint)
        if (drawVerticalEdges) {
            canvas.drawRect(0f,
                edgeShadowTop,
                mCardBounds.height() - 2 * inset,
                -mCornerRadius,
                mEdgeShadowPaint)
        }
        canvas.restoreToCount(saved)
    }

    private fun buildShadowCorners() {
        val innerBounds = RectF(-mCornerRadius, -mCornerRadius, mCornerRadius, mCornerRadius)
        val outerBounds = RectF(innerBounds)
        outerBounds.inset(-mShadowSize, -mShadowSize)
        if (mCornerShadowPath == null) {
            mCornerShadowPath = Path()
        } else {
            mCornerShadowPath!!.reset()
        }
        mCornerShadowPath!!.fillType = Path.FillType.EVEN_ODD
        mCornerShadowPath!!.moveTo(-mCornerRadius, 0f)
        mCornerShadowPath!!.rLineTo(-mShadowSize, 0f)
        // outer arc
        mCornerShadowPath!!.arcTo(outerBounds, 180f, 90f, false)
        // inner arc
        mCornerShadowPath!!.arcTo(innerBounds, 270f, -90f, false)
        mCornerShadowPath!!.close()
        val startRatio = mCornerRadius / (mCornerRadius + mShadowSize)
        mCornerShadowPaint.shader = RadialGradient(0f,
            0f,
            mCornerRadius + mShadowSize,
            intArrayOf(mShadowStartColor, mShadowStartColor, mShadowEndColor),
            floatArrayOf(0f, startRatio, 1f),
            Shader.TileMode.CLAMP)

        // we offset the content shadowSize/2 pixels up to make it more realistic.
        // this is why edge shadow shader has some extra space
        // When drawing bottom edge shadow, we use that extra space.
        mEdgeShadowPaint.shader = LinearGradient(0f,
            -mCornerRadius + mShadowSize,
            0f,
            -mCornerRadius - mShadowSize,
            intArrayOf(mShadowStartColor, mShadowStartColor, mShadowEndColor),
            floatArrayOf(0f, .5f, 1f),
            Shader.TileMode.CLAMP)
        mEdgeShadowPaint.isAntiAlias = false
    }

    private fun buildComponents(bounds: Rect) {
        // Card is offset SHADOW_MULTIPLIER * maxShadowSize to account for the shadow shift.
        // We could have different top-bottom offsets to avoid extra gap above but in that case
        // center aligning Views inside the CardView would be problematic.
        val verticalOffset = mRawMaxShadowSize * SHADOW_MULTIPLIER
        mCardBounds[bounds.left + mRawMaxShadowSize, bounds.top + verticalOffset, bounds.right - mRawMaxShadowSize] =
            bounds.bottom - verticalOffset
        mDrawable!!.setBounds(mCardBounds.left.toInt(),
            mCardBounds.top.toInt(),
            mCardBounds.right.toInt(),
            mCardBounds.bottom.toInt())
        buildShadowCorners()
    }

    val mCornerRect = RectF()

    init {
        mShadowStartColor = if (shadowColor == 0) Color.parseColor("#37000000") else shadowColor
        mShadowEndColor = Color.parseColor("#01000000")
        mInsetShadow = 0
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
        mPaint.color = Color.TRANSPARENT
        mCornerShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
        mCornerShadowPaint.style = Paint.Style.FILL
        mCornerRadius = 1.coerceAtLeast((radius + .5f).toInt()).toFloat()
        mCardBounds = RectF()
        mEdgeShadowPaint = Paint(mCornerShadowPaint)
        mEdgeShadowPaint.isAntiAlias = false
        setShadowSize(shadowSize, maxShadowSize)
        setWrappedDrawable(innerDrawable)
    }

    fun drawRoundRect(canvas: Canvas, bounds: RectF, cornerRadius: Float, paint: Paint?) {
        val twoRadius = cornerRadius * 2.0f
        val innerWidth = bounds.width() - twoRadius - 1.0f
        val innerHeight = bounds.height() - twoRadius - 1.0f
        if (cornerRadius >= 1.0f) {
            val roundedCornerRadius = cornerRadius + 0.5f
            mCornerRect[-roundedCornerRadius, -roundedCornerRadius, roundedCornerRadius] =
                roundedCornerRadius
            val saved = canvas.save()
            canvas.translate(bounds.left + roundedCornerRadius, bounds.top + roundedCornerRadius)
            canvas.drawArc(mCornerRect, 180.0f, 90.0f, true, paint!!)
            canvas.translate(innerWidth, 0.0f)
            canvas.rotate(90.0f)
            canvas.drawArc(mCornerRect, 180.0f, 90.0f, true, paint)
            canvas.translate(innerHeight, 0.0f)
            canvas.rotate(90.0f)
            canvas.drawArc(mCornerRect, 180.0f, 90.0f, true, paint)
            canvas.translate(innerWidth, 0.0f)
            canvas.rotate(90.0f)
            canvas.drawArc(mCornerRect, 180.0f, 90.0f, true, paint)
            canvas.restoreToCount(saved)
            canvas.drawRect(bounds.left + roundedCornerRadius - 1.0f,
                bounds.top,
                bounds.right - roundedCornerRadius + 1.0f,
                bounds.top + roundedCornerRadius,
                paint)
            canvas.drawRect(bounds.left + roundedCornerRadius - 1.0f,
                bounds.bottom - roundedCornerRadius,
                bounds.right - roundedCornerRadius + 1.0f,
                bounds.bottom,
                paint)
        }
        canvas.drawRect(bounds.left,
            bounds.top + cornerRadius,
            bounds.right,
            bounds.bottom - cornerRadius,
            paint!!)
    }

    var cornerRadius: Float
        get() = mCornerRadius
        set(radius) {
            var radius = radius
            require(radius >= 0f) { "Invalid radius $radius. Must be >= 0" }
            radius = (radius + .5f).toInt().toFloat()
            if (mCornerRadius == radius) {
                return
            }
            mCornerRadius = radius
            mDirty = true
            invalidateSelf()
        }

    fun getMaxShadowAndCornerPadding(into: Rect) {
        getPadding(into)
    }

    var shadowSize: Float
        get() = mRawShadowSize
        set(size) {
            setShadowSize(size, mRawMaxShadowSize)
        }
    var maxShadowSize: Float
        get() = mRawMaxShadowSize
        set(size) {
            setShadowSize(mRawShadowSize, size)
        }
    val minWidth: Float
        get() {
            val content = (2 * Math.max(mRawMaxShadowSize,
                mCornerRadius + mInsetShadow + mRawMaxShadowSize / 2))
            return content + (mRawMaxShadowSize + mInsetShadow) * 2
        }
    val minHeight: Float
        get() {
            val content = 2 * Math.max(mRawMaxShadowSize,
                mCornerRadius + mInsetShadow + mRawMaxShadowSize * SHADOW_MULTIPLIER / 2)
            return content + (mRawMaxShadowSize * SHADOW_MULTIPLIER + mInsetShadow) * 2
        }

    //    void setColor(@Nullable ColorStateList color) {
    //        setBackground(color);
    //        invalidateSelf();
    //    }
    override fun getOutline(outline: Outline) {
        super.getOutline(outline)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mDrawable!!.getOutline(outline)
        }
    }

    override fun setDither(dither: Boolean) {
        mDrawable!!.setDither(dither)
    }

    override fun setFilterBitmap(filter: Boolean) {
        mDrawable!!.isFilterBitmap = filter
    }

    override fun invalidateDrawable(who: Drawable) {
        invalidateSelf()
    }

    override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
        scheduleSelf(what, `when`)
    }

    override fun unscheduleDrawable(who: Drawable, what: Runnable) {
        unscheduleSelf(what)
    }

    override fun setAutoMirrored(mirrored: Boolean) {
        DrawableCompat.setAutoMirrored(mDrawable!!, mirrored)
    }

    override fun isAutoMirrored(): Boolean {
        return DrawableCompat.isAutoMirrored(mDrawable!!)
    }

    override fun setTint(tint: Int) {
        DrawableCompat.setTint(mDrawable!!, tint)
    }

    override fun setTintList(tint: ColorStateList?) {
        DrawableCompat.setTintList(mDrawable!!, tint)
    }

    override fun setTintMode(tintMode: PorterDuff.Mode?) {
        DrawableCompat.setTintMode(mDrawable!!, tintMode)
    }

    override fun setHotspot(x: Float, y: Float) {
        DrawableCompat.setHotspot(mDrawable!!, x, y)
    }

    override fun setHotspotBounds(left: Int, top: Int, right: Int, bottom: Int) {
        DrawableCompat.setHotspotBounds(mDrawable!!, left, top, right, bottom)
    }

    companion object {
        // used to calculate content padding
        private val COS_45 = Math.cos(Math.toRadians(45.0))
        private const val SHADOW_MULTIPLIER = 1.5f
        fun calculateVerticalPadding(
            maxShadowSize: Float, cornerRadius: Float,
            addPaddingForCorners: Boolean,
        ): Float {
            return if (addPaddingForCorners) {
                (maxShadowSize * SHADOW_MULTIPLIER + (1 - COS_45) * cornerRadius).toFloat()
            } else {
                maxShadowSize * SHADOW_MULTIPLIER
            }
        }

        fun calculateHorizontalPadding(
            maxShadowSize: Float, cornerRadius: Float,
            addPaddingForCorners: Boolean,
        ): Float {
            return if (addPaddingForCorners) {
                (maxShadowSize + (1 - COS_45) * cornerRadius).toFloat()
            } else {
                maxShadowSize
            }
        }
    }
}