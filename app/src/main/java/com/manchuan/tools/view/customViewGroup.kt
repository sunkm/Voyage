package com.manchuan.tools.view

import android.content.Context
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import com.kongzue.dialogx.dialogs.PopTip

/**
 * TODO: document your custom view class.
 */
class customViewGroup(context: Context?) : ViewGroup(context) {
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {}

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        Log.v("customViewGroup", "**********Intercepted")
        PopTip.show("触发事件")
        return false
    }
}