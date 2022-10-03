package com.manchuan.tools.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import com.alibaba.fastjson.JSON
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.grid
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.Get
import com.drake.net.utils.scope
import com.dylanc.longan.addNavigationBarHeightToMarginBottom
import com.dylanc.longan.startActivity
import com.gyf.immersionbar.ktx.immersionBar
import com.lxj.androidktx.core.load
import com.manchuan.tools.R
import com.manchuan.tools.base.AnimationActivity
import com.manchuan.tools.bean.WallpaperCategoryBean
import com.manchuan.tools.databinding.ActivityWallpaperCategoryBinding
import com.manchuan.tools.databinding.ItemCategoryBinding
import com.manchuan.tools.utils.UiUtils
import timber.log.Timber
import java.util.*

class WallpaperCategoryActivity : AnimationActivity() {
    private var wallpaperList = mutableListOf<WallpaperCategoryBean>()

    private val binding by lazy {
        ActivityWallpaperCategoryBinding.inflate(layoutInflater)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        immersionBar {
            titleBar(binding.toolbar)
            statusBarDarkFont(!UiUtils.isDarkMode())
            transparentBar()
        }
        binding.ctl.apply {
            title = "壁纸大全"
            subtitle = "多分类高清手机壁纸"
        }
        binding.toolbar.apply {
            setNavigationOnClickListener {
                finish()
            }
        }
        binding.page.addNavigationBarHeightToMarginBottom()
        binding.recyclerView.grid(2).setup {
            setAnimation(AnimationType.ALPHA)
            addType<WallpaperCategoryBean>(R.layout.item_category)
            onBind {
                val binding = ItemCategoryBinding.bind(itemView)
                val model = getModel<WallpaperCategoryBean>()
                binding.name.text = model.name
                binding.image.load(model.image, isCrossFade = true, isForceOriginalSize = true)
            }
            R.id.card.onClick {
                val model = getModel<WallpaperCategoryBean>()
                startActivity<WallpaperPreviewActivity>("id" to model.id, "title" to model.name)
            }
        }
        binding.page.setEnableLoadMore(false)
        binding.page.onRefresh {
            scope {
                val list =
                    Get<String>("http://service.picasso.adesk.com/v1/lightwp/category").await()
                val jsonObject = JSON.parseObject(list)
                val message = jsonObject.getString("res")
                val jsonArray = JSON.parseObject(message)
                val category = JSON.parseArray(jsonArray.getString("category"))
                wallpaperList.clear()
                for (i in 0 until category.size) {
                    val wallpaperCategoryBean = WallpaperCategoryBean()
                    val datas = JSON.parseObject(category.getString(i))
                    wallpaperCategoryBean.name = datas.getString("rname")
                    wallpaperCategoryBean.image = datas.getString("cover")
                    wallpaperCategoryBean.id = datas.getString("id")
                    wallpaperList.add(wallpaperCategoryBean)
                }
                binding.recyclerView.models = wallpaperList
            }.catch {
                Timber.tag("壁纸大全").e(it)
            }
        }.autoRefresh()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}