package com.manchuan.tools.activity.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.manchuan.tools.R

private val TAB_TITLES = arrayOf(
    R.string.tab_text_1,
    R.string.tab_text_2
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm) {

    private val mFragmentList: MutableList<Fragment> = ArrayList()

    fun addFragment(fragment: Fragment) {
        mFragmentList.add(fragment)
    }

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return mFragmentList[position]
    }

    override fun getPageTitle(position: Int): CharSequence? {
        when (position) {
            0 -> return "悦动圈"
            1 -> return "小米运动"
        }
        return null
    }

    override fun getCount(): Int {
        // Show 2 total pages.
        return mFragmentList.size
    }
}