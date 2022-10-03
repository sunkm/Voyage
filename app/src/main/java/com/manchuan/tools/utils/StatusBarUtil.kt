package com.manchuan.tools.utils

import android.app.Activity

/**
 * @author ManChuan
 */
object StatusBarUtil {

    /**
     * 修改状态栏颜色，支持4.4以上版本
     * @param activity
     * @param colorId
     */
    fun setStatusBarColor(activity: Activity, colorId: Int) {
        val window = activity.window
        window.statusBarColor = colorId
    }

}