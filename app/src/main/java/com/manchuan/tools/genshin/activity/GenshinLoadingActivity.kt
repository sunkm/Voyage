package com.manchuan.tools.genshin.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dylanc.longan.doOnClick
import com.lianyi.paimonsnotebook.lib.information.ActivityRequestCode
import com.lxj.androidktx.core.edit
import com.lxj.androidktx.core.gone
import com.lxj.androidktx.core.visible
import com.manchuan.tools.application.App
import com.manchuan.tools.databinding.ActivityGenshinLoadingBinding
import com.manchuan.tools.extensions.loge
import com.manchuan.tools.genshin.activity.login.GinshinLoginActivity
import com.manchuan.tools.genshin.bean.GetGameRolesByCookieBean
import com.manchuan.tools.genshin.bean.UpdateInformation
import com.manchuan.tools.genshin.ext.*
import com.manchuan.tools.genshin.information.ActivityResponseCode
import com.manchuan.tools.genshin.information.Constants
import com.manchuan.tools.genshin.information.JsonCacheName

class GenshinLoadingActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityGenshinLoadingBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        checkCookie()
        binding.check.doOnClick {
            goLogin()
        }
        UpdateInformation.refreshJSON()
    }

    private var initFinished = false

    private fun checkCookie() {
        if (mainUser?.isNull() == true) {
            //设置米游社Cookie
            goLogin()
        } else {
            loge(mainUser?.loginUid!!)
            GinshinLoginActivity.checkCookie(
                mainUser?.loginUid!!,
                mainUser?.lToken!!,
                mainUser?.cookieToken!!
            ) { b: Boolean, roles: GetGameRolesByCookieBean? ->
                if (b) {
                    //更新游戏等级
                    with(mainUser!!) {
                        gameLevel = roles!!.list.first().level
                    }
                    GinshinLoginActivity.refreshMainUserInformation()
                    initInfo()
                    initFinished = true
                } else {
                    goLogin()
                    removeMainUser()
                    runOnUiThread {
                        "Cookie失效,请重新设置Cookie".showLong()
                    }
                }
            }
        }
        showView()
    }

    private fun showView() {
        if (mainUser?.isNull() == true) {
            binding.loading.gone()
            binding.cookie.visible()
        } else {
            binding.cookie.gone()
            binding.loading.visible()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == ActivityResponseCode.OK) {
            when (requestCode) {
                ActivityRequestCode.SET_COOKIE -> {
                    checkCookie()
                }
            }
        }
    }

    //前往登录
    private fun goLogin() {
        val intent = Intent(this, GinshinLoginActivity::class.java)
        startActivityForResult(intent, ActivityRequestCode.SET_COOKIE)
    }


    //移除缓存的默认账号
    private fun removeMainUser() {
        usp.edit {
            remove(JsonCacheName.MAIN_USER_NAME)
            apply()
        }
    }

    private fun initInfo() {
        //判断上次启动时版本号和本次版本号是否相同 不同则刷新列表
        if (sp.getString(Constants.LAST_LAUNCH_APP_NAME, "") != App.APP_VERSION_NAME) {
            UpdateInformation.refreshJSON()
        }
        runOnUiThread {
            goA<GenshinActivity>()
            finish()
        }
    }

}