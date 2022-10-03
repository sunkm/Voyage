package com.manchuan.tools.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ScreenUtils
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar
import com.manchuan.tools.R

class RulerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ScreenUtils.setLandscape(this)
        ScreenUtils.setFullScreen(this)
        setContentView(R.layout.activity_ruler)
        ImmersionBar.with(this).fullScreen(true).hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR)
            .keyboardEnable(false).init()
    }
}