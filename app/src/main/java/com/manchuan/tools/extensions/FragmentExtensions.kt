package com.manchuan.tools.extensions

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.PowerManager
import android.widget.Toast
import androidx.annotation.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment

fun Fragment.getAnimation(@AnimRes id: Int) = requireContext().getAnimation(id)

fun Fragment.getBoolean(@BoolRes id: Int) = requireContext().getBoolean(id)

fun Fragment.getDimension(@DimenRes id: Int) = requireContext().getDimension(id)

fun Fragment.getDimensionPixelOffset(@DimenRes id: Int) =
    requireContext().getDimensionPixelOffset(id)

fun Fragment.getDimensionPixelSize(@DimenRes id: Int) = requireContext().getDimensionPixelSize(id)

fun Fragment.getFloat(@DimenRes id: Int) = requireContext().getFloat(id)

fun Fragment.getInteger(@IntegerRes id: Int) = requireContext().getInteger(id)

fun Fragment.getInterpolator(@InterpolatorRes id: Int) = requireContext().getInterpolator(id)

fun Fragment.getQuantityString(@PluralsRes id: Int, quantity: Int): String =
    requireContext().getQuantityString(id, quantity)

fun Fragment.getQuantityString(
    @PluralsRes id: Int,
    quantity: Int,
    vararg formatArgs: Any?
): String = requireContext().getQuantityString(id, quantity, *formatArgs)

fun Fragment.getQuantityText(@PluralsRes id: Int, quantity: Int): CharSequence =
    requireContext().getQuantityText(id, quantity)

fun Fragment.getStringArray(@ArrayRes id: Int) = requireContext().getStringArray(id)

fun Fragment.getBooleanByAttr(@AttrRes attr: Int) = requireContext().getBooleanByAttr(attr)

fun Fragment.getColorByAttr(@AttrRes attr: Int) = requireContext().getColorByAttr(attr)

fun Fragment.getColorStateListByAttr(@AttrRes attr: Int) =
    requireContext().getColorStateListByAttr(attr)

fun Fragment.getDimensionByAttr(@AttrRes attr: Int) = requireContext().getDimensionByAttr(attr)

fun Fragment.getDimensionPixelOffsetByAttr(@AttrRes attr: Int) =
    requireContext().getDimensionPixelOffsetByAttr(attr)

fun Fragment.getDimensionPixelSizeByAttr(@AttrRes attr: Int): Int =
    requireContext().getDimensionPixelSizeByAttr(attr)

fun Fragment.getDrawableByAttr(@AttrRes attr: Int) = requireContext().getDrawableByAttr(attr)

fun Fragment.getFloatByAttr(@AttrRes attr: Int) = requireContext().getFloatByAttr(attr)

fun Fragment.getResourceIdByAttr(@AttrRes attr: Int): Int =
    requireContext().getResourceIdByAttr(attr)

@Dimension
fun Fragment.dpToDimension(@Dimension(unit = Dimension.DP) dp: Float) =
    requireContext().dpToDimension(dp)

@Dimension
fun Fragment.dpToDimension(@Dimension(unit = Dimension.DP) dp: Int) =
    requireContext().dpToDimension(dp)

@Dimension
fun Fragment.dpToDimensionPixelOffset(@Dimension(unit = Dimension.DP) dp: Float) =
    requireContext().dpToDimensionPixelOffset(dp)

@Dimension
fun Fragment.dpToDimensionPixelOffset(@Dimension(unit = Dimension.DP) dp: Int) =
    requireContext().dpToDimensionPixelOffset(dp)

@Dimension
fun Fragment.dpToDimensionPixelSize(@Dimension(unit = Dimension.DP) dp: Float) =
    requireContext().dpToDimensionPixelSize(dp)

@Dimension
fun Fragment.dpToDimensionPixelSize(@Dimension(unit = Dimension.DP) dp: Int) =
    requireContext().dpToDimensionPixelSize(dp)

val Fragment.shortAnimTime
    get() = requireContext().shortAnimTime

val Fragment.mediumAnimTime
    get() = requireContext().mediumAnimTime

val Fragment.longAnimTime
    get() = requireContext().longAnimTime

fun Fragment.getIntRes(@IntegerRes int: Int): Int {
    return resources.getInteger(int)
}

fun Context.getIntRes(@IntegerRes int: Int): Int {
    return resources.getInteger(int)
}

fun Context.isSystemDarkModeEnabled(): Boolean {
    val isBatterySaverEnabled =
        (getSystemService<PowerManager>())?.isPowerSaveMode ?: false
    val isDarkModeEnabled =
        (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    return isBatterySaverEnabled or isDarkModeEnabled
}

inline fun <reified T : Any> Fragment.extra(key: String, default: T? = null) = lazy {
    val value = arguments?.get(key)
    if (value is T) value else default
}

inline fun <reified T : Any> Fragment.extraNotNull(key: String, default: T? = null) = lazy {
    val value = arguments?.get(key)
    requireNotNull(if (value is T) value else default) { key }
}

fun AppCompatActivity.currentFragment(navHostId: Int): Fragment? {
    val navHostFragment: NavHostFragment =
        supportFragmentManager.findFragmentById(navHostId) as NavHostFragment
    return navHostFragment.childFragmentManager.fragments.firstOrNull()
}

@Suppress("UNCHECKED_CAST")
fun <T> AppCompatActivity.whichFragment(@IdRes id: Int): T {
    return supportFragmentManager.findFragmentById(id) as T
}

@Suppress("UNCHECKED_CAST")
fun <T> Fragment.whichFragment(@IdRes id: Int): T {
    return childFragmentManager.findFragmentById(id) as T
}

fun Fragment.showToast(@StringRes stringRes: Int, duration: Int = Toast.LENGTH_SHORT) {
    showToast(getString(stringRes), duration)
}

fun Fragment.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(requireContext(), message, duration).show()
}

fun Context.getDrawableCompat(@DrawableRes drawableRes: Int): Drawable {
    return AppCompatResources.getDrawable(this, drawableRes)!!
}

fun Fragment.getDrawableCompat(@DrawableRes drawableRes: Int): Drawable {
    return AppCompatResources.getDrawable(requireContext(), drawableRes)!!
}

fun Fragment.dip(@DimenRes id: Int): Int {
    return resources.getDimensionPixelSize(id)
}