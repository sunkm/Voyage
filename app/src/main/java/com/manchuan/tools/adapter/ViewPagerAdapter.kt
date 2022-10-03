package com.manchuan.tools.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.blankj.utilcode.util.StringUtils
import com.manchuan.tools.R

class ViewPagerAdapter(manager: FragmentActivity?) : FragmentStateAdapter(
    manager!!
) {

    private val mFragmentList: MutableList<Fragment> = ArrayList()

    fun addFragment(fragment: Fragment) {
        mFragmentList.add(fragment)
    }

    fun getPageTitle(position: Int): CharSequence? {
        val home = StringUtils.getString(R.string.menu_home)
        val functions = StringUtils.getString(R.string.menu_gallery)
        when (position) {
            0 -> return home
            1 -> return functions
            2 -> return "资源库"
        }
        return null
    }

    override fun getItemCount(): Int {
        return mFragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return mFragmentList[position]
    }
}