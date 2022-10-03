package com.manchuan.tools.genshin.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.grid
import com.drake.brv.utils.setup
import com.dylanc.longan.activity
import com.dylanc.longan.addStatusBarHeightToPaddingTop
import com.gyf.immersionbar.ktx.immersionBar
import com.manchuan.tools.R
import com.manchuan.tools.databinding.ActivityWeaponBinding
import com.manchuan.tools.databinding.ItemEntityBinding
import com.manchuan.tools.extensions.loge
import com.manchuan.tools.genshin.bean.WeaponBean
import com.manchuan.tools.genshin.ext.copy
import com.manchuan.tools.genshin.ext.loadImage
import com.manchuan.tools.genshin.ext.select
import com.manchuan.tools.genshin.ext.setViewMarginBottomByNavigationBarHeight
import com.manchuan.tools.genshin.information.Star
import com.manchuan.tools.genshin.information.WeaponType
import com.manchuan.tools.utils.UiUtils

class WeaponActivity : AppCompatActivity() {

    private val weaponShowList = mutableListOf<WeaponBean>()
    private val selectWeapon = mutableListOf<Int>()

    private val binding by lazy {
        ActivityWeaponBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        immersionBar {
            transparentBar()
            statusBarDarkFont(!UiUtils.isDarkMode())
        }
        initView()
        initSelect()
        binding.list.addStatusBarHeightToPaddingTop()
    }

    private fun initView() {
        binding.rank.setOnClickListener {
            finish()
        }
        WeaponBean.weaponList.copy(weaponShowList)
        weaponShowList.sortByDescending { it.star }
        binding.list.setHasFixedSize(true)
        binding.list.grid(4).setup {
            setAnimation(AnimationType.ALPHA)
            addType<WeaponBean>(R.layout.item_entity)
            onBind {
                val item = ItemEntityBinding.bind(itemView)
                val weapon = getModel<WeaponBean>()
                item.name.text = weapon.name
                loadImage(item.icon, weapon.icon)
                item.type.setImageResource(WeaponType.getResourceByType(weapon.weaponType))
                loge(weapon.star.toString())
                item.starBackground.setBackgroundResource(
                    Star.getStarResourcesByStarNum(
                        weapon.star,
                        false
                    )
                )
                item.root.setOnClickListener {
                    /*
                    WeaponDetailActivity.weapon = weapon
                    startActivity(
                        Intent(requireActivity(), WeaponDetailActivity::class.java),
                        ActivityOptions.makeSceneTransitionAnimation(
                            requireActivity(),
                            item.icon,
                            "icon"
                        ).toBundle()
                    )
                     */
                }
            }
        }.models = weaponShowList
        setViewMarginBottomByNavigationBarHeight(binding.list)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun notificationUpdate() {
        weaponShowList.clear()
        WeaponBean.weaponList.forEach { entity ->
            if (selectWeapon.indexOf(entity.weaponType) != -1) {
                weaponShowList += entity
            }
        }
        weaponShowList.sortByDescending { it.star }
        activity.runOnUiThread {
            binding.list.adapter?.notifyDataSetChanged()
            binding.selectSpan.isEnabled = true
        }
    }

    private fun initSelect() {
        with(binding) {
            selectWeapon += WeaponType.ONE_HAND_SWORD
            selectWeapon += WeaponType.BOTH_HAND_SWORD
            selectWeapon += WeaponType.BOW_AND_ARROW
            selectWeapon += WeaponType.MAGIC_ARTS
            selectWeapon += WeaponType.SPEAR
            val selectWeaponViews = listOf(
                selectOneHandSword,
                selectBothHandSword,
                selectBowAndArrow,
                selectMagicArts,
                selectSpear
            )
            val selectWeapons = listOf(
                WeaponType.ONE_HAND_SWORD,
                WeaponType.BOTH_HAND_SWORD,
                WeaponType.BOW_AND_ARROW,
                WeaponType.MAGIC_ARTS,
                WeaponType.SPEAR
            )
            selectWeaponViews.forEachIndexed { index, selectView ->
                selectView.select {
                    if (it)
                        selectWeapon.add(selectWeapons[index])
                    else
                        selectWeapon.remove(selectWeapons[index])
                    notificationUpdate()
                }
            }
        }
    }

}