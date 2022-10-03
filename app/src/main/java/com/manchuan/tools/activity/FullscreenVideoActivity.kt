package com.manchuan.tools.activity

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.dylanc.longan.safeIntentExtras
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ktx.immersionBar
import com.just.agentweb.AgentWeb
import com.manchuan.tools.databinding.ActivityFullscreenVideoBinding
import com.manchuan.tools.extensions.accentColor
import com.manchuan.tools.extensions.colorPrimary
import me.jingbin.web.ByWebView
import me.jingbin.web.OnByWebClientCallback
import me.jingbin.web.OnTitleProgressCallback

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenVideoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullscreenVideoBinding
    private lateinit var agentWeb: AgentWeb
    private lateinit var byWebView: ByWebView

    @SuppressLint("ClickableViewAccessibility", "SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullscreenVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        immersionBar {
            fullScreen(true)
            hideBar(BarHide.FLAG_HIDE_BAR)
        }
        byWebView =
            ByWebView.with(this).setWebParent(binding.layout, FrameLayout.LayoutParams(-1, -1))
                .setOnTitleProgressCallback(onTitleProgressCallback)
                .setOnByWebClientCallback(onByWebClientCallback)
                .useWebProgress(colorPrimary(), accentColor(), 3)
                .loadUrl(safeIntentExtras<String>("url").value)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (byWebView.handleKeyEvent(keyCode, event)) {
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    private val onTitleProgressCallback: OnTitleProgressCallback =
        object : OnTitleProgressCallback() {
            override fun onReceivedTitle(title: String) {
                supportActionBar?.title = title
            }

            override fun onProgressChanged(newProgress: Int) {}
        }

    private val onByWebClientCallback: OnByWebClientCallback = object : OnByWebClientCallback() {
        override fun onPageFinished(view: WebView, url: String) {
        }

        override fun isOpenThirdApp(url: String): Boolean {
            // 处理三方链接
            return false
        }
    }

    override fun onPause() {
        super.onPause()
        byWebView.onPause()
    }

    override fun onResume() {
        super.onResume()
        byWebView.onResume()
    }

    override fun onDestroy() {
        byWebView.onDestroy()
        super.onDestroy()
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