package com.manchuan.tools.items

import android.graphics.drawable.Drawable

class AppItem(var app_icon: Drawable, var app_name: String, var package_name: String,var apk_path:String) :
    Comparable<AppItem> {
    override fun compareTo(other: AppItem): Int {
        return app_name.compareTo(other.app_name)
    }
}