package com.manchuan.tools.activity

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.core.util.Consumer
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import androidx.window.java.layout.WindowInfoTrackerCallbackAdapter
import androidx.window.layout.FoldingFeature
import androidx.window.layout.FoldingFeature.Orientation.Companion.VERTICAL
import androidx.window.layout.FoldingFeature.State.Companion.FLAT
import androidx.window.layout.FoldingFeature.State.Companion.HALF_OPENED
import androidx.window.layout.WindowLayoutInfo
import com.alibaba.fastjson.JSON
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.drake.channel.sendEvent
import com.drake.interval.Interval
import com.drake.net.Get
import com.drake.net.component.Progress
import com.drake.net.interfaces.ProgressListener
import com.drake.net.utils.scope
import com.drake.net.utils.scopeNet
import com.drake.net.utils.scopeNetLife
import com.drake.serialize.serialize.deserialize
import com.drake.serialize.serialize.serialize
import com.dylanc.longan.addNavigationBarHeightToMarginBottom
import com.dylanc.longan.context
import com.dylanc.longan.design.FragmentStateAdapter
import com.dylanc.longan.doOnClick
import com.dylanc.longan.externalDownloadsDirPath
import com.dylanc.longan.installAPK
import com.dylanc.longan.pressBackToNotExitApp
import com.dylanc.longan.toUri
import com.dylanc.longan.topActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec
import com.google.android.material.progressindicator.IndeterminateDrawable
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.gyf.immersionbar.ImmersionBar
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.kongzue.dialogx.dialogs.BottomDialog
import com.kongzue.dialogx.interfaces.OnBindView
import com.lxj.androidktx.core.doOnlyOnce
import com.lxj.androidktx.core.drawable
import com.lxj.androidktx.core.startActivity
import com.lxj.androidktx.core.string
import com.lxj.androidktx.core.tip
import com.manchuan.tools.BuildConfig
import com.manchuan.tools.R
import com.manchuan.tools.activity.app.SettingsActivity
import com.manchuan.tools.adapter.transformer.CascadeTransformer
import com.manchuan.tools.adaptive.AdaptiveUtils
import com.manchuan.tools.base.AnimationActivity
import com.manchuan.tools.base.BaseAlertDialogBuilder
import com.manchuan.tools.database.Global
import com.manchuan.tools.databinding.ActivityMainBinding
import com.manchuan.tools.extensions.getSpecStyleResId
import com.manchuan.tools.extensions.loge
import com.manchuan.tools.extensions.notification
import com.manchuan.tools.extensions.userAgent
import com.manchuan.tools.extensions.windowBackground
import com.manchuan.tools.fragment.ResourcesFragment
import com.manchuan.tools.genshin.json.CourseStudy
import com.manchuan.tools.json.SerializationConverter
import com.manchuan.tools.model.CovidTag
import com.manchuan.tools.ui.functions.GalleryFragment
import com.manchuan.tools.ui.home.HomeFragment
import com.manchuan.tools.utils.UiUtils
import com.maxkeppeler.sheets.options.DisplayMode
import com.maxkeppeler.sheets.options.Option
import com.maxkeppeler.sheets.options.OptionsSheet
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.descriptionText
import com.mikepenz.materialdrawer.model.interfaces.iconUrl
import com.mikepenz.materialdrawer.model.interfaces.nameText
import com.mikepenz.materialdrawer.widget.AccountHeaderView
import com.tencent.mmkv.MMKV
import com.thegrizzlylabs.sardineandroid.Sardine
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.util.concurrent.Executor

class MainActivity : AnimationActivity() {


    private lateinit var kv: MMKV
    private lateinit var pageChangeListener: OnPageChangeCallback
    private lateinit var locationClient: AMapLocationClient
    private lateinit var locationListener: AMapLocationListener
    private lateinit var locationOption: AMapLocationClientOption
    private val isAcceptPolicy: Boolean = deserialize("isAcceptPolicy", false)
    private val isNeverAsk: Boolean = deserialize("isNeverAsk", false)
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    companion object {
        var configuration: Configuration? = null
    }

    private val windowInfoTracker: WindowInfoTrackerCallbackAdapter? = null
    private val stateContainer: Consumer<WindowLayoutInfo> = StateContainer()
    private val handler: Handler = Handler(Looper.getMainLooper())
    private val executor = Executor { command: Runnable? ->
        handler.post {
            if (command != null) {
                handler.post(command)
            }
        }
    }

    private class StateContainer : Consumer<WindowLayoutInfo> {
        override fun accept(windowLayoutInfo: WindowLayoutInfo) {
            if (configuration?.screenWidthDp!! < AdaptiveUtils.MEDIUM_SCREEN_WIDTH_SIZE) {
                //feedFragment.setClosedLayout()
            } else {
                val displayFeatures = windowLayoutInfo.displayFeatures
                var isClosed = true
                for (displayFeature in displayFeatures) {
                    if (displayFeature is FoldingFeature) {
                        val foldingFeature = displayFeature
                        if (foldingFeature.state == HALF_OPENED || foldingFeature.state == FLAT) {
                            val orientation: FoldingFeature.Orientation = foldingFeature.orientation
                            if (orientation == VERTICAL) {
                                val foldPosition = foldingFeature.bounds.left
                                val foldWidth = foldingFeature.bounds.right - foldPosition
                                // Device is open and fold is vertical.
                                //feedFragment.setOpenLayout(foldPosition, foldWidth)
                            } else {
                                // Device is open and fold is horizontal.
                                //feedFragment.setOpenLayout(container.getWidth() / 2, 0)
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("PrivateResource")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val titles = listOf(string(R.string.app_name), "功能", "资源")
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        configuration = resources.configuration
        ImmersionBar.with(this).titleBar(binding.toolbar).transparentNavigationBar()
            .statusBarDarkFont(!UiUtils.isDarkMode()).init()
        binding.viewPager.addNavigationBarHeightToMarginBottom()
        val headerView = AccountHeaderView(this).apply {
            attachToSliderView(binding.navDrawer) // attach to the slider
            addProfiles(ProfileDrawerItem().apply {
                nameText = "航"; descriptionText = "pedragons@outlook.com"; iconUrl =
                "http://q2.qlogo.cn/headimg_dl?dst_uin=3299699002&spec=100"; identifier = 102
            })
            onAccountHeaderListener = { view, profile, current ->
                false
            }
            withSavedInstance(savedInstanceState)
        }
        binding.viewPager.apply {
            setPageTransformer(CascadeTransformer())
            setUpViewPager()
            currentItem = 0
        }
        pageChangeListener = object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.bottomBar.menu.getItem(position).isChecked = true
                binding.ctl.title = titles[position]
                if (position == 0) {
                    binding.ctl.subtitle = Global.localSentence
                }
            }
        }
        scope(dispatcher = Dispatchers.IO) {
            val sardine: Sardine = OkHttpSardine()
            sardine.setCredentials("admin", "admin")
            val resources = sardine.list("https://fengpotter.michongs.repl.co/dav")
            resources.forEach {
                loge(it.displayName)
            }
        }
        scopeNetLife {
            val data =
                Get<CourseStudy>("https://bbs-api.mihoyo.com/post/wapi/getPostFullInCollection?&gids=2&order_type=2&collection_id=428421") {
                    setHeader("User-Agent", userAgent())
                    converter = SerializationConverter("0", "retcode", "message")
                }.await()
            data.data.posts.forEach {
                //logError("标题:${it.post.subject}")
            }
        }.catch {
            loge(it.toString())
        }
        binding.viewPager.registerOnPageChangeCallback(pageChangeListener)
        binding.bottomBar.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    binding.viewPager.currentItem = 0
                    return@setOnItemSelectedListener true
                }

                R.id.functions -> {
                    binding.viewPager.currentItem = 1
                    return@setOnItemSelectedListener true
                }

                R.id.my -> {
                    binding.viewPager.currentItem = 2
                    return@setOnItemSelectedListener true
                }
            }
            true
        }
        binding.bottomBar.selectedItemId = R.id.home
        updateVersion()
        pressBackToNotExitApp()
        initStarDialog()
    }

    private fun setUpViewPager() {
        binding.viewPager.adapter = FragmentStateAdapter(
            HomeFragment(), GalleryFragment(), ResourcesFragment(), isLazyLoading = true
        )
    }

    private fun initStarDialog() {
        if (Global.countLaunch < 6) {
            Global.countLaunch = Global.countLaunch.inc()
        } else if (Global.countLaunch == 6) {
            doOnlyOnce("please_star", action = {
                OptionsSheet().show(context) {
                    title("评分")
                    multipleChoices(false)
                    displayMode(DisplayMode.LIST)
                    isCancelable = false
                    maxChoices(1)
                    with(
                        Option(R.drawable.baseline_sentiment_very_dissatisfied_24, "非常不满意"),
                        Option(R.drawable.baseline_sentiment_dissatisfied_24, "不满意"),
                        Option(R.drawable.baseline_sentiment_neutral_24, "一般"),
                        Option(R.drawable.baseline_sentiment_satisfied_24, "满意"),
                        Option(R.drawable.baseline_sentiment_satisfied_alt_24, "非常满意"),
                    )
                    onPositive { index: Int, option: Option ->
                        topActivity.tip("感谢您的评分")
                        // Handle selected option
                    }
                }
            })
        }
    }

    override fun attachBaseContext(newBase: Context) {
        newBase.let { ViewPumpContextWrapper.wrap(it) }.let { super.attachBaseContext(it) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> startActivity<SettingsActivity>()
            R.id.about -> startActivity<AboutActivity>()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        when {
            !isAcceptPolicy && !isNeverAsk -> {
                requestLocationPermission()
            }

            isAcceptPolicy && isNeverAsk -> {
                requestWeatherAndCovidForNetwork()
            }
        }
    }


    private lateinit var covidAndWeatherInterval: Interval

    private fun requestWeatherAndCovidForNetwork() {
        locationOption = AMapLocationClientOption()
        locationClient = AMapLocationClient(this)
        locationOption.apply {
            locationMode = AMapLocationClientOption.AMapLocationMode.Battery_Saving
            isOnceLocation = true
        }
        locationListener = AMapLocationListener { location ->
            location?.let {
                scopeNetLife {
                    val weather =
                        Get<String>("https://ovooa.com/API/santq/api.php?msg=${it.city}").await()
                    val jsonContent = JSON.parseObject(weather)
                    val jsonContentOne = jsonContent.getJSONObject("data")
                    val jsonArray = jsonContentOne.getJSONArray("data")
                    val jsonOne = jsonArray.getJSONObject(0)
                    val covid = Get<String>("https://ovooa.com/API/yiqing/api?type=json").await()
                    val covidData = JSON.parseObject(JSON.parseObject(covid).getString("data"))
                    sendEvent(
                        CovidTag(
                            covidData.getString("time"),
                            covidData.getString("total"),
                            covidData.getString("death"),
                            covidData.getString("suspected"),
                            covidData.getString("cure"),
                            covidData.getString("asymptom"),
                            covidData.getString("overseas"),
                            covidData.getString("econ"),
                            covidData.getString("server")
                        ), "covid"
                    )
                }.catch {

                }
            }
        }
        locationClient.apply {
            setLocationOption(locationOption)
            setLocationListener(locationListener)
        }
        locationClient.startLocation()
    }

    private fun requestLocationPermission() {
        BaseAlertDialogBuilder(this).setTitle("提示").setMessage("获取天气、疫情数据等需要定位权限")
            .setPositiveButton(string(android.R.string.ok)) { dialogInterface: DialogInterface, i: Int ->
                XXPermissions.with(this)
                    .permission(Permission.ACCESS_COARSE_LOCATION, Permission.ACCESS_FINE_LOCATION)
                    .request(object : OnPermissionCallback {
                        override fun onGranted(permissions: MutableList<String>, all: Boolean) {
                            serialize("isAcceptPolicy" to true)
                            serialize("isNeverAsk" to true)
                            AMapLocationClient.updatePrivacyShow(this@MainActivity, true, true)
                            AMapLocationClient.updatePrivacyAgree(this@MainActivity, true)
                            requestWeatherAndCovidForNetwork()
                        }

                        override fun onDenied(permissions: MutableList<String>, never: Boolean) {
                            if (never) {
                                notification("提示", "被永久拒绝授权，请手动授予权限")
                                XXPermissions.startPermissionActivity(
                                    this@MainActivity, permissions
                                )
                            } else {
                                tip("获取权限失败")
                            }
                        }
                    })
            }.setNegativeButton("拒绝") { dialogInterface: DialogInterface, i: Int ->
                serialize("isAcceptPolicy" to false)
            }.setNeutralButton("不再询问") { dialogInterface: DialogInterface, i: Int ->
                serialize("isNeverAsk" to true)
                serialize("isAcceptPolicy" to false)
            }.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching {
            locationClient.onDestroy()
        }
    }

    private fun updateVersion() {
        scopeNetLife {
            val string = Get<String>("https://wds.ecsxs.com/229075.json").await()
            val jsonObject = JSON.parseObject(string)
            if (jsonObject.getIntValue("version_code") > BuildConfig.VERSION_CODE) {
                BottomDialog.show(
                    null,
                    null,
                    object : OnBindView<BottomDialog?>(R.layout.update_version_dialog) {
                        override fun onBind(dialog: BottomDialog?, v: View) {
                            val buttonLayout = v.findViewById<LinearLayout>(R.id.buttonLayout)
                            val content = v.findViewById<TextView>(R.id.event_info)
                            val cancel = v.findViewById<MaterialButton>(R.id.cancel_button)
                            val confirm = v.findViewById<MaterialButton>(R.id.upgrade)
                            val progressBar =
                                v.findViewById<LinearProgressIndicator>(R.id.progressBar)
                            buttonLayout.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
                            content.text = HtmlCompat.fromHtml(
                                jsonObject.getString("update_content"),
                                HtmlCompat.FROM_HTML_MODE_LEGACY
                            )
                            cancel.setOnClickListener {
                                dialog?.dismiss()
                            }
                            confirm.setOnClickListener {
                                val spec = CircularProgressIndicatorSpec(
                                    context,  /*attrs=*/
                                    null, 0, getSpecStyleResId()
                                )
                                val progressIndicatorDrawable: IndeterminateDrawable<CircularProgressIndicatorSpec> =
                                    IndeterminateDrawable.createCircularDrawable(
                                        context,
                                        spec.apply {
                                            indicatorColors = intArrayOf(Color.WHITE)
                                            trackCornerRadius = 4
                                        })
                                confirm.icon = progressIndicatorDrawable
                                scopeNet {
                                    val file = Get<File>(jsonObject.getString("download_url")) {
                                        setDownloadFileName("update.apk")
                                        setDownloadDir(externalDownloadsDirPath.toString())
                                        setDownloadMd5Verify()
                                        addDownloadListener(object : ProgressListener() {
                                            @SuppressLint("SetTextI18n")
                                            override fun onProgress(p: Progress) {
                                                runOnUiThread {
                                                    confirm.text = "进度:${p.progress()}%"
                                                }
                                            }

                                        })
                                    }.await()
                                    confirm.icon = drawable(R.drawable.ic_open_in_new)
                                    confirm.text = "安装"
                                    confirm.doOnClick {
                                        installAPK(file.toUri(Global.authority))
                                    }
                                    confirm.performClick()
                                }.catch {
                                    println(it.printStackTrace())
                                }
                            }
                        }
                    }).setAllowInterceptTouch(false).setCancelable(false).backgroundColor =
                    windowBackground()
            }
        }
    }

    private val list = arrayListOf<String>()

    private fun String.joinQQGroup() {
        val intent = Intent()
        intent.data =
            Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D${this}")
        try {
            startActivity(intent)
        } catch (ignored: Exception) {
        }
    }

    private fun String.addQQFriend() {
        val intent = Intent()
        intent.data =
            Uri.parse("mqqapi://card/show_pslcard?src_type=internal&source=sharecard&version=1&uin=${this}")
        try {
            startActivity(intent)
        } catch (ignored: Exception) {
        }
    }

    private fun startAlipay(activity: Activity, urlCode: String): Boolean {
        return startIntentUrl(activity, URL_FORMAT.replace("{urlCode}", urlCode))
    }

    private fun startIntentUrl(activity: Activity, intentUrl: String): Boolean {
        runCatching {
            val intent = Intent.parseUri(intentUrl, Intent.URI_INTENT_SCHEME)
            activity.startActivity(intent)
        }.onFailure {
            tip("启动失败")
        }
        return false
    }

    private val URL_FORMAT =
        "intent://platformapi/startapp?saId=10000007&" + "clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2F{urlCode}%3F_s" + "%3Dweb-other&_t=1472443966571#Intent;" + "scheme=alipayqr;package=com.eg.android.AlipayGphone;end"

}