package com.manchuan.tools.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ClipboardUtils
import com.gyf.immersionbar.ImmersionBar
import com.kongzue.dialogx.dialogs.PopTip
import com.lxj.androidktx.core.decryptDES
import com.lxj.androidktx.core.encryptDES
import com.manchuan.tools.R
import com.manchuan.tools.databinding.ActivityDesCryptBinding
import com.manchuan.tools.utils.ColorUtils
import org.apache.commons.lang3.StringUtils

class DesCrypt : AppCompatActivity() {

    private val cryptBinding by lazy {
        ActivityDesCryptBinding.inflate(layoutInflater)
    }
    private var type = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(cryptBinding.root)
        setSupportActionBar(cryptBinding.toolbar)
        ImmersionBar.with(this)
            .autoDarkModeEnable(true)
            .statusBarColorInt(ColorUtils.statusBarColor)
            .navigationBarColorInt(ColorUtils.statusBarColor)
            .titleBar(cryptBinding.toolbar)
            .init()
        supportActionBar?.apply {
            title = "DES加解密"
            setDisplayHomeAsUpEnabled(true)
        }
        cryptBinding.toggleButton.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.interfaceOne -> type = 1
                    R.id.interfaceTwo -> type = 2
                }
            }
        }
        cryptBinding.jiexi.setOnClickListener {
            val string = cryptBinding.url.text.toString()
            val password = cryptBinding.password.text.toString()
            if (StringUtils.isBlank(string) || StringUtils.isBlank(password)) {
                PopTip.show("请输入内容和密钥")
            } else if (password.length > 8) {
                PopTip.show("密钥超过8位")
            } else if (password.length < 8) {
                PopTip.show("密钥必须8位，且不能包含特殊字符")
            } else if (StringUtils.isNotBlank(string)) {
                when (type) {
                    1 -> runCatching {
                        cryptBinding.info.setText(string.encryptDES(password))
                    }.onFailure {
                        PopTip.show("加密失败:${it.message}")
                    }
                    2 -> runCatching {
                        cryptBinding.info.setText(string.decryptDES(password))
                    }.onFailure {
                        PopTip.show("解密失败:${it.message}")
                    }
                }
            }
        }
        cryptBinding.imageview1.setOnClickListener {
            val string = cryptBinding.info.text.toString()
            if (StringUtils.isBlank(string)) {
                PopTip.show("无内容")
            } else {
                PopTip.show("已复制")
                ClipboardUtils.copyText(string)
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