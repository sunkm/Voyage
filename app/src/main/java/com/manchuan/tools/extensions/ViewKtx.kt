package com.manchuan.tools.extensions

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.res.Resources
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.*
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.viewpager2.widget.ViewPager2
import com.dylanc.longan.pxToSp
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.manchuan.tools.application.App
import com.manchuan.tools.utils.UiUtils

fun TextView.setTextOrHide(text: String?) {
    text?.let {
        this.text = it
    } ?: run {
        this.visibility = View.GONE
    }
}

fun LinearProgressIndicator.progress(progress: Int) = this.setProgressCompat(progress, true)

fun CircularProgressIndicator.progress(progress: Int) = this.setProgressCompat(progress, true)

/**
 * Setting new text to TextView with something like fade to alpha animation
 *
 * @property text - text to set to TextView
 * @property duration - animation final duration
 */
fun TextView.setTextWithAnimation(text: String, duration: Long? = 300) {
    val stepDuration = duration?.div(2)
    if (stepDuration != null) {
        this.animate().alpha(0f).setDuration(stepDuration).withEndAction {
            this.text = text
            this.animate().alpha(1f).setDuration(stepDuration).start()
        }.start()
    }
}

/**
 * Setting new text to TextView with something like fade to alpha animation
 *
 * @property text - text to set to TextView
 * @property duration - animation final duration
 */
fun TextView.text(text: String, duration: Long? = 300) {
    val stepDuration = duration?.div(2)
    if (stepDuration != null) {
        this.animate().alpha(0f).setDuration(stepDuration).withEndAction {
            this.text = text
            this.animate().alpha(1f).setDuration(stepDuration).start()
        }.start()
    }
}

fun TextView.hint(text: String, duration: Long? = 300) {
    val stepDuration = duration?.div(2)
    if (stepDuration != null) {
        this.animate().alpha(0f).setDuration(stepDuration).withEndAction {
            this.hint = text
            this.animate().alpha(1f).setDuration(stepDuration).start()
        }.start()
    }
}

fun View.gradientOfTopAndBottom(@ColorInt top: Int, @ColorInt bottom: Int) {
    val gradient =
        GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(top, bottom))
    this.background = gradient
}

fun View.gradientOfBottomAndTop(@ColorInt bottom: Int, @ColorInt top: Long) {
    val gradient =
        GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(bottom, top.toInt()))
    this.background = gradient
}


/**
 * Setting new text to TextView with something like width transition animation
 *
 * @property text - text to set to TextView
 * @property duration - animation final duration
 */
fun TextView.setTextWithTransition(text: String, animDuration: Long) {
    val with = this.width
    val thisText = this
    val textLayoutParams = this.layoutParams
    ValueAnimator.ofInt(with, 0).apply {
        addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Int
            val layoutParams: ViewGroup.LayoutParams = textLayoutParams
            layoutParams.width = value
            thisText.layoutParams = layoutParams
        }
        doOnEnd {
            thisText.text = text
            thisText.measure(0, 0)
            ValueAnimator.ofInt(0, thisText.measuredWidth).apply {
                addUpdateListener { valueAnimator ->
                    val value = valueAnimator.animatedValue as Int
                    val layoutParams: ViewGroup.LayoutParams = textLayoutParams
                    layoutParams.width = value
                    thisText.layoutParams = layoutParams
                }
                duration = animDuration
                interpolator = AccelerateDecelerateInterpolator()
            }.start()
        }
        duration = animDuration
        interpolator = AccelerateDecelerateInterpolator()
    }.start()
}

internal fun StaticLayout.textWidth(): Int {
    var width = 0f
    for (i in 0 until lineCount) {
        width = width.coerceAtLeast(getLineWidth(i))
    }
    return width.toInt()
}

/**
 * Linearly interpolate between two values.
 */
internal fun lerp(a: Float, b: Float, t: Float): Float {
    return a + (b - a) * t
}


fun TextView.setClickableText(
    clickableTextFragment: String,
    useUnderline: Boolean = false,
    onclickAction: () -> Unit,
) {
    text.indexOf(clickableTextFragment).takeIf { it >= 0 }?.let { startIndex ->
        val endIndex = startIndex + clickableTextFragment.length
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                onclickAction.invoke()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = useUnderline
            }
        }
        text = SpannableString(text).apply {
            setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        movementMethod = LinkMovementMethod.getInstance()
    } ?: throw Exception("Source TextView does not contain clickable text")
}

fun TextView.setStyledSpan(
    styledTextFragment: String,
    textColor: Int? = null,
    textSizeSp: Int? = null,
    isBoldText: Boolean = false,
) {
    text.indexOf(styledTextFragment).takeIf { it >= 0 }?.let { startIndex ->
        val endIndex = startIndex + styledTextFragment.length
        text = SpannableString(text).apply {
            textSizeSp?.let {
                setSpan(AbsoluteSizeSpan(it.pxToSp(), false),
                    startIndex,
                    endIndex,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            textColor?.let {
                setSpan(ForegroundColorSpan(it),
                    startIndex,
                    endIndex,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            if (isBoldText) setSpan(StyleSpan(Typeface.BOLD),
                startIndex,
                endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
}

val Number.dp: Int get() = (toInt() * Resources.getSystem().displayMetrics.density).toInt()

var View.paddingStartCompat: Int
    set(value) {
        setPadding(value, paddingTop, paddingEnd, paddingBottom)
    }
    get() = paddingStart

fun dpToPx(dp: Float): Int {
    return dpToPx(dp, App.instance.resources)
}

private fun dpToPx(dp: Float, resources: Resources): Int {
    val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
    return px.toInt()
}

fun getColorCompat(resId: Int) = ContextCompat.getColor(App.instance, resId)

fun View.setVisible() {
    visibility = View.VISIBLE
}

fun View.setInvisible() {
    visibility = View.INVISIBLE
}

fun View.setGone() {
    visibility = View.GONE
}

fun View.addPaddingStart(padding: Int) {
    setPadding(paddingStart + padding, paddingTop, paddingEnd, paddingBottom)
}

var View.paddingTopCompat: Int
    set(value) {
        setPadding(paddingStart, value, paddingEnd, paddingBottom)
    }
    get() = paddingTop

fun View.addPaddingTop(padding: Int) {
    setPadding(paddingStart, paddingTop + padding, paddingEnd, paddingBottom)
}

var View.paddingEndCompat: Int
    set(value) {
        setPadding(paddingStart, paddingTop, value, paddingBottom)
    }
    get() = paddingEnd

fun View.addPaddingEnd(padding: Int) {
    setPadding(paddingStart, paddingTop, paddingEnd + padding, paddingBottom)
}

var View.paddingBottomCompat: Int
    set(value) {
        setPadding(paddingStart, paddingTop, paddingEnd, value)
    }
    get() = paddingBottom

fun View.addPaddingBottom(padding: Int) {
    setPadding(paddingStart, paddingTop, paddingEnd, paddingBottom + padding)
}

fun ViewGroup.setSystemPadding() {
    val isOrientationLandscape = context.isOrientationLandscape
    fitsSystemWindows = isOrientationLandscape
    setPadding(0, if (isOrientationLandscape) 0 else UiUtils.getStatusBarHeight(), 0, 0)
}

fun TextView.tintHighlightText(highlightText: String, rawText: CharSequence) {
    text = rawText
    if (text.contains(highlightText, true)) {
        val builder = SpannableStringBuilder()
        val spannableString = SpannableString(text.toString())
        val start = text.indexOf(highlightText, 0, true)
        val color = context.getColorByAttr(com.google.android.material.R.attr.colorPrimary)
        spannableString.setSpan(ForegroundColorSpan(color),
            start,
            start + highlightText.length,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        builder.append(spannableString)
        text = builder
    }
}

fun TextView.tintTextToPrimary() {
    val builder = SpannableStringBuilder()
    val spannableString = SpannableString(text.toString())
    val color = context.getColorByAttr(com.google.android.material.R.attr.colorPrimary)
    spannableString.setSpan(ForegroundColorSpan(color),
        0,
        text.length,
        Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
    builder.append(spannableString)
    text = builder
}

fun ViewPager2.setCurrentItem(
    item: Int,
    duration: Long,
    interpolator: TimeInterpolator = AccelerateDecelerateInterpolator(),
    pagePxWidth: Int = width,
) {
    val pxToDrag: Int = pagePxWidth * (item - currentItem)
    val animator = ValueAnimator.ofInt(0, pxToDrag)
    var previousValue = 0
    animator.addUpdateListener { valueAnimator ->
        val currentValue = valueAnimator.animatedValue as Int
        val currentPxToDrag = (currentValue - previousValue).toFloat()
        fakeDragBy(-currentPxToDrag)
        previousValue = currentValue
    }
    animator.addListener(object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {
            beginFakeDrag()
        }

        override fun onAnimationEnd(animation: Animator) {
            endFakeDrag()
        }

        override fun onAnimationCancel(animation: Animator) {}
        override fun onAnimationRepeat(animation: Animator) {}
    })
    animator.interpolator = interpolator
    animator.duration = duration
    animator.start()
}

fun ViewGroup.setAlphaForAll(alpha: Float) = children.forEach {
    it.alpha = alpha
}

fun TextView.startStrikeThroughAnimation(): ValueAnimator {
    val span = SpannableString(text)
    val strikeSpan = StrikethroughSpan()
    val animator = ValueAnimator.ofInt(text.length)
    animator.addUpdateListener {
        span.setSpan(strikeSpan, 0, it.animatedValue as Int, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        text = span
        invalidate()
    }
    animator.interpolator = AccelerateDecelerateInterpolator()
    animator.duration = 1000
    animator.start()
    return animator
}

fun TextView.reverseStrikeThroughAnimation(): ValueAnimator {
    val span = SpannableString(text.toString())
    val strikeSpan = StrikethroughSpan()
    val animator = ValueAnimator.ofInt(text.length, 0)
    animator.addUpdateListener {
        span.setSpan(strikeSpan, 0, it.animatedValue as Int, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        text = span
        invalidate()
    }
    animator.interpolator = AccelerateDecelerateInterpolator()
    animator.duration = 1000
    animator.start()
    return animator
}