package com.manchuan.tools.activity

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.fastjson.JSON
import com.drake.net.Get
import com.drake.net.component.Progress
import com.drake.net.interfaces.ProgressListener
import com.drake.net.utils.scopeNetLife
import com.drake.statusbar.immersive
import com.dylanc.longan.design.snackbar
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.databinding.ActivityShortVideoBinding
import java.io.File

class ShortVideoActivity : AppCompatActivity() {
    private val shortVideoBinding by lazy {
        ActivityShortVideoBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(shortVideoBinding.root)
        setSupportActionBar(shortVideoBinding.toolbar)
        immersive(shortVideoBinding.toolbar)
        supportActionBar?.apply {
            title = "聚合短视频解析"
            setDisplayHomeAsUpEnabled(true)
        }
        shortVideoBinding.jiexi.setOnClickListener {
            if (shortVideoBinding.url.text.toString().isEmpty()) {
                snackbar("请输入链接")
            } else {
                shortVideoBinding.functionsLay.visibility = View.GONE
                WaitDialog.show("解析中...")
                scopeNetLife {
                    val content =
                        Get<String>("https://tenapi.cn/video/?url=${shortVideoBinding.url.text.toString()}").await()
                    val json = JSON.parseObject(content)
                    if (json.getIntValue("code") == 200) {
                        TipDialog.show("解析成功", WaitDialog.TYPE.SUCCESS)
                        shortVideoBinding.functionsLay.visibility = View.VISIBLE
                        shortVideoBinding.download.setOnClickListener {
                            shortVideoBinding.progressBar.visibility = View.VISIBLE
                            shortVideoBinding.download.isClickable = false
                            shortVideoBinding.download.isEnabled = false
                            shortVideoBinding.progressBar.isIndeterminate = false
                            scopeNetLife {
                                val file =
                                    Get<File>(json.getString("url")) {
                                        setDownloadFileName("net.apk")
                                        setDownloadMd5Verify()
                                        addDownloadListener(object : ProgressListener() {
                                            override fun onProgress(p: Progress) {
                                                supportActionBar?.apply {
                                                    title = "已下载:" + p.currentSize()
                                                }
                                                shortVideoBinding.progressBar.setProgressCompat(p.progress(),
                                                    true)
                                            }
                                        })
                                    }.await()
                                supportActionBar?.apply {
                                    title = "聚合短视频解析"
                                }
                                shortVideoBinding.progressBar.visibility = View.GONE
                                TipDialog.show("下载成功", WaitDialog.TYPE.SUCCESS)
                                shortVideoBinding.download.isClickable = true
                                shortVideoBinding.download.isEnabled = true
                                snackbar("已保存到手机存储空间/Movies")
                            }.catch {
                                shortVideoBinding.download.isClickable = false
                                shortVideoBinding.download.isEnabled = false
                                shortVideoBinding.progressBar.visibility = View.GONE
                                TipDialog.show("下载失败", WaitDialog.TYPE.ERROR)
                            }
                        }
                    } else {
                        snackbar("请确认该链接是否是所支持的平台")
                        TipDialog.show("解析失败", WaitDialog.TYPE.ERROR)
                        shortVideoBinding.functionsLay.visibility = View.GONE
                    }
                }.catch {
                    TipDialog.show("解析失败", WaitDialog.TYPE.ERROR)
                }
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