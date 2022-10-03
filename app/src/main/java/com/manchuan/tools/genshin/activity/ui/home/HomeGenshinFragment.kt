package com.manchuan.tools.genshin.activity.ui.home

import android.annotation.SuppressLint
import android.widget.ArrayAdapter
import com.blankj.utilcode.util.ThreadUtils.runOnUiThread
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.drake.engine.base.EngineFragment
import com.dylanc.longan.addStatusBarHeightToMarginTop
import com.dylanc.longan.doOnClick
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.manchuan.tools.R
import com.manchuan.tools.databinding.BottomMonthLedgerBinding
import com.manchuan.tools.databinding.FragmentGenshinHomeBinding
import com.manchuan.tools.databinding.ItemHomeNearActivityBinding
import com.manchuan.tools.databinding.ItemHomeNoticeBinding
import com.manchuan.tools.extensions.addPaddingBottom
import com.manchuan.tools.extensions.dp
import com.manchuan.tools.genshin.GenshinConfig
import com.manchuan.tools.genshin.activity.GenshinActivity
import com.manchuan.tools.genshin.activity.GenshinWebActivity
import com.manchuan.tools.genshin.activity.ui.adapter.HomeBannerAdapter
import com.manchuan.tools.genshin.base.CustomSheet
import com.manchuan.tools.genshin.bean.MonthLedgerBean
import com.manchuan.tools.genshin.bean.home.BlackBoardBean
import com.manchuan.tools.genshin.bean.home.HomeInformationBean
import com.manchuan.tools.genshin.bean.home.HomeOfficialCommendPostBean
import com.manchuan.tools.genshin.ext.*
import com.manchuan.tools.genshin.information.Format
import com.manchuan.tools.genshin.information.MiHoYoAPI
import com.manchuan.tools.genshin.transformations.BlurTransformation
import com.manchuan.tools.genshin.untils.GSON
import com.manchuan.tools.genshin.untils.RequestApi
import com.manchuan.tools.genshin.untils.ok
import com.youth.banner.indicator.CircleIndicator
import com.youth.banner.listener.OnPageChangeListener

class HomeGenshinFragment :
    EngineFragment<FragmentGenshinHomeBinding>(R.layout.fragment_genshin_home) {

    val mainActivity: GenshinActivity
        get() = activity as GenshinActivity

    private val nearActivity = mutableListOf<BlackBoardBean.DataBean.ListBean>()
    private val bannerData = mutableListOf<HomeInformationBean.CarouselsBean>()
    private val notices = mutableListOf<HomeOfficialCommendPostBean.ListBean>()
    override fun initData() {
        initOnceTip()
    }

    override fun initView() {
        initRecycler()
        refreshData()
        binding.root.addPaddingBottom(58.dp)
        binding.swipe.setOnRefreshListener {
            refreshData()
            binding.swipe.isRefreshing = false
        }
        monthLedger()
        binding.meInformation.doOnClick {
            mainActivity.goMineInformation()
        }
    }

    private fun initOnceTip() {

    }


    private val monthList = mutableListOf<Int>()
    private val pieEntry = mutableListOf<PieEntry>()

    private fun monthLedger() {
        binding.monthLedge.doOnClick {
            CustomSheet().show(requireContext()) {
                title("旅行札记")
            }
        }
    }

    private fun loadMonthLedger(
        month: Int = 0,
        bottomMonthLedgerBinding: BottomMonthLedgerBinding,
    ) {
        //加载本月获得的原石
        mainUser?.let { userBean ->
            RequestApi.get(
                MiHoYoAPI.getMonthLedgerUrl(month, userBean.gameUid, userBean.region),
                userBean
            ) {
                runOnUiThread {
                    if (it.ok) {
                        val monthLedger =
                            GSON.fromJson(
                                it.optString("data"),
                                MonthLedgerBean::class.java
                            )
                        if (monthList.size == 0) {
                            val monthShowList = mutableListOf<String>()
                            monthLedger.optional_month.forEach {
                                monthList += it
                                monthShowList += "${it}月"
                            }
                            bottomMonthLedgerBinding.monthSelect.adapter = ArrayAdapter(
                                bottomMonthLedgerBinding.root.context,
                                R.layout.item_text,
                                monthShowList
                            ).apply {
                                setDropDownViewResource(R.layout.spinner_drop_down_style)
                            }
                            bottomMonthLedgerBinding.monthSelect.setSelection(monthList.size - 1)
                        }

                        bottomMonthLedgerBinding.dayGemstone.text =
                            monthLedger.day_data.current_primogems.toString()
                        bottomMonthLedgerBinding.dayMora.text =
                            monthLedger.day_data.current_mora.toString()
                        bottomMonthLedgerBinding.monthGemstone.text =
                            monthLedger.month_data.current_primogems.toString()
                        bottomMonthLedgerBinding.monthMora.text =
                            monthLedger.month_data.current_mora.toString()
                        pieEntry.clear()
                        val actionIds = mutableListOf<Int>()
                        monthLedger.month_data.group_by.forEach {
                            actionIds += it.action_id
                            pieEntry += PieEntry(
                                it.percent.toFloat(),
                                "${it.action} ${it.percent}%"
                            )
                        }
                        with(bottomMonthLedgerBinding.pie) {
                            val pieDataSet = PieDataSet(pieEntry, "")
                            pieDataSet.colors =
                                com.manchuan.tools.genshin.information.Constants.getMonthLegendColors(
                                    actionIds
                                )
                            pieDataSet.setDrawValues(false)
                            val pieData = PieData(pieDataSet)
                            data = pieData
                            invalidate()
                        }
                    } else {
                        //val win = showAlertDialog(bind.root.context, layout.root)
                        "获取信息失败啦".show()
                        //layout.close.setOnClickListener {
                        //    win.dismiss()
                        //}
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initRecycler() {
        binding.homeBanner.addStatusBarHeightToMarginTop()
        binding.homeNotice.linear().setup {
            addType<HomeOfficialCommendPostBean.ListBean>(R.layout.item_home_notice)
            onBind {
                val item = ItemHomeNoticeBinding.bind(itemView)
                loadHomeNoticeImage(
                    item.cover,
                    getModel<HomeOfficialCommendPostBean.ListBean>(modelPosition).banner
                )
                item.title.text =
                    getModel<HomeOfficialCommendPostBean.ListBean>(modelPosition).subject
            }
            onClick(R.id.card) {
                if (sp.getBoolean(GenshinConfig.SP_HOME_NOTICE_JUMP_TO_ARTICLE, true)) {
                    GenshinWebActivity.articleId =
                        getModel<HomeOfficialCommendPostBean.ListBean>(modelPosition).post_id
                    goA<GenshinWebActivity>()
                }
            }
        }.models = notices
        binding.homeNearActivity.linear().setup {
            addType<BlackBoardBean.DataBean.ListBean>(R.layout.item_home_near_activity)
            onBind {
                val item = ItemHomeNearActivityBinding.bind(itemView)
                loadImage(
                    item.avatar,
                    getModel<BlackBoardBean.DataBean.ListBean>(modelPosition).img_url
                )
                item.title.text = getModel<BlackBoardBean.DataBean.ListBean>(modelPosition).title
                item.time.text =
                    "${Format.TIME.format(getModel<BlackBoardBean.DataBean.ListBean>(modelPosition).start_time.toLong() * 1000)} 至 ${
                        Format.TIME.format(getModel<BlackBoardBean.DataBean.ListBean>(modelPosition).end_time.toLong() * 1000)
                    }"
            }
            onClick(R.id.card) {
                if (sp.getBoolean(GenshinConfig.SP_HOME_NEAR_ACTIVITY_JUMP_TO_ARTICLE, true)) {
                    GenshinWebActivity.articleId = MiHoYoAPI.getArticlePostIdByUrl(
                        getModel<BlackBoardBean.DataBean.ListBean>(modelPosition).jump_url
                    )
                    goA<GenshinWebActivity>()
                }
            }
        }.models = nearActivity
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun refreshData() {
        binding.state.showLoading()
        //公告位
        RequestApi.get(MiHoYoAPI.HOME_PAGER_INFORMATION) {
            if (it.ok) {
                val bannerBean =
                    GSON.fromJson(it.optString("data"), HomeInformationBean::class.java)
                bannerBean.carousels.copy(bannerData)
                activity?.runOnUiThread {
                    binding.homeBanner.apply {
                        setBannerRound2(6F)
                        indicator = CircleIndicator(context)
                        addBannerLifecycleObserver(this@HomeGenshinFragment)
                        setAdapter(HomeBannerAdapter(bannerData, {
                        }) {

                        })
                        setBannerGalleryMZ(40)
                        addOnPageChangeListener(object : OnPageChangeListener {
                            override fun onPageScrolled(
                                position: Int,
                                positionOffset: Float,
                                positionOffsetPixels: Int,
                            ) {

                            }

                            override fun onPageSelected(position: Int) {
                                Glide.with(context)
                                    .load(bannerData[position].cover)
                                    .optionalCenterCrop()
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .apply(
                                        RequestOptions.bitmapTransform(
                                            BlurTransformation(
                                                context,
                                                24,
                                                5
                                            )
                                        )
                                    )
                                    .skipMemoryCache(true)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(binding.homeBackground)
                            }

                            override fun onPageScrollStateChanged(state: Int) {
                            }

                        })
                    }
                    Glide.with(requireContext())
                        .load(bannerData[0].cover)
                        .optionalCenterCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .apply(
                            RequestOptions.bitmapTransform(
                                BlurTransformation(
                                    requireContext(),
                                    24,
                                    5
                                )
                            )
                        )
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(binding.homeBackground)
                }
            }
        }

        //近期活动

        RequestApi.get(MiHoYoAPI.BLACK_BOARD) { it ->
            if (it.ok) {
                val blackBoard = GSON.fromJson(it.toString(), BlackBoardBean::class.java)
                nearActivity.clear()
                blackBoard.data.list.forEach {
                    if (it.kind == "1" && it.break_type == "0") {
                        nearActivity += it
                    }
                }
                activity?.runOnUiThread {
                    binding.homeNearActivity.adapter?.notifyDataSetChanged()
                }
            }
        }

        //下方公告
        RequestApi.get(MiHoYoAPI.OFFICIAL_RECOMMEND_POST) { it ->
            if (it.ok) {
                val recommendPost =
                    GSON.fromJson(it.optString("data"), HomeOfficialCommendPostBean::class.java)
                recommendPost.list.sortByDescending { it.post_id }
                recommendPost.list.copy(notices)
                activity?.runOnUiThread {
                    binding.homeNotice.adapter?.notifyDataSetChanged()
                }
            }
        }
        binding.state.showContent()
    }

}