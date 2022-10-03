package com.manchuan.tools.ui.functions

import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.drake.brv.utils.setup
import com.drake.engine.base.EngineFragment
import com.dylanc.longan.startActivity
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.transition.platform.MaterialFadeThrough
import com.itxca.spannablex.spannable
import com.kongzue.dialogx.dialogs.BottomMenu
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.OnMenuItemClickListener
import com.lxj.androidktx.core.animateGone
import com.lxj.androidktx.core.animateVisible
import com.manchuan.tools.R
import com.manchuan.tools.activity.*
import com.manchuan.tools.activity.touch.MainActivity
import com.manchuan.tools.activity.video.*
import com.manchuan.tools.databinding.FragmentGalleryBinding
import com.manchuan.tools.extensions.colorPrimary
import com.manchuan.tools.interfaces.HardwarePresenter
import com.manchuan.tools.model.FlexTagModel
import com.manchuan.tools.utils.CommonFunctions
import com.manchuan.tools.utils.RootCmd.haveRoot
import com.manchuan.tools.utils.RootCmd.runRootCommand
import com.manchuan.tools.utils.SettingsLoader
import org.koin.android.ext.android.inject

class GalleryFragment : EngineFragment<FragmentGalleryBinding>(R.layout.fragment_gallery) {
    private var image_text: TextView? = null
    private var system_text: TextView? = null
    private var decode_text: TextView? = null
    private var other_text: TextView? = null
    private var web_text: TextView? = null
    private var card2: MaterialCardView? = null
    private var card3: MaterialCardView? = null
    private var card4: MaterialCardView? = null
    private var card5: MaterialCardView? = null
    private var card6: MaterialCardView? = null
    private var card7: MaterialCardView? = null
    private var states_t = 1
    private var states_tr = 1
    private var states_f = 1
    private var states_fi = 1
    private var states_s = 1
    private var states_se = 1
    private var states_o = 1
    private var states_n = 1
    private var img1: MaterialButton? = null
    private var img2: MaterialButton? = null
    private var img3: MaterialButton? = null
    private var img4: MaterialButton? = null
    private var img5: MaterialButton? = null
    private var img6: MaterialButton? = null
    private var img7: MaterialButton? = null
    private var img9: MaterialButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
    }


    private fun openImage(view: View?) {
        val objectAnimator = ObjectAnimator.ofFloat(view, "rotation", 90f, 0f)
        objectAnimator.duration = 300
        objectAnimator.start()
    }

    private fun closeImage(view: View?) {
        val objectAnimator = ObjectAnimator.ofFloat(view, "rotation", 0f, 90f)
        objectAnimator.duration = 300
        objectAnimator.start()
    }

    private fun closeNoAnimate(view: View?) {
        val objectAnimator = ObjectAnimator.ofFloat(view, "rotation", 0f, 90f)
        objectAnimator.duration = 0
        objectAnimator.start()
    }

    override fun initData() {

    }

    private val hardwareFloat: HardwarePresenter by inject()

    override fun initView() {
        binding.layout.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        image_text = binding.imageText
        system_text = binding.systemText
        decode_text = binding.decodeText
        other_text = binding.otherText
        web_text = binding.webText
        img1 = binding.img1
        img2 = binding.img2
        img3 = binding.img3
        img4 = binding.img4
        img5 = binding.img5
        img6 = binding.img6
        img7 = binding.img7
        img9 = binding.img9
        val card1: MaterialCardView = binding.card1
        card2 = binding.card2
        card3 = binding.card3
        card4 = binding.card4
        card5 = binding.card5
        card6 = binding.card6
        card7 = binding.card7
        //网页工具
        closeNoAnimate(img1)
        closeNoAnimate(img2)
        closeNoAnimate(img3)
        closeNoAnimate(img4)
        closeNoAnimate(img5)
        closeNoAnimate(img6)
        closeNoAnimate(img7)
        closeNoAnimate(img9)
        card1.setOnClickListener {
            if (states_o == 1) {
                binding.dailyLabelView.visibility = View.GONE
                openImage(img1)
                //img1.startAnimation(reverseAnimate);
                states_o = 2
            } else if (states_o == 2) {
                binding.dailyLabelView.visibility = View.VISIBLE
                closeImage(img1)
                //img1.startAnimation(concurAnimate);
                states_o = 1
            }
        }
        with(card2) {
            this?.setOnClickListener {
                if (states_t == 1) {
                    binding.imageLabelView.visibility = View.GONE
                    openImage(img2)
                    states_t = 2
                } else if (states_t == 2) {
                    binding.imageLabelView.visibility = View.VISIBLE
                    closeImage(img2)
                    states_t = 1
                }
            }
        }
        with(card3) {
            this?.setOnClickListener {
                if (states_tr == 1) {
                    binding.systemLabelView.visibility = View.GONE
                    openImage(img3)
                    //img1.startAnimation(reverseAnimate);
                    states_tr = 2
                } else if (states_tr == 2) {
                    binding.systemLabelView.visibility = View.VISIBLE
                    closeImage(img3)
                    //img1.startAnimation(concurAnimate);
                    states_tr = 1
                }
            }
        }
        with(card4) {
            this?.setOnClickListener {
                if (states_f == 1) {
                    binding.codeLabelView.visibility = View.GONE
                    openImage(img4)
                    //img1.startAnimation(reverseAnimate);
                    states_f = 2
                } else if (states_f == 2) {
                    binding.codeLabelView.visibility = View.VISIBLE
                    closeImage(img4)
                    //img1.startAnimation(concurAnimate);
                    states_f = 1
                }
            }
        }
        with(card5) {
            this?.setOnClickListener {
                when (states_fi) {
                    1 -> {
                        binding.otherLabelView.visibility = View.GONE
                        openImage(img5)
                        //img1.startAnimation(reverseAnimate);
                        states_fi = 2
                    }
                    2 -> {
                        binding.otherLabelView.visibility = View.VISIBLE
                        closeImage(img5)
                        //img1.startAnimation(concurAnimate);
                        states_fi = 1
                    }
                }
            }
        }
        with(card6) {
            this?.setOnClickListener {
                when (states_s) {
                    1 -> {
                        binding.webLabelView.visibility = View.GONE
                        openImage(img6)
                        states_s = 2
                    }
                    2 -> {
                        binding.webLabelView.visibility = View.VISIBLE
                        closeImage(img6)
                        states_s = 1
                    }
                }
            }
        }
        with(card7) {
            this?.setOnClickListener {
                when (states_se) {
                    1 -> {
                        binding.serverLabelView.animateGone()
                        openImage(img7)
                        states_se = 2
                    }
                    2 -> {
                        binding.serverLabelView.visibility = View.VISIBLE
                        closeImage(img7)
                        states_se = 1
                    }
                }
            }
        }
        with(binding.card9) {
            this.setOnClickListener {
                when (states_n) {
                    1 -> {
                        binding.audioLabelView.animateGone()
                        openImage(img9)
                        states_n = 2
                    }
                    2 -> {
                        binding.audioLabelView.animateVisible()
                        closeImage(img9)
                        states_n = 1
                    }
                }
            }
        }
        binding.dailyLabelView.layoutManager = FlexboxLayoutManager(activity)
        binding.imageLabelView.layoutManager = FlexboxLayoutManager(activity)
        binding.audioLabelView.layoutManager = FlexboxLayoutManager(activity)
        binding.systemLabelView.layoutManager = FlexboxLayoutManager(activity)
        binding.codeLabelView.layoutManager = FlexboxLayoutManager(activity)
        binding.otherLabelView.layoutManager = FlexboxLayoutManager(activity)
        binding.serverLabelView.layoutManager = FlexboxLayoutManager(activity)
        binding.webLabelView.layoutManager = FlexboxLayoutManager(activity)
        binding.bottomSummary.text = spannable {
            "共".color(colorPrimary())
            "${getDailyData().size + getImageData().size + getAudioData().size + getCodeData().size + getOtherData().size + getServerData().size + getWebData().size + getSystemData().size}".span {
                typeface(Typeface.defaultFromStyle(Typeface.BOLD))
                color(colorPrimary())
                absoluteSize(18, dp = true)
            }
            "个功能".color(colorPrimary())
        }
        binding.dailyLabelView.setup {
            addType<FlexTagModel>(R.layout.item_tv)
            onClick(R.id.chip) {
                getModel<FlexTagModel>().unit.invoke()
            }
        }.models = getDailyData()
        binding.imageLabelView.setup {
            addType<FlexTagModel>(R.layout.item_tv)
            onClick(R.id.chip) {
                getModel<FlexTagModel>().unit.invoke()
            }
        }.models = getImageData()
        binding.audioLabelView.setup {
            addType<FlexTagModel>(R.layout.item_tv)
            onClick(R.id.chip) {
                getModel<FlexTagModel>().unit.invoke()
            }
        }.models = getAudioData()
        binding.systemLabelView.setup {
            addType<FlexTagModel>(R.layout.item_tv)
            onClick(R.id.chip) {
                getModel<FlexTagModel>().unit.invoke()
            }
        }.models = getSystemData()
        binding.codeLabelView.setup {
            addType<FlexTagModel>(R.layout.item_tv)
            onClick(R.id.chip) {
                getModel<FlexTagModel>().unit.invoke()
            }
        }.models = getCodeData()
        binding.otherLabelView.setup {
            addType<FlexTagModel>(R.layout.item_tv)
            onClick(R.id.chip) {
                getModel<FlexTagModel>().unit.invoke()
            }
        }.models = getOtherData()
        binding.serverLabelView.setup {
            addType<FlexTagModel>(R.layout.item_tv)
            onClick(R.id.chip) {
                getModel<FlexTagModel>().unit.invoke()
            }
        }.models = getServerData()
        binding.webLabelView.setup {
            addType<FlexTagModel>(R.layout.item_tv)
            onClick(R.id.chip) {
                getModel<FlexTagModel>().unit.invoke()
            }
        }.models = getWebData()
    }

    private fun getDailyData(): List<FlexTagModel> {
        return listOf(FlexTagModel("刻度尺") {
            startActivity(Intent(context, RulerActivity::class.java))
        }, FlexTagModel("噪音测量") {
            startActivity(Intent(context, NoiseMeasurementActivity::class.java))
        }, FlexTagModel("Google翻译") {
            startActivity(Intent(context, TranslateActivity::class.java))
        }, FlexTagModel("强密码生成") {
            startActivity(Intent(context, CreatePasswordActivity::class.java))
        }, FlexTagModel("时间屏幕") {
            startActivity(Intent(context, TimeActivity::class.java))
        }, FlexTagModel("金属探测器") {
            startActivity(Intent(context, MetalDetectionActivity::class.java))
        }, FlexTagModel("历史上的今天") {
            startActivity(Intent(context, HistoryTodayActivity::class.java))
        }, FlexTagModel("影视解析") {
            startActivity(Intent(context, MoviesActivity::class.java))
        }, FlexTagModel("经纬度查询") {
            startActivity(Intent(context, LocationInquireActivity::class.java))
        }, FlexTagModel("QQ查绑") {
            startActivity(Intent(context, QQNumQueryActivity::class.java))
        }, FlexTagModel("滚动字幕") {
            startActivity(Intent(context, MarQueeActivity::class.java))
        }, FlexTagModel("步数修改") {
            startActivity(Intent(context, StepsActivity::class.java))
        }, FlexTagModel("聚合短视频解析") {
            startActivity(Intent(context, ShortVideoActivity::class.java))
        }, FlexTagModel("聚合图集解析") {
            startActivity(Intent(context, ImageParagraphActivity::class.java))
        }, FlexTagModel("Github加速下载") {
            startActivity(Intent(context, GithubDownloadActivity::class.java))
        }, FlexTagModel("拼音查询") {
            startActivity(Intent(context, PinyinActivity::class.java))
        }, FlexTagModel("蓝奏云解析") {
            startActivity(Intent(context, LanzouActivity::class.java))
        }, FlexTagModel("手机号归属地查询") {
            startActivity(Intent(context, TelephoneActivity::class.java))
        }, FlexTagModel("视频提取音频") {
            startActivity(Intent(context, AudioFormatActivity::class.java))
        }, FlexTagModel("M3U8视频下载") {
            startActivity(Intent(context, M3u8Activity::class.java))
        }, FlexTagModel("禅定模式") {
            startActivity(Intent(context, ZenModeActivity::class.java))
        })
    }

    private fun getImageData(): List<FlexTagModel> {
        return listOf(FlexTagModel("二维码生成") {
            startActivity(Intent(context, QRCodeActivity::class.java))
        }, FlexTagModel("LowPoly图片生成") {
            startActivity(Intent(context, LowPolyActivity::class.java))
        }, FlexTagModel("图片压缩") {
            startActivity(Intent(context, CompressActivity::class.java))
        }, FlexTagModel("图片灰白化") {
            startActivity(Intent(context, BlackGreyPhotoActivity::class.java))
        }, FlexTagModel("图片文字化") {
            startActivity(Intent(context, PhotoTextActivity::class.java))
        }, FlexTagModel("图片像素化") {
            startActivity(Intent(context, PicturePixelActivity::class.java))
        }, FlexTagModel("照片信息编辑") {
            startActivity(Intent(context, TelephoneActivity::class.java))
        }, FlexTagModel("图片转链接") {
            startActivity(Intent(context, ImageToUrlActivity::class.java))
        }, FlexTagModel("图片水印") {
            startActivity(Intent(context, PhotoWaterMarkActivity::class.java))
        }, FlexTagModel("图片素描") {
            startActivity(Intent(context, PhotoMiaoActivity::class.java))
        }, FlexTagModel("壁纸大全") {
            startActivity(Intent(context, WallpaperCategoryActivity::class.java))
        })
    }

    private fun getSystemData(): List<FlexTagModel> {
        return listOf(FlexTagModel("应用管理") {
            startActivity(Intent(context, AppManagerActivity::class.java))
        }, FlexTagModel("硬件信息悬浮窗") {
            hardwareFloat.loadContext(requireContext())
        }, FlexTagModel("网速悬浮窗") {
            hardwareFloat.loadNetworkFloat(requireContext())
        }, FlexTagModel("屏幕HDR检测") {
            startActivity(Intent(context, HDRCheckActivity::class.java))
        }, FlexTagModel("媒体库刷新") {
            startActivity(Intent(context, MediaScannerActivity::class.java))
        }, FlexTagModel("高级重启") {
            val items = arrayOf<CharSequence>("重启",
                "软重启",
                "重启系统用户界面",
                "安全模式",
                "快速重启",
                "Recovery",
                "高通9008",
                "FastBoot")
            BottomMenu.show(items).setTitle("高级重启").onMenuItemClickListener =
                OnMenuItemClickListener { _: BottomMenu?, _: CharSequence?, index: Int ->
                    when (items[index].toString()) {
                        "重启" -> if (haveRoot()) {
                            runRootCommand("reboot")
                        } else {
                            PopTip.show(R.string.no_root)
                        }
                        "安全模式" -> if (haveRoot()) {
                            runRootCommand("su -c setprop persist.sys.safemode 1\nreboot")
                        } else {
                            PopTip.show(R.string.no_root)
                        }
                        "快速重启" -> if (haveRoot()) {
                            runRootCommand("reboot -p")
                        } else {
                            PopTip.show(R.string.no_root)
                        }
                        "Recovery" -> if (haveRoot()) {
                            runRootCommand("reboot recovery")
                        } else {
                            PopTip.show(R.string.no_root)
                        }
                        "高通9008" -> if (haveRoot()) {
                            runRootCommand("reboot edl")
                        } else {
                            PopTip.show(R.string.no_root)
                        }
                        "FastBoot" -> if (haveRoot()) {
                            runRootCommand("reboot fastboot")
                        } else {
                            PopTip.show(R.string.no_root)
                        }
                        "软重启" -> if (haveRoot()) {
                            runRootCommand("setprop ctl.restart zygote")
                        } else {
                            PopTip.show(R.string.no_root)
                        }
                        "重启系统用户界面" -> if (haveRoot()) {
                            runRootCommand("pkill -f com.android.systemui")
                        } else {
                            PopTip.show(R.string.no_root)
                        }
                    }
                    false
                }
        }, FlexTagModel("MIUI快捷方式") {
            startActivity(Intent(context, MIUIShortCutActivity::class.java))
        }, FlexTagModel("电量伪装") {
            CommonFunctions().setDumpsysBattery(requireActivity())
        }, FlexTagModel("查看设备信息") {
            SettingsLoader.deviceInfoDialog(requireContext())
        }, FlexTagModel("多点触控测试") {
            startActivity(Intent(context, MainActivity::class.java))
        }, FlexTagModel("保存手机壁纸") {
            CommonFunctions().saveWallpaper(requireActivity())
        }, FlexTagModel("系统字体大小调节") {
            startActivity(Intent(context, FontScale::class.java))
        }, FlexTagModel("振动器") {
            CommonFunctions().vibrateFunction(requireActivity())
        }, FlexTagModel("备份字库") {
            startActivity(Intent(context, BackupWordLibrary::class.java))
        }, FlexTagModel("Magisk机型修改模块生成") {
            startActivity(Intent(context, CustomModelActivity::class.java))
        })
    }

    private fun getCodeData(): List<FlexTagModel> {
        return listOf(FlexTagModel("3DES加解密") {
            startActivity(Intent(context, ThDesActivity::class.java))
        }, FlexTagModel("易经64卦编解码") {

        }, FlexTagModel("AES加解密") {
            startActivity(Intent(context, AesCrypt::class.java))
        }, FlexTagModel("RC4加解密") {
            startActivity(Intent(context, RC4EDActivity::class.java))
        }, FlexTagModel("DES加解密") {
            startActivity(Intent(context, DesCrypt::class.java))
        }, FlexTagModel("Base64加解密") {
            startActivity(Intent(context, BaseConvertActivity::class.java))
        }, FlexTagModel("MD5加密") {
            startActivity(Intent(context, EncryptMD5Activity::class.java))
        }, FlexTagModel("SHA加密") {
            startActivity(Intent(context, SHACrypt::class.java))
        }, FlexTagModel("HmacMD5加密") {
            startActivity(Intent(context, HmacMD5Activity::class.java))
        }, FlexTagModel("HmacSHA加密") {
            startActivity(Intent(context, TelephoneActivity::class.java))
        }, FlexTagModel("特殊文本生成") {
            startActivity(Intent(context, SpecialTextActivity::class.java))
        })
    }

    private fun getOtherData(): List<FlexTagModel> {
        return listOf(FlexTagModel("QQ快捷跳转") {
            CommonFunctions().openQQFunctions(requireContext())
        }, FlexTagModel("随机颜色") {
            startActivity(Intent(context, RanDomColorActivity::class.java))
        }, FlexTagModel("随机一文") {
            startActivity(Intent(context, RanDomArticleActivity::class.java))
        }, FlexTagModel("支付宝到账音效") {
            CommonFunctions().alipayAudio(requireContext(), requireActivity())
        })
    }

    private fun getServerData(): List<FlexTagModel> {
        return listOf(FlexTagModel("Ping") {
            startActivity(Intent(context, PingActivity::class.java))
        }, FlexTagModel("网站ICP备案查询") {
            startActivity(Intent(context, BeianActivity::class.java))
        }, FlexTagModel("服务器信息查询") {
            startActivity(Intent(context, ServerInfoActivity::class.java))
        }, FlexTagModel("端口扫描") {
            startActivity(Intent(context, PortsScanActivity::class.java))
        }, FlexTagModel("LAN唤醒") {
            startActivity(Intent(context, WakeOnLanActivity::class.java))
        })
    }


    private fun getWebData(): List<FlexTagModel> {
        return listOf(FlexTagModel("MiKuTool") {
            startActivity<WebActivity>("url" to "https://tools.miku.ac/")
        }, FlexTagModel("SnapDrop局域网互传") {
            startActivity<WebActivity>("url" to "https://snapdrop.fengmuchuan.cn/")
        }, FlexTagModel("智能移除图片背景") {
            startActivity<WebActivity>("url" to "https://www.remove.bg/zh")
        }, FlexTagModel("PDF免费工具") {
            startActivity<WebActivity>("url" to "https://smallpdf.com")
        }, FlexTagModel("音频格式转换") {
            startActivity<WebActivity>("url" to "https://convertio.co/zh/audio-converter/")
        }, FlexTagModel("Json在线") {
            startActivity<WebActivity>("url" to "https://www.sojson.com/")
        }, FlexTagModel("程序员工具") {
            startActivity<WebActivity>("url" to "https://tool.lu/")
        }, FlexTagModel("Desmos函数图象") {
            startActivity<WebActivity>("url" to "https://www.desmos.com/calculator?lang=zh-CN")
        })
    }


    private fun getAudioData(): List<FlexTagModel> {
        return listOf(FlexTagModel("视频拼接") {

        }, FlexTagModel("音频拼接") {

        }, FlexTagModel("压缩视频") {
            startActivity<CompressVideoActivity>()
        }, FlexTagModel("压缩音频") {
            startActivity<CompressAudioActivity>()
        }, FlexTagModel("音频格式转换") {
            startActivity<FormatAudioActivity>()
        }, FlexTagModel("视频格式转换") {
            startActivity<FormatVideoActivity>()
        }, FlexTagModel("视频转GIF") {
            startActivity<TOGifActivity>()
        }, FlexTagModel("视频转图片") {
            startActivity<TOImageActivity>()
        }, FlexTagModel("视频静音") {
            startActivity<MuteVideoActivity>()
        }, FlexTagModel("视频添加水印") {

        }, FlexTagModel("设置视频屏幕高宽比") {
            startActivity<AspectActivity>()
        }, FlexTagModel("添加海报图像到音频文件") {

        }, FlexTagModel("音频转PCM") {

        }, FlexTagModel("视频解码YUV") {
            startActivity<YUVideoActivity>()
        }, FlexTagModel("拉流保存") {

        }, FlexTagModel("视频增加字幕") {
            startActivity<CrtActivity>()
        }, FlexTagModel("视频编码H264") {
            startActivity<H264Activity>()
        }, FlexTagModel("生成静音音频") {
            startActivity<MakeMuteActivity>()
        }, FlexTagModel("提取视频ES数据") {

        }, FlexTagModel("修改视频对比度") {
            startActivity<ContrastActivity>()
        }, FlexTagModel("MP4转换DVD") {

        }, FlexTagModel("修改视频亮度") {
            startActivity<BrightActivity>()
        }, FlexTagModel("视频降噪") {
            startActivity<DenoiseActivity>()
        }, FlexTagModel("音频淡出入") {

        }, FlexTagModel("视频倍速播放") {

        }, FlexTagModel("视频切片") {
            startActivity<HlsActivity>()
        }, FlexTagModel("M3U8切片转视频") {
            startActivity<M3U8Activity>()
        }, FlexTagModel("视频倒放") {
            startActivity<ReverseActivity>()
        })
    }

}