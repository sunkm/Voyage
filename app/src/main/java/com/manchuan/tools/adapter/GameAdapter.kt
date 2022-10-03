package com.manchuan.tools.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class GameAdapter(manager: FragmentManager?) : FragmentStatePagerAdapter(
    manager!!
) {

    private val mFragmentList: MutableList<Fragment> = ArrayList()

    fun addFragment(fragment: Fragment) {
        mFragmentList.add(fragment)
    }

    override fun getCount(): Int {
        return mFragmentList.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        when (position) {
            0 -> return "街机游戏"
            1 -> return "FC游戏"
            2 -> return "SFC游戏"
            3 -> return "MD游戏"
            4 -> return "GBA游戏"
        }
        return null
    }

    override fun getItem(position: Int): Fragment {
        return mFragmentList[position]
    }

}