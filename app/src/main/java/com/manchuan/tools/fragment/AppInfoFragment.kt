package com.manchuan.tools.fragment

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.AppUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.kongzue.dialogx.dialogs.PopTip
import com.manchuan.tools.R
import com.manchuan.tools.activity.AppInformationActivity
import com.manchuan.tools.base.BaseAlertDialogBuilder
import java.io.DataOutputStream

class AppInfoFragment : Fragment() {
    private var AppType: String? = null
    private var versionName = "系统应用"

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val root = inflater.inflate(R.layout.fragment_app_info, container, false)
        initView(root)
        AppType = if (AppUtils.isAppSystem(AppInformationActivity.package_name)) {
            "系统应用"
        } else {
            "用户应用"
        }
        versionName = try {
            "版本:" + AppUtils.getAppVersionName(AppInformationActivity.package_name)
        } catch (e: Exception) {
            "版本:null"
        }
        try {
            val applicationInfo = requireActivity().packageManager.getApplicationInfo(
                AppInformationActivity.package_name!!, 0
            )
            data_folder!!.text = applicationInfo.dataDir
            resources_folder!!.text = applicationInfo.sourceDir
            jni_folder!!.text = applicationInfo.nativeLibraryDir
            min_sdk!!.text = "Android SDK " + applicationInfo.minSdkVersion
            target_sdk!!.text = "Android SDK " + applicationInfo.targetSdkVersion
        } catch (e: PackageManager.NameNotFoundException) {
        }
        Glide.with(requireActivity()).load(AppUtils.getAppIcon(AppInformationActivity.package_name))
            .skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(
                app_icon!!
            )
        app_name!!.text = AppInformationActivity.app_name
        app_pack!!.text = AppInformationActivity.package_name
        app_version!!.text = versionName
        val appType = Chip(requireActivity())
        val appStatus = Chip(requireActivity())
        appType.text = AppType
        val status = if (isEnabled(AppInformationActivity.package_name)) {
            "启用"
        } else {
            "停用"
        }
        appStatus.text = status
        appStatus.setOnClickListener {
            when (status) {
                "Enable" -> {
                    BaseAlertDialogBuilder(requireContext())
                        .setTitle("提示")
                        .setMessage("确定要禁用吗？")
                        .setPositiveButton("禁用") { dialogInterface: DialogInterface, i: Int ->
                            AppInformationActivity.package_name?.let { it1 ->
                                setAppState(
                                    it1,
                                    false
                                )
                            }
                            PopTip.show("已禁用")
                            appStatus.text = status
                        }
                        .show()
                }
                "Disable" -> {
                    AppInformationActivity.package_name?.let { it1 ->
                        setAppState(
                            it1,
                            enabled = true
                        )
                    }
                    PopTip.show("已启用")
                    appStatus.text = status
                }
            }
        }
        mChipGroup?.addView(appType)
        mChipGroup?.addView(appStatus)
        return root
    }

    fun run(command: String) {
        try {
            val ps = Runtime.getRuntime().exec("su")
            val writer = DataOutputStream(ps.outputStream)
            writer.writeBytes("$command\nexit\n")
            writer.flush()
            ps.waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setAppState(packageName: String, enabled: Boolean) {
        var command = "pm "
        command += if (enabled) {
            "enable $packageName"
        } else {
            "disable $packageName"
        }
        this.run(command)
    }

    fun isEnabled(packageName: String?): Boolean {
        var state = false
        try {
            state = requireActivity().packageManager.getPackageInfo(
                packageName!!,
                0
            ).applicationInfo.enabled
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return state
    }

    private var app_icon: ImageView? = null
    private var app_name: TextView? = null
    private var app_pack: TextView? = null
    private var app_version: TextView? = null
    private var mChipGroup: ChipGroup? = null
    private fun initView(view: View) {
        app_icon = view.findViewById(R.id.app_icon)
        app_name = view.findViewById(R.id.app_name)
        app_pack = view.findViewById(R.id.app_pack)
        app_version = view.findViewById(R.id.app_version)
        mChipGroup = view.findViewById(R.id.chipGroup)
        data_folder = view.findViewById(R.id.data_folder)
        jni_folder = view.findViewById(R.id.jni_folder)
        resources_folder = view.findViewById(R.id.resources_folder)
        min_sdk = view.findViewById(R.id.min_sdk)
        target_sdk = view.findViewById(R.id.target_sdk)
    }

    private var data_folder: TextView? = null
    private var jni_folder: TextView? = null
    private var resources_folder: TextView? = null
    private var min_sdk: TextView? = null
    private var target_sdk: TextView? = null

    companion object {
        fun isRunningApp(context: Context?, packageName: String?): Boolean {
            var isAppRunning = false
            val am = context!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val list = am.getRunningTasks(100)
            for (info in list) {
                if (info.topActivity!!.packageName == packageName && info.baseActivity!!.packageName == packageName) {
                    isAppRunning = true
                    // find it, break
                    break
                }
            }
            return isAppRunning
        }
    }
}