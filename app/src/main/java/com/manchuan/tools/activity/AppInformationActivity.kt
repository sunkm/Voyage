package com.manchuan.tools.activity

import android.app.Activity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.drake.statusbar.immersive
import com.google.android.material.tabs.TabLayout
import com.manchuan.tools.R
import com.manchuan.tools.adapter.AppInfoAdapter
import com.manchuan.tools.fragment.AppActivitiesFragment
import com.manchuan.tools.fragment.AppInfoFragment
import com.manchuan.tools.fragment.AppServicesFragment
import rikka.material.app.MaterialActivity

class AppInformationActivity : MaterialActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_info)
        initView(this)
        val intent = this.intent
        app_name = intent.getStringExtra("appName")
        package_name = intent.getStringExtra("packageName")
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        immersive(toolbar!!)
        tab_lay!!.setupWithViewPager(nav_app_viewpager)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toolbar!!.title = app_name
        setUpViewPager(nav_app_viewpager)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            else -> {}
        }
        return super.onOptionsItemSelected(item)
    }

    private var toolbar: Toolbar? = null
    private var tab_lay: TabLayout? = null
    private var nav_app_viewpager: ViewPager? = null
    private fun initView(activity: Activity) {
        toolbar = activity.findViewById(R.id.toolbar)
        tab_lay = activity.findViewById(R.id.tab_lay)
        nav_app_viewpager = activity.findViewById(R.id.nav_app_viewpager)
    }

    private fun setUpViewPager(viewPager: ViewPager?) {
        val adapter = AppInfoAdapter(supportFragmentManager)
        adapter.addFragment(AppInfoFragment())
        adapter.addFragment(AppActivitiesFragment())
        adapter.addFragment(AppServicesFragment())
        viewPager!!.adapter = adapter
    }

    companion object {
        @JvmField
        var package_name: String? = null
        @JvmField
        var app_name: String? = null
    }
}