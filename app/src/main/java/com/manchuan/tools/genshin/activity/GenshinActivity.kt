package com.manchuan.tools.genshin.activity

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ThreadUtils
import com.dylanc.longan.addNavigationBarHeightToMarginBottom
import com.dylanc.longan.design.FragmentStateAdapter
import com.dylanc.longan.startActivity
import com.dylanc.longan.toast
import com.gyf.immersionbar.ImmersionBar
import com.manchuan.tools.R
import com.manchuan.tools.databinding.ActivityGenshinBinding
import com.manchuan.tools.extensions.textColorPrimary
import com.manchuan.tools.genshin.activity.fragment.MapFragment
import com.manchuan.tools.genshin.activity.ui.dashboard.DashboardFragment
import com.manchuan.tools.genshin.activity.ui.home.HomeGenshinFragment
import com.manchuan.tools.genshin.bean.AccountBean
import com.manchuan.tools.genshin.ext.mainUser
import com.manchuan.tools.genshin.ext.onPageChange
import com.manchuan.tools.genshin.information.MiHoYoAPI
import com.manchuan.tools.genshin.untils.GSON
import com.manchuan.tools.genshin.untils.RequestApi
import com.manchuan.tools.genshin.untils.ok
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.*
import com.mikepenz.materialdrawer.widget.AccountHeaderView

class GenshinActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityGenshinBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        ImmersionBar.with(this)
            .transparentNavigationBar()
            .init()
        binding.viewPager.adapter =
            FragmentStateAdapter(
                HomeGenshinFragment(),
                MapFragment(),
                DashboardFragment(),
                isLazyLoading = false
            )
        binding.viewPager.addNavigationBarHeightToMarginBottom()
        binding.viewPager.apply {
            isUserInputEnabled = false
            onPageChange {
                binding.bottomBar.menu.getItem(it).isChecked = true
            }
        }
        binding.bottomBar.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    binding.viewPager.currentItem = 0
                    return@setOnItemSelectedListener true
                }
                R.id.functions -> {
                    binding.viewPager.setCurrentItem(1, true)
                    return@setOnItemSelectedListener true
                }
                R.id.my -> {
                    binding.viewPager.currentItem = 2
                    return@setOnItemSelectedListener true
                }
            }
            true
        }
        initDrawer(savedInstanceState)
    }

    fun goMineInformation() {
        binding.viewPager.currentItem = 2
    }


    private fun initDrawer(savedInstanceState: Bundle?) {
        mainUser?.let { user ->
            RequestApi.get(MiHoYoAPI.getAccountInformation(user.loginUid)) {
                if (it.ok) {
                    val accountInfo =
                        GSON.fromJson(it.optString("data"), AccountBean::class.java)
                    ThreadUtils.runOnUiThread {
                        val headerView = AccountHeaderView(this).apply {
                            attachToSliderView(binding.navDrawer) // attach to the slider
                            addProfiles(
                                ProfileDrawerItem().apply {
                                    nameText = user.nickName; descriptionText =
                                    user.gameUid; iconUrl =
                                    accountInfo.user_info.avatar_url; identifier =
                                    user.loginUid.toLong()
                                }
                            )
                            onAccountHeaderListener = { view, profile, current ->
                                // react to profile changes
                                false
                            }
                            val item2 =
                                SecondaryDrawerItem().apply {
                                    iconRes = R.drawable.icon_one_hand_sword;
                                    nameText = "武器图鉴";
                                    identifier = 2;
                                    iconColor = ColorStateList.valueOf(textColorPrimary());
                                    isSelectable = false
                                }
                            binding.navDrawer.itemAdapter.add(
                                item2,
                                SecondaryDrawerItem().apply { nameRes = R.string.action_settings }

                            )
                            binding.navDrawer.onDrawerItemClickListener =
                                { v, drawerItem, position ->
                                    when (drawerItem.identifier.toInt()) {
                                        2 -> {
                                            startActivity<WeaponActivity>()
                                        }
                                    }
                                    toast(drawerItem.identifier.toString())
                                    false
                                }
                            withSavedInstance(savedInstanceState)
                        }
                    }
                }
            }
        }
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        binding.rootLay.animate().alpha(1f).duration = 1000
    }

}