package com.manchuan.tools.service

import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import com.manchuan.tools.database.Global

class GoogleService : FirebaseInstanceIdService() {

    @Deprecated(
        "Deprecated in Java", ReplaceWith(
            "super.onTokenRefresh()",
            "com.google.firebase.iid.FirebaseInstanceIdService"
        )
    )
    override fun onTokenRefresh() {
        Global.firebaseToken = FirebaseInstanceId.getInstance().token.toString()
    }

}