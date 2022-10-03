package com.manchuan.tools.user

import android.annotation.SuppressLint
import com.drake.net.Post
import com.drake.net.utils.scopeNet
import com.dylanc.longan.encryptMD5
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.kongzue.dialogx.interfaces.DialogLifecycleCallback
import com.manchuan.tools.database.Global
import com.manchuan.tools.json.SerializationConverter
import com.manchuan.tools.model.UserModel
import com.manchuan.tools.user.listener.TencentLoginInterface
import com.manchuan.tools.user.model.LoginModel
import com.tencent.mmkv.MMKV

@SuppressLint("StaticFieldLeak")
var appId = "10000"
var appKey = "3d88fb643f48a81176f8ed9a96c3aacd"
var host = "https://user.fengmuchuan.cn/api.php"
var login = "$host?act=user_logon&app=$appId"
var tencentLoginUrl = "$host?act=qq_login&app=$appId"
var register = "$host?act=user_reg&app=$appId"
var forgetPassword = "$host?act=seek_pass&app=$appId"
var userInfo = "$host?act=get_info&app=$appId"
var header = "$host?act=upic&app=$appId"
val timeMills: Long
    get() = System.currentTimeMillis()
lateinit var mmkv: MMKV

val idVerify: String
    get() = Global.idVerify


fun reason(code: Int?): String {
    return when (code) {
        201 -> "失败"
        100 -> "请绑定应用ID"
        101 -> "应用不存在"
        102 -> "应用已关闭"
        103 -> "已关闭登录"
        104 -> "签名为空"
        105 -> "数据过期"
        106 -> "签名有误"
        107 -> "数据为空"
        108 -> "未发现时间变量"
        110 -> "请填写账号"
        111 -> "请填写密码"
        112 -> "请填写机器码"
        113 -> "账号密码不正确"
        114 -> "账号已被禁用"
        115 -> "账号已存在"
        116 -> "账号不合法"
        117 -> "账号注册频率过快"
        118 -> "邀请人不存在"
        119 -> "密码不合法"
        120 -> "验证码为空"
        121 -> "管理员未启动邮箱验证码功能"
        122 -> "账号不存在"
        123 -> "验证码发送频率过快"
        124 -> "验证码不正确"
        125 -> "TOKEN为空"
        126 -> "TOKEN不合法"
        127 -> "TOKEN不存在"
        128 -> "已设置账号不可更改"
        129 -> "名称为空"
        130 -> "订单号为空"
        131 -> "请选择支付方式"
        132 -> "请选择商品"
        133 -> "该应用未开启支付功能"
        134 -> "请先设置异步通知地址"
        135 -> "不支持该支付方式"
        136 -> "商品不存在"
        137 -> "订单入库失败"
        138 -> "支付错误信息"
        139 -> "支付未知错误"
        140 -> "请填写订单信息"
        141 -> "提交方式有误"
        142 -> "上传类型不支持"
        143 -> "积分ID为空"
        144 -> "积分事件不存在"
        145 -> "积分事件已关闭"
        146 -> "签到功能未启用"
        147 -> "今天已经签到过了"
        148 -> "卡密为空"
        149 -> "卡密不存在"
        150 -> "卡密已使用"
        151 -> "卡密已被禁用"
        152 -> "卡密类型不一致"
        153 -> "订单不存在"
        154 -> "等待支付"
        155 -> "未知订单状态"
        156 -> "请输入openid"
        157 -> "请输入access_token"
        158 -> "身份信息错误"
        159 -> "微信openid有误"
        160 -> "该微信已绑定其他账号"
        161 -> "请输入QQ互联ID"
        162 -> "未知登录错误"
        163 -> "该应用不允许使用此种登录方式"
        164 -> "该应用不允许使用当前操作"
        165 -> "当前账号未绑定邮箱"
        166 -> "一张被充值的卡密只能充值给一个账号或者一张主卡密"
        167 -> "不支持积分卡登录"
        168 -> "订单已存在"
        199 -> "您已经是永久会员了"
        400 -> "没有相关操作"
        401 -> "错误的数据"
        else -> "错误"
    }
}

private fun getLoginSign(account: String, password: String): String {
    return ("account=$account&password=$password&markcode=$idVerify&t=$timeMills&$appKey").encryptMD5()
}

fun tencentLoginSign(openid: String, accessToken: String, inviteId: String): String {
    return ("openid=$openid&access_token=$accessToken&qqappid=${Global.AppId}&inv=$inviteId&markcode=$idVerify&t=$timeMills&$appKey").encryptMD5()
}

private fun getForgetPasswordSign(
    email: String,
    verifyCode: String,
    password: String,
): String {
    return ("email=$email&crc=$verifyCode&newpassword=$password&t=$timeMills&$appKey").encryptMD5()
}

private fun getRegSign(name: String, account: String, password: String, inv: String): String {
    return ("name=$name&user=$account&password=$password&inv=$inv&markcode=$idVerify&t=$timeMills&$appKey").encryptMD5()
}

private fun getHeaderSign(token: String, upt: String): String {
    return ("token=$token&upt=$upt&t=$timeMills&$appKey").encryptMD5()
}

private fun getUserInfoSign(token: String): String {
    return ("token=$token&t=$timeMills&$appKey").encryptMD5()
}


private lateinit var listener: TencentLoginInterface

fun login(
    account: String,
    password: String, success: (UserModel) -> Unit, failed: (String) -> Unit,
) {
    scopeNet {
        WaitDialog.show("正在登录")
        val string = Post<LoginModel>(login) {
            param("account", account)
            param("password", password)
            param("markcode", idVerify)
            param("t", timeMills)
            param("sign", getLoginSign(account, password))
            converter = SerializationConverter("200", "code", "code")
        }.await().msg
        val userModel = UserModel(string.token,
            string.info.id,
            string.info.pic,
            string.info.name,
            string.info.vip,
            string.info.fen)
        Global.userModel.value = userModel
        TipDialog.show("登录成功", WaitDialog.TYPE.SUCCESS).dialogLifecycleCallback =
            object : DialogLifecycleCallback<WaitDialog>() {
                override fun onShow(dialog: WaitDialog?) {
                    super.onShow(dialog)
                }

                override fun onDismiss(dialog: WaitDialog?) {
                    super.onDismiss(dialog)
                    success.invoke(userModel)
                }

            }
        success.invoke(userModel)
    }.catch {
        TipDialog.show("登录失败", WaitDialog.TYPE.ERROR)
        runCatching {
            failed.invoke(reason(it.message?.toInt()))
        }.onFailure {
            failed.invoke(reason(201))
        }
    }
}

/*

@JvmStatic
fun userInfo(token: String, listener: OnUserListener) {
    OkGo.post<String>(userInfo)
        .params("token", token)
        .params("t", timeMills)
        .params("sign", getUserInfoSign(token))
        .execute(object : StringCallback() {
            override fun onSuccess(response: Response<String>) {
                try {
                    val jsonObject = JSONObject(response.body())
                    val code = jsonObject.optString("code")
                    if (code == "200") {
                        val jsonObject1 = JSONObject(jsonObject.getJSONObject("msg").toString())
                        listener.onSuccess(token, jsonObject1.toString())
                    } else {
                        listener.onFail("账号不存在")
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    listener.onFail("解析JSON失败")
                }
            }
        })
}

fun forgetPassword(
    email: String,
    verifyCode: String,
    newpassword: String,
    listener: OnUserListener
) {
    OkGo.post<String>(forgetPassword)
        .params("email", email)
        .params("crc", verifyCode)
        .params("newpassword", newpassword)
        .params("t", timeMills)
        .params("sign", getForgetPasswordSign(email, vertifyCode, newpassword))
        .execute(object : StringCallback() {
            override fun onSuccess(response: Response<String>) {
                try {
                    val json = JSONObject(response.body())
                    val code = json.optString("code")
                    if (code == "200") {
                        val msg = JSONObject(json.optString("msg"))
                        val token = msg.optString("token")
                        listener.onSuccess(token, response.body())
                    } else {
                        listener.onFail(json.optString("msg"))
                    }
                } catch (ignored: JSONException) {
                }
            }

            override fun onError(response: Response<String>) {
                super.onError(response)
                listener.onFail(response.body())
            }
        })
}

fun uploadHeader(token: String, file: File, listener: OnUserListener) {
    val string = UploadUtil.getInstance()
        .upload("$header&token=$token&upt=bbp&t=$timeMills&sign=${getHeaderSign(token, "bbp")}", file)
    val json = JSON.parseObject(string.string())
    if (json.getIntValue("code") == 200) {
        listener.onSuccess(token,json.getString("msg"))
    } else {
        listener.onFail(json.getString("msg"))
    }
}

fun login(account: String, password: String, listener: OnUserListener) {
    //PopTip.show(imeiConfig)
    OkGo.post<String>(login)
        .params("account", account)
        .params("password", password)
        .params("markcode", imeiConfig)
        .params("t", timeMills)
        .params("sign", getLoginSign(account, password))
        .execute(object : StringCallback() {
            override fun onSuccess(response: Response<String>) {
                val data = response.body()
                try {
                    val json = JSONObject(response.body())
                    val code = json.optString("code")
                    if (code == "200") {
                        val msg = JSONObject(json.optString("msg"))
                        val token = msg.optString("token")
                        listener.onSuccess(token, response.body())
                    } else {
                        listener.onFail(json.optString("msg"))
                    }
                } catch (ignored: JSONException) {
                }
            }

            override fun onError(response: Response<String>) {
                super.onError(response)
                listener.onFail(response.body())
            }
        })
}

@JvmStatic
fun register(
    name: String,
    user: String,
    password: String,
    inv: String,
    listener: OnUserListener
) {
    OkGo.post<String>(register)
        .params("name", name)
        .params("user", user)
        .params("password", password)
        .params("inv", inv)
        .params("markcode", imeiConfig)
        .params("t", timeMills)
        .params("sign", getRegSign(name, user, password, inv))
        .execute(object : StringCallback() {
            override fun onSuccess(response: Response<String>) {
                try {
                    val json = JSONObject(response.body())
                    val code = json.optString("code")
                    if (code == "200") {
                        listener.onSuccess(json.optString("msg"), response.body())
                    } else {
                        listener.onFail(json.optString("msg"))
                    }
                } catch (ignored: JSONException) {
                    listener.onFail("解析错误")
                }
            }

            override fun onError(response: Response<String>) {
                super.onError(response)
                listener.onFail(response.body())
            }
        })
}
/
 */