package com.manchuan.tools.activity

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import com.drake.net.Get
import com.drake.net.utils.scopeNetLife
import com.drake.statusbar.immersive
import com.kongzue.dialogx.dialogs.WaitDialog
import com.lxj.androidktx.core.tip
import com.manchuan.tools.R
import com.manchuan.tools.extensions.setTextWithAnimation
import com.manchuan.tools.utils.BaseActivity
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import io.github.inflationx.viewpump.ViewPumpContextWrapper

/**
 * @author padre
 */
class RanDomArticleActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article)
        initView(this)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        immersive(toolbar!!)
        mLinear4?.apply {
            layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        }
        loadArticle()
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.let { ViewPumpContextWrapper.wrap(it) })
    }

    private fun loadArticle() {
        WaitDialog.show("请稍后...")
        scopeNetLife {
            val string = Get<String>("https://meiriyiwen.com/") {
                setHeader("User-Agent", WebActivity.getUserAgent(this@RanDomArticleActivity))
            }.await()
            WaitDialog.dismiss()
            textview2?.setTextWithAnimation(string.substring(
                string.indexOf("<h2 class=\"articleTitle\">") + 25,
                string.indexOf("</h2>")
            ).trim { it <= ' ' }, 300)
            textview3?.setTextWithAnimation(string.substring(
                string.indexOf("<div class=\"articleAuthorName\">") + 31,
                string.indexOf("<div class=\"articleContent\">") - 20
            ).trim { it <= ' ' }, 300)
            val nr = string.substring(
                string.indexOf("<div class=\"articleContent\">") + 28,
                string.indexOf("<div class=\"randomBox\">") - 30
            ).trim { it <= ' ' }
            textview4?.setTextWithAnimation(nr.replace("<p>", "\n").replace("</p>", "\n"), 300)
        }.catch {
            tip("出错")
        }
    }

    private var toolbar: Toolbar? = null
    private var m_linear: LinearLayout? = null
    private var mSl: SmartRefreshLayout? = null
    private var mVscroll1: NestedScrollView? = null
    private var mLinear4: LinearLayout? = null
    private var textview2: AppCompatTextView? = null
    private var textview3: AppCompatTextView? = null
    private var textview4: AppCompatTextView? = null
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_article, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun initView(activity: Activity) {
        toolbar = activity.findViewById(R.id.toolbar)
        m_linear = activity.findViewById(R.id._linear)
        mSl = activity.findViewById(R.id.sl)
        mVscroll1 = activity.findViewById(R.id.vscroll1)
        mLinear4 = activity.findViewById(R.id.linear4)
        textview2 = activity.findViewById(R.id.textview2)
        textview3 = activity.findViewById(R.id.textview3)
        textview4 = activity.findViewById(R.id.textview4)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        supportActionBar?.title = "随机一文"
    }

    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.refresh -> loadArticle()
        }
        return super.onOptionsItemSelected(item)
    }
}