package com.manchuan.tools.genshin.appwidget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.RemoteViews
import com.manchuan.tools.R
import com.manchuan.tools.genshin.appwidget.utils.CardUtils
import com.manchuan.tools.genshin.service.ExpeditionListViewAdapterService
import com.manchuan.tools.genshin.service.GetDailyNoteService
import com.manchuan.tools.genshin.service.GetMonthLedgerService

class CardDailyNoteOverviewWidget : AppWidgetProvider() {
    lateinit var remoteViews: RemoteViews
    private lateinit var mContext: Context

    companion object {
        private var flag = true
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        mContext = context
        CardUtils.context = mContext
        remoteViews = RemoteViews(context.packageName, R.layout.card_daily_note_overview_widget)
        remoteViews.apply {
            setOnClickPendingIntent(R.id.root, registerUpdateService(context))
        }
        appWidgetIds.forEach {
            appWidgetManager.updateAppWidget(it, remoteViews)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        mContext = context!!
        CardUtils.context = context
        when (intent?.action) {
            CardUtils.APPWIDGET_UPDATE, CardUtils.CLICK_ACTION -> {
                startUpdateService(intent.action!!)
            }
            CardUtils.UPDATE_ACTION -> {
                updateUI()
            }
            CardUtils.CLICK_UPDATE_ACTION -> {
                flag = !flag
                if (flag && GetMonthLedgerService.requestOk) {
                    GetMonthLedgerService.requestOk = true
                    updateUI()
                }
            }
        }
    }

    private fun updateUI() {
        if (!this::remoteViews.isInitialized) {
            remoteViews =
                RemoteViews(mContext.packageName, R.layout.card_daily_note_overview_widget)
        }
        val dailyNoteBean = CardUtils.getCacheDailyNoteBean(CardUtils.mainUser.gameUid)
        val monthLedgerBean = CardUtils.getMonthLedgerBean(CardUtils.mainUser.gameUid)
        val adapter = Intent(mContext, ExpeditionListViewAdapterService::class.java).apply {
            data = Uri.parse("content" + (0..100000).random())
        }
        remoteViews.apply {
            setRemoteAdapter(R.id.grid_view, adapter)
            if (dailyNoteBean != null) {
                setTextViewText(
                    R.id.current_resin,
                    "${dailyNoteBean.current_resin}/${dailyNoteBean.max_resin}${
                        if (CardUtils.getRecoverTime(dailyNoteBean.resin_recovery_time.toLong()) == "00:00") " (恢复完成)" else " 还剩" + CardUtils.getRecoverTime(
                            dailyNoteBean.resin_recovery_time.toLong()
                        )
                    }"
                )
                setTextViewText(
                    R.id.daily_task_count,
                    "${dailyNoteBean.finished_task_num}/${dailyNoteBean.total_task_num}"
                )
                setTextViewText(
                    R.id.current_home_coin,
                    "${dailyNoteBean.current_home_coin}/${dailyNoteBean.max_home_coin}${
                        if (CardUtils.getRecoverTime(dailyNoteBean.home_coin_recovery_time.toLong()) == "00:00") " (恢复完成)" else " 还剩" + CardUtils.getRecoverTime(
                            dailyNoteBean.home_coin_recovery_time.toLong()
                        )
                    }"
                )
                setTextViewText(
                    R.id.weekly_challenge_count,
                    "${dailyNoteBean.resin_discount_num_limit - dailyNoteBean.remain_resin_discount_num}/${dailyNoteBean.resin_discount_num_limit}"
                )
                setTextViewText(
                    R.id.quality_convert_recover_time,
                    CardUtils.getQualityConvertTime(dailyNoteBean)
                )
            }
            if (monthLedgerBean != null) {
                setTextViewText(
                    R.id.month_gemstone,
                    "${monthLedgerBean.month_data.current_primogems}(${monthLedgerBean.month_data.primogems_rate}%)"
                )
                setTextViewText(
                    R.id.month_mora,
                    "${monthLedgerBean.month_data.current_mora}(${monthLedgerBean.month_data.mora_rate}%)"
                )
            }
        }
        AppWidgetManager.getInstance(mContext).updateAppWidget(
            ComponentName(mContext, CardDailyNoteOverviewWidget::class.java),
            remoteViews
        )
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun startUpdateService(action: String) {
        val dailyNoteIntent = Intent(mContext, GetDailyNoteService::class.java).apply {
            putExtra("type", CardUtils.TYPE_DAILY_NOTE_OVERVIEW)
            putExtra("action", action)
        }
        val monthLedgerIntent = Intent(mContext, GetMonthLedgerService::class.java).apply {
            putExtra("type", CardUtils.TYPE_DAILY_NOTE_OVERVIEW)
            putExtra("action", action)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mContext.startForegroundService(dailyNoteIntent)
            mContext.startForegroundService(monthLedgerIntent)
        } else {
            mContext.startService(dailyNoteIntent)
            mContext.startService(monthLedgerIntent)
        }
    }

    private fun registerUpdateService(context: Context): PendingIntent {
        val intent = Intent(CardUtils.CLICK_ACTION).apply {
            component = ComponentName(context, CardDailyNoteOverviewWidget::class.java)
        }
        return PendingIntent.getBroadcast(
            context,
            233,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun onEnabled(context: Context) {
    }

    override fun onDisabled(context: Context) {
    }
}
