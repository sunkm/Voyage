package com.manchuan.tools.fonts

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.Context
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.alibaba.fastjson2.JSON
import com.blankj.utilcode.util.AppUtils
import com.drake.net.Get
import com.drake.net.component.Progress
import com.drake.net.interfaces.ProgressListener
import com.drake.net.utils.scopeNet
import com.drake.softinput.setWindowSoftInput
import com.drake.spannable.movement.ClickableMovementMethod
import com.drake.spannable.replaceSpan
import com.drake.spannable.span.HighlightSpan
import com.dylanc.longan.asActivity
import com.dylanc.longan.design.snackbar
import com.dylanc.longan.doOnClick
import com.dylanc.longan.startActivity
import com.dylanc.longan.toast
import com.lxj.androidktx.core.gone
import com.lxj.androidktx.core.visible
import com.manchuan.tools.R
import com.manchuan.tools.activity.WebActivity
import com.manchuan.tools.base.BaseAlertDialogBuilder
import com.manchuan.tools.database.Global
import com.manchuan.tools.databinding.DialogDownloadFontBinding
import com.manchuan.tools.extensions.getColorByAttr
import com.manchuan.tools.extensions.layoutInflater
import java.io.File


object FontUi {

    fun isEmptyFont(): Boolean {
        return Global.defaultFontName.isEmpty()
    }

    @SuppressLint("SetTextI18n")
    fun downloadFont(context: Context) {
        context.toast("正在获取云端信息")
        scopeNet {
            val styles = Get<String>("https://wds.ecsxs.com/229413.json").await()
            val json = JSON.parseObject(styles)
            val fonts = json.getJSONArray("fonts")
            val loginDialog = BaseAlertDialogBuilder(context).create()
            val loginBinding = DialogDownloadFontBinding.inflate(context.layoutInflater)
            loginBinding.progressLay.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
            val style = ArrayList<String>()
            val downloadList = ArrayList<String>()
            for (items in 0 until fonts.size) {
                val styleList = JSON.parseObject(fonts.getString(items))
                style.add(styleList.getString("name"))
                downloadList.add(styleList.getString("url"))
            }
            var selectedUrl = downloadList[0]
            val adapter = ArrayAdapter(context, R.layout.cat_exposed_dropdown_popup_item, style)
            loginBinding.styles.setAdapter(adapter)
            loginBinding.styles.onItemClickListener =
                AdapterView.OnItemClickListener { adapterView, view, i, l ->
                    selectedUrl = downloadList[i]
                }
            context.asActivity()?.setWindowSoftInput(float = loginBinding.root, setPadding = true)
            loginBinding.textView3.movementMethod = ClickableMovementMethod.getInstance()
            loginBinding.textView3.text =
                "未检测到内置的霞骛文楷字体，可能是您在开始向导过程中未选择使用霞骛文楷字体。\n\n字体开源地址"
                    .replaceSpan("字体开源地址") {
                        HighlightSpan(context.getColorByAttr(android.R.attr.textColorLink)) {
                            context.startActivity<WebActivity>("url" to json.getString("url"))
                        }
                    }
            loginBinding.cancel.doOnClick {
                loginDialog.dismiss()
            }
            loginBinding.confirm.doOnClick {
                scopeNet {
                    loginBinding.progressLay.visible()
                    val file =
                        Get<File>(selectedUrl) {
                            setDownloadDir(Global.defaultFontPath)
                            setDownloadMd5Verify()
                            addDownloadListener(object : ProgressListener() {
                                override fun onProgress(p: Progress) {
                                    loginBinding.progress.post {
                                        val progress = p.progress()
                                        loginBinding.progress.setProgressCompat(progress, true)
                                        loginBinding.progressText.text = "$progress%"
                                    }
                                }
                            })
                        }.await()
                    Global.defaultFontName = file.name
                    Global.isEnabledDefaultFont = true
                    loginDialog.dismiss()
                    context.asActivity()?.snackbar("需要重启才可应用更改", "确定") {
                        AppUtils.relaunchApp(true)
                    }
                }.catch {
                    loginBinding.progressLay.gone()
                }
            }
            loginDialog.setView(loginBinding.root)
            loginDialog.show()
        }.catch {
            context.asActivity()?.snackbar("获取失败")
        }
    }

}