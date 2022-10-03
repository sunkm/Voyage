package com.manchuan.tools.genshin.appwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.manchuan.tools.R
import com.manchuan.tools.extensions.successToast
import com.manchuan.tools.genshin.appwidget.utils.CardUtils
import com.manchuan.tools.genshin.service.GetDailyNoteService

class CardResinLargeWidget : AppWidgetProvider() {

    private lateinit var remoteViews: RemoteViews
    lateinit var mContext: Context

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        remoteViews = RemoteViews(context.packageName, R.layout.card_resin_type2_widget)
        mContext = context
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
            //系统调用更新
            CardUtils.APPWIDGET_UPDATE, CardUtils.CLICK_ACTION -> {
                startGetDailyNoteService(intent.action!!)
            }
            CardUtils.UPDATE_ACTION -> {
                updateUI()
            }
            CardUtils.CLICK_UPDATE_ACTION -> {
                "树脂小组件更新".successToast()
                updateUI()
            }
        }
    }

    private fun updateUI() {
        if (!this::remoteViews.isInitialized) {
            remoteViews = RemoteViews(mContext.packageName, R.layout.card_resin_type2_widget)
        }
        remoteViews.apply {
            val dailyNoteBean = CardUtils.getCacheDailyNoteBean(CardUtils.mainUser.gameUid)
            if (dailyNoteBean != null) {
                setTextViewText(
                    R.id.current_resin,
                    "${dailyNoteBean.current_resin}/${dailyNoteBean.max_resin}"
                )
            }
        }
        AppWidgetManager.getInstance(mContext)
            .updateAppWidget(ComponentName(mContext, CardResinLargeWidget::class.java), remoteViews)
    }

    private fun startGetDailyNoteService(action: String) {
        val intent = Intent(mContext, GetDailyNoteService::class.java).apply {
            putExtra("type", CardUtils.TYPE_RESIN_TYPE2)
            putExtra("action", action)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mContext.startForegroundService(intent)
        } else {
            mContext.startService(intent)
        }
    }

    private fun registerUpdateService(context: Context): PendingIntent {
        val intent = Intent(CardUtils.CLICK_ACTION).apply {
            component = ComponentName(context, CardResinLargeWidget::class.java)
        }
        return PendingIntent.getBroadcast(
            context,
            233,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}