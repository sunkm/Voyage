package com.manchuan.tools.ui.home

import android.animation.LayoutTransition
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.palette.graphics.Palette
import com.alibaba.fastjson.JSON
import com.blankj.utilcode.util.ColorUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.drake.channel.receiveEvent
import com.drake.net.Get
import com.drake.net.cache.CacheMode
import com.drake.net.utils.scopeNetLife
import com.dylanc.longan.doOnClick
import com.dylanc.longan.startActivity
import com.dylanc.longan.toast
import com.google.android.material.card.MaterialCardView
import com.kongzue.dialogx.dialogs.PopTip
import com.lxj.androidktx.core.*
import com.manchuan.tools.activity.GamesActivity
import com.manchuan.tools.activity.SpeakAudioActivity
import com.manchuan.tools.activity.WebActivity
import com.manchuan.tools.activity.movies.MoviesMainActivity
import com.manchuan.tools.activity.transfer.TransferFileActivity
import com.manchuan.tools.bean.AdsBean
import com.manchuan.tools.database.Global
import com.manchuan.tools.databinding.FragmentHomeBinding
import com.manchuan.tools.databinding.ScrolltextBinding
import com.manchuan.tools.extensions.getColorByAttr
import com.manchuan.tools.extensions.sheetDialog
import com.manchuan.tools.genshin.activity.GenshinLoadingActivity
import com.manchuan.tools.model.CovidTag
import com.manchuan.tools.utils.SettingsLoader
import com.tencent.mmkv.MMKV


class HomeFragment : Fragment() {

    private val binding by lazy {
        FragmentHomeBinding.inflate(layoutInflater)
    }

    private lateinit var kv: MMKV
    private lateinit var containers: LinearLayout
    lateinit var root: View
    private lateinit var daily: MaterialCardView
    private lateinit var adsList: MutableList<AdsBean>

    private fun joinQQGroup(key: String) {
        val intent = Intent()
        intent.data =
            Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D$key")
        try {
            startActivity(intent)
        } catch (ignored: Exception) {
        }
    }

    private fun addQQFriend(qq: String) {
        runCatching {
            val intent = Intent()
            intent.data =
                Uri.parse("mqqapi://card/show_pslcard?src_type=internal&source=sharecard&version=1&uin=$qq")
            startActivity(intent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        initView()
        binding.huaijiu.setOnClickListener {
            //throw RuntimeException("Test Crash")
            requireActivity().startActivity<GamesActivity>()
            //ImagePicker.startPicker(this, reqCode = 10001,isCrop = true)
        }
        binding.speakAudio.setOnClickListener {
            requireActivity().startActivity<SpeakAudioActivity>()
        }
        binding.movies.doOnClick {
            startActivity<MoviesMainActivity>()
        }
        binding.genshintool.setOnClickListener {
            requireActivity().startActivity<WebActivity>(flag = Intent.FLAG_ACTIVITY_CLEAR_TOP,
                bundle = arrayOf("url" to "https://support.qq.com/products/320218/"))
        }
        binding.genshin.setOnClickListener {
            requireActivity().startActivity<GenshinLoadingActivity>()
        }
        binding.trans.doOnClick {
            requireActivity().startActivity<TransferFileActivity>()
        }
        Global.textDayOne.observe(viewLifecycleOwner) {
            if (it) {
                doOnceInDay("", "", whenHasDone = {

                }, action = {
                    refreshContent()
                })
            } else {
                refreshContent()
                binding.refresh.doOnClick {
                    refreshContent()
                }
            }
        }
        Global.textDailyGone.observe(viewLifecycleOwner) {
            if (it) {
                binding.refresh.animateGone()
            } else {
                binding.refresh.animateVisible()
            }
        }
        daily.setOnClickListener {
            scopeNetLife {
                val string = Get<String>("http://bjb.yunwj.top/php/qq.php").await()
                val json = JSON.parseObject(string)
                requireActivity().sheetDialog(ScrolltextBinding.inflate(layoutInflater).root,
                    "60秒看世界") {
                    val binding = ScrolltextBinding.bind(it)
                    binding.content.text = json.getString("wb").replace("【换行】", "\n")
                }
            }.catch {
                PopTip.show("获取出错")
            }
        }
        receiveEvent<CovidTag>("covid") {
            val covidStringBuild = StringBuilder()
            covidStringBuild.append("\n时间:${it.time}")
            covidStringBuild.append("\n累计确诊:${it.total}")
            covidStringBuild.append("\n累计死亡:${it.death}")
            covidStringBuild.append("\n累计治愈:${it.cure}")
            covidStringBuild.append("\n现存疑似:${it.suspected}")
            covidStringBuild.append("\n无症感染:${it.asymptote}")
            covidStringBuild.append("\n境外输入:${it.overseas}")
            covidStringBuild.append("\n现有确诊:${it.econ}")
            covidStringBuild.append("\n现有重症:${it.server}")
        }
        return binding.root
    }


    private fun refreshContent() {
        binding.state.showLoading()
        scopeNetLife {
            val string =
                Get<String>("https://tuapi.eees.cc/api.php?category={fengjing}&type=json") {
                    setCacheMode(CacheMode.REQUEST_THEN_READ)
                }.await()
            val content = Get<String>("https://v1.hitokoto.cn/?c=i&encode=text") {
                setCacheMode(CacheMode.REQUEST_THEN_READ)
            }.await()
            val json = JSON.parseObject(string)
            binding.content.text = content
            binding.state.showContent()
            context?.let { it ->
                Glide.with(it).asBitmap().load(json.getString("img"))
                    .transition(BitmapTransitionOptions.withCrossFade()).skipMemoryCache(true)
                    .diskCacheStrategy(SettingsLoader.diskCacheMethod)
                    .listener(object : RequestListener<Bitmap> {
                        override fun onResourceReady(
                            resource: Bitmap?,
                            model: Any?,
                            target: Target<Bitmap>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean,
                        ): Boolean {
                            if (resource != null) {
                                Palette.from(resource).generate { palette ->
                                    palette?.let {
                                        val defaultColor =
                                            getColorByAttr(com.google.android.material.R.attr.colorAccent)
                                        if (!ColorUtils.isLightColor(
                                                it.getDominantColor(
                                                    defaultColor,
                                                ),
                                            )
                                        ) {
                                            binding.title.setTextColor(Color.WHITE)
                                            binding.content.setTextColor(Color.WHITE)
                                        } else {
                                            binding.title.setTextColor(Color.BLACK)
                                            binding.content.setTextColor(Color.BLACK)
                                        }
                                    }
                                }
                            }
                            return false
                        }

                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Bitmap>?,
                            isFirstResource: Boolean,
                        ): Boolean {
                            toast("图片加载失败")
                            return false
                        }

                    }).into(binding.dongman)
            }
        }.catch {
            toast(it.message)
        }
    }

    fun initView() {
        kv = MMKV.defaultMMKV()!!
        adsList = ArrayList()
        containers = binding.containers
        daily = binding.daily
        binding.containers.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
    }


}