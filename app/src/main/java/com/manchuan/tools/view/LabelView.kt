package com.manchuan.tools.view

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.material.chip.Chip
import com.manchuan.tools.R
import com.manchuan.tools.entity.LabelEntity
import com.manchuan.tools.interfaces.OnLabelSelectedListener

class LabelView : ViewGroup {
    private var mTvs: MutableList<Chip>? = null
    private var mWidth = 0
    private val mHeight = 0
    private var mLabelPading = 0
    private var mListener: OnLabelSelectedListener? = null
    private var mLabelColor = Color.parseColor("#ffeeeeee")

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    fun setLabelColor(labelColor: Int) {
        mLabelColor = labelColor
    }

    fun setListener(listener: OnLabelSelectedListener?) {
        mListener = listener
    }

    fun setLabels(labels: List<LabelEntity>) {
        mTvs = ArrayList()
        mLabelPading = 10
        for (i in labels.indices) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.item_tv, this, false)
            val card: Chip = view.findViewById(R.id.chip)
            card.text = (labels[i].title)
            card.typeface = Typeface.SERIF
            card.setOnClickListener {
                if (mListener != null) {
                    mListener!!.selected(i, labels[i].title)
                }
            }
            val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
            addView(view, params)
            mTvs?.add(card)
        }
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mWidth = MeasureSpec.getSize(widthMeasureSpec)
        val count = 0
        setMeasuredDimension(mWidth, count * (mLabelPading * 2 + getChildAt(0).measuredHeight))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var startX = mLabelPading
        var startY = mLabelPading
        for (i in 0 until childCount) {
            val card = getChildAt(i) as LinearLayout
            if (startX + card.measuredWidth > mWidth) {
                startX = mLabelPading
                startY += mLabelPading * 2 + card.measuredHeight
            }
            card.layout(startX, startY, startX + card.measuredWidth, startY + card.measuredHeight)
            startX += card.measuredWidth + mLabelPading
        }
    }
}