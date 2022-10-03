package com.manchuan.tools.user.listener

import com.drake.channel.sendEvent
import com.google.gson.Gson
import com.lxj.androidktx.core.tip
import com.manchuan.tools.application.App
import com.manchuan.tools.database.Global
import com.manchuan.tools.extensions.loge
import com.manchuan.tools.model.QQInfo
import com.manchuan.tools.model.QQLogin
import com.manchuan.tools.model.TencentLoginModel
import com.tencent.connect.UserInfo
import com.tencent.mmkv.MMKV
import com.tencent.tauth.DefaultUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import org.json.JSONObject

open class TencentLoginInterface(private val tencent: Tencent) : DefaultUiListener() {
    private val kv = MMKV.defaultMMKV()
    override fun onComplete(response: Any?) {
        if (response == null) {
            sendEvent(TencentLoginModel(false, null), "tencent_login")
            loge("返回为空,登录失败")
            return
        }
        val jsonResponse = response as JSONObject
        if (jsonResponse.length() == 0) {
            sendEvent(TencentLoginModel(false, null), "tencent_login")
            loge("返回为空,登录失败")
            return
        }
        loge("登录成功")
        doComplete(response)
        getQqInfo()
    }

    private fun doComplete(values: JSONObject?) {
        loge(tag = "QQ登录回调", values)
        val gson = Gson()
        val qqLogin = gson.fromJson(values.toString(), QQLogin::class.java)
        App.tencent.setAccessToken(qqLogin.access_token, qqLogin.expires_in.toString())
        App.tencent.openId = qqLogin.openid
        sendEvent(TencentLoginModel(false, qqLogin), "tencent_login")
    }

    override fun onError(e: UiError) {
        loge("fund", "onError: ${e.errorDetail}")
    }

    override fun onCancel() {
        "取消登录".tip()
    }

    private fun getQqInfo() {
        val qqToken = tencent.qqToken
        val info = UserInfo(App.context, qqToken)
        info.getUserInfo(object : TencentLoginInterface(tencent) {
            override fun onComplete(response: Any?) {
                Global.qqUserModel = Gson().fromJson(response.toString(), QQInfo::class.java)
            }
        })
    }

}
