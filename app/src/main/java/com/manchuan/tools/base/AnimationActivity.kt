package com.manchuan.tools.base

//noinspection SuspiciousImport
import android.R
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.Window
import com.dylanc.longan.getCompatDrawable
import com.dylanc.longan.roundCorners
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.manchuan.tools.extensions.buildContainerTransform
import com.manchuan.tools.extensions.colorPrimary
import rikka.material.internal.ThemedAppCompatActivity

open class AnimationActivity : ThemedAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        val root = findViewById<View>(R.id.content)
        root.transitionName = "root"
        root.roundCorners = 24F
        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        window.sharedElementEnterTransition = buildContainerTransform(true)
        window.sharedElementReturnTransition = buildContainerTransform(false)
        window.allowEnterTransitionOverlap = true
        window.sharedElementsUseOverlay = false
        super.onCreate(savedInstanceState)
        runCatching {
            val upArrow: Drawable? = getCompatDrawable(com.manchuan.tools.R.drawable.baseline_arrow_back_24)
            if (upArrow != null) {
                upArrow.setColorFilter(colorPrimary(), PorterDuff.Mode.SRC_ATOP)
                supportActionBar?.setHomeAsUpIndicator(upArrow)
            }
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

}