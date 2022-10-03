package com.manchuan.tools.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ClipboardUtils
import com.drake.statusbar.immersive
import com.kongzue.dialogx.dialogs.PopTip
import com.lxj.androidktx.core.sha1Hmac
import com.lxj.androidktx.core.sha256Hmac
import com.manchuan.tools.R
import com.manchuan.tools.databinding.ActivityHmacShacryptBinding
import org.apache.commons.lang3.StringUtils

class HmacSHACrypt : AppCompatActivity() {
    private val cryptBinding by lazy {
        ActivityHmacShacryptBinding.inflate(layoutInflater)
    }
    private var type = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(cryptBinding.root)
        setSupportActionBar(cryptBinding.toolbar)
        immersive(cryptBinding.toolbar)
        supportActionBar?.apply {
            title = "HMACSHA加密"
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
            } else if (StringUtils.isNotBlank(string)) {
                when (type) {
                    1 -> cryptBinding.info.setText(string.sha1Hmac(password))
                    2 -> cryptBinding.info.setText(string.sha256Hmac(password))
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