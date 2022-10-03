package com.lxj.androidktx.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.FragmentUtils
import com.lxj.statelayout.StateLayout

/**
 * 自带StateLayout的Fragment基类，适用于ViewPager2的懒加载方案
 */
abstract class StateFragment : Fragment(), FragmentUtils.OnBackClickListener {
    private var hasInitView = false
    private var hasInitData = false
    protected var stateLayout: StateLayout? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        if (stateLayout == null) {
            val cacheView = inflater.inflate(getLayoutId(), container, false)
            stateLayout = StateLayout(requireContext()).config(defaultShowLoading = true).wrap(cacheView)
            onConfigStateLayout()
        }
        return stateLayout!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(!hasInitView){
            hasInitView = true
            initView()
        }
    }

    /**
     * 用来对StateLayout进行各种配置
     */
    open fun onConfigStateLayout(){

    }

    open fun showContent() = stateLayout?.showContent()
    open fun showLoading() = stateLayout?.showLoading()
    open fun showError() = stateLayout?.showError()
    open fun showEmpty() = stateLayout?.showEmpty()

    override fun onResume() {
        super.onResume()
        if(!hasInitData){
            hasInitData = true
            initData()
        }
    }
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if(isVisibleToUser) onShow() else onHide()
    }
    //执行初始化，只会执行一次
    protected abstract fun getLayoutId(): Int
    abstract fun initView()
    abstract fun initData()
    open fun onShow(){}
    open fun onHide(){}

    override fun onBackClick() = false
}