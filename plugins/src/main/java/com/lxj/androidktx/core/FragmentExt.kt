package com.lxj.androidktx.core

import android.app.Application
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.*
import com.lxj.androidktx.AndroidKTX
import com.lxj.androidktx.livedata.LifecycleHandler


/**
 * Description: Fragment相关扩展
 * Create by dance, at 2018/12/5
 */

/**
 * fragment批处理，自动commit
 */
fun FragmentActivity.fragmentManager(action: FragmentTransaction.() -> Unit){
    supportFragmentManager.beginTransaction()
            .apply { action() }
            .commitAllowingStateLoss()
}

fun FragmentActivity.replace(layoutId: Int, f: Fragment, bundle: Array<out Pair<String, Any?>>? = null){
    if(bundle!=null)f.arguments = bundle.toBundle()
    supportFragmentManager.beginTransaction()
            .replace(layoutId, f)
            .commitAllowingStateLoss()
}

fun FragmentActivity.add(layoutId: Int, f: Fragment,  bundle: Array<out Pair<String, Any?>>? = null){
    if(bundle!=null)f.arguments = bundle.toBundle()
    supportFragmentManager.beginTransaction()
            .add(layoutId, f)
            .commitAllowingStateLoss()
}

fun FragmentActivity.hide(f: Fragment){
    supportFragmentManager.beginTransaction()
            .hide(f)
            .commitAllowingStateLoss()
}

fun FragmentActivity.show(f: Fragment){
    supportFragmentManager.beginTransaction()
            .show(f)
            .commitAllowingStateLoss()
}
fun FragmentActivity.remove(f: Fragment){
    supportFragmentManager.beginTransaction()
            .remove(f)
            .commitAllowingStateLoss()
}


fun Fragment.replace(layoutId: Int, f: Fragment,  bundle: Array<out Pair<String, Any?>>? = null){
    if(bundle!=null)f.arguments = bundle.toBundle()
    childFragmentManager.beginTransaction()
            .replace(layoutId, f)
            .commitAllowingStateLoss()
}

fun Fragment.add(layoutId: Int, f: Fragment,  bundle: Array<out Pair<String, Any?>>? = null){
    if(bundle!=null)f.arguments = bundle.toBundle()
    childFragmentManager.beginTransaction()
            .add(layoutId, f)
            .commitAllowingStateLoss()
}

fun Fragment.hide( f: Fragment){
    childFragmentManager.beginTransaction()
            .hide(f)
            .commitAllowingStateLoss()
}

fun Fragment.show(f: Fragment){
    childFragmentManager.beginTransaction()
            .show(f)
            .commitAllowingStateLoss()
}
fun Fragment.remove(f: Fragment){
    childFragmentManager.beginTransaction()
            .remove(f)
            .commitAllowingStateLoss()
}

//post, postDelay
fun Fragment.post(action: ()->Unit){
    LifecycleHandler(this).post { action() }
}

fun Fragment.postDelay(delay:Long = 0, action: ()->Unit){
    LifecycleHandler(this).postDelayed({ action() }, delay)
}

//view model
fun <T: ViewModel> Fragment.getVM(clazz: Class<T>) = ViewModelProvider(this).get(clazz)
/**
 * saved state view model，要求ViewModel的构造必须接受SavedStateHandle类型的参数，比如：
 * ```
 * class TestVM( handler: SavedStateHandle): ViewModel()
 * ```
 */
fun <T: ViewModel> Fragment.getSavedStateVM(clazz: Class<T>) = ViewModelProvider(this, SavedStateViewModelFactory(
    AndroidKTX.context as Application, this)
).get(clazz)


fun <T : Fragment> T.withArguments(vararg pairs: Pair<String, *>) = apply {
    arguments = bundleOf(*pairs)
}
