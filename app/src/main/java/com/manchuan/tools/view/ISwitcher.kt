package com.manchuan.tools.view

import com.manchuan.tools.view.AbsTimeView
import androidx.viewpager.widget.ViewPager

/**
 * @author Felix.Liang
 */
interface ISwitcher {
    fun check(index: Int)
    fun getItem(index: Int): AbsTimeView?
    val checkedItem: AbsTimeView?
    val checkedViewId: Int
    fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?)
    interface OnCheckedChangeListener {
        fun onCheckedChanged(itemView: AbsTimeView?, isChecked: Boolean)
    }

    fun bindViewPager(pager: ViewPager?)
}