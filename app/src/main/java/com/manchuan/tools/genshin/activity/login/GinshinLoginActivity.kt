package com.manchuan.tools.genshin.activity.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.dylanc.longan.addStatusBarHeightToPaddingTop
import com.dylanc.longan.doOnClick
import com.dylanc.longan.immerseStatusBar
import com.lianyi.paimonsnotebook.lib.information.ActivityRequestCode
import com.manchuan.tools.databinding.ActivityGinshinLoginBinding
import com.manchuan.tools.extensions.accentColor
import com.manchuan.tools.extensions.loge
import com.manchuan.tools.genshin.bean.GetGameRolesByCookieBean
import com.manchuan.tools.genshin.bean.UserBean
import com.manchuan.tools.genshin.ext.*
import com.manchuan.tools.genshin.information.ActivityResponseCode
import com.manchuan.tools.genshin.information.Constants
import com.manchuan.tools.genshin.information.JsonCacheName
import com.manchuan.tools.genshin.untils.GSON
import com.manchuan.tools.genshin.untils.RequestApi
import com.manchuan.tools.genshin.untils.ok
import com.manchuan.tools.genshin.untils.toList
import com.manchuan.tools.utils.UiUtils
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import com.skydoves.balloon.createBalloon
import com.skydoves.balloon.showAlignBottom
import org.json.JSONArray

class GinshinLoginActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityGinshinLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        immerseStatusBar(!UiUtils.isDarkMode())
        binding.startRoot.addStatusBarHeightToPaddingTop()
        binding.check.doOnClick {
            check()
        }
        val month = createBalloon(this) {
            setHeight(BalloonSizeSpec.WRAP)
            setText("如果你不知道什么是Cookie，可以使用米游社登录")
            setTextSize(15f)
            setPadding(12)
            setCornerRadius(8f)
            setBackgroundColor(accentColor())
            setBalloonAnimation(BalloonAnimation.OVERSHOOT)
            setAutoDismissDuration(4000)
            setLifecycleOwner(lifecycleOwner)
            build()
        }
        binding.button5.showAlignBottom(balloon = month)
        binding.button5.doOnClick {
            val intent = Intent(this, HoYoLabLoginActivity::class.java)
            HoYoLabLoginActivity.isAddUser = isAddUser
            startActivityForResult(intent, ActivityRequestCode.LOGIN)
        }
    }


    companion object {
        var isAddUser = false
        fun checkCookie(
            account_id: String,
            ltoken: String,
            cookie_token: String,
            block: (Boolean, GetGameRolesByCookieBean?) -> Unit,
        ) {
            val cookie = "ltuid=${account_id};ltoken=${ltoken};"
            RequestApi.getGameRolesByCookie(cookie) { it ->
                loge("错误", it.toString())
                if (it.ok) {
                    val cookie2 = "account_id=${account_id};cookie_token=${cookie_token}"
                    RequestApi.getGameRolesByCookie(cookie2) {
                        if (it.ok) {
                            val roles = GSON.fromJson(
                                it.optString("data"),
                                GetGameRolesByCookieBean::class.java
                            )
                            block(true, roles)
                        } else {
                            loge("cookie验证", it.toString())
                            block(false, null)
                        }
                    }
                } else {
                    loge("错误", it.toString())
                    block(false, null)
                }
            }
        }

        fun refreshMainUserInformation() {
            usp.edit().apply {
                putString(
                    JsonCacheName.MAIN_USER_NAME,
                    GSON.toJson(mainUser)
                )
                apply()
            }
        }
    }

    private fun check() {
        if (binding.cookie.text.isNullOrEmpty()) {
            "你还没有输入cookie".show()
            return
        }

        val cookieMap = mutableMapOf<String, String>()
        binding.cookie.text.toString().filter { !it.isWhitespace() }.split(";").forEach { split ->
            val map = split.split("=")
            cookieMap += map.first() to map.last()
        }
        val account_id =
            cookieMap[Constants.LTUID_NAME] ?: cookieMap[Constants.ACCOUNT_ID_NAME] ?: ""
        val ltoken = cookieMap[Constants.LTOKEN_NAME] ?: cookieMap["lToken"] ?: ""
        val cookie_token = cookieMap[Constants.COOKIE_TOKEN_NAME] ?: ""
        if (account_id.isNotEmpty() && ltoken.isNotEmpty() && cookie_token.isNotEmpty()) {
            checkCookie(
                account_id,
                ltoken,
                cookie_token
            ) { b: Boolean, roles: GetGameRolesByCookieBean? ->
                runOnUiThread {
                    if (b) {
                        roles!!
                        loge("test")
                        if (roles.list.size > 0) {
                            val getUserList = mutableListOf<UserBean>()
                            roles.list?.forEach { gameRole ->
                                getUserList += UserBean(
                                    gameRole.nickname,
                                    account_id,
                                    gameRole.region,
                                    gameRole.region_name,
                                    gameRole.game_uid,
                                    ltoken,
                                    cookie_token,
                                    gameRole.level
                                )
                            }
                            if (isAddUser) {
                                val userList = mutableListOf<UserBean>()
                                JSONArray(
                                    usp.getString(
                                        JsonCacheName.USER_LIST,
                                        "[]"
                                    )
                                ).toList(userList)
                                getUserList.forEach {
                                    userList.add(0, it)
                                }
                                usp.edit().apply {
                                    putString(
                                        JsonCacheName.USER_LIST,
                                        GSON.toJson(userList)
                                    )
                                    apply()
                                }
                            } else {
                                usp.edit().apply {
                                    mainUser = getUserList.first()
                                    putString(
                                        JsonCacheName.MAIN_USER_NAME,
                                        GSON.toJson(getUserList.first())
                                    )
                                    apply()
                                }
                                sp.edit {
                                    putBoolean("main_user_change", true)
                                    apply()
                                }
                                val userList = mutableListOf<UserBean>()
                                JSONArray(
                                    usp.getString(
                                        JsonCacheName.USER_LIST,
                                        "[]"
                                    )
                                ).toList(userList)
                                userList += getUserList.last()
                                usp.edit().apply {
                                    putString(
                                        JsonCacheName.USER_LIST,
                                        GSON.toJson(userList)
                                    )
                                    apply()
                                }
                            }
                            setResult(ActivityResponseCode.OK)
                            finish()
                            "Cookie设置成功"
                        } else {
                            "该账号没有原神角色信息"
                        }
                    } else {
                        "错误的Cookie:验证失败"
                    }.show()
                }
            }
        } else {
            "错误的Cookie:缺少所需的参数(account_id/ltuid,lToken,cookie_token)".showLong()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == ActivityResponseCode.OK) {
            when (requestCode) {
                ActivityRequestCode.LOGIN -> {
                    setResult(ActivityResponseCode.OK)
                    finish()
                }
            }
        }
    }

    override fun onDestroy() {
        isAddUser = false
        super.onDestroy()
    }

}