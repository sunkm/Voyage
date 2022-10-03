package com.manchuan.tools.adapter

import android.view.View
import java.util.*

/**
 * Created by lvruheng on 2017/8/1.
 */
abstract class FlowAdapter<T> {
    private var mList: List<T>?

    constructor(datas: List<T>?) {
        mList = datas
    }

    constructor(datas: Array<T>) {
        mList = ArrayList(listOf(*datas))
    }

    fun getItem(position: Int): T {
        return mList!![position]
    }

    val count: Int
        get() = if (mList == null) 0 else mList!!.size

    abstract fun getView(position: Int): View?
}