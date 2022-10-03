package com.manchuan.tools.activity

import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.drake.statusbar.immersive
import com.dylanc.longan.context
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.kongzue.dialogx.dialogs.PopTip
import com.manchuan.tools.databinding.ActivityFontScaleBinding
import com.manchuan.tools.interceptor.PermissionInterceptor

class FontScale : AppCompatActivity() {

    private val scaleBinding by lazy {
        ActivityFontScaleBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(scaleBinding.root)
        setSupportActionBar(scaleBinding.toolbar)
        immersive(scaleBinding.toolbar)
        supportActionBar?.apply {
            title = "系统字体大小调整"
            setDisplayHomeAsUpEnabled(true)
        }
        scaleBinding.size.value = (systemFontScale * 100).toInt().toFloat()
        scaleBinding.edit.setOnClickListener {
            XXPermissions.with(context).permission(Permission.WRITE_SETTINGS)
                .interceptor(PermissionInterceptor()).request { permissions, all ->
                    if (all) {
                        runCatching {
                            systemFontScale = (scaleBinding.size.value / 100)
                        }.onFailure {
                            PopTip.show("修改失败，可能是系统不支持该字号")
                        }
                    }
                }
        }
    }

    private var systemFontScale
        get() = Settings.System.getFloat(contentResolver, Settings.System.FONT_SCALE, 1.0f)
        set(value) {
            Settings.System.putFloat(contentResolver, Settings.System.FONT_SCALE, value)
        }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}