package com.manchuan.tools.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.res.AssetManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.webkit.JsResult
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.browser.customtabs.CustomTabsIntent
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebSettingsCompat.FORCE_DARK_OFF
import androidx.webkit.WebSettingsCompat.FORCE_DARK_ON
import androidx.webkit.WebViewFeature
import com.blankj.utilcode.util.ClipboardUtils
import com.dylanc.longan.safeIntentExtras
import com.gyf.immersionbar.ktx.immersionBar
import com.just.agentweb.AgentWeb
import com.just.agentweb.WebChromeClient
import com.just.agentweb.WebViewClient
import com.kongzue.dialogx.dialogs.PopTip
import com.manchuan.tools.R
import com.manchuan.tools.base.AnimationActivity
import com.manchuan.tools.base.BaseAlertDialogBuilder
import com.manchuan.tools.databinding.ActivityWebBinding
import com.manchuan.tools.extensions.colorPrimary
import com.manchuan.tools.utils.UiUtils
import org.apache.commons.lang3.StringUtils
import java.io.InputStream
import java.util.*


class WebActivity : AnimationActivity() {

    private val binding by lazy {
        ActivityWebBinding.inflate(layoutInflater)
    }

    private lateinit var agentWeb: AgentWeb

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
        immersionBar {
            titleBar(binding.toolbar)
            transparentBar()
            statusBarDarkFont(!UiUtils.isDarkMode())
        }
        agentWeb =
            AgentWeb.with(this).setAgentWebParent(binding.relWeb, FrameLayout.LayoutParams(-1, -1))
                .useDefaultIndicator(colorPrimary()).setWebChromeClient(webChromeClient)
                .setWebViewClient(webViewClient).createAgentWeb().ready()
                .go(safeIntentExtras<String>("url").value)
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    WebSettingsCompat.setForceDark(agentWeb.webCreator.webView.settings,
                        FORCE_DARK_ON)
                }
                Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                    WebSettingsCompat.setForceDark(agentWeb.webCreator.webView.settings,
                        FORCE_DARK_OFF)
                }
                else -> {
                    //
                }
            }
        }
    }

    override fun onPause() {
        agentWeb.webLifeCycle.onPause()
        super.onPause()
    }

    override fun onResume() {
        agentWeb.webLifeCycle.onResume()
        super.onResume()
    }

    override fun onDestroy() {
        agentWeb.webLifeCycle.onDestroy()
        super.onDestroy()
    }

    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.web_refresh -> agentWeb.webCreator.webView.reload()
            R.id.copy_url -> {
                ClipboardUtils.copyText(agentWeb.webCreator.webView.url)
                PopTip.show("已复制网址到剪贴板")
            }
            R.id.open_in_browser -> CustomTabsIntent.Builder().build()
                .launchUrl(this@WebActivity, Uri.parse(agentWeb.webCreator.webView.url))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (agentWeb.handleKeyEvent(keyCode, event)) {
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    private val webViewClient: WebViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?,
        ): Boolean {
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            val assetManager: AssetManager = assets
            val inputStream: InputStream = assetManager.open("remove.js")
            val stringBuilder = StringBuilder()
            var len: Int
            val buf = ByteArray(4096)
            while (inputStream.read(buf).also { len = it } != -1) {
                stringBuilder.append(String(buf, 0, len))
            }
            val js = stringBuilder.toString()
            inputStream.close()
            view.evaluateJavascript(js, null)
            if (url.contains("appid=1101326786")) {
                val openId = StringUtils.substringBetween(url, "&openid=", "&appid")
                val accessToken = StringUtils.substringBetween(url, "&access_token=", "&pay_token")
                BaseAlertDialogBuilder(this@WebActivity).setTitle("悦动圈登录信息")
                    .setMessage("ID:$openId\nTOKEN:$accessToken").setNegativeButton("取消", null)
                    .setCancelable(false).setPositiveButton("复制并返回") { dialog, which ->
                        ClipboardUtils.copyText("悦动圈登录信息", "ID:$openId\nTOKEN:$accessToken")
                        finish()
                    }.show()
            }
        }
    }

    private val webChromeClient: WebChromeClient = object : WebChromeClient() {
        override fun onJsAlert(
            webView: WebView,
            url: String,
            message: String,
            result: JsResult,
        ): Boolean {
            BaseAlertDialogBuilder(webView.context).setTitle("网页提示").setMessage(message)
                .setCancelable(false)
                .setPositiveButton("确定") { dialogInterface: DialogInterface, i: Int ->

                }.create().show()
            result.confirm()
            return true
        }

        override fun onReceivedTitle(view: WebView, title: String) {
            super.onReceivedTitle(view, title)
            supportActionBar?.title = title
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.web_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    companion object {
        @SuppressLint("ObsoleteSdkInt")
        fun getUserAgent(ctx: Context?): String {
            val system_ua = System.getProperty("http.agent")
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                WebSettings.getDefaultUserAgent(ctx) + "__" + system_ua
            } else {
                WebView(ctx!!).settings.userAgentString + "__" + system_ua
            }
        }
    }
}