package com.manchuan.tools.application

import ando.file.core.FileOperator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.widget.ImageView
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.amap.api.location.AMapLocationClient
import com.blankj.utilcode.util.CacheDiskUtils
import com.blankj.utilcode.util.CrashUtils
import com.blankj.utilcode.util.RomUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.coder.ffmpeg.jni.FFmpegCommand
import com.drake.brv.utils.BRV
import com.drake.debugkit.DevTool
import com.drake.engine.utils.GB
import com.drake.net.NetConfig
import com.drake.net.okhttp.trustSSLCertificate
import com.drake.serialize.serialize.deserialize
import com.drake.serialize.serialize.serial
import com.drake.serialize.serialize.serialLazy
import com.drake.statelayout.StateConfig
import com.dylanc.longan.context
import com.dylanc.longan.dp
import com.dylanc.longan.externalDownloadsDirPath
import com.dylanc.longan.fileProviderAuthority
import com.dylanc.longan.handleUncaughtException
import com.dylanc.longan.isOppoRom
import com.dylanc.longan.roundCorners
import com.github.gzuliyujiang.oaid.DeviceIdentifier
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.SketchFactory
import com.github.panpf.sketch.cache.CachePolicy
import com.github.panpf.sketch.cache.internal.LruDiskCache
import com.github.panpf.sketch.decode.ApkIconBitmapDecoder
import com.github.panpf.sketch.decode.AppIconBitmapDecoder
import com.github.panpf.sketch.decode.FFmpegVideoFrameBitmapDecoder
import com.github.panpf.sketch.decode.GifAnimatedDrawableDecoder
import com.github.panpf.sketch.decode.GifDrawableDrawableDecoder
import com.github.panpf.sketch.decode.GifMovieDrawableDecoder
import com.github.panpf.sketch.decode.HeifAnimatedDrawableDecoder
import com.github.panpf.sketch.decode.SvgBitmapDecoder
import com.github.panpf.sketch.decode.VideoFrameBitmapDecoder
import com.github.panpf.sketch.decode.WebpAnimatedDrawableDecoder
import com.github.panpf.sketch.fetch.AppIconUriFetcher
import com.github.panpf.sketch.http.OkHttpStack
import com.github.panpf.sketch.request.ImageOptions
import com.github.panpf.sketch.request.PauseLoadWhenScrollingDisplayInterceptor
import com.github.panpf.sketch.request.SaveCellularTrafficDisplayInterceptor
import com.github.panpf.sketch.util.Logger
import com.google.android.material.color.DynamicColors
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.hjq.toast.ToastUtils
import com.jeffmony.downloader.VideoDownloadConfig
import com.jeffmony.downloader.VideoDownloadManager
import com.jeffmony.downloader.common.DownloadConstants
import com.lxj.androidktx.AndroidKTX
import com.lxj.androidktx.core.startActivity
import com.lxj.androidktx.core.tip
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.notification.INotification
import com.lzx.starrysky.notification.NotificationConfig
import com.lzx.starrysky.notification.imageloader.ImageLoaderCallBack
import com.lzx.starrysky.notification.imageloader.ImageLoaderStrategy
import com.manchuan.tools.BR
import com.manchuan.tools.BuildConfig
import com.manchuan.tools.R
import com.manchuan.tools.activity.ErrorActivity
import com.manchuan.tools.database.Global
import com.manchuan.tools.extensions.notification
import com.manchuan.tools.genshin.bean.UserBean
import com.manchuan.tools.genshin.ext.mainUser
import com.manchuan.tools.genshin.ext.usp
import com.manchuan.tools.genshin.information.JsonCacheName
import com.manchuan.tools.genshin.untils.GSON
import com.manchuan.tools.model.appModels
import com.manchuan.tools.service.SafetyService
import com.manchuan.tools.theme.untils.ThemeStore
import com.manchuan.tools.utils.ColorUtils
import com.manchuan.tools.utils.OsUtils
import com.manchuan.tools.utils.SettingsLoader
import com.manchuan.tools.utils.StorageManager
import com.manchuan.tools.utils.ThemeUtils
import com.manchuan.tools.utils.Utility
import com.mikepenz.iconics.Iconics
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerImageLoader
import com.rommansabbir.networkx.NetworkXLifecycle
import com.rommansabbir.networkx.NetworkXProvider
import com.rommansabbir.networkx.SmartConfig
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.tencent.mmkv.MMKV
import com.tencent.tauth.Tencent
import com.umeng.commonsdk.UMConfigure
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import rikka.material.app.DayNightDelegate
import timber.log.Timber
import timber.log.Timber.Forest.plant
import xyz.doikki.videoplayer.exo.ExoMediaPlayerFactory
import xyz.doikki.videoplayer.player.VideoViewConfig
import xyz.doikki.videoplayer.player.VideoViewManager
import java.io.File
import java.util.concurrent.TimeUnit


class App : MultiDexApplication(), SketchFactory {

    @SuppressLint("ObsoleteSdkInt")
    override fun createSketch(): Sketch = Sketch.Builder(this).apply {
        logger(Logger(Logger.Level.VERBOSE))
        httpStack(OkHttpStack.Builder().build())
        components {
            // global image request config
            LruDiskCache.ForDownloadBuilder(applicationContext).maxSize(2.GB)
            LruDiskCache.ForResultBuilder(applicationContext).maxSize(2.GB)
            globalImageOptions(ImageOptions {
                runCatching {
                    if (Global.cachePolicyEnabled) {
                        resultCachePolicy(CachePolicy.ENABLED)
                        downloadCachePolicy(CachePolicy.ENABLED)
                    } else {
                        resultCachePolicy(CachePolicy.DISABLED)
                        downloadCachePolicy(CachePolicy.DISABLED)
                    }
                }.onFailure {
                    resultCachePolicy(CachePolicy.ENABLED)
                    downloadCachePolicy(CachePolicy.ENABLED)
                }
            })
            addRequestInterceptor(SaveCellularTrafficDisplayInterceptor())
            addRequestInterceptor(PauseLoadWhenScrollingDisplayInterceptor())
            // app icon
            addFetcher(AppIconUriFetcher.Factory())
            addBitmapDecoder(AppIconBitmapDecoder.Factory())
            // apk icon
            addBitmapDecoder(ApkIconBitmapDecoder.Factory())
            // svg
            addBitmapDecoder(SvgBitmapDecoder.Factory())
            // video
            addBitmapDecoder(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    VideoFrameBitmapDecoder.Factory()
                } else {
                    FFmpegVideoFrameBitmapDecoder.Factory()
                }
            )
            // gif
            addDrawableDecoder(
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> GifAnimatedDrawableDecoder.Factory()
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> GifMovieDrawableDecoder.Factory()
                    else -> GifDrawableDrawableDecoder.Factory()
                }
            )
            // webp animated
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                addDrawableDecoder(WebpAnimatedDrawableDecoder.Factory())
            }
            // heif animated
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                addDrawableDecoder(HeifAnimatedDrawableDecoder.Factory())
            }
        }
    }.build()


    private var homeActivity: Activity? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var instance: App

        lateinit var tencent: Tencent

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        val APP_VERSION_NAME: String by lazy {
            context.packageManager.getPackageInfo(
                context.packageName, PackageManager.GET_CONFIGURATIONS
            ).versionName
        }
    }

    fun getHomeActivity(): Activity? {
        return homeActivity
    }

    fun setHomeActivity(homeActivity: Activity?) {
        this.homeActivity = homeActivity
    }


    override fun onCreate() {
        super.onCreate()
        tencent = Tencent.createInstance("102024802", context, "com.tencent.login.fileprovider")
        ToastUtils.init(this)
        SafetyService.enableSafety(this)
        CrashUtils.init()
        DeviceIdentifier.register(this)
        VideoViewManager.setConfig(
            VideoViewConfig.newBuilder()
                //使用使用IjkPlayer解码
                .setPlayOnMobileNetwork(true).setLogEnabled(BuildConfig.DEBUG)
                .setPlayerFactory(ExoMediaPlayerFactory.create()).build()
        )
        runCatching {
            FFmpegCommand.setDebug(BuildConfig.DEBUG)
        }
        fileProviderAuthority = "$packageName.fileProvider"
        Companion.context = baseContext
        if (!ThemeStore.isConfigured(this, 3)) {
            ThemeStore.editTheme(this).accentColorRes(R.color.colorAccent)
                .coloredNavigationBar(true).commit()
        }
        instance = this
        val config: VideoDownloadConfig =
            VideoDownloadManager.Build(this).setCacheRoot(externalDownloadsDirPath)
                .setTimeOut(DownloadConstants.READ_TIMEOUT, DownloadConstants.CONN_TIMEOUT)
                .setConcurrentCount(DownloadConstants.CONCURRENT).setIgnoreCertErrors(true)
                .setShouldM3U8Merged(true).buildConfig()
        VideoDownloadManager.getInstance().initConfig(config)
        handleUncaughtException { thread, throwable ->
            Thread {
                Looper.prepare()
                this.startActivity<ErrorActivity>(
                    flag = Intent.FLAG_ACTIVITY_NEW_TASK, bundle = arrayOf("error" to throwable)
                )
                Looper.loop()
            }.start()
        }
        CacheDiskUtils.getInstance(cacheDir)
        ColorUtils.initialize(this)
        StorageManager.createAllFolder(this)
        SettingsLoader.init(this)
        SettingsLoader.loadAnalytic(this)
        SettingsLoader.loadSettings()
        SettingsLoader.loadDialogConfig(this)
        FileOperator.init(this, BuildConfig.DEBUG)
        SettingsLoader.nightMode?.let { DayNightDelegate.setDefaultNightMode(it) }
        ThemeUtils.init(this)
        DevTool.debug = BuildConfig.DEBUG
        DayNightDelegate.setApplicationContext(this)
        Utility.init(this)
        //
        AndroidKTX.init(this)
        if (BuildConfig.DEBUG) {
            plant(Timber.DebugTree())
        }
        NetworkXProvider.enable(SmartConfig(this, true, NetworkXLifecycle.Application))
        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(appModels)
        }
        mainUser =
            GSON.fromJson(usp.getString(JsonCacheName.MAIN_USER_NAME, ""), UserBean::class.java)
        if (mainUser == null) {
            mainUser = UserBean()
        }
        Iconics.init(this)
        UMConfigure.init(
            this, "62f3ceb888ccdf4b7efd04a7", "正式版渠道", UMConfigure.DEVICE_TYPE_PHONE, ""
        )
        Global.defaultFontPath = cacheDir.absolutePath + File.separator + "fonts/"
        if (!File(Global.defaultFontPath).exists()) {
            File(Global.defaultFontPath).mkdirs()
        }
        DrawerImageLoader.init(object : AbstractDrawerImageLoader() {
            override fun set(imageView: ImageView, uri: Uri, placeholder: Drawable, tag: String?) {
                imageView.roundCorners = 16.dp
                Glide.with(imageView).load(uri).skipMemoryCache(true).placeholder(placeholder)
                    .into(imageView)
            }

            override fun cancel(imageView: ImageView) {
                Glide.with(imageView).clear(imageView)
            }
        })
        when {
            Global.isEnabledDefaultFont -> {
                ViewPump.init(
                    ViewPump.builder().addInterceptor(
                        CalligraphyInterceptor(
                            CalligraphyConfig.Builder()
                                .setDefaultFontPath(Global.defaultFontPath + Global.defaultFontName)
                                .setFontAttrId(io.github.inflationx.calligraphy3.R.attr.fontPath)
                                .build()
                        )
                    ).build()
                )
            }
        }
        runCatching {
            val notificationConfig = NotificationConfig.create {
                targetClass { "com.manchuan.tools.service.SongNotificationReceiver" }
                targetClassBundle {
                    val bundle = Bundle()
                    bundle.putString("title", "我是点击通知栏转跳带的参数")
                    bundle.putString("targetClass", "com.manchuan.tools.activity.PlaySpeakActivity")
                    //参数自带当前音频播放信息，不用自己传
                    return@targetClassBundle bundle
                }
                pendingIntentMode { NotificationConfig.MODE_BROADCAST }
            }
            if (XXPermissions.isGranted(this, Permission.READ_PHONE_STATE)) {
                StarrySky.init(this).setOpenCache(true).setDebug(BuildConfig.DEBUG)
                    .setAutoManagerFocus(true).setNotificationSwitch(true)
                    .setNotificationConfig(notificationConfig)
                    .setImageLoader(object : ImageLoaderStrategy {
                        //使用自定义图片加载器
                        override fun loadImage(
                            context: Context,
                            url: String?,
                            callBack: ImageLoaderCallBack,
                        ) {
                            Glide.with(context).asBitmap().load(url)
                                .into(object : CustomTarget<Bitmap?>() {
                                    override fun onLoadCleared(placeholder: Drawable?) {}

                                    override fun onResourceReady(
                                        resource: Bitmap,
                                        transition: Transition<in Bitmap?>?,
                                    ) {
                                        callBack.onBitmapLoaded(resource)
                                    }

                                    override fun onLoadFailed(errorDrawable: Drawable?) {
                                        super.onLoadFailed(errorDrawable)
                                        callBack.onBitmapFailed(errorDrawable)
                                    }
                                })
                        }
                    }).setNotificationType(INotification.SYSTEM_NOTIFICATION).apply()
            } else if (!XXPermissions.isGranted(
                    this, Permission.READ_PHONE_STATE
                ) && RomUtils.isOppo()
            ) {
                notification(
                    "警告",
                    "您当前是OPPO手机，无电话权限无法配置音频服务，将会导致有声小说播放造成闪退"
                )
            }
        }.onFailure {
            tip("音频服务配置失败")
        }
        serialLazy(false, "isNeverAsk")
        serialLazy(false, "isAcceptPolicy")
        if (deserialize("isAcceptPolicy", false) && deserialize("isNeverAsk", false)) {
            AMapLocationClient.updatePrivacyShow(this, true, true)
            AMapLocationClient.updatePrivacyAgree(this, true)
        }
        runCatching {
            serial(OsUtils.atLeastS(), "isEnabledDynamicColors")
            if (deserialize("isEnabledDynamicColors", OsUtils.atLeastS())) {
                DynamicColors.applyToActivitiesIfAvailable(this)
            }
        }.onFailure {
            tip("动态取色配置失败")
        }
        fileProviderAuthority = "com.manchuan.tools.fileprovider"
        AndroidKTX.init(this)
        BRV.modelId = BR.m
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, layout -> ClassicsHeader(this) }
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, layout -> ClassicsFooter(this) }
        NetConfig.initialize {
            trustSSLCertificate()
            connectTimeout(30, TimeUnit.SECONDS)
            addInterceptor(
                ChuckerInterceptor.Builder(this@App).collector(ChuckerCollector(context)).build()
            )
        }
        StateConfig.apply {
            loadingLayout = R.layout.layout_loading // 配置全局的加载中布局
            errorLayout = R.layout.layout_error
            emptyLayout = R.layout.layout_empty
            onLoading {}
            onEmpty {}
            onError {}
        }
        Global.idVerify = when {
            isOppoRom -> DeviceIdentifier.getClientId()
            else -> DeviceIdentifier.getOAID(this)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        stopKoin()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
        xcrash.XCrash.init(this)
        MMKV.initialize(base)
        initAcra {
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.JSON
        }
    }

}