package com.manchuan.tools.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.drake.net.Get
import com.drake.net.utils.scopeNetLife
import com.dylanc.longan.doOnClick
import com.dylanc.longan.isTextNotEmpty
import com.dylanc.longan.textString
import com.dylanc.longan.toast
import com.github.vipulasri.timelineview.TimelineView
import com.manchuan.tools.R
import com.manchuan.tools.adapter.TimeLineAdapter
import com.manchuan.tools.adapter.TimelineAttributes
import com.manchuan.tools.databinding.ActivityKuaidiBinding
import com.manchuan.tools.extensions.accentColor
import com.manchuan.tools.extensions.dpToPx
import com.manchuan.tools.extensions.errorToast
import com.manchuan.tools.json.SerializationConverter
import com.manchuan.tools.model.ExpressModel

class KuaidiActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityKuaidiBinding.inflate(layoutInflater)
    }
    private val mDataList = ArrayList<ExpressModel.Data>()

    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var mAttributes: TimelineAttributes
    private val express = ArrayList<KuaidiModel>()
    private var selectedExpress: String = ""

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        mAttributes = TimelineAttributes(
            markerSize = dpToPx(20f),
            markerColor = accentColor(),
            markerInCenter = true,
            markerLeftPadding = dpToPx(0f),
            markerTopPadding = dpToPx(0f),
            markerRightPadding = dpToPx(0f),
            markerBottomPadding = dpToPx(0f),
            linePadding = dpToPx(2f),
            startLineColor = accentColor(),
            endLineColor = accentColor(),
            lineStyle = TimelineView.LineStyle.DASHED,
            lineWidth = dpToPx(2f),
            lineDashWidth = dpToPx(4f),
            lineDashGap = dpToPx(2f)
        )
        initAdapter()
        initExpress()
        val expressList = ArrayList<String>()
        express.forEach {
            expressList.add(it.name)
        }
        selectedExpress = express[0].suoxie
        val adapter =
            ArrayAdapter(this, R.layout.cat_exposed_dropdown_popup_item, expressList)
        binding.autocomplete1.setAdapter(adapter)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
        binding.jiexi.doOnClick {
            if (binding.url.isTextNotEmpty()) {
                binding.functionsLay.showLoading()
                scopeNetLife {
                    val express =
                        Get<ExpressModel>("http://www.kuaidi100.com/query?type=$selectedExpress&postid=${binding.url.textString}") {
                            converter = SerializationConverter()
                        }.await().data
                    mDataList.clear()
                    express.forEach {
                        mDataList.add(ExpressModel.Data(it.context, it.ftime, it.time))
                    }
                    binding.recyclerView.adapter?.notifyDataSetChanged()
                    binding.functionsLay.showContent()
                }.catch {
                    it.message?.errorToast()
                }
            }
        }
        binding.autocomplete1.setOnItemClickListener { parent, view, position, id ->
            toast(position.toString())
            selectedExpress = express[position].suoxie
        }
    }


    private fun initExpress() {
        express.add(KuaidiModel("youzhengguonei", "EMS邮政"))
        express.add(KuaidiModel("shengtong", "申通快递"))
        express.add(KuaidiModel("yuantong", "圆通快递"))
        express.add(KuaidiModel("shunfeng", "顺风快递"))
        express.add(KuaidiModel("yunda", "韵达快递"))
        express.add(KuaidiModel("zhongtong", "中通快递"))
        express.add(KuaidiModel("tiantian", "天天快递"))
        express.add(KuaidiModel("debangwuliu", "德邦物流"))
        express.add(KuaidiModel("huitongkuaidi", "汇通快递"))
    }

    private fun initAdapter() {
        mLayoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.recyclerView.apply {
            layoutManager = mLayoutManager
            adapter = TimeLineAdapter(mDataList, mAttributes)
        }
    }

    data class KuaidiModel(var suoxie: String, var name: String)

}