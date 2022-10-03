package com.manchuan.tools.genshin.ext

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.google.android.material.tabs.TabLayout
import com.lxj.androidktx.AndroidKTX.context
import com.manchuan.tools.genshin.bean.UserBean
import com.manchuan.tools.genshin.information.Constants
import com.manchuan.tools.genshin.listener.AnimatorFinished
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody


fun loadImage(imageView: ImageView, url: String?) {
    Glide.with(imageView).load(url ?: "").into(imageView)
}

fun loadHomeNoticeImage(imageView: ImageView, url: String?) {
    try {
        Glide.with(imageView)
            .load(url ?: "")
            .override(context.resources.displayMetrics.widthPixels, Target.SIZE_ORIGINAL)
            .into(imageView)
    } catch (e: Exception) {
        e.printStackTrace()
        "加载${url}图片时发生错误:${e}".show()
    }
}


var mainUser: UserBean? = null
    get() = field

//数据缓存
val sp: SharedPreferences
    get() = context.getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE)

//用户信息缓存
val usp: SharedPreferences
    get() = context.getSharedPreferences(Constants.USP_NAME, Context.MODE_PRIVATE)

//角色缓存
val csp: SharedPreferences
    get() = context.getSharedPreferences(Constants.CSP_NAME, Context.MODE_PRIVATE)

//武器缓存
val wsp: SharedPreferences
    get() = context.getSharedPreferences(Constants.WSP_NAME, Context.MODE_PRIVATE)


val Int.dp
    get() = context.resources.displayMetrics.density * this + 0.5f

val Int.sp
    get() = (this * context.resources.displayMetrics.scaledDensity + 0.5f)

val Int.px2dp
    get() = (this / context.resources.displayMetrics.density + 0.5f)


//获得底部导航栏高度
val navigationBarHeight: Int
        by lazy {
            val resourceId =
                context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
            var height = 0
            //判断是否有底部导航栏
            if (resourceId > 0) {
                //判断是否显示了导航栏
                val configShowNav =
                    context.resources.getIdentifier("config_showNavigationBar", "bool", "android")
                var isHave = false

                if (configShowNav != 0) {
                    isHave = context.resources.getBoolean(configShowNav)
                }

                if (isHave) {
                    height = context.resources.getDimensionPixelSize(resourceId)
                }
            }
            height
        }

//根据底部导航栏高度设置控件底部外边距
fun setViewMarginBottomByNavigationBarHeight(vararg views: View) {
    views.forEach { view ->
        val lp = view.layoutParams as ViewGroup.MarginLayoutParams
        lp.setMargins(
            view.marginLeft,
            view.marginTop,
            view.marginRight,
            view.marginBottom + navigationBarHeight
        )
        view.layoutParams = lp
    }
}

fun setListItemMargin(view: View, position: Int, topMargin: Int) {
    when (position) {
        0 -> {
            val lp = view.layoutParams as ViewGroup.MarginLayoutParams
            lp.setMargins(
                view.marginLeft,
                view.marginTop + topMargin.dp.toInt(),
                view.marginRight,
                view.marginBottom
            )
            view.layoutParams = lp
        }
    }
}

fun String.show() {
    Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
}

fun String.showLong() {
    Toast.makeText(context, this, Toast.LENGTH_LONG).show()
}

inline fun <reified T> goA() {
    val intent = Intent(context, T::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun getSP(sp: SharedPreferences, name: String): String {
    return sp.getString(name, "")!!
}

fun getListSP(sp: SharedPreferences, name: String): String {
    return sp.getString(name, "[]")!!
}


//tab layout选中回调
fun TabLayout.tab(block: (Int) -> Unit) {
    this.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(p0: TabLayout.Tab?) {
            block(p0!!.position)
        }

        override fun onTabUnselected(p0: TabLayout.Tab?) {
        }

        override fun onTabReselected(p0: TabLayout.Tab?) {
        }
    })
}

//动画完成回调
fun Animation.onFinished(block: () -> Unit) {
    this.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(p0: Animation?) {
        }

        override fun onAnimationEnd(p0: Animation?) {
            block()
        }

        override fun onAnimationRepeat(p0: Animation?) {
        }
    })
}

//checkbox选中时回调
fun AppCompatCheckBox.select(block: (Boolean) -> Unit) {
    this.setOnCheckedChangeListener { p0, p1 -> block(p1) }
}

fun SeekBar.onChange(block: (Int) -> Unit) {
    this.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            block(p1)
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {
        }

        override fun onStopTrackingTouch(p0: SeekBar?) {
        }
    })
}


inline fun <reified VB : ViewBinding> Activity.las() = lazy {
    (VB::class.java.getMethod("inflate", LayoutInflater::class.java)
        .invoke(null, layoutInflater) as VB).apply {
        setContentView(root)
    }
}

inline fun <reified VB : ViewBinding> View.las(): VB {
    return (VB::class.java.getMethod("bind", View::class.java).invoke(null, this) as VB)
}


//设置组件宽度 期间动画
fun openAndCloseAnimationHor(
    target: View, start: Int, end: Int, time: Long,
    block: () -> Unit = object : () -> Unit {
        override fun invoke() {
        }
    },
) {
    val anim = ValueAnimator.ofInt(start.dp.toInt(), end.dp.toInt())
    anim.duration = time
    anim.addUpdateListener {
        target.layoutParams.width = it.animatedValue as Int
        target.requestLayout()
    }
    anim.start()
    anim.addListener(AnimatorFinished {
        block()
    })
}

fun ViewPager2.onPageChange(block: (Int) -> Unit) {
    this.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            block(position)
        }
    })
}

fun openAndCloseAnimationVer(
    target: View, start: Int, end: Int, time: Long,
    block: () -> Unit = object : () -> Unit {
        override fun invoke() {
        }
    },
) {
    target.clearAnimation()
    val anim = ValueAnimator.ofInt(start, end)
    anim.duration = time
    anim.addUpdateListener {
        target.layoutParams.height = it.animatedValue as Int
        target.requestLayout()
    }
    anim.start()
}

fun String.toMyRequestBody(): RequestBody {
    return this.toRequestBody("application/json;charset=utf-8".toMediaType())
}

fun String.substring(start: String, end: String): String {
    val from = if (start.isEmpty()) {
        0
    } else {
        val index = this.indexOf(start)
        if (index == -1) 0 else index
    }
    val to = if (end.isEmpty()) {
        0
    } else {
        val index = this.indexOf(end)
        if (index == -1) 0 else index
    }

    return this.substring(from, to)
}

fun <T> List<T>.copy(list: MutableList<T>) {
    list.clear()
    this.forEach {
        list += it
    }
}

fun <T> MutableList<T>.addFromList(list: List<T>) {
    list.forEach {
        this += it
    }
}

fun Spinner.select(block: (position: Int, id: Long) -> Unit) {
    this.onItemSelectedListener = null
    this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            block(p2, p3)
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {
        }
    }
}

fun SwitchCompat.click(block: (Boolean) -> Unit) {
    this.setOnCheckedChangeListener { compoundButton, b ->
        block(b)
    }
}

fun Fragment.setOnHandleBackPressed(block: (() -> Unit)? = null) {
    requireActivity().onBackPressedDispatcher.addCallback {
        block?.invoke()
    }
}

fun MotionLayout.onFinished(block: () -> Unit) {
    this.setTransitionListener(object : MotionLayout.TransitionListener {
        override fun onTransitionStarted(motionLayout: MotionLayout?, startId: Int, endId: Int) {
        }

        override fun onTransitionChange(
            motionLayout: MotionLayout?,
            startId: Int,
            endId: Int,
            progress: Float,
        ) {
        }

        override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
            block()
        }

        override fun onTransitionTrigger(
            motionLayout: MotionLayout?,
            triggerId: Int,
            positive: Boolean,
            progress: Float,
        ) {
        }
    })
}


fun MotionLayout.onPlaying(block: (progress: Float) -> Unit) {
    this.setTransitionListener(object : MotionLayout.TransitionListener {
        override fun onTransitionStarted(motionLayout: MotionLayout?, startId: Int, endId: Int) {
        }

        override fun onTransitionChange(
            motionLayout: MotionLayout?,
            startId: Int,
            endId: Int,
            progress: Float,
        ) {
            block(progress)
        }

        override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
        }

        override fun onTransitionTrigger(
            motionLayout: MotionLayout?,
            triggerId: Int,
            positive: Boolean,
            progress: Float,
        ) {
        }
    })
}
