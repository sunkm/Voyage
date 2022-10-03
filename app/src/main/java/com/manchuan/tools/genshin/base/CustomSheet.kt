package com.manchuan.tools.genshin.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import androidx.annotation.StringRes
import com.blankj.utilcode.util.ThreadUtils
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.manchuan.tools.R
import com.manchuan.tools.databinding.BottomMonthLedgerBinding
import com.manchuan.tools.extensions.dp
import com.manchuan.tools.extensions.textColorPrimary
import com.manchuan.tools.genshin.bean.MonthLedgerBean
import com.manchuan.tools.genshin.ext.mainUser
import com.manchuan.tools.genshin.ext.select
import com.manchuan.tools.genshin.ext.show
import com.manchuan.tools.genshin.information.Constants
import com.manchuan.tools.genshin.information.MiHoYoAPI
import com.manchuan.tools.genshin.untils.GSON
import com.manchuan.tools.genshin.untils.RequestApi
import com.manchuan.tools.genshin.untils.ok
import com.maxkeppeler.sheets.core.Sheet

private typealias PositiveListener = () -> Unit

@Suppress("unused")
class CustomSheet : Sheet() {

    override val dialogTag = "CustomSheet"

    private lateinit var binding: BottomMonthLedgerBinding

    fun onPositive(positiveListener: PositiveListener) {
        this.positiveListener = positiveListener
    }

    fun onPositive(@StringRes positiveRes: Int, positiveListener: PositiveListener? = null) {
        this.positiveText = windowContext.getString(positiveRes)
        this.positiveListener = positiveListener
    }


    private val monthList = mutableListOf<Int>()
    private val pieEntry = mutableListOf<PieEntry>()

    private fun loadMonthLedger(
        month: Int = 0,
        binding: BottomMonthLedgerBinding,
    ) {
        //加载本月获得的原石
        mainUser?.let { userBean ->
            RequestApi.get(
                MiHoYoAPI.getMonthLedgerUrl(month, userBean.gameUid, userBean.region),
                userBean
            ) {
                ThreadUtils.runOnUiThread {
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
                            binding.monthSelect.adapter = ArrayAdapter(
                                binding.root.context,
                                R.layout.item_text,
                                monthShowList
                            ).apply {
                                setDropDownViewResource(R.layout.spinner_drop_down_style)
                            }
                            binding.monthSelect.setSelection(monthList.size - 1)
                        }

                        binding.dayGemstone.text =
                            monthLedger.day_data.current_primogems.toString()
                        binding.dayMora.text =
                            monthLedger.day_data.current_mora.toString()
                        binding.monthGemstone.text =
                            monthLedger.month_data.current_primogems.toString()
                        binding.monthMora.text =
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
                        with(binding.pie) {
                            val pieDataSet = PieDataSet(pieEntry, "")
                            pieDataSet.colors =
                                Constants.getMonthLegendColors(
                                    actionIds
                                )
                            pieDataSet.setDrawValues(false)
                            val pieData = PieData(pieDataSet)
                            data = pieData
                            invalidate()
                        }
                    } else {
                        "获取信息失败啦".show()
                    }
                }
            }
        }
    }


    fun onPositive(positiveText: String, positiveListener: PositiveListener? = null) {
        this.positiveText = positiveText
        this.positiveListener = positiveListener
    }

    /**
     * Implement this method and add your own layout, which will be appended to the default sheet with toolbar and buttons.
     */
    override fun onCreateLayoutView(): View {
        binding = BottomMonthLedgerBinding.inflate(LayoutInflater.from(activity))
        with(binding.pie) {
            isRotationEnabled = false
            isSelected = true
            description.isEnabled = false
            animateXY(800,600)
            setNoDataText("暂无数据,请稍后再来看看吧。")
            holeRadius = 80f
            extraRightOffset = 20.dp.toFloat()
            legend.textSize = 15f
            legend.textColor = textColorPrimary()
            legend.orientation =
                com.github.mikephil.charting.components.Legend.LegendOrientation.VERTICAL
            legend.horizontalAlignment =
                com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.RIGHT
            legend.verticalAlignment =
                com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP
            setTouchEnabled(true)
            setDrawEntryLabels(false)
            setEntryLabelColor(textColorPrimary())
        }
        binding.monthSelect.select { position, id ->
            loadMonthLedger(monthList[position], binding)
        }
        val monthSelectPosition =
            binding.monthSelect.selectedItemPosition
        loadMonthLedger(
            if (monthSelectPosition == -1) 0 else monthList[monthSelectPosition],
            binding
        )
        // Inflate layout through binding class and return the root view
        return binding.root

//        Or without binding
//        return LayoutInflater.from(activity).inflate(R.layout.sheets_custom, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        setButtonPositiveListener {  } If you want to override the default positive click listener
//        displayButtonsView() If you want to change the visibility of the buttons view
//        displayButtonPositive() Hiding the positive button will prevent clicks
//        hideToolbar() Hide the toolbar of the sheet, the title and the icon
    }

    /** Build [CustomSheet] and show it later. */
    fun build(ctx: Context, width: Int? = null, func: CustomSheet.() -> Unit): CustomSheet {
        this.windowContext = ctx
        this.width = width
        this.func()
        return this
    }

    /** Build and show [CustomSheet] directly. */
    fun show(ctx: Context, width: Int? = null, func: CustomSheet.() -> Unit): CustomSheet {
        this.windowContext = ctx
        this.width = width
        this.func()
        this.show()
        return this
    }
}