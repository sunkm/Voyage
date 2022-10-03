package com.manchuan.tools.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import com.blankj.utilcode.util.NetworkUtils
import com.bumptech.glide.Glide
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.layoutmanager.HoverStaggeredGridLayoutManager
import com.drake.brv.utils.addModels
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.brv.utils.staggered
import com.drake.channel.receiveEvent
import com.drake.channel.sendEvent
import com.drake.engine.base.EngineFragment
import com.dylanc.longan.toast
import com.lxj.androidktx.core.startActivity
import com.manchuan.tools.R
import com.manchuan.tools.activity.FullscreenGameActivity
import com.manchuan.tools.databinding.FragmentGameJieJiBinding
import com.manchuan.tools.databinding.ItemsGamesBinding
import com.manchuan.tools.extensions.loge
import com.manchuan.tools.model.GameModel
import com.manchuan.tools.utils.SettingsLoader
import com.skydoves.transformationlayout.onTransformationStartContainer
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import org.jsoup.Jsoup

class GameJieJiFragment : EngineFragment<FragmentGameJieJiBinding>(R.layout.fragment_game_jie_ji) {

    private val gameList: ArrayList<GameModel> = ArrayList()
    private var page = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        onTransformationStartContainer()
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("MissingPermission")
    override fun initView() {
        FastScrollerBuilder(binding.recyclerView).useMd2Style().build()
        binding.recyclerView.staggered(2, HoverStaggeredGridLayoutManager.VERTICAL).setup {
            addType<GameModel>(R.layout.items_games)
            setAnimation(AnimationType.SCALE)
            onBind {
                val binding = ItemsGamesBinding.bind(itemView)
                binding.name.text = getModel<GameModel>().title
                Glide.with(context)
                    .load(getModel<GameModel>().imageUrl)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(SettingsLoader.diskCacheMethod)
                    .into(binding.image)
            }
            onClick(R.id.card) {
                ItemsGamesBinding.bind(itemView)
                context.startActivity<FullscreenGameActivity>(
                    bundle = arrayOf(
                        "url" to getModel<GameModel>().playUrl,
                    )
                )
            }
        }
        receiveEvent<ArrayList<GameModel>>("models") {
            binding.recyclerView.models = it
        }
        receiveEvent<ArrayList<GameModel>>("load_more_models") {
            binding.recyclerView.addModels(it)
        }
        binding.page.onRefresh {
            page = 1
            Thread {
                runCatching {
                    gameList.clear()
                    val document =
                        Jsoup.connect("https://www.yikm.net/nes?page=$page&tag=9&e=").get()
                    val row = document.getElementsByClass("row")[2]
                    val stringBuilder =
                        StringBuilder()
                    row.getElementsByClass("col-md-3 col-xs-6").forEach { item ->
                        val items = item.getElementsByClass("card card-blog").first()
                        val table = items?.getElementsByClass("table")?.first()
                        val image = items?.getElementsByClass("img img-raised")?.attr("abs:src")
                        val playUrlElement = items?.getElementsByTag("h4")?.first()
                        val playUrl =
                            playUrlElement?.getElementsByTag("a")?.select("a")?.attr("abs:href")
                        val title = playUrlElement?.getElementsByTag("a")?.select("a")?.text()
                        table?.getElementsByTag("span")!!.forEach { categoryItem ->
                            val category = ArrayList<String>()
                            category.add(categoryItem.text())
                            sendEvent(category, "category")
                        }
                        gameList.add(GameModel(playUrl!!, title!!, image!!, arrayListOf("街机")))
                        stringBuilder.append("\n${playUrlElement.attr("abs:href")}")
                        loge(stringBuilder.toString())
                    }
                    sendEvent(gameList, "models")
                    page++
                    this@onRefresh.finish(true)
                }.onFailure {
                    this@onRefresh.finish(false)
                    if (!NetworkUtils.isAvailable()) {
                        toast("网络不可用")
                    }
                }
            }.start()
        }.autoRefresh()
        binding.page.onLoadMore {
            Thread {
                runCatching {
                    val gameList: ArrayList<GameModel> = ArrayList()
                    val document =
                        Jsoup.connect("https://www.yikm.net/nes?page=$page&tag=9&e=").get()
                    val row = document.getElementsByClass("row")[2]
                    val stringBuilder = StringBuilder()
                    for (item in row.getElementsByClass("col-md-3 col-xs-6")) {
                        val items = item.getElementsByClass("card card-blog").first()
                        val table = items?.getElementsByClass("table")?.first()
                        val image = items?.getElementsByClass("img img-raised")?.attr("abs:src")
                        val playUrlElement = items?.getElementsByTag("h4")?.first()
                        val playUrl =
                            playUrlElement?.getElementsByTag("a")?.select("a")?.attr("abs:href")
                        val title = playUrlElement?.getElementsByTag("a")?.select("a")?.text()
                        val category = ArrayList<String>()
                        for (categoryItem in table?.getElementsByTag("span")!!) {
                            category.add(categoryItem.text())
                        }
                        gameList.add(GameModel(playUrl!!, title!!, image!!, category))
                        stringBuilder.append("\n${playUrlElement.attr("abs:href")}")
                        loge(stringBuilder.toString())
                    }
                    sendEvent(gameList, "load_more_models")
                    page++
                    this.finish(true)
                }.onFailure {
                    this.finish(false)
                    if (!NetworkUtils.isAvailable()) {
                        toast("网络不可用")
                    }
                }
            }.start()
        }
    }

    override fun initData() {

    }
}