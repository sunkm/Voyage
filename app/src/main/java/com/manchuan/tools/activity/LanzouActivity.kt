package com.manchuan.tools.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.fastjson.JSON
import com.blankj.utilcode.util.ClipboardUtils
import com.drake.net.Get
import com.drake.net.component.Progress
import com.drake.net.interfaces.ProgressListener
import com.drake.net.utils.scopeNetLife
import com.drake.statusbar.immersive
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.databinding.ActivityLanzouBinding
import java.io.File

class LanzouActivity : AppCompatActivity() {

    private val lanzouBinding by lazy {
        ActivityLanzouBinding.inflate(layoutInflater)
    }

    var type = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(lanzouBinding.root)
        setSupportActionBar(lanzouBinding.toolbar)
        immersive(lanzouBinding.toolbar)
        supportActionBar?.apply {
            title = "蓝奏云解析"
            setDisplayHomeAsUpEnabled(true)
        }
        lanzouBinding.jiexi.setOnClickListener {
            val string = lanzouBinding.url.text.toString()
            val password = lanzouBinding.password.text.toString()
            if (TextUtils.isEmpty(string)) {
                PopTip.show("请输入链接")
            } else {
                if (lanzouBinding.directDown.isChecked) {
                    lanzouBinding.progressBar.visibility = View.VISIBLE
                    lanzouBinding.progressBar.isIndeterminate = true
                    scopeNetLife {
                        val content =
                            Get<String>("https://tenapi.cn/lanzou/?url=$string&pwd=$password&type=$type").await()
                        val json = JSON.parseObject(content)
                        val data = JSON.parseObject(json.getString("data"))
                        lanzouBinding.jiexi.isEnabled = false
                        lanzouBinding.jiexi.isClickable = false
                        val file =
                            Get<File>(data.getString("url")) {
                                addDownloadListener(object : ProgressListener() {
                                    override fun onProgress(p: Progress) {
                                        lanzouBinding.progressBar.isIndeterminate = false
                                        lanzouBinding.progressBar.max = 10000
                                        lanzouBinding.progressBar.setProgressCompat(
                                            p.progress(), true)
                                        supportActionBar?.apply {
                                            title = "已下载:" + p.currentSize()
                                        }
                                    }
                                })
                            }.await()
                        lanzouBinding.jiexi.isEnabled = true
                        lanzouBinding.jiexi.isClickable = true
                        lanzouBinding.progressBar.visibility = View.GONE
                        lanzouBinding.progressBar.isIndeterminate = false
                        supportActionBar?.apply {
                            title = "蓝奏云解析"
                        }
                        TipDialog.show("已保存到下载目录", WaitDialog.TYPE.SUCCESS)
                    }.catch {
                        lanzouBinding.jiexi.isEnabled = true
                        lanzouBinding.jiexi.isClickable = true
                        lanzouBinding.progressBar.visibility = View.GONE
                        lanzouBinding.progressBar.isIndeterminate = false
                        TipDialog.show("下载失败", WaitDialog.TYPE.ERROR)
                    }
                } else if (!lanzouBinding.directDown.isChecked) {
                    WaitDialog.show("解析中...")
                    scopeNetLife {
                        val content =
                            Get<String>("https://tenapi.cn/lanzou/?url=$string&pwd=$password&type=$type").await()
                        val json = JSON.parseObject(content)
                        if (json.getIntValue("code") == 200) {
                            TipDialog.show("解析完成", WaitDialog.TYPE.SUCCESS)
                            val data = JSON.parseObject(json.getString("data"))
                            val stringBuilder = StringBuilder()
                            stringBuilder.append("文件名:${data.getString("name")}")
                            stringBuilder.append("\n作者:${data.getString("author")}")
                            stringBuilder.append("\n文件大小:${data.getString("size")}")
                            stringBuilder.append("\n日期:${data.getString("time")}")
                            stringBuilder.append("\n下载链接:${data.getString("url")}")
                            lanzouBinding.info.setText(stringBuilder)
                        } else {
                            TipDialog.show("解析失败", WaitDialog.TYPE.ERROR)
                        }
                    }.catch {
                        PopTip.show(it.message)
                        TipDialog.show("解析失败", WaitDialog.TYPE.ERROR)
                    }
                }
            }
        }
        lanzouBinding.imageview1.setOnClickListener {
            if (lanzouBinding.info.text.toString().isEmpty()) {
                PopTip.show("无内容")
            } else {
                ClipboardUtils.copyText(lanzouBinding.info.text.toString())
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