package com.manchuan.tools.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.EncryptUtils
import com.gyf.immersionbar.ImmersionBar
import com.kongzue.dialogx.dialogs.PopTip
import com.manchuan.tools.databinding.ActivityEncryptMd5Binding
import com.manchuan.tools.utils.ColorUtils
import rikka.material.app.MaterialActivity

class EncryptMD5Activity : MaterialActivity() {
    private lateinit var encryptBinding: ActivityEncryptMd5Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        encryptBinding = ActivityEncryptMd5Binding.inflate(LayoutInflater.from(this))
        setContentView(encryptBinding.root)
        setSupportActionBar(encryptBinding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "MD5加密"
        ImmersionBar.with(this)
            .titleBar(encryptBinding.toolbar)
            .statusBarColorInt(ColorUtils.statusBarColor)
            .autoDarkModeEnable(true)
            .transparentNavigationBar()
            .init()
        encryptBinding.encrypt.setOnClickListener {
            if (encryptBinding.edittext1.text.toString().isEmpty()) {
                PopTip.show("请输入内容")
            } else {
                encryptBinding.autocomplete1.setText(EncryptUtils.encryptMD5ToString(encryptBinding.edittext1.text.toString()))
            }
        }
        encryptBinding.imageview1.setOnClickListener {
            if (encryptBinding.autocomplete1.text.toString().isEmpty()) {
                PopTip.show("无内容")
            } else {
                PopTip.show("已复制")
                ClipboardUtils.copyText(encryptBinding.autocomplete1.text.toString())
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

}