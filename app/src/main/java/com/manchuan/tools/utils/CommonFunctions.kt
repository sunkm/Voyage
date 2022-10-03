package com.manchuan.tools.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.WallpaperManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.os.VibrationEffect
import android.text.InputType
import android.util.Base64
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.fragment.app.FragmentActivity
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.drake.net.Get
import com.drake.net.utils.scopeNet
import com.drake.softinput.setWindowSoftInput
import com.dylanc.longan.isWebUrl
import com.dylanc.longan.startActivity
import com.dylanc.longan.toast
import com.dylanc.longan.urlDecode
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.kongzue.dialogx.dialogs.BottomMenu
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.kongzue.dialogx.interfaces.OnMenuItemClickListener
import com.manchuan.tools.R
import com.manchuan.tools.activity.FullscreenVideoActivity
import com.manchuan.tools.activity.WebSiteSourceCodeActivity
import com.manchuan.tools.extensions.loge
import com.maxkeppeler.sheets.input.InputSheet
import com.maxkeppeler.sheets.input.type.InputEditText
import com.pawegio.kandroid.vibrator
import com.wajahatkarim3.easyvalidation.core.view_ktx.validUrl
import java.io.IOException
import java.util.*

class CommonFunctions {

    private val mParcelFileDescriptor: ParcelFileDescriptor? = null

    fun vibrateFunction(activity: Activity) {
        val mBottomSheetDialog =
            BottomSheetDialog(activity, R.style.ModalBottomSheetDialog)
        val view = activity.layoutInflater.inflate(R.layout.bottom_vibrator, null)
        val start_vibrator: MaterialCardView =
            view.findViewById(R.id.start_vibrator)
        val stop_vibrator: MaterialCardView = view.findViewById(R.id.stop_vibrator)
        mBottomSheetDialog.setContentView(view)
        val parent = view.parent
        val group = parent as ViewGroup
        BottomSheetBehavior.from(group)
        val dm = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(dm)
        start_vibrator.setOnClickListener { view12: View? ->
            runCatching {
                activity.vibrator?.vibrate(VibrationEffect.createOneShot(60L * 3600 * 6000 * 600L,
                    VibrationEffect.DEFAULT_AMPLITUDE))
            }.onFailure {
                activity.toast("配置振动器失败")
            }
        }
        stop_vibrator.setOnClickListener { view13: View? ->
            runCatching {
                activity.vibrator?.cancel()
            }
        }
        mBottomSheetDialog.create()
        mBottomSheetDialog.show()
        mBottomSheetDialog.setOnDismissListener { p1: DialogInterface? ->
            runCatching {
                activity.vibrator?.cancel()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun wallpaper(activity: Activity): Bitmap {
        val wallpaperManager = WallpaperManager.getInstance(activity)
        var mParcelFileDescriptor =
            wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_LOCK)
        if (mParcelFileDescriptor == null) {
            mParcelFileDescriptor =
                wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM)
        }
        val fileDescriptor = mParcelFileDescriptor!!.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        try {
            mParcelFileDescriptor.close()
        } catch (e: Exception) {
        }
        return image
    }


    fun saveWallpaper(activity: Activity) {
        val Wallpapers = arrayOf<CharSequence>("桌面壁纸", "锁屏壁纸")
        BottomMenu.show(Wallpapers)
            .setTitle("保存手机壁纸").onMenuItemClickListener =
            OnMenuItemClickListener { _: BottomMenu?, text: CharSequence?, index: Int ->
                when (Wallpapers[index].toString()) {
                    "桌面壁纸" -> if (mParcelFileDescriptor == null) {
                        ImageUtil.saveImageToGallery(
                            activity,
                            wallpaper(activity),
                            "Wallpaper_Launcher_" + System.currentTimeMillis() + ".png"
                        )
                    }
                    "锁屏壁纸" -> try {
                        ImageUtil.saveImageToGallery(
                            activity,
                            wallpaper(activity),
                            "Wallpaper_Lock_" + System.currentTimeMillis() + ".png"
                        )
                    } catch (e: Exception) {
                        PopTip.show("无法获取锁屏壁纸")
                    }
                }
                false
            }
    }

    fun setDumpsysBattery(activity: Activity) {
        val battery_menus = arrayOf("电量伪装", "充电伪装", "恢复伪装")
        BottomMenu.show(battery_menus)
            .setTitle("电量伪装").onMenuItemClickListener =
            OnMenuItemClickListener { dialog: BottomMenu?, text: CharSequence?, index: Int ->
                when (battery_menus[index]) {
                    "充电伪装" -> {
                        val charging_status =
                            arrayOf("未充电", "AC充电", "USB充电", "无线充电")
                        BottomMenu.show(charging_status)
                            .setTitle("充电伪装").onMenuItemClickListener =
                            OnMenuItemClickListener { _: BottomMenu?, _: CharSequence?, index1: Int ->
                                when (charging_status[index1]) {
                                    "未充电" -> if (!RootCmd.haveRoot()) {
                                        TipDialog.show(
                                            R.string.no_root,
                                            WaitDialog.TYPE.ERROR
                                        )
                                    } else {
                                        RootCmd.runRootCommand("dumpsys battery unplug")
                                    }
                                    "AC充电" -> if (!RootCmd.haveRoot()) {
                                        TipDialog.show(
                                            R.string.no_root,
                                            WaitDialog.TYPE.ERROR
                                        )
                                    } else {
                                        RootCmd.runRootCommand("dumpsys battery set ac 1")
                                    }
                                    "USB充电" -> if (!RootCmd.haveRoot()) {
                                        TipDialog.show(
                                            R.string.no_root,
                                            WaitDialog.TYPE.ERROR
                                        )
                                    } else {
                                        RootCmd.runRootCommand("dumpsys battery set usb 1")
                                    }
                                    "无线充电" -> if (!RootCmd.haveRoot()) {
                                        TipDialog.show(
                                            R.string.no_root,
                                            WaitDialog.TYPE.ERROR
                                        )
                                    } else {
                                        RootCmd.runRootCommand("dumpsys battery set wireless 1")
                                    }
                                    else -> {}
                                }
                                false
                            }
                    }
                    "电量伪装" -> {
                        val battery = BottomSheetDialog(
                            activity,R.style.ModalBottomSheetDialog
                        )
                        @SuppressLint("InflateParams") val views =
                            activity.layoutInflater.inflate(R.layout.dialog_battery, null)
                        @SuppressLint("CutPasteId") val battery_str: TextInputEditText =
                            views.findViewById(
                                R.id.battery_ints
                            )
                        val confirm = views.findViewById<Button>(R.id.confirm)
                        confirm.setOnClickListener {
                            if (Objects.requireNonNull(battery_str.text).toString()
                                    .isEmpty()
                            ) {
                                TipDialog.show("电量值不能为空")
                            } else {
                                if (!RootCmd.haveRoot()) {
                                    TipDialog.show(
                                        R.string.no_root,
                                        WaitDialog.TYPE.ERROR
                                    )
                                } else {
                                    RootCmd.execRootCmd("dumpsys battery set level " + battery_str.text.toString())
                                }
                            }
                        }
                        battery.setContentView(views)
                        battery.create()
                        battery.show()
                    }
                    "恢复伪装" -> if (!RootCmd.haveRoot()) {
                        TipDialog.show(R.string.no_root, WaitDialog.TYPE.ERROR)
                    } else {
                        RootCmd.execRootCmd("dumpsys battery reset")
                    }
                }
                false
            }
    }

    fun getShortUrl(context: Context) {
        InputSheet().show(context) {
            title("短网址生成")
            with(InputEditText {
                required()
                startIconDrawable(R.drawable.twotone_web_black_24dp)
                inputType(InputType.TYPE_TEXT_VARIATION_URI)
                label("请输入要生成短网址的链接")
                hint("网址")
                changeListener { value ->
                    value?.validUrl {
                        context.toast("请输入网址")
                    }
                }
                resultListener { value -> }
            })
            onNegative { }
            onPositive { result ->
                val text = result.getString("0") // Read value of inputs by index
                if (text?.isWebUrl() == true) {
                    scopeNet {
                        val string =
                            Get<String>("https://api.suolink.cn/api.?url=${text.urlDecode(Charsets.UTF_8.name())}&key=62ff452f2bd664f6735bc675df@ba9613f2181a94ca91da78fd603ad542&expireDate=2030-03-31&domain=nxw.so").await()
                        ClipboardUtils.copyText(string)
                        context.toast("生成的短网址已复制")
                        loge("生成的短网址已复制")
                    }
                } else {
                    context.toast("不是有效网址")
                }
            }
        }
    }

    fun getWebSource(activity: Activity) {
        InputSheet().show(activity) {
            title("网页取源")
            with(InputEditText {
                required()
                startIconDrawable(R.drawable.twotone_web_black_24dp)
                inputType(InputType.TYPE_TEXT_VARIATION_URI)
                label("请输入要取源的网址")
                hint("网址")
                changeListener { value ->
                    value?.validUrl {
                        activity.toast("请输入网址")
                    }
                }
                resultListener { value -> }
            })
            onNegative { }
            onPositive { result ->
                val text = result.getString("0") // Read value of inputs by index
                if (text?.validUrl() == true) {
                    activity.startActivity<WebSiteSourceCodeActivity>("url" to text)
                } else {
                    activity.toast("不是有效网址")
                }
            }
        }
    }

    fun getVideo(context: Activity) {
        val mBottomSheetDialog =
            BottomSheetDialog(context,R.style.ModalBottomSheetDialog)
        @SuppressLint("InflateParams") val view =
            context.layoutInflater.inflate(R.layout.bottom_movie, null)
        val web_url: TextInputLayout = view.findViewById(R.id.web_url)
        val web_url_edit: TextInputEditText = view.findViewById(R.id.battery_ints)
        val get_code = view.findViewById<Button>(R.id.get_code)
        get_code.setOnClickListener { view1: View? ->
            if (Objects.requireNonNull(web_url_edit.text).toString().isEmpty()) {
                web_url.error = "不能留空"
            } else {
                val intent = Intent(context, FullscreenVideoActivity::class.java)
                intent.putExtra(
                    "url",
                    "https://m2090.com/?url=" + web_url_edit.text.toString()
                )
                context.startActivity(intent)
            }
        }
        mBottomSheetDialog.setContentView(view)
        mBottomSheetDialog.create()
        mBottomSheetDialog.show()
    }

    fun openQQFunctions(context: Context) {
        val items = arrayOf<CharSequence>(
            "QQ好友/群恢复",
            "QQ群解封",
            "QQ空白资料",
            "QQ一键群发",
            "QQ靓号注册",
            "QQ注册日期",
            "QQ亲密度排行榜",
            "QQ冻结查询解冻",
            "QQ开通业务查询",
            "QQ好友克隆",
            "QQ群克隆",
            "QQ安全中心",
            "QQ个人中心",
            "QQ加群组建",
            "QQ群管理",
            "QQ所有铭牌",
            "王者大会铭牌",
            "QQ官网",
            "QQ群官网",
            "3K人群创建",
            "恢复好友标识",
            "6折开大会员",
            "大会员每月领红钻",
            "QQ群状态查询",
            "QQ空间认证",
            "QQ加速0.2天",
            "实名认证信息",
            "腾讯游戏信用",
            "黄钻签到一",
            "黄钻签到二",
            "开启QQ空间",
            "关闭QQ空间"
        )
        val url = arrayOf<CharSequence>(
            "https://huifu.qq.com/",
            "https://kf.qq.com/self_help/qq_group_status.html",
            "https://id.qq.com/index.html#info?",
            "https://ti.qq.com/mass-blessing/index.html?_wv=2361270393&mobile_area=86&source=1&is_mobile_forbidden=9&from=groupmessage",
            "https://ssl.zc.qq.com/v3/index-chs.html?type=3",
            "http://imgcache.qq.com/qqshow_v3/htdocs/my/inc/info.html?",
            "https://h5.qzone.qq.com/close/rank?",
            "https://aq.qq.com/cn2/login_limit/index_smart?",
            "https://kf.qq.com/pay_qq/index.html?mp_sourceid=0.2.2",
            "https://vip.qq.com/client/fr_index.html",
            "https://vip.qq.com/client/groupclone",
            "https://aq.qq.com/cn2/mobile_index",
            "https://id.qq.com/?",
            "https://qun.qq.com/join.html?",
            "https://qun.qq.com/manage.html?",
            "https://club.vip.qq.com/medal/mine?_wv=16777216&_wwv=4&_wvx=10",
            "https://club.vip.qq.com/card?",
            "https://im.qq.com/immobile/index.html?_wv=1025&adtag=share",
            "https://qun.qq.com/?",
            "https://h5.vip.qq.com/p/mc/group/create3k?_wwv=4&_wv=1025&_wvx=3&adtag=gntq",
            "https://club.vip.qq.com/interact/selector?from=function",
            "https://act.qzone.qq.com/vip/meteor/blockly/p/3532x28a62?enteranceId=&friuin=1609155942",
            "https://act.qzone.qq.com/vip/meteor/blockly/p/1547xfb67f?",
            "https://kf.qq.com/self_help/qq_group_status.html",
            "https://h5.qzone.qq.com/brand/apply/index?",
            "http://reader.sh.vip.qq.com/cgi-bin/common_async_cgi?g_tk=5381&plat=1&version=6.6.6&param=%7B%22key0%22%3A%7B%22param%22%3A%7B%22bid%22%3A13792605%7D%2C%22module%22%3A%22reader_comment_read_svr%22%2C%22method%22%3A%22GetReadAllEndPageMsg%22%7D%7D",
            "https://jiazhang.qq.com/wap/health/dist/home/index.html#/",
            "https://credit.gamesafe.qq.com/static/gamecredit_refactor/index.html#/index?",
            "https://h5.qzone.qq.com/vip/score?_proxy=1",
            "https://h5.qzone.qq.com/vip/home?_wv=16778243&amp;qzUseTransparentNavBar=1&amp;_wwv=1&amp;_ws=32&amp;from=coupon&amp;aid=zrtzlkdwx#From_QQqun237955606?",
            "http://imgcache.qq.com/qzone/web/load2.htm?",
            "https://imgcache.qq.com/qzone/web/qzone_submit_close.html?"
        )
        BottomMenu.show(items)
            .setTitle("QQ快捷跳转").onMenuItemClickListener =
            OnMenuItemClickListener { dialog: BottomMenu?, text: CharSequence?, index: Int ->
                try {
                    openQQIntent(url[index].toString(), context)
                } catch (e: Exception) {
                    PopTip.show(R.string.no_install_qq)
                }
                false
            }
    }

    @SuppressLint("InflateParams")
    fun alipayAudio(context: Context, fragmentActivity: FragmentActivity) {
        val mBottomSheetDialog =
            BottomSheetDialog(context, R.style.ModalBottomSheetDialog)
        val view = fragmentActivity.layoutInflater.inflate(R.layout.bottom_zfb, null)
        val web_url: TextInputLayout = view.findViewById(R.id.money)
        val web_url_edit: TextInputEditText = view.findViewById(R.id.money_edit)
        val get_code = view.findViewById<Button>(R.id.get_code)
        get_code.setOnClickListener { view14: View? ->
            if (Objects.requireNonNull(web_url_edit.text).toString()
                    .isEmpty()
            ) {
                web_url.error = "不能留空"
            } else {
                WaitDialog.show("请稍等...")
                get_code.isEnabled = false
                get_code.isClickable = false
                Thread {
                    val media = MediaPlayer()
                    try {
                        media.setDataSource("https://mm.cqu.cc/share/zhifubaodaozhang/mp3/" + web_url_edit.text.toString() + ".mp3")
                        media.prepareAsync()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    media.setOnPreparedListener { p1: MediaPlayer? ->
                        media.start()
                        WaitDialog.dismiss()
                    }
                    media.setOnCompletionListener { p1: MediaPlayer? ->
                        WaitDialog.dismiss()
                        get_code.isEnabled = true
                        get_code.isClickable = true
                    }
                }.start()
            }
        }
        KeyboardUtils.fixAndroidBug5497(fragmentActivity)
        KeyboardUtils.fixSoftInputLeaks(fragmentActivity)
        fragmentActivity.setWindowSoftInput(float = view, setPadding = true)
        mBottomSheetDialog.setContentView(view)
        if (OsUtils.atLeastS()) {
            mBottomSheetDialog.window?.apply {
                addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                attributes.blurBehindRadius = 32
            }
        }
        mBottomSheetDialog.create()
        mBottomSheetDialog.show()
    }


    private fun openQQIntent(Url: String, context: Context) {
        val url = "mqqapi://forward/url?url_prefix=" + Base64.encodeToString(
            Url.toByteArray(),
            Base64.DEFAULT
        )
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

}