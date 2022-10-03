package com.manchuan.tools.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.utils.getTargetClass
import com.manchuan.tools.activity.PlaySpeakActivity

class SongNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        //当前播放的音频信息
        val songInfo = intent?.getParcelableExtra<SongInfo?>("songInfo")
        //bundleInfo是你在配置通知栏的那个bundle，里面可以拿到你自定义的参数
        val bundleInfo = intent?.getBundleExtra("bundleInfo")
        val targetClass = bundleInfo?.getString("targetClass")?.getTargetClass()
        if (StarrySky.getActivityStack().isEmpty()) {
            val mainIntent = Intent(context, PlaySpeakActivity::class.java)
            mainIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            val targetIntent = Intent(context, targetClass)
            targetIntent.putExtra("songId", songInfo?.songId)
            val intents = arrayOf(mainIntent, targetIntent)
            context?.startActivities(intents)
        } else {
            val targetIntent = Intent(context, targetClass)
            targetIntent.putExtra("songId", songInfo?.songId)
            targetIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context?.startActivity(targetIntent)
        }
    }
    
}