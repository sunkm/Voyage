package com.lxj.androidktx.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.blankj.utilcode.util.ConvertUtils

/**
 * 字母索引
 */
class LetterIndexBar : View {
    /**
     * 索引字母数组
     */
    var indexs = arrayOf(
        "#", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O",
        "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
    )

    /**
     * 控件的宽高
     */
    private var mWidth = 0
    private var mHeight = 0

    /**
     * 单元格的高度
     */
    private var mCellHeight = 0f
    private var mPaint: Paint? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        mPaint = Paint()
        mPaint!!.color = LETTER_COLOR
        mPaint!!.textSize = ConvertUtils.sp2px(12f).toFloat()
        mPaint!!.isAntiAlias = true // 去掉锯齿，让字体边缘变得平滑
    }

    @JvmName("setIndexs1")
    fun setIndexs(indexs: Array<String>) {
        this.indexs = indexs
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        //字母的坐标点：(x,y)
        if (indexs.isEmpty()) {
            return
        }
        for (i in indexs.indices) {
            val letter = indexs[i]
            val x = mWidth / 2 - getTextWidth(letter) / 2
            val y = mCellHeight / 2 + getTextHeight(letter) / 2 + mCellHeight * i + paddingTop
            canvas.drawText(letter, x, y, mPaint!!)
        }
    }

    /**
     * 获取字符的宽度
     *
     * @param text 需要测量的字母
     * @return 对应字母的高度
     */
    private fun getTextWidth(text: String): Float {
        val bounds = Rect()
        mPaint!!.getTextBounds(text, 0, text.length, bounds)
        return bounds.width().toFloat()
    }

    /**
     * 获取字符的高度
     *
     * @param text 需要测量的字母
     * @return 对应字母的高度
     */
    private fun getTextHeight(text: String): Float {
        val bounds = Rect()
        mPaint!!.getTextBounds(text, 0, text.length, bounds)
        return bounds.height().toFloat()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = measuredWidth - paddingLeft - paddingRight
        mHeight = measuredHeight - paddingBottom - paddingTop
        mCellHeight = mHeight * 1f / indexs.size //26个字母加上“#”
    }

    private var letterIndex = -1
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                // 按下字母的下标
                val index = ((event.y - paddingTop) / mCellHeight).toInt()
                if (index != letterIndex) {
                    letterIndex = index
                    // 判断是否越界
                    if (letterIndex >= 0 && letterIndex < indexs.size) {
                        //通过回调方法通知列表定位
                        if (mOnIndexChangedListener != null) {
                            mOnIndexChangedListener!!.onIndexChanged(indexs[letterIndex])
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> letterIndex = -1
        }
        return true
    }

    interface OnIndexChangedListener {
        /**
         * 按下字母改变了
         *
         * @param letter 按下的字母
         */
        fun onIndexChanged(letter: String?)
    }

    private var mOnIndexChangedListener: OnIndexChangedListener? = null
    fun setOnIndexChangedListener(onIndexChangedListener: OnIndexChangedListener?) {
        mOnIndexChangedListener = onIndexChangedListener
    }

    companion object {
        /**
         * 索引字母颜色
         */
        private const val LETTER_COLOR = -0xddddde
    }
}