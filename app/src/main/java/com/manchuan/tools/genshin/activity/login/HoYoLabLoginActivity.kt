package com.manchuan.tools.genshin.activity.login

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.JsResult
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.drake.statusbar.immersive
import com.dylanc.longan.immerseStatusBar
import com.just.agentweb.AgentWeb
import com.just.agentweb.WebChromeClient
import com.just.agentweb.WebViewClient
import com.manchuan.tools.databinding.ActivityWebBinding
import com.manchuan.tools.extensions.getColorByAttr
import com.manchuan.tools.genshin.bean.GetGameRolesByCookieBean
import com.manchuan.tools.genshin.bean.UserBean
import com.manchuan.tools.genshin.ext.mainUser
import com.manchuan.tools.genshin.ext.sp
import com.manchuan.tools.genshin.ext.usp
import com.manchuan.tools.genshin.information.ActivityResponseCode
import com.manchuan.tools.genshin.information.Constants
import com.manchuan.tools.genshin.information.JsonCacheName
import com.manchuan.tools.genshin.information.MiHoYoAPI
import com.manchuan.tools.genshin.untils.GSON
import com.manchuan.tools.genshin.untils.RequestApi
import com.manchuan.tools.genshin.untils.ok
import com.manchuan.tools.genshin.untils.toList
import com.manchuan.tools.utils.UiUtils
import org.json.JSONArray

class HoYoLabLoginActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityWebBinding.inflate(layoutInflater)
    }


    companion object {
        var isAddUser = false
    }

    private val cookieMap = mutableMapOf<String, String>()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        immersive(binding.toolbar)
        val agentWeb = AgentWeb.with(this)
            .setAgentWebParent(binding.relWeb, FrameLayout.LayoutParams(-1, -1))
            .useDefaultIndicator(getColorByAttr(com.google.android.material.R.attr.colorAccent)) //进度条
            .setWebChromeClient(webChromeClient)
            .setWebViewClient(webViewClient)
            .setSecurityType(AgentWeb.SecurityType.DEFAULT_CHECK)
            .createAgentWeb()
            .ready().go(MiHoYoAPI.LOGIN)
        agentWeb.agentWebSettings?.apply {
            this.webSettings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
            }
        }
    }

    //WebViewClient主要帮助WebView处理各种通知、请求事件
    private val webViewClient: WebViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) { //页面加载完成
            val cookieManager = CookieManager.getInstance()
            val cookies = cookieManager.getCookie(url)
            //当网页加载完毕
            if (view.progress == 100 && cookies != null && cookies.isNotEmpty()) {
                val cookieSplit = cookies.split(";")
                var isLogin = false
                cookieSplit.forEach {
                    val cookie = it.trim().split("=")
                    cookieMap += cookie.first().trim() to cookie.last().trim()
                    when (cookie.first()) {
                        Constants.ACCOUNT_ID_NAME -> isLogin = true
                    }
                }
                //当登录完成时
                if (isLogin && isAddUser) {
                    val cookie =
                        "ltuid=${cookieMap[Constants.LTUID_NAME] ?: ""};ltoken=${cookieMap[Constants.LTOKEN_NAME] ?: ""};account_id=${cookieMap[Constants.ACCOUNT_ID_NAME] ?: ""};cookie_token=${cookieMap[Constants.COOKIE_TOKEN_NAME] ?: ""}"
                    getGameRolesByCookie(cookie) {
                        val gameRoleList = mutableListOf<UserBean>()
                        if (it != null) {
                            it.list?.forEach { gameRole ->
                                gameRoleList += UserBean(
                                    gameRole.nickname,
                                    cookieMap[Constants.LTUID_NAME]!!,
                                    gameRole.region,
                                    gameRole.region_name,
                                    gameRole.game_uid,
                                    cookieMap[Constants.LTOKEN_NAME]!!,
                                    cookieMap[Constants.COOKIE_TOKEN_NAME]!!,
                                    gameRole.level
                                )
                                addUserToList(gameRoleList)
                            }
                        }
                        clearCookie()
                    }
                } else if (isLogin) {
                    mainUser?.lToken = cookieMap[Constants.LTOKEN_NAME] ?: ""
                    mainUser?.loginUid = cookieMap[Constants.LTUID_NAME] ?: ""
                    mainUser?.cookieToken = cookieMap[Constants.COOKIE_TOKEN_NAME] ?: ""
                    RequestApi.get(MiHoYoAPI.GET_GAME_ROLES_BY_COOKIE) {
                        if (it.ok) {
                            val roles = GSON.fromJson(
                                it.optString("data"),
                                GetGameRolesByCookieBean::class.java
                            )
                            with(roles.list.first()) {
                                mainUser?.gameLevel = level
                                mainUser?.nickName = nickname
                                mainUser?.region = region
                                mainUser?.regionName = region_name
                                mainUser?.gameUid = game_uid
                            }

                            if (roles.list.size > 1) {
                                val gameRoleList = mutableListOf<UserBean>()
                                (1 until roles.list.size).forEach { index ->
                                    with(roles.list[index]) {
                                        gameRoleList += UserBean(
                                            nickname,
                                            cookieMap[Constants.LTUID_NAME]!!,
                                            region,
                                            region_name,
                                            game_uid,
                                            cookieMap[Constants.LTOKEN_NAME]!!,
                                            cookieMap[Constants.COOKIE_TOKEN_NAME]!!,
                                            level
                                        )
                                    }
                                }
                                addUserToList(gameRoleList)
                            }

                            usp.edit().apply {
                                putString(JsonCacheName.MAIN_USER_NAME, GSON.toJson(mainUser))
                                apply()
                            }
                            sp.edit {
                                putBoolean("main_user_change", true)
                                apply()
                            }

                            //请理Cookie
                            clearCookie()
                        }
                    }
                }
            }
        }
    }

    //WebChromeClient主要辅助WebView处理Javascript的对话框、网站图标、网站title、加载进度等
    private val webChromeClient: WebChromeClient = object : WebChromeClient() {
        //不支持js的alert弹窗，需要自己监听然后通过dialog弹窗
        override fun onJsAlert(
            webView: WebView,
            url: String,
            message: String,
            result: JsResult,
        ): Boolean {
            val localBuilder = AlertDialog.Builder(webView.context)
            localBuilder.setMessage(message).setPositiveButton("确定", null)
            localBuilder.setCancelable(false)
            localBuilder.create().show()
            result.confirm()
            return true
        }

        //获取网页标题
        override fun onReceivedTitle(view: WebView, title: String) {
            super.onReceivedTitle(view, title)
            supportActionBar?.apply {
                this.title = title
                setDisplayHomeAsUpEnabled(true)
            }
        }
    }


    private fun addUserToList(gameRoleList: List<UserBean>) {
        val userList = mutableListOf<UserBean>()
        JSONArray(usp.getString(JsonCacheName.USER_LIST, "[]")).toList(userList)
        gameRoleList.forEach {
            userList.add(0, it)
        }
        usp.edit().apply {
            putString(JsonCacheName.USER_LIST, GSON.toJson(userList))
            apply()
        }
    }

    private fun getGameRolesByCookie(cookie: String, block: (GetGameRolesByCookieBean?) -> Unit) {
        RequestApi.getGameRolesByCookie(cookie) {
            if (it.ok) {
                val roles = GSON.fromJson(
                    it.optString("data"),
                    GetGameRolesByCookieBean::class.java
                )
                block(roles)
            } else {
                block(null)
            }
        }
    }


    private fun clearCookie(finish: Boolean = true) {
        runOnUiThread {
            CookieManager.getInstance().removeAllCookies {
                if (it) {
                    CookieManager.getInstance().flush()
                    setResult(ActivityResponseCode.OK)
                    if (finish) {
                        finish()
                    }
                }
            }
        }
    }

}