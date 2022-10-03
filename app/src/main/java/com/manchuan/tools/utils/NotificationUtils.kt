package com.manchuan.tools.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.manchuan.tools.R
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat

object NotificationUtils {
    /**
     * 默认通知
     * @param text 文本通知内容
     * @param title 文本通知标题
     */
    fun standardNotificationBuilder(
        context: Context,
        notificationId: Int,
        channelId: String,
        title: String?,
        text: String?
    ) {
        createDefaultNotificationChannel(context, channelId)
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationId, builder.build())
    }

    /**
     * 带有进度条的通知
     * @param notificationId 通知唯一ID，更新或者移除通知
     * @param proportion 进度比
     * @param isComplete 完成进度
     */
    @JvmField
    var ACTION_CANCEL = "ACTION_CANCEL"
    var EXTRA_NOTIFICATION_ID = "EXTRA_NOTIFICATION_ID"
    fun progressNotificationBuilder(
        context: Context,
        notificationId: Int,
        proportion: Double,
        isComplete: Boolean
    ) {
        //创建默认通道
        createDefaultNotificationChannel(context, "Download")
        //创建通知
        val notificationManager = NotificationManagerCompat.from(context)
        val builder = NotificationCompat.Builder(context, "download_file")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("File Download")
            .setContentText(notificationId.toString() + "Download in progress")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        //更新进度
        if (isComplete) {
            builder.setContentText(notificationId.toString() + "Download complete")
                .setProgress(0, 0, false)
        } else {
            builder.setProgress(100, (100 * proportion).toInt(), false)
        }
        notificationManager.notify(notificationId, builder.build())
    }

    //取消通知
    fun cancelNotificationId(context: Context?, notificationId: Int) {
        val notificationManager = NotificationManagerCompat.from(context!!)
        notificationManager.cancel(notificationId)
    }

    /**
     * 一个计算进度百分比的方法，方便进度条通知使用
     * @param progress 当前进度
     * @param total 总进度
     * @param scale 精确到几位小数
     */
    fun accuracy(progress: Double, total: Double, scale: Int): Double {
        val df = NumberFormat.getInstance() as DecimalFormat
        //可以设置精确几位小数
        df.maximumFractionDigits = scale
        //模式 例如四舍五入
        df.roundingMode = RoundingMode.HALF_UP
        val accuracy_num = progress / total
        return df.format(accuracy_num).toDouble()
    }

    /**
     * 默认通知通道
     */
    private fun createDefaultNotificationChannel(context: Context, channelId: String) {
        if (!isCreateNotificationChannel) {
            createNotificationChannel(
                context,
                channelId,
                "DefaultChannel",
                "Default notification channel, receive all notifications"
            )
        }
    }

    /**
     * 创建通道
     * 由于您必须先创建通知渠道，然后才能在 Android 8.0 及更高版本上发布任何通知，
     * 因此应在应用启动时立即执行这段代码。反复调用这段代码是安全的，因为创建现有通知渠道不会执行任何操作。
     *
     * @param channelId 通道ID
     * @param channelName 通道名字
     * @param description 通道介绍
     */
    private var isCreateNotificationChannel = false
    private fun createNotificationChannel(
        context: Context,
        channelId: String?,
        channelName: CharSequence?,
        description: String?
    ) {
        isCreateNotificationChannel = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance)
            channel.description = description
            val notificationManager = context.getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
    }
}