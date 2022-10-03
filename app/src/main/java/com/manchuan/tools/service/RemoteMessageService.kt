package com.manchuan.tools.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.manchuan.tools.database.Global
import io.karn.notify.Notify

class RemoteMessageService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        if (remoteMessage.notification != null && remoteMessage.notification!!.body != null) {
            Notify.with(applicationContext)
                .content {
                    title = remoteMessage.notification?.title
                    text = remoteMessage.notification?.body
                }
                .show()
        } else {
            Notify.with(applicationContext)
                .content {
                   title = remoteMessage.data["title"]
                   text = remoteMessage.data["body"]
                }
                .show()
        }
    }

    override fun onNewToken(token: String) {
        Global.messageToken = token
    }

}