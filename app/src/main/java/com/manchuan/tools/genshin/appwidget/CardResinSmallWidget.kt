package com.manchuan.tools.genshin.appwidget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.manchuan.tools.R
import com.manchuan.tools.genshin.appwidget.utils.CardUtils
import com.manchuan.tools.genshin.appwidget.utils.CardUtils.cShow
import com.manchuan.tools.genshin.service.GetDailyNoteService

class CardResinSmallWidget : AppWidgetProvider() {

    private lateinit var remoteViews: RemoteViews

    private lateinit var mContext:Context

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        mContext = context
        remoteViews = RemoteViews(context.packageName,R.layout.card_resin_small_widget)

        //设置手动更新
        remoteViews.setOnClickPendingIntent(R.id.root,registerUpdateService(context))

        appWidgetIds.forEach {
            appWidgetManager.updateAppWidget(it, remoteViews)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        mContext = context!!
        CardUtils.context = context
        when(intent?.action){
            //系统调用更新
            CardUtils.APPWIDGET_UPDATE, CardUtils.CLICK_ACTION->{
                startGetDailyNoteService(intent.action!!)
            }
            CardUtils.UPDATE_ACTION-> {
                updateUI()
            }
            CardUtils.CLICK_UPDATE_ACTION->{
                "树脂小组件更新".cShow()
                updateUI()
            }
        }
    }

    private fun updateUI(){
        val dailyNoteBean = CardUtils.getCacheDailyNoteBean(CardUtils.mainUser.gameUid)
        if(!this::remoteViews.isInitialized){
            remoteViews = RemoteViews(mContext.packageName,R.layout.card_resin_small_widget)
        }
        remoteViews.apply {
           if(dailyNoteBean!=null){
               setTextViewText(R.id.current_resin,dailyNoteBean.current_resin.toString())
               setTextViewText(R.id.max_resin,dailyNoteBean.max_resin.toString())
               setTextViewText(R.id.time, CardUtils.formatTime(dailyNoteBean.resin_recovery_time.toLong()))
           }
        }
        val componentName = ComponentName(mContext, CardResinSmallWidget::class.java)
        AppWidgetManager.getInstance(mContext).updateAppWidget(componentName,remoteViews)
    }

    private fun startGetDailyNoteService(action:String){
        val intent = Intent(mContext, GetDailyNoteService::class.java).apply {
            putExtra("type", CardUtils.TYPE_RESIN_TYPE1)
            putExtra("action",action)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mContext.startForegroundService(intent)
        } else {
            mContext.startService(intent)
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun registerUpdateService(context: Context):PendingIntent{
        val intent = Intent(CardUtils.CLICK_ACTION).apply {
            component = ComponentName(context, CardResinSmallWidget::class.java)
        }
        return PendingIntent.getBroadcast(context,233,intent,PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onEnabled(context: Context) {
    }

    override fun onDisabled(context: Context) {
    }
}
