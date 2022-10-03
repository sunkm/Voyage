package com.manchuan.tools.colorpicker

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.ImageView
import androidx.preference.PreferenceViewHolder
import com.manchuan.tools.R
import com.manchuan.tools.colorpicker.ColorPickerView.WHEEL_TYPE
import com.manchuan.tools.colorpicker.builder.ColorPickerDialogBuilder

open class ColorPickerPreference : androidx.preference.Preference {
    private var alphaSlider = false
    private var lightSlider = false
    private var border = false
    private var selectedColor = 0
    private var wheelType: WHEEL_TYPE? = null
    protected var density = 0
    private var pickerColorEdit = false
    private var pickerTitle: String? = null
    private var pickerButtonCancel: String? = null
    private var pickerButtonOk: String? = null
    private var colorIndicator: ImageView? = null

    constructor(context: Context?) : super(context!!)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initWith(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context,
        attrs,
        defStyleAttr) {
        initWith(context, attrs)
    }

    private fun initWith(context: Context, attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ColorPickerPreference)
        try {
            alphaSlider =
                typedArray.getBoolean(R.styleable.ColorPickerPreference_alphaSlider, false)
            lightSlider =
                typedArray.getBoolean(R.styleable.ColorPickerPreference_lightnessSlider, false)
            border = typedArray.getBoolean(R.styleable.ColorPickerPreference_border, true)
            density = typedArray.getInt(R.styleable.ColorPickerPreference_density, 8)
            wheelType =
                WHEEL_TYPE.indexOf(typedArray.getInt(R.styleable.ColorPickerPreference_wheelType,
                    0))
            selectedColor = typedArray.getInt(R.styleable.ColorPickerPreference_initialColor, -0x1)
            pickerColorEdit =
                typedArray.getBoolean(R.styleable.ColorPickerPreference_pickerColorEdit, true)
            pickerTitle = typedArray.getString(R.styleable.ColorPickerPreference_pickerTitle)
            if (pickerTitle == null) pickerTitle = "Choose color"
            pickerButtonCancel =
                typedArray.getString(R.styleable.ColorPickerPreference_pickerButtonCancel)
            if (pickerButtonCancel == null) pickerButtonCancel = "cancel"
            pickerButtonOk = typedArray.getString(R.styleable.ColorPickerPreference_pickerButtonOk)
            if (pickerButtonOk == null) pickerButtonOk = "ok"
        } finally {
            typedArray.recycle()
        }
        widgetLayoutResource = R.layout.color_widget
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val tmpColor = if (isEnabled) selectedColor else darken(selectedColor, .5f)
        colorIndicator = holder.findViewById(R.id.color_indicator) as ImageView
        var colorChoiceDrawable: ColorCircleDrawable? = null
        val currentDrawable = colorIndicator!!.drawable
        if (currentDrawable != null && currentDrawable is ColorCircleDrawable) colorChoiceDrawable =
            currentDrawable
        if (colorChoiceDrawable == null) colorChoiceDrawable = ColorCircleDrawable(tmpColor)
        colorIndicator!!.setImageDrawable(colorChoiceDrawable)
    }

    fun setValue(value: Int) {
        if (callChangeListener(value)) {
            selectedColor = value
            persistInt(value)
            notifyChanged()
        }
    }

    @Deprecated("Deprecated in Java",
        ReplaceWith("setValue((if (restoreValue) getPersistedInt(0) else defaultValue as Int))"))
    override fun onSetInitialValue(restoreValue: Boolean, defaultValue: Any) {
        setValue((if (restoreValue) getPersistedInt(0) else defaultValue as Int))
    }

    @Deprecated("Deprecated in Java")
    override fun onClick() {
        val builder = ColorPickerDialogBuilder
            .with(context)
            .setTitle(pickerTitle)
            .initialColor(selectedColor)
            .showBorder(border)
            .wheelType(wheelType)
            .density(density)
            .showColorEdit(pickerColorEdit)
            .setPositiveButton(pickerButtonOk) { dialog, selectedColorFromPicker, allColors ->
                setValue(selectedColorFromPicker)
            }
            .setNegativeButton(pickerButtonCancel, null)
        if (!alphaSlider && !lightSlider) builder.noSliders() else if (!alphaSlider) builder.lightnessSliderOnly() else if (!lightSlider) builder.alphaSliderOnly()
        builder
            .build()
            .show()
    }

    companion object {
        fun darken(color: Int, factor: Float): Int {
            val a = Color.alpha(color)
            val r = Color.red(color)
            val g = Color.green(color)
            val b = Color.blue(color)
            return Color.argb(a,
                Math.max((r * factor).toInt(), 0),
                Math.max((g * factor).toInt(), 0),
                Math.max((b * factor).toInt(), 0))
        }
    }
}