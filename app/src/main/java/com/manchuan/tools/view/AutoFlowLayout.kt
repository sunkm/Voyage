package com.manchuan.tools.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import com.manchuan.tools.R
import com.manchuan.tools.adapter.FlowAdapter

/**
 * 自定义LinearLayout，支持自动换行，指定行数,实现流式布局
 */
class AutoFlowLayout<T> : ViewGroup {
    /**
     * 存储所有的View，按行记录
     */
    private val mAllViews: MutableList<MutableList<View>> = ArrayList()

    /**
     * 记录每一行的宽度
     */
    private val mWidthList: MutableList<Int> = ArrayList()

    /**
     * 记录设置单行显示的标志
     */
    private var mIsSingleLine = false

    /**
     * 记录每一行的最大高度
     */
    private val mLineHeight: MutableList<Int> = ArrayList()
    /**
     * 支持显示的最大行数
     * @return 最大行数
     */
    /**
     * 记录设置最大行数量
     */
    var maxLineNumbers = 0
        private set

    /**
     * 记录当前行数
     */
    private var mCount = 0

    /**
     * 是否还有数据没显示
     */
    private var mHasMoreData = false

    /**
     * 子View的点击事件
     */
    private var mOnItemClickListener: OnItemClickListener? = null

    /**
     * 当前view的索引
     */
    private var mCurrentItemIndex = -1
    /**
     * 是否支持多选
     * @return
     */
    /**
     * 多选标志，默认支持单选
     */
    var isMultiChecked = false
    /**
     * 获取上一个被选中的View
     * @return 被选中的view
     */
    /**
     * 记录选中的View
     */
    var selectedView: View? = null
        private set

    /**
     * 记录选中的View
     */
    private val mCheckedViews: MutableList<View?> = ArrayList()

    /**
     * 记录展示的数量
     */
    private var mDisplayNumbers = 0

    /**
     * 数据适配器
     */
    private var mAdapter: FlowAdapter<T>? = null
    /**
     * 返回网格布局的水平距离
     * @return
     */
    /**
     * 水平方向View之间的间距
     */
    var horizontalSpace = 0f
        private set
    /**
     * 返回网格布局的垂直距离
     */
    /**
     * 竖直方向View之间的间距
     */
    var verticalSpace = 0f
        private set

    /**
     * 列数
     */
    private var mColumnNumbers = 0

    /**
     * 行数
     */
    private var mRowNumbers = 0

    /**
     * 是否设置了网格布局
     */
    private var mIsGridMode = false

    /**
     * 是否设置了分割线
     */
    private var mIsCutLine = false

    /**
     * 记录分割线的宽度
     */
    private var mCutLineWidth = 0f

    /**
     * 记录分割线的长度
     */
    private var mCutLineColor = 0

    /**
     * 是否每行居中处理
     */
    private var mIsCenter = false

    /**
     * 长按监听
     */
    private var mOnLongItemClickListener: OnLongItemClickListener? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.AutoFlowLayout)
        mIsSingleLine = ta.getBoolean(R.styleable.AutoFlowLayout_singleLine, false)
        maxLineNumbers = ta.getInteger(R.styleable.AutoFlowLayout_maxLines, Int.MAX_VALUE)
        isMultiChecked = ta.getBoolean(R.styleable.AutoFlowLayout_multiChecked, false)
        horizontalSpace = ta.getDimension(R.styleable.AutoFlowLayout_horizontalSpace, 0f)
        verticalSpace = ta.getDimension(R.styleable.AutoFlowLayout_verticalSpace, 0f)
        mColumnNumbers = ta.getInteger(R.styleable.AutoFlowLayout_columnNumbers, 0)
        mRowNumbers = ta.getInteger(R.styleable.AutoFlowLayout_rowNumbers, 0)
        mCutLineColor = ta.getColor(
            R.styleable.AutoFlowLayout_cutLineColor,
            resources.getColor(android.R.color.darker_gray)
        )
        mCutLineWidth = ta.getDimension(R.styleable.AutoFlowLayout_cutLineWidth, 1f)
        mIsCutLine = ta.getBoolean(R.styleable.AutoFlowLayout_cutLine, false)
        mIsCenter = ta.getBoolean(R.styleable.AutoFlowLayout_lineCenter, false)
        if (mColumnNumbers != 0) {
            mIsGridMode = true
        }
        ta.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (mIsGridMode) {
            setGridMeasure(widthMeasureSpec, heightMeasureSpec)
        } else {
            setFlowMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    /**
     * 网格布局的测量模式 默认各个子VIEW宽高值相同
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    private fun setGridMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 获得它的父容器为它设置的测量模式和大小
        val sizeWidth = MeasureSpec.getSize(widthMeasureSpec)
        val sizeHeight = MeasureSpec.getSize(heightMeasureSpec)
        val modeWidth = MeasureSpec.getMode(widthMeasureSpec)
        val modeHeight = MeasureSpec.getMode(heightMeasureSpec)
        //获取viewgroup的padding
        val paddingLeft = paddingLeft
        val paddingRight = paddingRight
        val paddingTop = paddingTop
        val paddingBottom = paddingBottom
        //最终的宽高值
        val heightResult: Int
        val widthResult: Int
        //未设置行数 推测行数
        if (mRowNumbers == 0) {
            mRowNumbers =
                if (childCount % mColumnNumbers == 0) childCount / mColumnNumbers else childCount / mColumnNumbers + 1
        }
        var maxChildHeight = 0
        var maxWidth = 0
        var maxHeight = 0
        var maxLineWidth = 0
        //统计最大高度/最大宽度
        for (i in 0 until mRowNumbers) {
            for (j in 0 until mColumnNumbers) {
                val child = getChildAt(i * mColumnNumbers + j)
                if (child != null) {
                    if (child.visibility != GONE) {
                        measureChild(child, widthMeasureSpec, heightMeasureSpec)
                        // 得到child的lp
                        val lp = child
                            .layoutParams as MarginLayoutParams
                        maxLineWidth += child.measuredWidth + lp.leftMargin + lp.rightMargin
                        maxChildHeight = Math.max(
                            maxChildHeight,
                            child.measuredHeight + lp.topMargin + lp.bottomMargin
                        )
                    }
                }
            }
            maxWidth = Math.max(maxLineWidth, maxWidth)
            maxLineWidth = 0
            maxHeight += maxChildHeight
            maxChildHeight = 0
        }
        val tempWidth =
            (maxWidth + horizontalSpace * (mColumnNumbers - 1) + paddingLeft + paddingRight).toInt()
        val tempHeight =
            (maxHeight + verticalSpace * (mRowNumbers - 1) + paddingBottom + paddingTop).toInt()
        widthResult = if (tempWidth > sizeWidth) {
            sizeWidth
        } else {
            tempWidth
        }
        //宽高超过屏幕大小，则进行压缩存放
        heightResult = if (tempHeight > sizeHeight) {
            sizeHeight
        } else {
            tempHeight
        }
        setMeasuredDimension(
            if (modeWidth == MeasureSpec.EXACTLY) sizeWidth else widthResult,
            if (modeHeight == MeasureSpec.EXACTLY) sizeHeight else heightResult
        )
    }

    /**
     * 流式布局的测量模式
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    private fun setFlowMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        mCount = 0
        // 获得它的父容器为它设置的测量模式和大小
        val sizeWidth = MeasureSpec.getSize(widthMeasureSpec)
        val sizeHeight = MeasureSpec.getSize(heightMeasureSpec)
        val modeWidth = MeasureSpec.getMode(widthMeasureSpec)
        val modeHeight = MeasureSpec.getMode(heightMeasureSpec)
        // 如果是warp_content情况下，记录宽和高
        var width = 0
        var height = paddingTop + paddingBottom

        /**
         * 记录每一行的宽度，width不断取最大宽度
         */
        var lineWidth = paddingLeft + paddingRight

        /**
         * 每一行的高度，累加至height
         */
        var lineHeight = 0
        val cCount = childCount

        // 遍历每个子元素
        for (i in 0 until cCount) {
            val child = getChildAt(i)
            // 测量每一个child的宽和高
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            // 得到child的lp
            val lp = child
                .layoutParams as MarginLayoutParams
            // 当前子空间实际占据的宽度
            val childWidth = (child.measuredWidth + lp.leftMargin
                    + lp.rightMargin)
            // 当前子空间实际占据的高度
            val childHeight = (child.measuredHeight + lp.topMargin
                    + lp.bottomMargin)
            /**
             * 如果加入当前child，则超出最大宽度，则的到目前最大宽度给width，类加height 然后开启新行
             */
            if (lineWidth + childWidth > sizeWidth) {
                width = Math.max(lineWidth, childWidth) // 取最大的
                lineWidth = childWidth // 重新开启新行，开始记录
                // 叠加当前高度，
                height += lineHeight
                // 开启记录下一行的高度
                lineHeight = childHeight
                mCount++
                if (mCount >= maxLineNumbers) {
                    setHasMoreData(i + 1, cCount)
                    break
                }
                if (mIsSingleLine) {
                    setHasMoreData(i + 1, cCount)
                    break
                }
            } else  // 否则累加值lineWidth,lineHeight取最大高度
            {
                lineWidth += childWidth
                lineHeight = Math.max(lineHeight, childHeight)
            }
            // 如果是最后一个，则将当前记录的最大宽度和当前lineWidth做比较
            if (i == cCount - 1) {
                width = Math.max(width, lineWidth)
                height += lineHeight
            }
        }
        setMeasuredDimension(
            if (modeWidth == MeasureSpec.EXACTLY) sizeWidth else width,
            if (modeHeight == MeasureSpec.EXACTLY) sizeHeight else height
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (mIsGridMode) {
            setGridLayout()
        } else {
            setFlowLayout()
        }
    }

    /**
     * 网格布局的布局模式
     */
    private fun setGridLayout() {
        mCheckedViews.clear()
        mCurrentItemIndex = -1
        val sizeWidth = width
        val sizeHeight = height
        //子View的平均宽高 默认所有View宽高一致
        val tempChild = getChildAt(0)
        val lp = tempChild
            .layoutParams as MarginLayoutParams
        val childAvWidth =
            ((sizeWidth - paddingLeft - paddingRight - horizontalSpace * (mColumnNumbers - 1)) / mColumnNumbers).toInt() - lp.leftMargin - lp.rightMargin
        val childAvHeight =
            ((sizeHeight - paddingTop - paddingBottom - verticalSpace * (mRowNumbers - 1)) / mRowNumbers).toInt() - lp.topMargin - lp.bottomMargin
        for (i in 0 until mRowNumbers) {
            for (j in 0 until mColumnNumbers) {
                val child = getChildAt(i * mColumnNumbers + j)
                if (child != null) {
                    mCurrentItemIndex++
                    if (child.visibility != GONE) {
                        setChildClickOperation(child, -1)
                        val childLeft =
                            (paddingLeft + j * (childAvWidth + horizontalSpace)).toInt() + j * (lp.leftMargin + lp.rightMargin) + lp.leftMargin
                        val childTop =
                            (paddingTop + i * (childAvHeight + verticalSpace)).toInt() + i * (lp.topMargin + lp.bottomMargin) + lp.topMargin
                        child.layout(
                            childLeft,
                            childTop,
                            childLeft + childAvWidth,
                            childAvHeight + childTop
                        )
                    }
                }
            }
        }
    }

    /**
     * 流式布局的布局模式
     */
    private fun setFlowLayout() {
        mCurrentItemIndex = -1
        mCount = 0
        mAllViews.clear()
        mLineHeight.clear()
        mWidthList.clear()
        mCheckedViews.clear()
        val width = width
        var lineWidth = paddingLeft
        var lineHeight = paddingTop
        // 存储每一行所有的childView
        var lineViews: MutableList<View> = ArrayList()
        val cCount = childCount
        // 遍历所有的孩子
        for (i in 0 until cCount) {
            val child = getChildAt(i)
            val lp = child
                .layoutParams as MarginLayoutParams
            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight

            // 如果已经需要换行
            if (childWidth + lp.leftMargin + lp.rightMargin + paddingRight + lineWidth > width) {
                // 记录这一行所有的View以及最大高度
                mLineHeight.add(lineHeight)
                // 将当前行的childView保存，然后开启新的ArrayList保存下一行的childView
                mAllViews.add(lineViews)
                mWidthList.add(lp.leftMargin + lp.rightMargin + paddingRight + lineWidth)
                lineWidth = 0 // 重置行宽
                lineViews = ArrayList()
                mCount++
                if (mCount >= maxLineNumbers) {
                    setHasMoreData(i + 1, cCount)
                    break
                }
                if (mIsSingleLine) {
                    setHasMoreData(i + 1, cCount)
                    break
                }
            }
            /**
             * 如果不需要换行，则累加
             */
            lineWidth += childWidth + lp.leftMargin + lp.rightMargin
            lineHeight = Math.max(
                lineHeight, childHeight + lp.topMargin
                        + lp.bottomMargin
            )
            lineViews.add(child)
        }
        // 记录最后一行
        mLineHeight.add(lineHeight)
        mAllViews.add(lineViews)
        mWidthList.add(lineWidth)
        var left = paddingLeft
        var top = paddingTop
        // 得到总行数
        var lineNums = mAllViews.size
        if (mAllViews[mAllViews.size - 1].size == 0) {
            lineNums = mAllViews.size - 1
        }
        for (i in 0 until lineNums) {
            // 每一行的所有的views
            lineViews = mAllViews[i]
            // 当前行的最大高度
            lineHeight = mLineHeight[i]
            if (mIsCenter) {
                if (mWidthList[i] < getWidth()) {
                    left += (getWidth() - mWidthList[i]) / 2
                }
            }
            // 遍历当前行所有的View
            for (j in lineViews.indices) {
                val child = lineViews[j]
                mCurrentItemIndex++
                if (child.visibility == GONE) {
                    continue
                }
                setChildClickOperation(child, -1)
                val lp = child
                    .layoutParams as MarginLayoutParams

                //计算childView的left,top,right,bottom
                val lc = left + lp.leftMargin
                val tc = top + lp.topMargin
                val rc = lc + child.measuredWidth
                val bc = tc + child.measuredHeight
                child.layout(lc, tc, rc, bc)
                left += (child.measuredWidth + lp.rightMargin
                        + lp.leftMargin)
            }
            val lp = getChildAt(0)
                .layoutParams as MarginLayoutParams
            left = paddingLeft
            top += lineHeight + lp.topMargin + lp.bottomMargin
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: LayoutParams): LayoutParams {
        return MarginLayoutParams(p)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return MarginLayoutParams(super.generateDefaultLayoutParams())
    }

    /**
     * 执行子View的点击相关事件
     * @param child
     * @param key
     */
    private fun setChildClickOperation(child: View, key: Int) {
        if (child.tag == null) {
            child.tag = mCurrentItemIndex
        }
        child.setOnLongClickListener(OnLongClickListener { view ->
            if (mOnLongItemClickListener != null) {
                mOnLongItemClickListener!!.onLongItemClick(
                    (if (key == -1) view.tag else key) as Int,
                    view
                )
                return@OnLongClickListener true
            }
            false
        })
        child.setOnClickListener(object : OnClickListener {
            override fun onClick(view: View) {
                if (isMultiChecked) {
                    if (mCheckedViews.contains(view)) {
                        mCheckedViews.remove(view)
                        view.isSelected = false
                    } else {
                        view.isSelected = true
                        mCheckedViews.add(view)
                        selectedView = view
                    }
                } else {
                    if (view.isSelected) {
                        view.isSelected = false
                    } else {
                        if (selectedView != null) {
                            selectedView!!.isSelected = false
                        }
                        view.isSelected = true
                        selectedView = view
                    }
                }
                if (mOnItemClickListener != null) {
                    mOnItemClickListener!!.onItemClick(
                        (if (key == -1) view.tag else key) as Int,
                        view
                    )
                }
            }
        })
    }

    /**
     * 网格布局状态下，绘制分割线
     * @param canvas
     */
    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (mIsGridMode && mIsCutLine) {
            val linePaint = Paint()
            linePaint.style = Paint.Style.STROKE
            linePaint.strokeWidth = mCutLineWidth
            linePaint.color = mCutLineColor
            for (i in 0 until mRowNumbers) {
                for (j in 0 until mColumnNumbers) {
                    val child = getChildAt(i * mColumnNumbers + j)
                    //最后一列
                    if (j == mColumnNumbers - 1) {
                        //不是最后一行  只画底部
                        if (i != mRowNumbers - 1) {
                            canvas.drawLine(
                                child.left - horizontalSpace / 2, child.bottom + verticalSpace / 2,
                                child.right.toFloat(), child.bottom + verticalSpace / 2, linePaint
                            )
                        }
                    } else {
                        //最后一行 只画右部
                        if (i == mRowNumbers - 1) {
                            canvas.drawLine(
                                child.right + horizontalSpace / 2, child.top - verticalSpace / 2,
                                child.right + horizontalSpace / 2, child.bottom.toFloat(), linePaint
                            )
                        } else {
                            //底部 右部 都画
                            if (j == 0) {
                                canvas.drawLine(
                                    child.left.toFloat(),
                                    child.bottom + verticalSpace / 2,
                                    child.right + horizontalSpace / 2,
                                    child.bottom + verticalSpace / 2,
                                    linePaint
                                )
                            } else {
                                canvas.drawLine(
                                    child.left - horizontalSpace / 2,
                                    child.bottom + verticalSpace / 2,
                                    child.right + horizontalSpace / 2,
                                    child.bottom + verticalSpace / 2,
                                    linePaint
                                )
                            }
                            if (i == 0) {
                                canvas.drawLine(
                                    child.right + horizontalSpace / 2,
                                    child.top.toFloat(),
                                    child.right + horizontalSpace / 2,
                                    child.bottom + verticalSpace / 2,
                                    linePaint
                                )
                            } else {
                                canvas.drawLine(
                                    child.right + horizontalSpace / 2,
                                    child.top - verticalSpace / 2,
                                    child.right + horizontalSpace / 2,
                                    child.bottom + verticalSpace / 2,
                                    linePaint
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 判断是否还有跟多View未展示
     * @param i 当前展示的View
     * @param count 总共需要展示的View
     */
    private fun setHasMoreData(i: Int, count: Int) {
        if (i < count) {
            mHasMoreData = true
        }
    }

    fun setAllViews(views: List<View?>?) {
        removeAllViews()
        if (views == null || views.size == 0) {
            return
        }
        for (i in views.indices) {
            val view = views[i]
            addView(view)
        }
        requestLayout()
    }

    /**
     * 删除指定索引的view
     * @param index true删除成功 false删除失败
     * @return
     */
    fun deleteView(index: Int): Boolean {
        if (mCurrentItemIndex != 0) {
            mDisplayNumbers = mCurrentItemIndex
            return if (index > mDisplayNumbers) {
                false
            } else {
                removeViewAt(index)
                true
            }
        }
        return false
    }

    /**
     * 删除最后一个view
     * @return  true删除成功 false删除失败
     */
    fun deleteView(): Boolean {
        if (mCurrentItemIndex != 0) {
            mDisplayNumbers = mCurrentItemIndex
            removeViewAt(mDisplayNumbers)
            return true
        }
        return false
    }

    /**
     * 删除指定范围的所有view
     * @param start 开始范围
     * @param end   结束范围
     * @return
     */
    fun deleteView(start: Int, end: Int): Boolean {
        var start = start
        var end = end
        if (mCurrentItemIndex != 0) {
            mDisplayNumbers = mCurrentItemIndex
            if (start < 0) {
                start = 0
            }
            if (end > mDisplayNumbers) {
                end = mDisplayNumbers
            }
            removeViews(start, end - start + 1)
            return true
        }
        return false
    }

    /**
     * 删除所有view
     * @return
     */
    fun clearViews(): Boolean {
        if (childCount > 0) {
            removeAllViews()
            return true
        }
        return false
    }

    /**
     * 设置最多显示的行数
     * @param number
     */
    fun setMaxLines(number: Int) {
        maxLineNumbers = number
        requestLayout()
    }
    /**
     * 是否单行显示
     * @return true 单行显示 false 多行显示
     */
    /**
     * 是否只显示单行
     * @param isSingle
     */
    var isSingleLine: Boolean
        get() = mIsSingleLine
        set(isSingle) {
            mIsSingleLine = isSingle
            requestLayout()
        }

    /**
     * 是否还有更多数据未显示
     * @return true 还有未显示数据 false 完全显示
     */
    fun hasMoreData(): Boolean {
        return mHasMoreData
    }

    /**
     * 获得选中的View集合
     * @return view集合
     */
    val checkedViews: List<View?>
        get() = if (isMultiChecked) {
            mCheckedViews
        } else {
            mCheckedViews.add(selectedView)
            mCheckedViews
        }

    /**
     * 设置数据适配器
     * @param adapter
     */
    fun setAdapter(adapter: FlowAdapter<T>?) {
        mAdapter = adapter
        if (mAdapter!!.count != 0) {
            for (i in 0 until mAdapter!!.count) {
                val view = mAdapter!!.getView(i)
                addView(view)
            }
            requestLayout()
        }
    }

    /**
     * 设置网格布局的水平间距
     * @param horizontalSpace 单位px
     */
    fun setHorizontalSpace(horizontalSpace: Int) {
        this.horizontalSpace = horizontalSpace.toFloat()
        requestLayout()
    }

    /**
     * 设置网格布局的垂直间距
     * @param verticalSpace 单位px
     */
    fun setVerticalSpace(verticalSpace: Int) {
        this.verticalSpace = verticalSpace.toFloat()
        requestLayout()
    }
    /**
     * 获得列数
     * @return
     */
    /**
     * 设置列数
     * @param columnNumbers
     */
    var columnNumbers: Int
        get() = mColumnNumbers
        set(columnNumbers) {
            mColumnNumbers = columnNumbers
            requestLayout()
        }
    /**
     * 得到行数
     * @return
     */
    /**
     * 设置行数
     * @param rowNumbers
     */
    var rowNumbers: Int
        get() = mRowNumbers
        set(rowNumbers) {
            mRowNumbers = rowNumbers
            requestLayout()
        }
    /**
     * 获取分割线的宽度
     * @return
     */
    /**
     * 设置分割线的宽度
     * @param width
     */
    var cutLineWidth: Float
        get() = mCutLineWidth
        set(width) {
            mCutLineWidth = width
            invalidate()
        }
    /**
     * 获取分割线的颜色
     * @return
     */
    /**
     * 设置分割线的颜色
     * @param color
     */
    var cutLineColor: Int
        get() = mCutLineColor
        set(color) {
            mCutLineColor = color
            invalidate()
        }

    /**
     * 设置分割线
     * @param isCutLine true 设置 false 不设置
     */
    fun setCutLine(isCutLine: Boolean) {
        mIsCutLine = isCutLine
        invalidate()
    }

    /**
     * 是否设置了分割线
     * @return
     */
    fun hasCutLine(): Boolean {
        return mIsCutLine
    }
    /**
     * 是否设置了行居中显示
     * @return
     */
    /**
     * 设置是否进行行居中显示
     * @param isCenter
     */
    var isLineCenter: Boolean
        get() = mIsCenter
        set(isCenter) {
            mIsCenter = isCenter
            requestLayout()
        }

    interface OnItemClickListener {
        fun onItemClick(position: Int, view: View?)
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        mOnItemClickListener = onItemClickListener
    }

    interface OnLongItemClickListener {
        fun onLongItemClick(position: Int, view: View?)
    }

    fun setOnLongItemClickListener(onLongItemClickListener: OnLongItemClickListener?) {
        mOnLongItemClickListener = onLongItemClickListener
    }
}