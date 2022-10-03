package com.manchuan.tools.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.drake.net.Get
import com.drake.net.cache.CacheMode
import com.drake.net.utils.scopeNetLife
import com.dylanc.longan.pressBackTwiceToExitApp
import com.dylanc.longan.startActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.ResponseInfo
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.kongzue.dialogx.dialogs.MessageDialog
import com.lxj.androidktx.core.doOnceInDay
import com.lxj.androidktx.core.postDelay
import com.manchuan.tools.base.AnimationActivity
import com.manchuan.tools.database.Global
import com.manchuan.tools.databinding.ActivitySplashBinding
import com.manchuan.tools.extensions.loge
import com.manchuan.tools.interceptor.PermissionInterceptor
import io.github.inflationx.viewpump.ViewPumpContextWrapper


@SuppressLint("CustomSplashScreen")
class SplashActivity : AnimationActivity() {

    private val binding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }

    @SuppressLint("WrongConstant", "VisibleForTests")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        MobileAds.initialize(this)
        val adRequest: AdRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
        doOnceInDay("getSentence", "", whenHasDone = {}, action = {
            scopeNetLife {
                val content = Get<String>("https://v1.hitokoto.cn/?c=i&encode=text") {
                    setCacheMode(CacheMode.REQUEST_THEN_READ)
                }.await()
                Global.localSentence = content
            }
        })
        binding.adView.adListener = object : AdListener() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                super.onAdFailedToLoad(error)
                val errorDomain: String = error.domain
                // Gets the error code. See
                // https://developers.google.com/android/reference/com/google/android/gms/ads/AdRequest#constant-summary
                // for a list of possible codes.
                // Gets the error code. See
                // https://developers.google.com/android/reference/com/google/android/gms/ads/AdRequest#constant-summary
                // for a list of possible codes.
                val errorCode: Int = error.code
                // Gets an error message.
                // For example "Account not approved yet". See
                // https://support.google.com/admob/answer/9905175 for explanations of
                // common errors.
                // Gets an error message.
                // For example "Account not approved yet". See
                // https://support.google.com/admob/answer/9905175 for explanations of
                // common errors.
                val errorMessage: String = error.message
                // Gets additional response information about the request. See
                // https://developers.google.com/admob/android/response-info for more
                // information.
                // Gets additional response information about the request. See
                // https://developers.google.com/admob/android/response-info for more
                // information.
                val responseInfo: ResponseInfo? = error.responseInfo
                // Gets the cause of the error, if available.
                // Gets the cause of the error, if available.
                val cause: AdError? = error.cause
                // All of this information is available via the error's toString() method.
                // All of this information is available via the error's toString() method.
                loge("Ads", error.toString())
            }
        }
        binding.lottie.pauseAnimation()
        if (!checkPMProxy()) {
            MessageDialog.show("警告", "请使用官方正版软件！").setOkButton("确定") { _, _ ->
                finish()
                false
            }.isCancelable = false
        } else if (checkPMProxy()) {
            if (Global.isFirst) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    XXPermissions.with(this).permission(Permission.READ_MEDIA_AUDIO)
                        .permission(Permission.READ_MEDIA_VIDEO).unchecked()
                        .interceptor(PermissionInterceptor()).request { permissions, all ->
                            if (all) {
                                initAnimationListener()
                            }
                        }
                } else {
                    XXPermissions.with(this).permission(Permission.Group.STORAGE).unchecked()
                        .interceptor(PermissionInterceptor()).request { permissions, all ->
                            if (all) {
                                initAnimationListener()
                            }
                        }
                }
            }
        }
        pressBackTwiceToExitApp("再按一次退出")
    }

    private fun initAnimationListener() {
        binding.lottie.playAnimation()
        binding.lottie.addAnimatorUpdateListener { valueAnimator -> // 判断动画加载结束
            if (valueAnimator.animatedFraction == 1f) {
                postDelay(200) {
                    startActivity<MainActivity>()
                    finish()
                }
            }
        }
    }

    /**
     * 检测 PackageManager 代理
     */
    @SuppressLint("PrivateApi")
    private fun checkPMProxy(): Boolean {
        val truePMName = "android.content.pm.IPackageManager\$Stub\$Proxy"
        var nowPMName = ""
        try {
            val packageManager = packageManager
            val mPMField = packageManager.javaClass.getDeclaredField("mPM")
            mPMField.isAccessible = true
            val mPM = mPMField[packageManager]!!
            nowPMName = mPM.javaClass.name
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return truePMName == nowPMName
    }

    override fun attachBaseContext(newBase: Context) {
        newBase.let { ViewPumpContextWrapper.wrap(it) }.let { super.attachBaseContext(it) }
    }
}