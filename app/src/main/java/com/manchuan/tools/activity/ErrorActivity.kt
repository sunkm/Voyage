package com.manchuan.tools.activity

import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.window.OnBackInvokedDispatcher
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.dylanc.longan.doOnClick
import com.dylanc.longan.relaunchApp
import com.dylanc.longan.safeIntentExtras
import com.gyf.immersionbar.ktx.immersionBar
import com.itxca.spannablex.spannable
import com.lxj.androidktx.core.animateGone
import com.lxj.androidktx.core.animateVisible
import com.lxj.androidktx.core.drawable
import com.lxj.androidktx.core.isVisible
import com.manchuan.tools.BuildConfig
import com.manchuan.tools.R
import com.manchuan.tools.databinding.ActivityErrorBinding
import com.manchuan.tools.extensions.*
import com.manchuan.tools.utils.BuildUtils
import com.manchuan.tools.utils.UiUtils
import com.mcxiaoke.koi.ext.dateNow

class ErrorActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityErrorBinding.inflate(layoutInflater)
    }

    private lateinit var error: Lazy<Throwable>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        immersionBar {
            titleBar(binding.toolbar)
            transparentBar()
        }
        binding.toolbar.apply {
            setNavigationOnClickListener {
                if (binding.second.root.isVisible) {
                    binding.second.root.animateGone()
                    errorToolbar()
                    binding.content.animateVisible()
                }
            }
        }
        runCatching {
            error = safeIntentExtras("error")
        }.onFailure {
            error = lazy {
                Throwable("警告，无法获取到错误信息")
            }
        }
        binding.error.text = error.value.toString()
        binding.close.doOnClick {
            finishAndRemoveTask()
        }
        binding.infos.doOnClick {
            binding.content.animateGone()
            infoToolbar()
            binding.second.root.animateVisible()
        }
        binding.restart.doOnClick {
            relaunchApp(true)
        }
        binding.second.errorTwo.text = spannable {
            "安卓版本: ".text()
            image(androidLogo())
            androidString().text()
            "\n崩溃时间: ${dateNow()}".text()
            "\n软件版本号: ${BuildConfig.VERSION_CODE}".text()
            "\n软件版本名: ${BuildConfig.VERSION_NAME}".text()
            "\n发布构建类型: ${BuildConfig.BUILD_TYPE}".text()
            "\n设备名称: ${BuildUtils.name}".text()
            "\n设备厂商: ${BuildUtils.manufacturer}".text()
            "\n设备主板: ${BuildUtils.brand}".text()
            "\nROM指纹: ${BuildUtils.fingerprint}".text()
            "\n设备硬件: ${BuildUtils.hardware}".text()
            newline(3)
            "错误详情: ".text().newline()
            error.value.fillInStackTrace().toString().span {
                underline()
                color(errorColor())
                style(Typeface.BOLD, arrayOf(error.value.message))
            }
        }
    }

    private fun infoToolbar() {
        immersionBar {
            statusBarDarkFont(!UiUtils.isDarkMode())
            transparentBar()
        }
        binding.root.setBackgroundColor(windowBackground())
        binding.toolbar.apply {
            navigationIcon = drawable(R.drawable.baseline_arrow_back_24)
            setNavigationIconTint(colorPrimary())
            setTitleTextColor(colorPrimary())
            title = "错误详情"
        }
    }


    private fun errorToolbar() {
        immersionBar {
            transparentBar()
            titleBar(binding.toolbar)
            statusBarDarkFont(false)
        }
        binding.root.setBackgroundColor(0xff660205.toInt())
        binding.content.animateVisible()
        binding.toolbar.apply {
            title = "发生错误"
            navigationIcon = null
            setTitleTextColor(Color.WHITE)
        }
    }

    @Deprecated(
        "Deprecated in Java",
        ReplaceWith("super.onBackPressed()", "androidx.appcompat.app.AppCompatActivity")
    )
    override fun onBackPressed() {
        if (binding.second.root.isVisible) {
            binding.second.root.animateGone()
            errorToolbar()
            binding.content.animateVisible()
            return
        } else {
            finishAndRemoveTask()
        }
        super.onBackPressed()
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun getOnBackInvokedDispatcher(): OnBackInvokedDispatcher {
        onBackInvokedDispatcher.unregisterOnBackInvokedCallback {
            if (binding.second.root.isVisible) {
                binding.second.root.animateGone()
                errorToolbar()
                binding.content.animateVisible()
            }
        }
        return super.getOnBackInvokedDispatcher()
    }

}