package com.manchuan.tools.activity

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.text.HtmlCompat
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.R
import com.manchuan.tools.utils.UiModeUtils
import okhttp3.*
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class WebSiteSourceCodeActivity : AppCompatActivity() {
    private var toolbar: Toolbar? = null
    private lateinit var toolBarLayout: CollapsingToolbarLayout
    private var web_content: TextView? = null
    private var url: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_website)
        toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        web_content = findViewById(R.id.web_content)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val intent = intent
        val bundle = intent.extras
        url = bundle!!.getString("url")
        toolBarLayout = findViewById<View>(R.id.toolbar_layout) as CollapsingToolbarLayout
        WaitDialog.show("获取中...")
        Thread(runnable).start()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        supportActionBar!!.title = "网页源码"
        toolbar!!.setTitleTextColor(Color.WHITE)
        //toolBarLayout.setT
        if (UiModeUtils.isDarkMode) {
            toolBarLayout.setCollapsedTitleTextColor(-0x1)
        } else {
            toolBarLayout.setCollapsedTitleTextColor(Color.BLACK)
        }
        toolBarLayout.setExpandedTitleTextColor(ColorStateList.valueOf(-0xae780c))
        toolBarLayout.setBackgroundColor(getColor(R.color.backgroundColor))
    }

    @SuppressLint("HandlerLeak")
    var handler: Handler = object : Handler(Looper.getMainLooper()) {
        @SuppressLint("HandlerLeak")
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            WaitDialog.dismiss()
            when (msg.what) {
                0 -> {
                    val data = msg.data
                    val `val` = data.getString("value")
                    web_content!!.text = `val`
                }
                1 -> {
                    val data2 = msg.data
                    val val2 = data2.getString("value")
                    PopTip.show("请求失败:( 服务器拒绝连接")
                    web_content!!.text =
                        val2?.let { HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_COMPACT) }
                }
                2 -> {
                    val data3 = msg.data
                    val val3 = data3.getString("value")
                    PopTip.show("请求失败:( 响应时间过长")
                    web_content!!.text = val3
                }
            }
        }
    }
    var runnable = Runnable {
        // TODO: http request.
        val okHttpClient = OkHttpClient()
        okHttpClient.newBuilder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .readTimeout(20, TimeUnit.SECONDS).build()
        val request = Request.Builder()
            .url(url.toString())
            .get()
            .build()
        val call = okHttpClient.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is SocketTimeoutException) {
                    val msg = Message()
                    val data = Bundle()
                    msg.what = 2
                    data.putString(
                        "value",
                        "<h1>无法访问网站</h1><br><b>$url</b> 响应时间过长<br>ERR_TIMED_OUT"
                    )
                    msg.data = data
                    handler.sendMessage(msg)
                }
                if (e is ConnectException) {
                    val msg = Message()
                    val data = Bundle()
                    msg.what = 1
                    data.putString("value", "无法访问网站\nERR_REFUSED_CONNECT")
                    msg.data = data
                    handler.sendMessage(msg)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val msg = Message()
                val data = Bundle()
                data.putString("value", response.body?.string())
                msg.data = data
                msg.what = 0
                handler.sendMessage(msg)
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}