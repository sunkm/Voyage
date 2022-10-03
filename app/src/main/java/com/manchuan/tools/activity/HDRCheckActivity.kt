package com.manchuan.tools.activity

import android.content.DialogInterface
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.gyf.immersionbar.ktx.immersionBar
import com.manchuan.tools.base.BaseAlertDialogBuilder
import com.manchuan.tools.databinding.ActivityHdrcheckBinding
import com.manchuan.tools.utils.UiUtils

class HDRCheckActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityHdrcheckBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        immersionBar {
            titleBar(binding.toolbar)
            statusBarDarkFont(!UiUtils.isDarkMode())
            transparentBar()
        }
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "屏幕HDR检测"
        }
        BaseAlertDialogBuilder(this).setTitle("提示").setMessage("点击确定以开始检测")
            .setPositiveButton("确定") { _: DialogInterface, _: Int ->
                for (types in if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    this.display?.hdrCapabilities?.supportedHdrTypes!!
                } else {
                    TODO("VERSION.SDK_INT < R")
                }) {
                    when (types) {
                        2 -> {
                            binding.hdr10.text = "支持"
                            binding.hdr10.setTextColor(Color.GREEN)
                        }

                        1 -> {
                            binding.vision.text = "支持"
                            binding.vision.setTextColor(Color.GREEN)
                        }

                        3 -> {
                            binding.hlg.text = "支持"
                            binding.hlg.setTextColor(Color.GREEN)
                        }

                        4 -> {
                            binding.hdr10p.text = "支持"
                            binding.hdr10p.setTextColor(Color.GREEN)
                        }
                    }
                }
            }.setCancelable(false).setOnKeyListener { dialogInterface, i, keyEvent ->
                if (i == KeyEvent.KEYCODE_BACK) {
                    finish()
                }
                true
            }.create().show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}