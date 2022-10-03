package com.manchuan.tools.activity

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.setup
import com.drake.brv.utils.staggered
import com.drake.net.Get
import com.drake.net.utils.scopeNet
import com.dylanc.longan.addNavigationBarHeightToMarginBottom
import com.dylanc.longan.doOnClick
import com.dylanc.longan.logError
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.google.android.material.transition.platform.MaterialFadeThrough
import com.gyf.immersionbar.ktx.immersionBar
import com.lxj.androidktx.core.dateNow
import com.lxj.androidktx.core.load
import com.lxj.androidktx.core.toDateString
import com.manchuan.tools.R
import com.manchuan.tools.activity.history.DetailsActivity
import com.manchuan.tools.bean.HistoryBean
import com.manchuan.tools.databinding.ActivityHistoryBinding
import com.manchuan.tools.databinding.ItemHolidayBinding
import com.manchuan.tools.extensions.accentColor
import com.manchuan.tools.extensions.userAgent
import com.manchuan.tools.utils.UiUtils
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import rikka.material.app.MaterialActivity
import java.util.*


class HistoryTodayActivity : MaterialActivity() {
    private var toolbar: Toolbar? = null
    private var recyclerView: RecyclerView? = null
    private var historyBeans = mutableListOf<HistoryBean>()

    private val binding by lazy {
        ActivityHistoryBinding.inflate(layoutInflater)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        val enter = MaterialFadeThrough()
        window.enterTransition = enter
        window.allowEnterTransitionOverlap = true
        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        window.sharedElementsUseOverlay = false
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        toolbar = binding.toolbar
        recyclerView = binding.rvHoliday
        setSupportActionBar(toolbar)
        immersionBar {
            transparentBar()
            titleBar(binding.toolbar)
            statusBarDarkFont(!UiUtils.isDarkMode())
        }
        binding.fab.addNavigationBarHeightToMarginBottom()
        binding.refreshData.addNavigationBarHeightToMarginBottom()
        binding.refreshData.setSize(SwipeRefreshLayout.DEFAULT)
        binding.refreshData.setOnRefreshListener {
            historyBeans.clear()
            binding.rvHoliday.adapter?.notifyDataSetChanged()
            loadData()
        }
        binding.fab.doOnClick {
            val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("选择日期")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds()).build()
            datePicker.addOnPositiveButtonClickListener {
                // Respond to positive button click.
                loadDataAboutDate(it.toDateString("MMdd"))
                binding.ctl.subtitle = "当前选择日期:${it.toDateString("MM月dd日")}"

            }
            datePicker.show(supportFragmentManager, "选择日期")
        }
        supportActionBar?.apply {
            title = "历史上的今天"
            subtitle = "今天日期:${dateNow("yyyy年MM月dd日")}"
            setDisplayHomeAsUpEnabled(true)
        }
        historyBeans = ArrayList()
        binding.rvHoliday.staggered(2).setup {
            addType<HistoryBean>(R.layout.item_holiday)
            setAnimation(AnimationType.ALPHA)
            onBind {
                val view = ItemHolidayBinding.bind(itemView)
                val models = getModel<HistoryBean>()
                view.title.text = models.title
                view.time.setTextColor(accentColor())
                view.time.text = models.time
                view.holidayImage.load(models.image, isForceOriginalSize = true, isCrossFade = true)
            }
            onClick(R.id.card) {
                val view = ItemHolidayBinding.bind(itemView)
                val intent = Intent(context, DetailsActivity::class.java)
                intent.putExtra("content", getModel<HistoryBean>(modelPosition).content)
                intent.putExtra("image", getModel<HistoryBean>(modelPosition).image)
                val options: ActivityOptions =
                    ActivityOptions.makeSceneTransitionAnimation(this@HistoryTodayActivity,
                        view.card,
                        "shared_element_end_root")
                startActivity(intent, options.toBundle())
            }
        }.models = historyBeans
        loadData()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadData() {
        binding.state.showLoading()
        scopeNet {
            val content = Get<String>("https://hao.360.com/histoday/") {
                setHeader("User-Agent", userAgent())
            }.await()
            val document = Jsoup.parse(content)
            document.getElementsByClass("tih-list").first()?.let { element ->
                element.getElementsByClass("tih-item").forEach {
                    val bean = HistoryBean()
                    val title = it.getElementsByTag("dt").first()
                    title?.getElementsByTag("em")?.remove()
                    bean.time =
                        StringUtils.substringBefore(title?.text()?.replace(". ", ""), "-").ifEmpty {
                            StringUtils.substringBefore(title?.text()?.replace(". ", ""), "：")
                        }
                    bean.title =
                        StringUtils.substringAfter(title?.text()?.replace(". ", ""), "-").ifEmpty {
                            StringUtils.substringAfter(title?.text()?.replace(". ", ""), "：")
                        }
                    bean.content = it.getElementsByClass("desc").text()
                    bean.readLink = it.getElementsByClass("read-btn").attr("href")
                    runCatching {
                        val img = it.getElementsByTag("img").first()
                        if (img!!.hasAttr("src") && !img.hasAttr("data-src")) {
                            img.absUrl("src")
                        } else if (img.hasAttr("data-src")) {
                            img.absUrl("data-src")
                        } else {
                            ""
                        }
                    }.onSuccess { img -> // 图片地址
                        bean.image = img
                    }
                    // 内容
                    historyBeans.add(bean)
                    logError(bean)
                }
            }
            logError(historyBeans)
            runOnUiThread {
                binding.rvHoliday.adapter?.notifyDataSetChanged()
                historyBeans.sort()
                binding.refreshData.isRefreshing = false
                binding.state.showContent()
            }
        }.catch {
            binding.state.showError()
            logError(it.printStackTrace())
            println(it.printStackTrace())
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun loadDataAboutDate(date: String) {
        historyBeans.clear()
        binding.state.showLoading()
        scopeNet {
            val content = Get<String>("https://hao.360.com/histoday/$date.html") {
                setHeader("User-Agent", userAgent())
            }.await()
            val document = Jsoup.parse(content)
            document.getElementsByClass("tih-list").first()?.let { element ->
                element.getElementsByClass("tih-item").forEach {
                    val bean = HistoryBean()
                    val title = it.getElementsByTag("dt").first()
                    title?.getElementsByTag("em")?.remove()
                    bean.time =
                        StringUtils.substringBefore(title?.text()?.replace(". ", ""), "-").ifEmpty {
                            StringUtils.substringBefore(title?.text()?.replace(". ", ""), "：")
                        }
                    bean.title =
                        StringUtils.substringAfter(title?.text()?.replace(". ", ""), "-").ifEmpty {
                            StringUtils.substringAfter(title?.text()?.replace(". ", ""), "：")
                        }
                    bean.content = it.getElementsByClass("desc").text()
                    bean.readLink = it.getElementsByClass("read-btn").attr("href")
                    runCatching {
                        val img = it.getElementsByTag("img").first()
                        if (img!!.hasAttr("src") && !img.hasAttr("data-src")) {
                            img.absUrl("src")
                        } else if (img.hasAttr("data-src")) {
                            img.absUrl("data-src")
                        } else {
                            ""
                        }
                    }.onSuccess { img -> // 图片地址
                        bean.image = img
                    }
                    // 内容
                    historyBeans.add(bean)
                    logError(bean)
                }
            }
            logError(historyBeans)
            runOnUiThread {
                binding.rvHoliday.adapter?.notifyDataSetChanged()
                historyBeans.sort()
                binding.refreshData.isRefreshing = false
                binding.state.showContent()
            }
        }.catch {
            binding.state.showError()
            logError(it.printStackTrace())
            println(it.printStackTrace())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

}