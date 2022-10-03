package com.manchuan.tools.activity

import android.animation.LayoutTransition
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.vectordrawable.graphics.drawable.ArgbEvaluator
import com.gyf.immersionbar.ImmersionBar
import com.manchuan.tools.R
import com.manchuan.tools.databinding.ActivityRandomcolorBinding
import rikka.material.app.MaterialActivity
import java.util.*

class RanDomColorActivity : MaterialActivity() {
    private var toolbar: Toolbar? = null
    private var randomColorBinding: ActivityRandomcolorBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        randomColorBinding = ActivityRandomcolorBinding.inflate(LayoutInflater.from(this))
        setContentView(randomColorBinding?.root)
        toolbar = randomColorBinding?.toolbar
        mBlueStr = randomColorBinding?.blueStr
        mGreenStr = randomColorBinding?.greenStr
        mRedStr = randomColorBinding?.redStr
        root_lay = randomColorBinding?.rootLay
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar?.apply {
            layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        }
        root_lay?.apply {
            layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        last_color = ranDomColor()
    }

    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.color_refresh -> last_color = ranDomColor()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.random_color, menu)
        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("SetTextI18n", "ResourceType")
    fun ranDomColor(): Int {
        val random = Random()
        val red = random.nextInt(256)
        val green = random.nextInt(256)
        val blue = random.nextInt(256)
        mRedStr!!.text = "Red:$red"
        mGreenStr!!.text = "Green:$green"
        mBlueStr!!.text = "Blue:$blue"
        val color: Int = Color.rgb(red, green, blue)
        root_lay!!.setBackgroundColor(color)
        toolbar!!.setBackgroundColor(color)
        toolbar!!.title = String.format("#FF%02X%02X%02X", red, green, blue)
        ImmersionBar.with(this).transparentStatusBar()
            .transparentNavigationBar().autoDarkModeEnable(true).titleBar(toolbar).init()
        last_color?.let { startBackGroundAnimation(root_lay, it, color) }
        last_color?.let { startBackGroundAnimation(toolbar, it, color) }
        return color
    }

    private var last_color: Int? = null

    private fun startBackGroundAnimation(bgView: View?, startColor: Int, endColor: Int) {
        //创建动画,这里的关键就是使用ArgbEvaluator, 后面2个参数就是 开始的颜色,和结束的颜色.
        @SuppressLint("RestrictedApi") val colorAnimator =
            ValueAnimator.ofObject(ArgbEvaluator(), startColor, endColor)
        colorAnimator.addUpdateListener { animation ->
            val color = animation.animatedValue as Int //之后就可以得到动画的颜色了
            bgView!!.setBackgroundColor(color) //设置一下, 就可以看到效果.
            //textView.setTextColor();
        }
        colorAnimator.duration = 200
        colorAnimator.start()
    }

    private var mRedStr: TextView? = null
    private var mGreenStr: TextView? = null
    private var mBlueStr: TextView? = null
    private var root_lay: LinearLayout? = null
}