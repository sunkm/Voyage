package com.manchuan.tools.utils

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnticipateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.DeviceUtils
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogxmaterialyou.style.MaterialYouStyle
import com.manchuan.tools.R
import com.manchuan.tools.base.BaseAlertDialogBuilder
import com.manchuan.tools.extensions.userAgent
import com.manchuan.tools.user.idVerify
import com.manchuan.tools.utils.SystemProperties.getProperty
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import rikka.material.app.DayNightDelegate
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException

@SuppressLint("StaticFieldLeak")
object SettingsLoader {
    private lateinit var mContext: Context

    @JvmStatic
    fun init(context: Context) {
        mContext = context
        UiModeUtils.initialize(context)
    }

    /**
     * 获取本机蓝牙地址, 确保蓝牙已开启，关闭状态无法获取到
     */

    val interpolator: Interpolator
        get() {
            val interpolator = when (PreferenceManager.getDefaultSharedPreferences(mContext)
                .getString("time_style", "linear")) {
                "linear" -> LinearInterpolator()
                "reduce" -> DecelerateInterpolator()
                "speed_down" -> AccelerateDecelerateInterpolator()
                "speed" -> AccelerateInterpolator()
                "stage" -> BounceInterpolator()
                "back_forth" -> AnticipateInterpolator()
                else -> {
                    LinearInterpolator()
                }
            }
            return interpolator
        }


    val nightMode: Int?
        get() {
            var mode: Int? = null
            val theme_pres = mContext.let { PreferenceManager.getDefaultSharedPreferences(it) }
            if (theme_pres != null) {
                when {
                    theme_pres.getString("theme_style", "follow_system") == "follow_system" -> {
                        mode = DayNightDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    }
                    theme_pres.getString("theme_style", "follow_system") == "day" -> {
                        mode = DayNightDelegate.MODE_NIGHT_NO
                    }
                    theme_pres.getString("theme_style", "follow_system") == "night" -> {
                        mode = DayNightDelegate.MODE_NIGHT_YES
                    }
                }
            }
            return mode
        }

    @JvmStatic
    fun loadDialogConfig(context: Context) {
        DialogX.init(context)
        DialogX.DEBUGMODE = false
        DialogX.globalStyle = MaterialYouStyle()
        DialogX.autoRunOnUIThread = true
        DialogX.globalTheme = DialogX.THEME.AUTO
        DialogX.useHaptic = true
        DialogX.autoShowInputKeyboard = true
        DialogX.onlyOnePopTip = false
        DialogX.cancelable = true
    }

    fun loadSettings() {
        try {
            val theme_pres = mContext.let { PreferenceManager.getDefaultSharedPreferences(it) }
            if (theme_pres != null) {
                when {
                    theme_pres.getString("theme_style", "") == "follow_system" -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    }
                    theme_pres.getString("theme_style", "") == "day" -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }
                    theme_pres.getString("theme_style", "") == "night" -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }
                }
            }
        } catch (ignored: Exception) {
        }
    }

    @JvmStatic
    val diskCacheMethod: DiskCacheStrategy
        get() {
            var method = DiskCacheStrategy.AUTOMATIC
            runCatching {
                val cache_pres = mContext.let { PreferenceManager.getDefaultSharedPreferences(it) }
                if (cache_pres != null) {
                    when {
                        cache_pres.getString("glide_cache", "auto").equals("auto") -> {
                            method = DiskCacheStrategy.AUTOMATIC
                        }
                        cache_pres.getString("glide_cache", "auto").equals("not_cache") -> {
                            method = DiskCacheStrategy.NONE
                        }
                        cache_pres.getString("glide_cache", "auto").equals("original_cache") -> {
                            method = DiskCacheStrategy.RESOURCE
                        }
                        cache_pres.getString("glide_cache", "auto").equals("convert_cache") -> {
                            method = DiskCacheStrategy.DATA
                        }
                        cache_pres.getString("glide_cache", "auto").equals("all_cache") -> {
                            method = DiskCacheStrategy.ALL
                        }
                    }
                }
            }
            return method
        }

    @SuppressLint("LogNotTimber")
    fun loadAnalytic(context: Application) {
        val analytics = PreferenceManager.getDefaultSharedPreferences(context)
        if (analytics != null) {
            if (analytics.getBoolean("none_analytic", true)) {
                AppCenter.start(context,
                    "f4fcab92-3554-482b-9115-218580ae9e51",
                    Analytics::class.java,
                    Crashes::class.java)
                Log.e("AppCenter已加载", "已加载")
            }
        }
    }

    fun deviceInfoDialog(context: Context?) {
        if (context != null) {
            val cpuArchitectureCheck = CPUArchitecture.check()
            val cpuArchDesc = when (cpuArchitectureCheck?.cpuArch) {
                ABI.ARM32 -> R.string.checker_cpu_architecture_arm32_subtitle
                ABI.ARM32_BINDER64 -> R.string.checker_cpu_architecture_arm32_binder64_subtitle
                ABI.ARM64 -> R.string.checker_cpu_architecture_arm64_subtitle
                ABI.X86 -> R.string.checker_cpu_architecture_x86_subtitle
                ABI.X86_64 -> R.string.checker_cpu_architecture_x86_64_subtitle
                else -> {
                    "未知"
                }
            }
            val deviceInfo = arrayOf<CharSequence>("设备名称:" + BuildUtils.name,
                "设备厂商:" + DeviceUtils.getManufacturer(),
                "产品:" + BuildUtils.product,
                "主板:" + BuildUtils.board,
                "引导加载程序:" + BuildUtils.bootLoader,
                "型号:" + BuildUtils.model,
                "HW版本:" + getProperty("ro.boot.hwversion"),
                "ADB启用:" + when (DeviceUtils.isAdbEnabled()) {
                    true -> {
                        "是"
                    }
                    false -> {
                        "否"
                    }
                },
                "硬件识别码:" + BuildUtils.fingerprint,
                "构建时间:" + getProperty("ro.vendor.build.date"),
                "OAID:$idVerify",
                "ID:" + BuildUtils.id,
                "SDK_INT:" + BuildUtils.sdkVersion,
                "基带:" + getProperty("gsm.version.baseband"),
                "5G组网模式:" + when (getProperty("ro.vendor.radio.5g")) {
                    "3" -> {
                        "NSA+SA组网模式"
                    }
                    "2" -> {
                        "SA组网模式"
                    }
                    "1" -> {
                        "NSA组网模式"
                    }
                    else -> {
                        "未知"
                    }
                },
                "硬件:" + when (BuildUtils.hardware == "mt6891") {
                    true -> {
                        "天玑1100"
                    }
                    false -> {
                        BuildUtils.hardware
                    }
                },
                "A/B 插槽:" + getProperty("ro.boot.slot"),
                "Project Treble:" + when (getProperty("ro.treble.enabled") != "true") {
                    true -> {
                        "否"
                    }
                    false -> {
                        "是"
                    }
                },
                "System As Root:" + when (SystemAsRoot.check()) {
                    true -> {
                        "是"
                    }
                    false -> {
                        "否"
                    }
                    else -> {
                        "未知"
                    }
                },
                "无缝系统更新:" + when (AB.check()?.isVirtual) {
                    true -> {
                        context.getString(R.string.checker_ab_supported_virtual_supporting_text)
                    }
                    false -> {
                        context.getString(R.string.checker_ab_supported_supporting_text)
                    }
                    else -> {
                        "您的设备不支持无缝更新"
                    }
                },
                "CPU架构:" + context.getString(R.string.checker_cpu_architecture_known_supporting_text,
                    context.getString(cpuArchDesc as Int)),
                "WebView Agent:" + context.userAgent())
            BaseAlertDialogBuilder(context).setTitle("设备详细信息")
                .setItems(deviceInfo) { dialog, which ->
                    ClipboardUtils.copyText(deviceInfo[which])
                }.setPositiveButton("确定", null).create().show()
        }
    }

    data class ArchitectureResult(
        val cpuArch: ABI,
    )

    enum class ABI {
        ARM32, ARM32_BINDER64, ARM64, X86, X86_64
    }

    object CPUArchitecture {

        fun check(): ArchitectureResult? {

            /**
             * 初始化软件支持的 ABI，它是首选 ABI，在 Build.SUPPORTED_ABIS 中首先列出。
             */
            val supportedABIs = Build.SUPPORTED_ABIS
            val cpuArch = supportedABIs.first()

            /**
             * 通过操作系统支持的结构集确定位数。
             */
            val softwareResult = when {
                cpuArch.contains("arm64-v8a") -> ABI.ARM64
                cpuArch.contains("armeabi-v7a") -> ABI.ARM32
                cpuArch.contains("x86_64") -> ABI.X86_64
                cpuArch.contains("x86") -> ABI.X86
                else -> null
            }

            /**
             * 通过 CPU 架构确定位数。
             */
            var hardwareResult: String? = null
            try {
                val br = BufferedReader(FileReader("/proc/cpuinfo"))
                var line: String?
                var stop = false
                while (br.readLine().also { line = it } != null && !stop) {
                    line?.let {
                        val data = it.split(":")

                        if (data.size > 1) {
                            val key = data[0].trim().replace(" ", "_")
                            if (key.equals("cpu_architecture", true)) {
                                hardwareResult = data[1].trim()
                                stop = true
                            }
                        }

                    }
                }
            } catch (t: Throwable) {
                hardwareResult = null
            }

            /**
             * 如果操作系统仅支持 32 位但硬件支持 64 位，则返回 ARM32_BINDER64，否则返回软件的结果。
             */
            return if (softwareResult == ABI.ARM32 && (hardwareResult == "8" || hardwareResult.equals(
                    "aarch64",
                    true))
            ) {
                ArchitectureResult(ABI.ARM32_BINDER64)
            } else {
                softwareResult?.let { ArchitectureResult(it) }
            }

        }

    }

    data class ABResult(
        val isVirtual: Boolean,
    )

    object AB {

        fun check(): ABResult? {

            /**
             * 检查设备是否支持虚拟 AB 分区
             */
            if (getProperty("ro.virtual_ab.enabled") == "true" && getProperty("ro.virtual_ab.retrofit") == "false") {
                return ABResult(true)
            }

            /**
             * 检查设备是否支持传统的 AB 分区
             */
            if (!getProperty("ro.boot.slot_suffix").isNullOrBlank() || getProperty("ro.build.ab_update") == "true") {
                return ABResult(false)
            }

            /**
             * 如果设备根本不支持 AB 分区，则返回 null
             */
            return null
        }

    }

    data class MountPoint(
        val device: String,
        val mountPoint: String,
        val fileSystem: String,
        val prop: String,
        val dummy1: String,
        val dummy2: String,
    )

    object SystemAsRoot {

        fun check(): Boolean? {

            val mountsPoints = ArrayList<MountPoint>()
            val br: BufferedReader

            /**
             * 读取挂载的分区并检查系统是否以 root 身份挂载。如果不确定，则返回 null。
             */
            try {
                br = BufferedReader(FileReader("/proc/mounts"))
                var line: String?
                while (br.readLine().also { line = it } != null) {
                    line?.let {
                        val mountDetails = it.split(" ").toTypedArray()
                        if (mountDetails.size == 6) {
                            val mountPoint = MountPoint(mountDetails[0],
                                mountDetails[1],
                                mountDetails[2],
                                mountDetails[3],
                                mountDetails[4],
                                mountDetails[5])
                            mountsPoints.add(mountPoint)
                        }
                    }
                }

                /**
                 * 检查系统是否有与其关联的设备块，它不是临时的
                 */
                val systemOnBlock =
                    mountsPoints.none { it.device != "none" && it.mountPoint == "/system" && it.fileSystem != "tmpfs" }

                /**
                 * 检查设备符号链接是否安装在 root 上
                 */
                val deviceMountedOnRoot =
                    mountsPoints.any { it.device == "/dev/root" && it.mountPoint == "/" }

                /**
                 * 检查系统根目录上是否挂载了非临时块
                 */
                val systemOnRoot =
                    mountsPoints.any { it.mountPoint == "/system_root" && it.fileSystem != "tmpfs" }

                return systemOnBlock || deviceMountedOnRoot || systemOnRoot

            } catch (e: IOException) {
                return null
            } catch (e: FileNotFoundException) {
                return null
            }

        }

    }

}