package com.manchuan.tools.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.fastjson.JSON
import com.blankj.utilcode.util.ClipboardUtils
import com.drake.net.Get
import com.drake.net.utils.scopeNetLife
import com.drake.statusbar.immersive
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.databinding.ActivityTelephoneBinding
import com.manchuan.tools.extensions.applyAccentColor

class TelephoneActivity : AppCompatActivity() {

    private val telephoneBinding by lazy {
        ActivityTelephoneBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(telephoneBinding.root)
        setSupportActionBar(telephoneBinding.toolbar)
        immersive(telephoneBinding.toolbar)
        supportActionBar?.apply {
            title = "手机号归属地查询"
            setDisplayHomeAsUpEnabled(true)
        }
        telephoneBinding.imageview1.applyAccentColor()
        telephoneBinding.query.setOnClickListener {
            val telephone = telephoneBinding.url.text.toString()
            if (telephone.isEmpty()) {
                PopTip.show("请输入手机号码")
            } else if (com.drake.engine.utils.RegexUtils.isMobileSimple(telephone)) {
                WaitDialog.show("查询中...")
                scopeNetLife {
                    val content = Get<String>("https://tenapi.cn/tel/?tel=$telephone").await()
                    val json = JSON.parseObject(content)
                    if (json.getIntValue("code") == 200) {
                        TipDialog.show("查询成功", WaitDialog.TYPE.SUCCESS)
                        val stringBuilder = StringBuilder()
                        stringBuilder.append("手机号:${json.getString("tel")}")
                        stringBuilder.append("\n${json.getString("local")}")
                        stringBuilder.append("\n${json.getString("duan")}")
                        stringBuilder.append("\n${json.getString("type")}")
                        stringBuilder.append("\n${json.getString("yys")}")
                        stringBuilder.append("\n${json.getString("bz")}")
                        telephoneBinding.info.setText(stringBuilder)
                    } else {
                        TipDialog.show("查询失败", WaitDialog.TYPE.ERROR)
                    }
                }.catch {
                    PopTip.show(it.message)
                    TipDialog.show("查询失败", WaitDialog.TYPE.ERROR)
                }
            }
        }
        telephoneBinding.imageview1.setOnClickListener {
            if (telephoneBinding.info.text.toString().isEmpty()) {
                PopTip.show("无内容")
            } else {
                ClipboardUtils.copyText(telephoneBinding.info.text.toString())
                PopTip.show("已复制")
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}