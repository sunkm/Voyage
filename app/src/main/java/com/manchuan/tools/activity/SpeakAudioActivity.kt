package com.manchuan.tools.activity

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dylanc.longan.design.FragmentStateAdapter
import com.dylanc.longan.design.setupWithViewPager2
import com.google.android.material.tabs.TabLayout
import com.gyf.immersionbar.ktx.immersionBar
import com.manchuan.tools.databinding.ActivityGamesBinding
import com.manchuan.tools.fragment.*
import com.manchuan.tools.utils.UiUtils
import io.github.inflationx.viewpump.ViewPumpContextWrapper

class SpeakAudioActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityGamesBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "有声小说"
            setDisplayHomeAsUpEnabled(true)
        }
        immersionBar {
            titleBar(binding.toolbar)
            transparentBar()
            statusBarDarkFont(!UiUtils.isDarkMode())
        }
        binding.viewPager.adapter = FragmentStateAdapter(XuanHuanFragment(),
            WuXiaFragment(),
            DuShiFragment(),
            PingShuFragment(),
            XiangShengFragment(),
            LiShiFragment(),
            JunShiFragment(),
            BaiJiaFragment(),
            isLazyLoading = true)
        val category = listOf("玄幻", "武侠", "都市", "评书", "相声", "历史", "军事", "百家讲坛")
        binding.tabLay.setupWithViewPager2(binding.viewPager,
            autoRefresh = true,
            enableScroll = true) { tab: TabLayout.Tab, i: Int ->
            tab.text = category[i]
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.let { ViewPumpContextWrapper.wrap(it) })
    }

}