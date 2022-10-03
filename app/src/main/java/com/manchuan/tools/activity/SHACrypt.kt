package com.manchuan.tools.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ClipboardUtils
import com.gyf.immersionbar.ImmersionBar
import com.kongzue.dialogx.dialogs.PopTip
import com.lxj.androidktx.core.sha1
import com.lxj.androidktx.core.sha256
import com.lxj.androidktx.core.sha512
import com.manchuan.tools.R
import com.manchuan.tools.databinding.ActivityShacryptBinding
import com.manchuan.tools.utils.ColorUtils
import org.apache.commons.lang3.StringUtils

class SHACrypt : AppCompatActivity() {
    private val cryptBinding by lazy {
        ActivityShacryptBinding.inflate(layoutInflater)
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
        cryptBinding.toggleButton.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.interfaceOne -> type = 1
                    R.id.interfaceTwo -> type = 2
                    R.id.interfaceThree -> type = 3
                }
            }
        }
        supportActionBar?.apply {
            title = "SHA加密"
            setDisplayHomeAsUpEnabled(true)
        }
        cryptBinding.jiexi.setOnClickListener {
            val string = cryptBinding.url.text.toString()
            if (StringUtils.isBlank(string)) {
                PopTip.show("请输入内容")
            } else if (StringUtils.isNotBlank(string)) {
                when (type) {
                    1 -> cryptBinding.info.setText(string.sha1())
                    2 -> cryptBinding.info.setText(string.sha256())
                    3 -> cryptBinding.info.setText(string.sha512())
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