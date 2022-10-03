package com.manchuan.tools.activity.app

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.gyf.immersionbar.ktx.immersionBar
import com.manchuan.tools.R
import com.manchuan.tools.base.AnimationActivity
import com.manchuan.tools.databinding.ActivitySettingsBinding
import com.manchuan.tools.utils.KeepShell
import com.manchuan.tools.utils.UiUtils
import com.safframework.ext.string
import io.github.inflationx.viewpump.ViewPumpContextWrapper

class SettingsActivity : AnimationActivity() {
    private val settingsBinding by lazy {
        ActivitySettingsBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        val enter = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        window.enterTransition = enter
        super.onCreate(savedInstanceState)
        setContentView(settingsBinding.root)
        setSupportActionBar(settingsBinding.toolbar)
        immersionBar {
            titleBar(settingsBinding.toolbar)
            transparentBar()
            statusBarDarkFont(!UiUtils.isDarkMode())
        }
        supportActionBar?.apply {
            title = string(R.string.action_settings)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun attachBaseContext(newBase: Context) {
        newBase.let { ViewPumpContextWrapper.wrap(it) }.let { super.attachBaseContext(it) }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onSupportNavigateUp(): Boolean {
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finishAfterTransition()
        }
        return super.onOptionsItemSelected(item)
    }

    fun clearAppUserData(packageName: String) {
        KeepShell(false).doCmdSync("pm clear $packageName")
    }
}