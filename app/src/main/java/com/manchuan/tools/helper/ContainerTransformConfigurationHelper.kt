package com.manchuan.tools.helper

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.util.SparseIntArray
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.Interpolator
import android.view.animation.OvershootInterpolator
import android.widget.EditText
import androidx.core.view.animation.PathInterpolatorCompat
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform

class ContainerTransformConfigurationHelper {
    /**
     * Whether or not to a custom container transform should use [ ].
     */
    val isArcMotionEnabled = false

    /** The enter duration to be used by a custom container transform.  */
    val enterDuration: Long = 0

    /** The return duration to be used by a custom container transform.  */
    val returnDuration: Long = 0

    /** The interpolator to be used by a custom container transform.  */
    val interpolator: Interpolator? = null
    private val fadeModeButtonId = 0

    /** Whether or not the custom transform should draw debugging lines.  */
    val isDrawDebugEnabled = false

    /** Set up the androidx transition according to the config helper's parameters.  */
    fun configure(transform: MaterialContainerTransform, entering: Boolean) {
        val duration = if (entering) enterDuration else returnDuration
        if (duration != NO_DURATION) {
            transform.duration = duration
        }
        if (interpolator != null) {
            transform.interpolator = interpolator
        }
        if (isArcMotionEnabled) {
            transform.setPathMotion(MaterialArcMotion())
        }
        transform.fadeMode = fadeMode
        transform.isDrawDebugEnabled = isDrawDebugEnabled
    }

    /** Set up the platform transition according to the config helper's parameters.  */
    @SuppressLint("ObsoleteSdkInt")
    fun configure(
        transform: com.google.android.material.transition.platform.MaterialContainerTransform,
        entering: Boolean,
    ) {
        val duration = if (entering) enterDuration else returnDuration
        if (duration != NO_DURATION) {
            transform.duration = duration
        }
        if (interpolator != null) {
            transform.interpolator = interpolator
        }
        if (isArcMotionEnabled) {
            transform.pathMotion =
                com.google.android.material.transition.platform.MaterialArcMotion()
        }
        transform.fadeMode = fadeMode
        transform.isDrawDebugEnabled = isDrawDebugEnabled
    }

    /** The fade mode used by a custom container transform.  */
    val fadeMode: Int
        get() = FADE_MODE_MAP[fadeModeButtonId]

    /** A custom overshoot interpolator which exposes its tension.  */
    private class CustomOvershootInterpolator @JvmOverloads internal constructor(val tension: Float = DEFAULT_TENSION) :
        OvershootInterpolator(
            tension) {
        companion object {
            // This is the default tension value in OvershootInterpolator
            const val DEFAULT_TENSION = 2.0f
        }
    }

    /** A custom anticipate overshoot interpolator which exposes its tension.  */
    private class CustomAnticipateOvershootInterpolator @JvmOverloads internal constructor(val tension: Float = DEFAULT_TENSION) :
        AnticipateOvershootInterpolator(
            tension) {
        companion object {
            // This is the default tension value in AnticipateOvershootInterpolator
            const val DEFAULT_TENSION = 2.0f
        }
    }

    /** A custom cubic bezier interpolator which exposes its control points.  */
    private class CustomCubicBezier internal constructor(
        val controlX1: Float,
        val controlY1: Float,
        val controlX2: Float,
        val controlY2: Float,
    ) : Interpolator {
        private val interpolator: Interpolator
        override fun getInterpolation(input: Float): Float {
            return interpolator.getInterpolation(input)
        }

        init {
            interpolator = PathInterpolatorCompat.create(controlX1, controlY1, controlX2, controlY2)
        }
    }

    companion object {
        private const val CUBIC_CONTROL_FORMAT = "%.3f"
        private const val DURATION_FORMAT = "%.0f"
        private const val NO_DURATION: Long = -1
        private val FADE_MODE_MAP = SparseIntArray()

        @SuppressLint("DefaultLocale")
        private fun setTextFloat(editText: EditText, value: Float) {
            editText.setText(String.format(CUBIC_CONTROL_FORMAT, value))
        }

        private fun getTextFloat(editText: EditText?): Float? {
            if (editText == null) {
                return null
            }
            val text = editText.text.toString()
            return try {
                java.lang.Float.valueOf(text)
            } catch (e: Exception) {
                null
            }
        }

        private fun setTextInputLayoutError(layout: TextInputLayout) {
            layout.error = " "
        }

        private fun setTextInputClearOnTextChanged(layout: TextInputLayout) {
            layout.editText?.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence,
                        start: Int,
                        count: Int,
                        after: Int,
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence,
                        start: Int,
                        before: Int,
                        count: Int,
                    ) {
                        layout.error = null
                    }

                    override fun afterTextChanged(s: Editable) {}
                })
        }

        private fun isValidCubicBezierControlValue(value: Float?): Boolean {
            return value == null || value < 0 || value > 1
        }
    }
}