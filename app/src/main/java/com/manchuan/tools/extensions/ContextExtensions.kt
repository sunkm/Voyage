package com.manchuan.tools.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.ContextWrapper
import android.content.res.AssetManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import android.widget.Toast
import androidx.annotation.AnimRes
import androidx.annotation.ArrayRes
import androidx.annotation.AttrRes
import androidx.annotation.BoolRes
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.Dimension
import androidx.annotation.IntegerRes
import androidx.annotation.InterpolatorRes
import androidx.annotation.PluralsRes
import androidx.annotation.StyleRes
import androidx.annotation.StyleableRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.TintTypedArray
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.kongzue.dialogx.dialogs.PopNotification
import com.lxj.androidktx.core.drawable
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.enums.PopupAnimation
import com.manchuan.tools.R
import com.manchuan.tools.compat.use
import timber.log.Timber
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader


fun Context.loadingDialog(message: String): BasePopupView =
    XPopup.Builder(this).hasNavigationBar(false).hasShadowBg(true).dismissOnBackPressed(false)
        .dismissOnTouchOutside(false).popupAnimation(PopupAnimation.ScaleAlphaFromCenter)
        .asLoading(message)

class MaterialSharedElement : MaterialContainerTransformSharedElementCallback() {
    override fun onCaptureSharedElementSnapshot(
        sharedElement: View,
        viewToGlobalMatrix: Matrix,
        screenBounds: RectF,
    ): Parcelable? {
        sharedElement.alpha = 1F
        return super.onCaptureSharedElementSnapshot(sharedElement, viewToGlobalMatrix, screenBounds)
    }
}

@SuppressLint("DiscouragedApi", "Recycle")
@ColorRes
fun Context.foregroundColor(name: String, chroma: Int): Int {
    return resources.getIdentifier(
        "material_" + name + "_" + chroma, "color", packageName
    )
}

@ColorRes
fun Context.backgroundColor(name: String, chroma: Int): Int {
    return resources.getIdentifier(
        "material_" + name + "_" + chroma, "color", packageName
    )
}

val Context.activity: Activity?
    get() {
        var context = this
        while (true) {
            when (context) {
                is Activity -> return context
                is ContextWrapper -> context = context.baseContext
                else -> return null
            }
        }
    }


fun Context.textCopyThenPost(textCopied: String) {
    val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    // 设置文本
    clipboardManager.setPrimaryClip(ClipData.newPlainText("", textCopied))
    // 仅针对 Android 12 及更低版本显示吐司。
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) Toast.makeText(
        this, "已复制", Toast.LENGTH_SHORT
    ).show()
}

const val SHARED_AXIS_KEY = "activity_shared_axis_axis"

fun Activity.buildContainerTransform(
    entering: Boolean,
    duration: Long? = 500,
): MaterialContainerTransform {
    val transform = MaterialContainerTransform(this, entering)
    transform.setAllContainerColors(
        MaterialColors.getColor(
            this.findViewById(android.R.id.content), com.google.android.material.R.attr.colorSurface
        )
    )
    transform.addTarget(android.R.id.content)
    transform.duration = duration!!
    return transform
}

@StyleRes
fun getSpecStyleResId(): Int {
    return com.google.android.material.R.style.Widget_Material3_CircularProgressIndicator_ExtraSmall
}

fun Context.androidLogo(): Drawable {
    return when (Build.VERSION.SDK_INT) {
        Build.VERSION_CODES.ICE_CREAM_SANDWICH -> drawable(R.drawable.ic_android_i)
        Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 -> drawable(R.drawable.ic_android_i)
        Build.VERSION_CODES.JELLY_BEAN -> drawable(R.drawable.ic_android_j)
        Build.VERSION_CODES.JELLY_BEAN_MR1 -> drawable(R.drawable.ic_android_j)
        Build.VERSION_CODES.JELLY_BEAN_MR2 -> drawable(R.drawable.ic_android_j)
        Build.VERSION_CODES.KITKAT -> drawable(R.drawable.ic_android_k)
        Build.VERSION_CODES.KITKAT_WATCH -> drawable(R.drawable.ic_android_k)
        Build.VERSION_CODES.LOLLIPOP -> drawable(R.drawable.ic_android_l)
        Build.VERSION_CODES.LOLLIPOP_MR1 -> drawable(R.drawable.ic_android_l)
        Build.VERSION_CODES.M -> drawable(R.drawable.ic_android_m)
        Build.VERSION_CODES.N -> drawable(R.drawable.ic_android_n)
        Build.VERSION_CODES.N_MR1 -> drawable(R.drawable.ic_android_n)
        Build.VERSION_CODES.O -> drawable(R.drawable.ic_android_o)
        Build.VERSION_CODES.O_MR1 -> drawable(R.drawable.ic_android_o_mr1)
        Build.VERSION_CODES.P -> drawable(R.drawable.ic_android_p)
        Build.VERSION_CODES.Q -> drawable(R.drawable.ic_android_q)
        Build.VERSION_CODES.R -> drawable(R.drawable.ic_android_r)
        Build.VERSION_CODES.S -> drawable(R.drawable.ic_android_s)
        Build.VERSION_CODES.S_V2 -> drawable(R.drawable.ic_android_s)
        Build.VERSION_CODES.TIRAMISU -> drawable(R.drawable.ic_android_t)
        else -> drawable(io.karn.notify.R.drawable.ic_android_black)
    }
}

fun androidString(): String {
    return when (Build.VERSION.SDK_INT) {
        Build.VERSION_CODES.ICE_CREAM_SANDWICH -> "Android 4.0.1-2"
        Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 -> "Android 4.0.3-4"
        Build.VERSION_CODES.JELLY_BEAN -> "Android 4.1"
        Build.VERSION_CODES.JELLY_BEAN_MR1 -> "Android 4.2"
        Build.VERSION_CODES.JELLY_BEAN_MR2 -> "Android 4.3"
        Build.VERSION_CODES.KITKAT -> "Android 4.4"
        Build.VERSION_CODES.KITKAT_WATCH -> "Android 4.4.4"
        Build.VERSION_CODES.LOLLIPOP -> "Android 5.0"
        Build.VERSION_CODES.LOLLIPOP_MR1 -> "Android 5.1"
        Build.VERSION_CODES.M -> "Android 6.0"
        Build.VERSION_CODES.N -> "Android 7.0"
        Build.VERSION_CODES.N_MR1 -> "Android 7.1"
        Build.VERSION_CODES.O -> "Android 8.0"
        Build.VERSION_CODES.O_MR1 -> "Android 8.1"
        Build.VERSION_CODES.P -> "Android 9"
        Build.VERSION_CODES.Q -> "Android 10"
        Build.VERSION_CODES.R -> "Android 11"
        Build.VERSION_CODES.S -> "Android 12"
        Build.VERSION_CODES.S_V2 -> "Android 12.1"
        Build.VERSION_CODES.TIRAMISU -> "Android 13"
        else -> "未知"
    }
}

fun Context.readFileFromAssets(fileName: String?): String {
    if (null == fileName) return "无法读取内容，请检查资源文件是否被移动！"
    val am: AssetManager = assets
    val input: InputStream = am.open(fileName)
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(1024)
    var len: Int
    while (input.read(buffer).also { len = it } != -1) {
        output.write(buffer, 0, len)
    }
    output.close()
    input.close()
    return output.toString()
}

fun Context.readAssetsTxt(fileName: String): String {
    val input = assets.open(fileName)
    val reactivestreams = BufferedReader(InputStreamReader(input))
    try {
        Timber.tag("获取的assets文本内容----").e(reactivestreams.readLines().toString())
        return reactivestreams.readLines().toString()
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        reactivestreams.close()
    }
    return "读取失败,请检查文件名称及文件是否存在!"
}


fun Context.getAnimation(@AnimRes id: Int): Animation = AnimationUtils.loadAnimation(this, id)

fun Context.getBoolean(@BoolRes id: Int) = resources.getBoolean(id)

fun Context.getDimension(@DimenRes id: Int) = resources.getDimension(id)

fun Context.getDimensionPixelOffset(@DimenRes id: Int) = resources.getDimensionPixelOffset(id)

fun Context.getDimensionPixelSize(@DimenRes id: Int) = resources.getDimensionPixelSize(id)

fun Context.getFloat(@DimenRes id: Int) = resources.getFloatCompat(id)

fun Context.getInteger(@IntegerRes id: Int) = resources.getInteger(id)

fun notification(title: String, message: String) {
    PopNotification.show(R.mipmap.ic_launcher, title, message)
}

fun Context.getInterpolator(@InterpolatorRes id: Int): Interpolator =
    AnimationUtils.loadInterpolator(this, id)

fun Context.getQuantityString(@PluralsRes id: Int, quantity: Int): String =
    resources.getQuantityString(id, quantity)

fun Context.getQuantityString(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any?): String =
    resources.getQuantityString(id, quantity, *formatArgs)

fun Context.getQuantityText(@PluralsRes id: Int, quantity: Int): CharSequence =
    resources.getQuantityText(id, quantity)

fun Context.getStringArray(@ArrayRes id: Int): Array<String> = resources.getStringArray(id)

@SuppressLint("RestrictedApi")
fun Context.getBooleanByAttr(@AttrRes attr: Int): Boolean =
    obtainStyledAttributesCompat(attrs = intArrayOf(attr)).use { it.getBoolean(0, false) }

fun Context.getColorByAttr(@AttrRes attr: Int): Int = getColorStateListByAttr(attr).defaultColor

@SuppressLint("RestrictedApi")
fun Context.getColorStateListByAttr(@AttrRes attr: Int): ColorStateList =
    obtainStyledAttributesCompat(attrs = intArrayOf(attr)).use { it.getColorStateList(0) }

@SuppressLint("RestrictedApi")
fun Context.getDimensionByAttr(@AttrRes attr: Int): Float =
    obtainStyledAttributesCompat(attrs = intArrayOf(attr)).use { it.getDimension(0, 0f) }

@SuppressLint("RestrictedApi")
fun Context.getDimensionPixelOffsetByAttr(@AttrRes attr: Int): Int =
    obtainStyledAttributesCompat(attrs = intArrayOf(attr)).use {
        it.getDimensionPixelOffset(0, 0)
    }

@SuppressLint("RestrictedApi")
fun Context.getDimensionPixelSizeByAttr(@AttrRes attr: Int): Int =
    obtainStyledAttributesCompat(attrs = intArrayOf(attr)).use { it.getDimensionPixelSize(0, 0) }

@SuppressLint("RestrictedApi")
fun Context.getDrawableByAttr(@AttrRes attr: Int): Drawable =
    obtainStyledAttributesCompat(attrs = intArrayOf(attr)).use { it.getDrawable(0) }

@SuppressLint("RestrictedApi")
fun Context.getFloatByAttr(@AttrRes attr: Int): Float =
    obtainStyledAttributesCompat(attrs = intArrayOf(attr)).use { it.getFloat(0, 0f) }

@SuppressLint("RestrictedApi")
fun Context.getResourceIdByAttr(@AttrRes attr: Int): Int =
    obtainStyledAttributesCompat(attrs = intArrayOf(attr)).use { it.getResourceId(0, 0) }

@Dimension
fun Context.dpToDimension(@Dimension(unit = Dimension.DP) dp: Float): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

@Dimension
fun Context.dpToDimension(@Dimension(unit = Dimension.DP) dp: Int) = dpToDimension(dp.toFloat())

@Dimension
fun Context.dpToDimensionPixelOffset(@Dimension(unit = Dimension.DP) dp: Float): Int =
    dpToDimension(dp).toInt()

@Dimension
fun Context.dpToDimensionPixelOffset(@Dimension(unit = Dimension.DP) dp: Int) =
    dpToDimensionPixelOffset(dp.toFloat())

@Dimension
fun Context.dpToDimensionPixelSize(@Dimension(unit = Dimension.DP) dp: Float): Int {
    val value = dpToDimension(dp)
    val size = (if (value >= 0) value + 0.5f else value - 0.5f).toInt()
    return when {
        size != 0 -> size
        value == 0f -> 0
        value > 0 -> 1
        else -> -1
    }
}

@Dimension
fun Context.dpToDimensionPixelSize(@Dimension(unit = Dimension.DP) dp: Int) =
    dpToDimensionPixelSize(dp.toFloat())

val Context.shortAnimTime: Int
    get() = getInteger(android.R.integer.config_shortAnimTime)

val Context.mediumAnimTime: Int
    get() = getInteger(android.R.integer.config_mediumAnimTime)

val Context.longAnimTime: Int
    get() = getInteger(android.R.integer.config_longAnimTime)

val Context.displayWidth: Int
    get() = resources.displayMetrics.widthPixels

val Context.displayHeight: Int
    get() = resources.displayMetrics.heightPixels

fun Context.hasSwDp(@Dimension(unit = Dimension.DP) dp: Int): Boolean =
    resources.configuration.smallestScreenWidthDp >= dp

val Context.hasSw600Dp: Boolean
    get() = hasSwDp(600)

fun Context.hasWDp(@Dimension(unit = Dimension.DP) dp: Int): Boolean =
    resources.configuration.screenWidthDp >= dp

val Context.hasW600Dp: Boolean
    get() = hasWDp(600)

val Context.hasW960Dp: Boolean
    get() = hasWDp(960)


val Context.isOrientationLandscape: Boolean
    get() = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

val Context.isOrientationPortrait: Boolean
    get() = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

val Context.isLightTheme: Boolean
    get() = getBooleanByAttr(ando.file.R.attr.isLightTheme)

val Context.layoutInflater: LayoutInflater
    get() = LayoutInflater.from(this)

fun Context.withTheme(@StyleRes themeRes: Int): Context =
    if (themeRes != 0) ContextThemeWrapper(this, themeRes) else this

fun Resources.getFloatCompat(@DimenRes id: Int) = ResourcesCompat.getFloat(this, id)

@SuppressLint("RestrictedApi")
fun Context.obtainStyledAttributesCompat(
    set: AttributeSet? = null,
    @StyleableRes attrs: IntArray,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0,
): TintTypedArray =
    TintTypedArray.obtainStyledAttributes(this, set, attrs, defStyleAttr, defStyleRes)