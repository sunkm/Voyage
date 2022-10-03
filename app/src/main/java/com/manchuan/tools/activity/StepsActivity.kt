package com.manchuan.tools.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.drake.statusbar.immersive
import com.google.android.material.tabs.TabLayout
import com.manchuan.tools.activity.ui.main.PlaceholderFragment
import com.manchuan.tools.activity.ui.main.SectionsPagerAdapter
import com.manchuan.tools.databinding.ActivityStepsBinding
import com.manchuan.tools.fragment.XiaoMiFragment

class StepsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStepsBinding

    @SuppressLint("ShowToast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStepsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "步数修改"
            setDisplayHomeAsUpEnabled(true)
        }
        immersive(binding.toolbar)
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        sectionsPagerAdapter.addFragment(PlaceholderFragment())
        sectionsPagerAdapter.addFragment(XiaoMiFragment())
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}