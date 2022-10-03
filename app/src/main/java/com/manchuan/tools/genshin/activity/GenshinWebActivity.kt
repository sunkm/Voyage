package com.manchuan.tools.genshin.activity

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.just.agentweb.WebViewClient
import com.manchuan.tools.databinding.ActivityGenshinWebBinding
import com.manchuan.tools.genshin.bean.ArticleBean
import com.manchuan.tools.genshin.ext.mainUser
import com.manchuan.tools.genshin.ext.show
import com.manchuan.tools.genshin.html.ImageGetter
import com.manchuan.tools.genshin.information.Constants
import com.manchuan.tools.genshin.information.Format
import com.manchuan.tools.genshin.information.MiHoYoAPI
import com.manchuan.tools.genshin.untils.GSON
import com.manchuan.tools.genshin.untils.RequestApi
import com.manchuan.tools.genshin.untils.ok
import kotlin.concurrent.thread

class GenshinWebActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityGenshinWebBinding.inflate(layoutInflater)
    }

    companion object {
        lateinit var articleId: String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initArticle()
        binding.content.movementMethod = LinkMovementMethod()
    }

    private var loading = true

    private fun initArticle() {
        binding.state.showLoading()
        RequestApi.get(
            MiHoYoAPI.getArticleDataUrl(articleId),
            arrayOf("Referer" to "https://bbs.mihoyo.com/")
        ) {
            if (it.ok) {
                val article = GSON.fromJson(it.optString("data"), ArticleBean::class.java)
                thread {
                    try {
                        val htmlText = HtmlCompat.fromHtml(
                            article.post.post.content, HtmlCompat.FROM_HTML_MODE_LEGACY,
                            ImageGetter(), null
                        )
                        runOnUiThread {
                            binding.content.text = htmlText
                            binding.title.text = article.post.post.subject
                            binding.createTime.text =
                                Format.TIME_HYPHEN_MONTH_DAY.format((article.post.post.created_at.toLong() * 1000))
                            binding.state.showContent()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        runOnUiThread {
                            "获取文章失败啦".show()
                            finish()
                        }
                    }
                }
            } else {
                runOnUiThread {
                    binding.web.apply {
                        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        settings.javaScriptEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.domStorageEnabled = true
                        settings.blockNetworkImage = false
                        settings.useWideViewPort = true
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                if (progress == 100 && loading) {
                                    loading = false
                                    binding.web.show()
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
                                    cookieManager.setCookie(
                                        ".mihoyo.com",
                                        "${Constants.COOKIE_TOKEN_NAME} = ${mainUser!!.cookieToken}"
                                    )
                                    cookieManager.setCookie(
                                        ".mihoyo.com",
                                        "${Constants.ACCOUNT_ID_NAME} = ${mainUser!!.loginUid}"
                                    )
                                    cookieManager.flush()

                                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
                                    "当前加载的内容是网页\n网页内部分功能不可用\n竖屏锁定已解除".show()
                                    thread {
                                        Thread.sleep(500)
                                        runOnUiThread {
                                            //隐藏一般文章界面的logo
                                            loadUrl("javascript:(function() {document.getElementsByClassName('mhy-bbs-app-header')[0].style.display = 'none';})()")
                                            //隐藏特殊界面的logo
                                            loadUrl("javascript:(function() {document.getElementsByClassName('components-common-assets-bbs-header___bbs-header--2rXKS')[0].style.display = 'none';})()")
                                        }
                                    }
                                }

                            }

                            override fun onReceivedHttpError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                errorResponse: WebResourceResponse?,
                            ) {
                                super.onReceivedHttpError(view, request, errorResponse)
                                runOnUiThread {
                                    "加载文章失败啦,请重新尝试一下吧!".show()
                                    finish()
                                }
                            }
                        }
                        loadUrl(articleId)
                    }
                }
            }
        }
    }

}