package com.manchuan.tools.activity.history

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.dylanc.longan.roundCorners
import com.dylanc.longan.safeIntentExtras
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.gyf.immersionbar.ktx.immersionBar
import com.lxj.androidktx.core.animateGone
import com.lxj.androidktx.core.load
import com.manchuan.tools.databinding.HistoryDetailsBinding
import com.manchuan.tools.extensions.buildContainerTransform
import com.manchuan.tools.utils.UiUtils


class DetailsActivity : AppCompatActivity() {

    private val binding by lazy {
        HistoryDetailsBinding.inflate(layoutInflater)
    }

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        val root = findViewById<View>(android.R.id.content)
        root.transitionName = "shared_element_end_root"
        root.roundCorners = 24F
        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        window.sharedElementEnterTransition = buildContainerTransform(true)
        window.sharedElementReturnTransition = buildContainerTransform(false)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "详情"
            setDisplayHomeAsUpEnabled(true)
        }
        immersionBar {
            titleBar(binding.toolbar)
            transparentBar()
            statusBarDarkFont(!UiUtils.isDarkMode())
        }
        val image = safeIntentExtras<String>("image")
        val content = safeIntentExtras<String>("content")
        image.value.ifEmpty {
            binding.image.animateGone()
        }
        binding.image.load(image.value, isForceOriginalSize = true, isCrossFade = true)
        binding.details.text = content.value
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }


}