package com.manchuan.tools.genshin.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.manchuan.tools.R
import com.manchuan.tools.genshin.appwidget.CardDailyNoteOverviewWidget
import com.manchuan.tools.genshin.appwidget.CardRequest
import com.manchuan.tools.genshin.appwidget.DailyNote
import com.manchuan.tools.genshin.appwidget.utils.CardUtils
import com.manchuan.tools.genshin.appwidget.utils.CardUtils.cShowLong
import kotlinx.coroutines.Job
import org.json.JSONObject

class GetMonthLedgerService : Service() {
    override fun onBind(p0: Intent?): IBinder? = null

    lateinit var action: String
    lateinit var cardType: String
    lateinit var timeOutJob: Job

    private lateinit var context: Context

    companion object {
        private const val SP_CACHE_TIME = "month_ledger_get_time"
        private const val MIN_CACHE_TIME = 10000
        private val requestQueue = mutableListOf<Intent>()
        private var isWorking = false
        var requestOk = false
    }

    override fun onCreate() {
        super.onCreate()
        println("GetMonthLedgerService Start")

        context = baseContext
        sendNotification()
        CardUtils.context = context
        CardRequest.context = context
        timeOutJob = CardUtils.setServiceTimeOut(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        isWorking = false
        println("GetMonthLedgerService End")
    }

    private fun getMonthLedger() {
        isWorking = true
        CardUtils.checkStatus({
            val cacheTime = CardUtils.sp.getLong(SP_CACHE_TIME, 0L)
            if (System.currentTimeMillis() - cacheTime >= MIN_CACHE_TIME) {
                CardRequest.getMonthLedger {
                    setMonthLedger(it)
                }
            } else {
                notifyUpdate()
            }
        }, {
            it.cShowLong()
            stopSelf()
        })
    }

    private fun setMonthLedger(it: JSONObject) {
        if (it.optString("retcode") == "0") {
            CardUtils.setMonthLedgerCache(CardUtils.mainUser.gameUid, it.optString("data"))
            CardUtils.setValue(SP_CACHE_TIME, System.currentTimeMillis())
            notifyUpdate()
        } else {
            Handler(Looper.getMainLooper()).post {
                "获取旅行者札记失败:${it.optString("message")}".cShowLong()
                stopSelf()
            }
        }
    }

    private fun notifyUpdate() {
        requestOk = true
        sendBroadcast(Intent(
            when (action) {
                CardUtils.CLICK_ACTION -> CardUtils.CLICK_UPDATE_ACTION
                else -> CardUtils.UPDATE_ACTION
            }
        ).apply {
            component = ComponentName(
                context,
                when (cardType) {
                    CardUtils.TYPE_DAILY_NOTE_OVERVIEW -> CardDailyNoteOverviewWidget::class.java
                    CardUtils.TYPE_DAILY_NOTE_OVERVIEW_TWO -> DailyNote::class.java
                    else -> CardDailyNoteOverviewWidget::class.java
                }
            )
        })
        checkRequestQueue()
    }

    //检查队列内是否还有请求
    private fun checkRequestQueue() {
        requestQueue.removeIf {
            val iType = it.getStringExtra("type")
            val iAction = it.getStringExtra("action")
            iType == cardType && iAction == action
        }
        if (requestQueue.isNotEmpty()) {
            cardType = requestQueue.first().getStringExtra("type") ?: CardUtils.TYPE_RESIN_TYPE1
            action = requestQueue.first().getStringExtra("action") ?: CardUtils.UPDATE_ACTION
            getMonthLedger()
        } else {
            timeOutJob.cancel()
            stopSelf()
        }
    }

    private fun sendNotification() {
        val notice = Notification.Builder(context).apply {
            setContentTitle("正在获取旅行者札记")
            setContentText("获取完成后自动关闭")
                .setSmallIcon(R.drawable.icon_klee)
        }
        val channelId = "getMonthLedgerChannelId"
        val channelName = "getMonthLedgerChannelName"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
                createNotificationChannel(
                    NotificationChannel(
                        channelId,
                        channelName,
                        NotificationManager.IMPORTANCE_HIGH
                    )
                )
            }
            notice.setChannelId(channelId)
        }
        startForeground(234, notice.build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        action = intent?.getStringExtra("action") ?: CardUtils.UPDATE_ACTION
        cardType = intent?.getStringExtra("type") ?: CardUtils.TYPE_RESIN_TYPE1
        requestQueue += intent!!

        if (!isWorking) {
            getMonthLedger()
        }

        return START_STICKY
    }
}