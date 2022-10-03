package com.manchuan.tools.activity

import android.app.Activity
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ScreenUtils
import com.manchuan.tools.R
import com.manchuan.tools.view.MarqueeView

/**
 * @author pedra
 */
class FullScreenMarQuee : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        //NavigationBarStatusBar(this,true);
        ScreenUtils.setFullScreen(this)
        BarUtils.setNavBarVisibility(this, false)
        setContentView(R.layout.fullscreen_marquee)
        initView(this)
        val intent = intent
        val bundle = intent.extras
        val a: Any? = bundle!!.getString("content")
        runOnUiThread { marquee_view!!.setContent(a.toString()) }
        try {
            marquee_view!!.setTextColor(bundle.getInt("color"))
        } catch (ignored: Exception) {
        }
        if (bundle.getInt("size") == 0) {
        } else {
            marquee_view!!.setTextSize(bundle.getInt("size").toFloat())
        }
        if (bundle.getInt("speed") == 0) {
        } else {
            marquee_view!!.setTextSpeed(bundle.getInt("speed").toFloat())
        }
    }

    private var marquee_view: MarqueeView? = null
    private fun initView(activity: Activity) {
        marquee_view = activity.findViewById(R.id.marquee_view)
    }
}