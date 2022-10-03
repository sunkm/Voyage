package com.manchuan.tools.genshin.activity.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.*
import androidx.fragment.app.Fragment
import com.dylanc.longan.addStatusBarHeightToPaddingTop
import com.dylanc.longan.navigationBarHeight
import com.gyf.immersionbar.ImmersionBar
import com.manchuan.tools.R
import com.manchuan.tools.databinding.FragmentMapBinding
import com.manchuan.tools.extensions.addPaddingBottom
import com.manchuan.tools.extensions.dp
import com.manchuan.tools.genshin.ext.mainUser
import com.manchuan.tools.genshin.ext.setViewMarginBottomByNavigationBarHeight
import com.manchuan.tools.genshin.information.Constants
import com.manchuan.tools.genshin.information.MiHoYoAPI
import com.manchuan.tools.utils.UiUtils

class MapFragment : Fragment(R.layout.fragment_map) {
    lateinit var bind: FragmentMapBinding

    @SuppressLint("SetJavaScriptEnabled", "ResourceType")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind = FragmentMapBinding.bind(view)
        bind.rootLay.addStatusBarHeightToPaddingTop()
        ImmersionBar.with(this)
            .transparentNavigationBar()
            .statusBarDarkFont(!UiUtils.isDarkMode())
            .init()
        val resourceId = resources.getIdentifier(
            "design_bottom_navigation_height",
            "dimen",
            requireContext().packageName
        )
        var height = 0
        if (resourceId > 0) {
            height = resources.getDimensionPixelSize(resourceId)
        }
        val density = resources.displayMetrics.density
        val dp = height / density
        bind.rootLay.addPaddingBottom(54.dp)
        bind.rootLay.addPaddingBottom(navigationBarHeight.dp)
        with(bind.web) {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            settings.javaScriptEnabled = true
            settings.loadWithOverviewMode = true
            settings.domStorageEnabled = true
            settings.blockNetworkImage = false
            settings.useWideViewPort = true

            loadUrl(MiHoYoAPI.MAP_V2)

            //设置webView跳转到新页面时在当前页面跳转 而不是创建新的意图
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?,
                ): Boolean {
                    view?.loadUrl(url ?: "")
                    return true
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    MiHoYoAPI.getCookie(mainUser!!).split(";")
                    val cookieManager = CookieManager.getInstance()
                    cookieManager.removeSessionCookies(null)
                    cookieManager.setAcceptCookie(true)
                    cookieManager.setCookie(
                        ".mihoyo.com",
                        "${Constants.LTOKEN_NAME} = ${mainUser!!.lToken}"
                    )
                    cookieManager.setCookie(
                        ".mihoyo.com",
                        "${Constants.LTUID_NAME} = ${mainUser!!.loginUid}"
                    )
                    cookieManager.flush()
                    //移除顶部二维码
                    loadUrl("javascript:(function() {document.getElementsByClassName('mhy-bbs-app-header')[0].style.display = 'none';})()")
                }

            }

        }

        bind.reload.setOnClickListener {
            bind.web.reload()
        }
        setViewMarginBottomByNavigationBarHeight(bind.web)
    }
}