package com.manchuan.tools.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewPagerCompassAdapter(manager: FragmentManager?) : FragmentPagerAdapter(
    manager!!
) {
    private val mFragmentList: MutableList<Fragment> = ArrayList()
    override fun getItem(position: Int): Fragment {
        return mFragmentList[position]
    }

    override fun getCount(): Int {
        return mFragmentList.size
    }

    fun addFragment(fragment: Fragment) {
        mFragmentList.add(fragment)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        when (position) {
            0 -> return "指南针"
            1 -> return "水平仪"
        }
        return null
    }
}