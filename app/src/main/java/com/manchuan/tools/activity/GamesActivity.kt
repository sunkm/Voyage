package com.manchuan.tools.activity

import android.os.Bundle
import com.dylanc.longan.design.FragmentStateAdapter
import com.dylanc.longan.design.setupWithViewPager2
import com.google.android.material.tabs.TabLayout
import com.gyf.immersionbar.ktx.immersionBar
import com.manchuan.tools.adapter.transformer.CascadeTransformer
import com.manchuan.tools.base.AnimationActivity
import com.manchuan.tools.databinding.ActivityGamesBinding
import com.manchuan.tools.fragment.*
import com.manchuan.tools.utils.UiUtils
import com.skydoves.transformationlayout.onTransformationStartContainer


class GamesActivity : AnimationActivity() {
    private val binding by lazy {
        ActivityGamesBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        onTransformationStartContainer()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "怀旧游戏大全"
            setDisplayHomeAsUpEnabled(true)
        }
        immersionBar {
            titleBar(binding.toolbar)
            transparentBar()
            statusBarDarkFont(!UiUtils.isDarkMode())
        }
        initViewPager()
        val category = listOf("街机游戏", "FC游戏", "SFC游戏", "MD游戏", "GBA游戏")
        binding.tabLay.setupWithViewPager2(binding.viewPager,
            autoRefresh = true,
            enableScroll = true,
            tabConfigurationStrategy = { tab: TabLayout.Tab, i: Int ->
                tab.text = category[i]
            })
    }

    private fun initViewPager() {
        binding.viewPager.adapter = FragmentStateAdapter(GameJieJiFragment(),
            GameFCFragment(),
            GameSFCFragment(),
            GameMDFragment(),
            GameGBAFragment(),
            isLazyLoading = true)
        binding.viewPager.setPageTransformer(CascadeTransformer())
    }

}