package com.manchuan.tools.crypto.keystore

import androidx.core.util.Pair
import java.security.PrivateKey
import java.security.PublicKey
import javax.security.auth.DestroyFailedException
import javax.security.cert.Certificate
import kotlin.Throws

class KeyPair(first: PrivateKey?, second: Certificate?) :
    Pair<PrivateKey?, Certificate?>(first, second) {
    val privateKey: PrivateKey
        get() = first!!
    val publicKey: PublicKey
        get() = second!!.publicKey
    val certificate: Certificate
        get() = second!!

    @Throws(DestroyFailedException::class)
    fun destroy() {
        first!!.destroy()
    }
}