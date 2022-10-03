package com.manchuan.tools.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.manchuan.tools.auth.Authenticator

/**
 * A bound Service that instantiates the authenticator
 * when started.
 */
class AuthenticatorService : Service() {

    // Instance field that stores the authenticator object
    private lateinit var mAuthenticator: Authenticator

    override fun onCreate() {
        // Create a new authenticator object
        mAuthenticator = Authenticator(this)
    }

    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    override fun onBind(intent: Intent?): IBinder = mAuthenticator.iBinder
}
