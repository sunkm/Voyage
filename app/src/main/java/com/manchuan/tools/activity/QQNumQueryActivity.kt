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
import com.manchuan.tools.databinding.ActivityQqnumQueryBinding

class QQNumQueryActivity : AppCompatActivity() {
    private val queryBinding by lazy {
        ActivityQqnumQueryBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(queryBinding.root)
        setSupportActionBar(queryBinding.toolbar)
        supportActionBar?.apply {
            title = "QQ查绑"
            setDisplayHomeAsUpEnabled(true)
        }
        immersive(queryBinding.toolbar)
        queryBinding.query.setOnClickListener {
            val qq = queryBinding.url.text.toString()
            if (qq.isEmpty()) {
                PopTip.show("请输入QQ号")
            } else {
                WaitDialog.show("查询中...")
                scopeNetLife {
                    val string = Get<String>("https://zy.xywlapi.cc/qqcx?qq=$qq").await()
                    val json = JSON.parseObject(string)
                    if (json.getIntValue("status") == 200) {
                        TipDialog.show("查询成功", WaitDialog.TYPE.SUCCESS)
                        PopTip.show(json.getString("message"))
                        val stringBuilder = StringBuilder()
                        stringBuilder.append("手机号:${json.getString("phone")}")
                        stringBuilder.append("\n归属地:${json.getString("phonediqu")}")
                        stringBuilder.append("\n英雄联盟名称:${json.getString("lol")}")
                        stringBuilder.append("\n微博账号:${json.getString("wb")}")
                        stringBuilder.append("\n旧密码:${json.getString("qqlm")}")
                        queryBinding.info.setText(stringBuilder)
                    } else {
                        TipDialog.show("无结果", WaitDialog.TYPE.WARNING)
                    }
                }
            }
        }
        queryBinding.imageview1.setOnClickListener {
            if (queryBinding.info.text.toString().isEmpty()) {
                PopTip.show("无内容")
            } else {
                ClipboardUtils.copyText(queryBinding.info.text.toString())
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