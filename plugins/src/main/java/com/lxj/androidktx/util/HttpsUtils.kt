package com.lxj.androidktx.util

import android.annotation.SuppressLint
import java.io.IOException
import java.io.InputStream
import java.security.KeyManagementException
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.*

/**
 * Description:
 * Create by dance, at 2019/1/5
 */
object HttpsUtils {
    val sslSocketFactory: SSLParams
        get() = getSslSocketFactoryBase(null, null, null)

    fun getSslSocketFactory(trustManager: X509TrustManager?): SSLParams {
        return getSslSocketFactoryBase(trustManager, null, null)
    }

    fun getSslSocketFactory(vararg certificates: InputStream?): SSLParams {
        return getSslSocketFactoryBase(null, null, null, *certificates as Array<out InputStream>)
    }

    fun getSslSocketFactory(
        bksFile: InputStream?,
        password: String?,
        vararg certificates: InputStream?,
    ): SSLParams {
        return getSslSocketFactoryBase(null,
            bksFile,
            password,
            *certificates as Array<out InputStream>)
    }

    fun getSslSocketFactory(
        bksFile: InputStream?,
        password: String?,
        trustManager: X509TrustManager?,
    ): SSLParams {
        return getSslSocketFactoryBase(trustManager, bksFile, password)
    }

    private fun getSslSocketFactoryBase(
        trustManager: X509TrustManager?,
        bksFile: InputStream?,
        password: String?,
        vararg certificates: InputStream,
    ): SSLParams {
        val sslParams = SSLParams()
        return try {
            val keyManagers = prepareKeyManager(bksFile, password)
            val trustManagers = prepareTrustManager(*certificates)
            val manager: X509TrustManager? = trustManager ?: if (trustManagers != null) {
                chooseTrustManager(trustManagers)
            } else {
                UnSafeTrustManager
            }
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(keyManagers, arrayOf<TrustManager?>(manager), null)
            sslParams.sSLSocketFactory = sslContext.socketFactory
            sslParams.trustManager = manager
            sslParams
        } catch (e: NoSuchAlgorithmException) {
            throw AssertionError(e)
        } catch (e: KeyManagementException) {
            throw AssertionError(e)
        }
    }

    private fun prepareKeyManager(bksFile: InputStream?, password: String?): Array<KeyManager>? {
        try {
            if (bksFile == null || password == null) return null
            val clientKeyStore = KeyStore.getInstance("BKS")
            clientKeyStore.load(bksFile, password.toCharArray())
            val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            kmf.init(clientKeyStore, password.toCharArray())
            return kmf.keyManagers
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun prepareTrustManager(vararg certificates: InputStream): Array<TrustManager>? {
        if (certificates.isEmpty()) return null
        try {
            val certificateFactory = CertificateFactory.getInstance("X.509")
            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            keyStore.load(null)
            for ((index, certStream) in certificates.withIndex()) {
                val certificateAlias = (index).toString()
                val cert = certificateFactory.generateCertificate(certStream)
                keyStore.setCertificateEntry(certificateAlias, cert)
                try {
                    certStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            tmf.init(keyStore)
            return tmf.trustManagers
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun chooseTrustManager(trustManagers: Array<TrustManager>): X509TrustManager? {
        for (trustManager in trustManagers) {
            if (trustManager is X509TrustManager) {
                return trustManager
            }
        }
        return null
    }

    var UnSafeTrustManager: X509TrustManager =
        @SuppressLint("CustomX509TrustManager") object : X509TrustManager {
            @SuppressLint("TrustAllX509TrustManager")
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            @SuppressLint("TrustAllX509TrustManager")
            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        }

    class SSLParams {
        var sSLSocketFactory: SSLSocketFactory? = null
        var trustManager: X509TrustManager? = null
    }
}