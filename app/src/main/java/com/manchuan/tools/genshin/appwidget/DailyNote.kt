package com.manchuan.tools.genshin.appwidget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import com.manchuan.tools.R
import com.manchuan.tools.genshin.appwidget.utils.CardUtils
import com.manchuan.tools.genshin.service.GetDailyNoteService
import com.manchuan.tools.genshin.service.GetMonthLedgerService

/**
 * Implementation of App Widget functionality.
 */
class DailyNote : AppWidgetProvider() {
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
        remoteViews = RemoteViews(context.packageName, R.layout.daily_note)
        remoteViews.apply {
            setOnClickPendingIntent(R.id.rootLayout, registerUpdateService(context))
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

    @SuppressLint("ObsoleteSdkInt")
    private fun startUpdateService(action: String) {
        val dailyNoteIntent = Intent(mContext, GetDailyNoteService::class.java).apply {
            putExtra("type", CardUtils.TYPE_DAILY_NOTE_OVERVIEW_TWO)
            putExtra("action", action)
        }
        val monthLedgerIntent = Intent(mContext, GetMonthLedgerService::class.java).apply {
            putExtra("type", CardUtils.TYPE_DAILY_NOTE_OVERVIEW_TWO)
            putExtra("action", action)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            runCatching {
                mContext.startForegroundService(dailyNoteIntent)
                mContext.startForegroundService(monthLedgerIntent)
            }
        } else {
            mContext.startService(dailyNoteIntent)
            mContext.startService(monthLedgerIntent)
        }
    }

    private fun registerUpdateService(context: Context): PendingIntent {
        val intent = Intent(CardUtils.CLICK_ACTION).apply {
            component = ComponentName(context, DailyNote::class.java)
        }
        return PendingIntent.getBroadcast(
            context,
            233,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun updateUI() {
        if (!this::remoteViews.isInitialized) {
            remoteViews =
                RemoteViews(mContext.packageName, R.layout.daily_note)
        }
        val dailyNoteBean = CardUtils.getCacheDailyNoteBean(CardUtils.mainUser.gameUid)
        val monthLedgerBean = CardUtils.getMonthLedgerBean(CardUtils.mainUser.gameUid)
        remoteViews.apply {
            if (dailyNoteBean != null) {
                setViewVisibility(R.id.errorLayout, View.GONE)
                setViewVisibility(R.id.contentLayout, View.VISIBLE)
                setTextViewText(
                    R.id.contentTv1,
                    "${dailyNoteBean.current_resin}/${dailyNoteBean.max_resin}"
                )
                setTextViewText(
                    R.id.contentTv2,
                    "${dailyNoteBean.current_expedition_num}/${dailyNoteBean.max_expedition_num}"
                )
                setTextViewText(
                    R.id.contentTv3,
                    if (dailyNoteBean.finished_task_num == dailyNoteBean.total_task_num) "全部完成" else "${dailyNoteBean.finished_task_num}/${dailyNoteBean.total_task_num}"
                )
                setTextViewText(
                    R.id.contentTv4,
                    "${dailyNoteBean.current_home_coin}/${dailyNoteBean.max_home_coin}"
                )
            } else {
                setViewVisibility(R.id.errorLayout, View.VISIBLE)
                setViewVisibility(R.id.contentLayout, View.GONE)
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
            ComponentName(mContext, DailyNote::class.java),
            remoteViews
        )
    }

}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
) {

    val views = RemoteViews(context.packageName, R.layout.daily_note)
    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}