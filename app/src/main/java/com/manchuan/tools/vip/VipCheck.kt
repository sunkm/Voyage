package com.manchuan.tools.vip

import android.annotation.SuppressLint
import android.content.Context
import android.text.InputType
import com.drake.serialize.serialize.serialLazy
import com.dylanc.longan.asActivity
import com.dylanc.longan.design.snackbar
import com.dylanc.longan.doOnClick
import com.dylanc.longan.startActivity
import com.lxj.androidktx.core.tip
import com.manchuan.tools.R
import com.manchuan.tools.activity.WebActivity
import com.manchuan.tools.base.BaseAlertDialogBuilder
import com.manchuan.tools.databinding.DialogUserinfoBinding
import com.manchuan.tools.extensions.layoutInflater
import com.manchuan.tools.extensions.loge
import com.maxkeppeler.sheets.core.IconButton
import com.maxkeppeler.sheets.input.InputSheet
import com.maxkeppeler.sheets.input.type.InputEditText

object VipCheck {

    private var login: Boolean by serialLazy(false)

    fun isLogin(): Boolean {
        return login
    }

    @SuppressLint("SetTextI18n")
    fun loginUi(context: Context) {
        InputSheet().show(context) {
            title("激活")
            with(InputEditText {
                required()
                startIconDrawable(R.drawable.ic_primary_card_giftcard_24)
                inputType(InputType.TYPE_CLASS_TEXT)
                label("请在下方输入您购买的卡密并激活。\n如果卡密绑定设备上限，请联系我们解绑。\nQQ:3299699002  微信:mysteryoflovem\n邮箱:pedragons@outlook.com")
                hint("卡密")
                changeListener { value ->

                }
                resultListener { value -> }
            })
            withIconButton(IconButton(R.drawable.baseline_shopping_cart_24)) {
                context.startActivity<WebActivity>("url" to "https://v1.zhiyunka.com/links/4559E364")
            }
            onNegative { }
            onPositive { result ->
                val text = result.getString("0") // Read value of inputs by index
                if (text?.isNotEmpty() == true) {
                    context.tip("该入口已关闭")
                } else {
                    context.tip("请输入卡密")
                }
            }
        }
    }

    fun infoUi(context: Context) {
        runCatching {
            val loginDialog = BaseAlertDialogBuilder(context).create()
            val loginBinding = DialogUserinfoBinding.inflate(context.layoutInflater)
            loginBinding.confirm.doOnClick {
                loginDialog.dismiss()
            }
            val stringBuilder = StringBuilder()
            //stringBuilder.append("卡密类型:${Global.userModel.amount}")
            //stringBuilder.append("\n到期时间:${Global.userModel.endTime}")
            //stringBuilder.append("\n绑定设备码:${Global.userModel.bindDevice}")
            //stringBuilder.append("\n\n如果您对此还有疑问，请联系我们。\nQQ:3299699002  微信:mysteryoflovem\n邮箱:pedragons@outlook.com")
            loginBinding.info.text = stringBuilder
            loginDialog.setView(loginBinding.root)
            loginDialog.show()
        }.onFailure {
            loge("错误", it)
            context.asActivity()?.snackbar("数据丢失，是否重新登录？", "确定") {
                login = false
                loginUi(context)
            }
        }
    }

}