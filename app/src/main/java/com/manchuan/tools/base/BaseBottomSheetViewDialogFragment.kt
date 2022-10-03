package com.manchuan.tools.base


import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Dialog
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.animation.doOnEnd
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentManager
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.blankj.utilcode.util.KeyboardUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.transition.platform.MaterialFadeThrough
import com.manchuan.tools.R
import com.manchuan.tools.databinding.BottomsheetBaseBinding
import com.manchuan.tools.utils.UiUtils
import kotlin.properties.Delegates


open class BottomSheet :
    BottomSheetDialogFragment(), View.OnLayoutChangeListener {

    companion object {
        var resId by Delegates.notNull<View>()
        fun initLayoutRes(layoutIdRes: View) {
            resId = layoutIdRes
        }

        lateinit var title: String
    }

    private var animationDuration = 350L
    private var maxPeekSize: Int = 0
    private var _root: BottomsheetBaseBinding? = null
    private var isHandlerActivated = false
    private var animator: ValueAnimator = ObjectAnimator()
    private val behavior by lazy { BottomSheetBehavior.from(root.behavior) }
    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_DRAGGING -> {
                    if (!isHandlerActivated) {
                        isHandlerActivated = true
                    }
                }
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    if (isHandlerActivated) {
                        isHandlerActivated = false
                    }
                }
                BottomSheetBehavior.STATE_EXPANDED -> {
                    if (isHandlerActivated) {
                        isHandlerActivated = false
                    }
                }
                else -> {
                }
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
        }
    }

    val root get() = _root!!
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        object : BottomSheetDialog(requireContext(), R.style.ModalBottomSheetDialog) {
            override fun onAttachedToWindow() {
                super.onAttachedToWindow()
                window?.let {
                    it.attributes?.windowAnimations = R.style.DialogAnimation
                    WindowCompat.setDecorFitsSystemWindows(it, false)
                    UiUtils.setSystemBarStyle(it)
                    WindowInsetsControllerCompat(it, it.decorView)
                        .isAppearanceLightNavigationBars = !UiUtils.isDarkMode()

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        it.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                        it.attributes.blurBehindRadius = 64
                        it.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                    }
                }
                findViewById<View>(com.google.android.material.R.id.container)?.fitsSystemWindows =
                    false
                findViewById<View>(com.google.android.material.R.id.coordinator)?.fitsSystemWindows =
                    false
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _root = BottomsheetBaseBinding.inflate(inflater)
        _root?.container?.addView(resId)
        _root?.title?.text = title
        KeyboardUtils.fixSoftInputLeaks(requireActivity())
        KeyboardUtils.fixAndroidBug5497(requireActivity())
        return _root?.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
        reenterTransition = MaterialFadeThrough()
    }

    override fun onStart() {
        super.onStart()
        behavior.addBottomSheetCallback(bottomSheetCallback)
        root.root.addOnLayoutChangeListener(this)
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            root.root.post {
                Class.forName(behavior::class.java.name).apply {
                    getDeclaredMethod("setStateInternal", Int::class.java).apply {
                        isAccessible = true
                        invoke(behavior, BottomSheetBehavior.STATE_EXPANDED)
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        behavior.removeBottomSheetCallback(bottomSheetCallback)
    }

    override fun onDetach() {
        animator.cancel()
        super.onDetach()
    }

    override fun onDestroyView() {
        animator.cancel()
        root.root.removeOnLayoutChangeListener(this)
        _root = null
        super.onDestroyView()
    }

    override fun show(manager: FragmentManager, tag: String?) {
        runCatching {
            super.show(manager, tag)
        }.onFailure {
        }
    }

    override fun onLayoutChange(
        view: View,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        oldLeft: Int,
        oldTop: Int,
        oldRight: Int,
        oldBottom: Int,
    ) {
        if ((bottom - top) != (oldBottom - oldTop)) {
            enqueueAnimation {
                animateHeight(from = oldBottom - oldTop, to = bottom - top, onEnd = { })
            }
        }
    }

    private fun animateHeight(from: Int, to: Int, onEnd: () -> Unit) {
        animator.cancel()
        animator = ObjectAnimator.ofFloat(0f, 1f).apply {
            duration = animationDuration
            interpolator = FastOutSlowInInterpolator()
            addUpdateListener {
                val scale = it.animatedValue as Float
                val newHeight = ((to - from) * scale + from).toInt()
                setClippedHeight(newHeight)
            }
            doOnEnd { onEnd() }
            start()
        }
    }

    private fun enqueueAnimation(action: () -> Unit) {
        if (!animator.isRunning) action()
        else animator.doOnEnd { action() }
    }

    private fun setClippedHeight(newHeight: Int) {
        if (newHeight <= maxPeekSize || maxPeekSize == 0) {
            behavior.peekHeight = newHeight
        }
    }
}
