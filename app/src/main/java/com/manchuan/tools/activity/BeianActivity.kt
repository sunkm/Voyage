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
import com.manchuan.tools.databinding.ActivityBeianBinding

class BeianActivity : AppCompatActivity() {

    private val beanieActivity by lazy {
        ActivityBeianBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(beanieActivity.root)
        setSupportActionBar(beanieActivity.toolbar)
        immersive(beanieActivity.toolbar)
        supportActionBar?.apply {
            title = "网站ICP备案查询"
            setDisplayHomeAsUpEnabled(true)
        }
        beanieActivity.query.setOnClickListener {
            val telephone = beanieActivity.url.text.toString()
            if (telephone.isEmpty()) {
                PopTip.show("请输入网站")
            } else {
                WaitDialog.show("查询中...")
                scopeNetLife {
                    val content = Get<String>("http://api.botwl.cn/api/icp?url=$telephone").await()
                    val json = JSON.parseObject(content)
                    if (json.getIntValue("code") == 1) {
                        TipDialog.show("查询成功", WaitDialog.TYPE.SUCCESS)
                        val data = JSON.parseObject(json.getString("data"))
                        val stringBuilder = StringBuilder()
                        stringBuilder.append("主办单位:${json.getString("主办单位")}")
                        stringBuilder.append("\n单位性质:${json.getString("单位性质")}")
                        stringBuilder.append("\n备案号:${json.getString("备案号")}")
                        stringBuilder.append("\n网站名称:${json.getString("网站名称")}")
                        stringBuilder.append("\n网站首页:${json.getString("网站首页")}")
                        stringBuilder.append("\n审核时间:${json.getString("审核时间")}")
                        beanieActivity.info.setText(stringBuilder)
                    } else {
                        TipDialog.show("无结果", WaitDialog.TYPE.WARNING)
                    }
                }.catch {
                    PopTip.show(it.message)
                    TipDialog.show("查询失败", WaitDialog.TYPE.ERROR)
                }
            }
        }
        beanieActivity.imageview1.setOnClickListener {
            if (beanieActivity.info.text.toString().isEmpty()) {
                PopTip.show("无内容")
            } else {
                ClipboardUtils.copyText(beanieActivity.info.text.toString())
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