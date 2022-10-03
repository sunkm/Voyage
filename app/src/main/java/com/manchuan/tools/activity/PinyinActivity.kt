package com.manchuan.tools.activity

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.KeyboardUtils
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.setup
import com.drake.net.utils.scope
import com.dylanc.longan.*
import com.github.houbb.pinyin.constant.enums.PinyinStyleEnum
import com.github.houbb.pinyin.util.PinyinHelper
import com.google.android.flexbox.FlexboxLayoutManager
import com.lxj.androidktx.core.animateGone
import com.lxj.androidktx.core.animateVisible
import com.manchuan.tools.databinding.ActivityPinyinBinding
import com.manchuan.tools.databinding.ItemChipBinding
import com.manchuan.tools.extensions.errorToast
import io.github.inflationx.viewpump.ViewPumpContextWrapper

class PinyinActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityPinyinBinding.inflate(layoutInflater)
    }

    private val duoyinList = ArrayList<DuoyinModel>()
    private val tongyinList = ArrayList<DuoyinModel>()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
        binding.pinyin.roundCorners = 4.dp
        binding.fontsLay.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        initRecycler()
        KeyboardUtils.fixSoftInputLeaks(this)
        KeyboardUtils.fixAndroidBug5497(this)
        binding.jiexi.doOnClick {
            if (binding.url.isTextNotEmpty()) {
                binding.functionsLay.animateGone()
                duoyinList.clear()
                tongyinList.clear()
                binding.font.text = binding.url.textString
                binding.pinyin.text = PinyinHelper.toPinyin(binding.url.textString)
                scope {
                    binding.url.textString.forEach {
                        PinyinHelper.toPinyinList(it).forEach { string ->
                            duoyinList.add(DuoyinModel(string))
                        }
                        runCatching {
                            PinyinHelper.samePinyinList(
                                PinyinHelper.toPinyin(
                                    it.toString(),
                                    PinyinStyleEnum.NUM_LAST
                                )
                            ).forEach { string ->
                                tongyinList.add(DuoyinModel(string.toString()))
                            }
                        }.onFailure {
                            "错误,无法查询该字的同音字,你可能是输入了特殊字符".errorToast()
                        }
                    }
                }
                binding.duoyin.adapter?.notifyDataSetChanged()
                binding.tongyin.adapter?.notifyDataSetChanged()
                binding.functionsLay.animateVisible()
            }
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.let { ViewPumpContextWrapper.wrap(it) })
    }

    private fun initRecycler() {
        binding.duoyin.layoutManager = FlexboxLayoutManager(this)
        binding.tongyin.layoutManager = FlexboxLayoutManager(this)
        binding.duoyin.setHasFixedSize(true)
        binding.tongyin.setHasFixedSize(true)
        binding.duoyin.setup {
            addType<DuoyinModel>(com.manchuan.tools.R.layout.item_chip)
            setAnimation(AnimationType.ALPHA)
            onBind {
                val item = ItemChipBinding.bind(itemView)
                item.chip.text = getModel<DuoyinModel>(modelPosition).pinyin
            }
        }.models = duoyinList
        binding.tongyin.setup {
            addType<DuoyinModel>(com.manchuan.tools.R.layout.item_chip)
            setAnimation(AnimationType.ALPHA)
            onBind {
                val item = ItemChipBinding.bind(itemView)
                item.chip.text = getModel<DuoyinModel>(modelPosition).pinyin
            }
        }.models = tongyinList
    }

    data class DuoyinModel(var pinyin: String)

}