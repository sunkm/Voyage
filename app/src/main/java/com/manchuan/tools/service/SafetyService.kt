package com.manchuan.tools.service

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory

object SafetyService {

    fun enableSafety(context: Context) {
        FirebaseApp.initializeApp(/*context=*/context)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            SafetyNetAppCheckProviderFactory.getInstance()
        )
    }

}