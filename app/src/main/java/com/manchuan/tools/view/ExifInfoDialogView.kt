package com.manchuan.tools.view

import android.content.Context
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.marginTop
import com.google.android.material.button.MaterialButton
import com.manchuan.tools.R

class ExifInfoDialogView(context: Context) : AViewGroup(context), IHeaderView {

    private val header = BottomSheetHeaderView(context).apply {
        layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        title.text = context.getString(R.string.cloud_rules)
    }

    val cloudRulesContentView = CloudRulesContentView(context).apply {
        layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    init {
        addView(header)
        addView(cloudRulesContentView)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        header.layout(0, paddingTop)
        cloudRulesContentView.layout(0, header.bottom)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        header.autoMeasure()
        cloudRulesContentView.autoMeasure()
        setMeasuredDimension(measuredWidth,
            header.measuredHeight + cloudRulesContentView.measuredHeight)
    }

    class CloudRulesContentView(context: Context) : AViewGroup(context) {
        val desc = AppCompatEditText(context).apply {
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).also {
                it.topMargin = 48.dp
            }
            hint = "数值"
            isSingleLine = true
        }
        val updateButton = MaterialButton(context).apply {
            layoutParams = LayoutParams(300.dp, ViewGroup.LayoutParams.WRAP_CONTENT).also {
                it.topMargin = 48.dp
            }
            isEnabled = false
            text = context.getString(android.R.string.ok)
        }

        init {
            setPadding(0, 48.dp, 0,0)
            addView(desc)
            addView(updateButton)
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            desc.autoMeasure()
            updateButton.autoMeasure()
            setMeasuredDimension(
                measuredWidth,
                paddingTop + desc.measuredHeight + updateButton.marginTop + updateButton.measuredHeight + paddingBottom
            )
        }

        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            updateButton.layout(
                updateButton.toHorizontalCenter(this),
                desc.bottom + updateButton.marginTop
            )
            desc.layout(desc.toHorizontalCenter(this), 0)
        }
    }

    override fun getHeaderView(): BottomSheetHeaderView {
        return header
    }
}
