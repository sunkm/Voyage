package com.manchuan.tools.fragment

import android.annotation.SuppressLint
import android.content.Intent
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
import com.manchuan.tools.activity.PlaySpeakActivity
import com.manchuan.tools.databinding.FragmentSpeakAudioBinding
import com.manchuan.tools.databinding.ItemsSpeakBinding
import com.manchuan.tools.extensions.loge
import com.manchuan.tools.model.GameModel
import com.manchuan.tools.model.SpeakAudioModel
import com.manchuan.tools.utils.SettingsLoader
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import org.jsoup.Jsoup

class XiangShengFragment : EngineFragment<FragmentSpeakAudioBinding>(R.layout.fragment_speak_audio) {

    private val audioList: ArrayList<SpeakAudioModel> = ArrayList()
    private var page = 1

    @SuppressLint("MissingPermission")
    override fun initView() {
        FastScrollerBuilder(binding.recyclerView).useMd2Style().build()
        binding.recyclerView.staggered(2, HoverStaggeredGridLayoutManager.VERTICAL).setup {
            addType<SpeakAudioModel>(R.layout.items_speak)
            setAnimation(AnimationType.SCALE)
            onBind {
                val binding = ItemsSpeakBinding.bind(itemView)
                binding.name.text = getModel<SpeakAudioModel>().title
                Glide.with(context)
                    .load(getModel<SpeakAudioModel>().image)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(SettingsLoader.diskCacheMethod)
                    .into(binding.image)
            }
            onClick(R.id.card) {
                context.startActivity<PlaySpeakActivity>(Intent.FLAG_ACTIVITY_NEW_TASK,
                    bundle = arrayOf(
                        "image" to getModel<SpeakAudioModel>(modelPosition).image,
                        "startUrl" to getModel<SpeakAudioModel>(modelPosition).startUrl,
                        "author" to getModel<SpeakAudioModel>(modelPosition).author,
                        "title" to getModel<SpeakAudioModel>(modelPosition).title
                    ))
            }
        }
        receiveEvent<ArrayList<GameModel>>("models_xs") {
            binding.recyclerView.models = it
        }
        receiveEvent<ArrayList<GameModel>>("load_more_models_xs") {
            binding.recyclerView.addModels(it)
        }
        binding.page.onRefresh {
            page = 1
            runCatching {
                Thread {
                    audioList.clear()
                    val document = Jsoup.connect("http://www.tingbook.cc/book/14-$page.html").get()
                    val row = document.getElementsByClass("row3 row-b").first()
                    val stringBuilder = StringBuilder()
                    for (items in row?.getElementsByClass("col-12 col-m-24 col3-")!!) {
                        val table = items?.getElementsByClass("style-img clearfix")?.first()
                        val startUrl = table?.getElementsByClass("img-80 fl mr15")?.attr("abs:href")
                        val image = table?.getElementsByTag("img")?.attr("abs:src")
                        val title = table?.getElementsByTag("img")?.attr("alt")
                        val author = table?.getElementsByClass("fr f-12 f-gray")?.text()
                        audioList.add(SpeakAudioModel(title!!, image!!,author!!, startUrl!!))
                    }
                    loge(stringBuilder.toString())
                    sendEvent(audioList, "models_xs")
                    page++
                    this.finish(true)
                }.start()
            }.onFailure {
                this.finish(false)
                if (!NetworkUtils.isAvailable()) {
                    toast("网络不可用")
                }
            }
        }.autoRefresh()
        binding.page.onLoadMore {
            runCatching {
                Thread {
                    val audioList: ArrayList<SpeakAudioModel> = ArrayList()
                    val document = Jsoup.connect("http://www.tingbook.cc/book/14-$page.html").get()
                    val row = document.getElementsByClass("row3 row-b").first()
                    val stringBuilder = StringBuilder()
                    for (items in row?.getElementsByClass("col-12 col-m-24 col3-")!!) {
                        val table = items?.getElementsByClass("style-img clearfix")?.first()
                        val startUrl = table?.getElementsByClass("img-80 fl mr15")?.attr("abs:href")
                        val image = table?.getElementsByTag("img")?.attr("abs:src")
                        val title = table?.getElementsByTag("img")?.attr("alt")
                        val author = table?.getElementsByClass("fr f-12 f-gray")?.text()
                        audioList.add(SpeakAudioModel(title!!, image!!,author!!, startUrl!!))
                    }
                    loge(stringBuilder.toString())
                    sendEvent(audioList, "load_more_models_xs")
                    page++
                    this.finish(true)
                }.start()
            }.onFailure {
                this.finish(false)
                if (!NetworkUtils.isAvailable()) {
                    toast("网络不可用")
                }
            }
        }
    }

    override fun initData() {

    }
}