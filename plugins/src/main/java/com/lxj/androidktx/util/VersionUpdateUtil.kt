package com.lxj.androidktx.util

import android.content.Context
import com.blankj.utilcode.util.*
import com.lxj.androidktx.core.md5
import com.lxj.androidktx.core.putString
import com.lxj.androidktx.core.sp
import com.lxj.androidktx.core.toJson
import com.lxj.androidktx.okhttp.*
import com.lxj.androidktx.popup.VersionUpdatePopup
import com.lxj.xpopup.XPopup
import java.io.File
import java.io.IOException


data class CommonUpdateInfo(
        var download_url: String? = null,
        var version_name: String? = null,
        var package_name: String? = null,
        var update_info: String? = null,
        var force_update: Boolean? = false
)

/**
 * 版本更新工具，功能有2个：
 * 1. 弹出版本更新提示的弹窗，如果对UI有要求，可以自己实现弹窗，然后调用第2个方法
 * 2. 下载并安装Apk
 */
object VersionUpdateUtil {
    const val cacheKey = "_version_update_download_apk_"
    private fun showDefaultUpdateUI(context: Context, updateData: CommonUpdateInfo, path: String) {
        XPopup.Builder(context)
                .dismissOnBackPressed(updateData.force_update)
                .dismissOnTouchOutside(updateData.force_update)
                .asCustom(VersionUpdatePopup(context = context, updateInfo = updateData, onOkClick = {
                    installApk(path)
                }))
                .show()
    }

    /**
     * 安装apk
     */
    fun installApk(path: String){
        //删除缓存
        sp().putString(cacheKey, "")
        AppUtils.installApp(path)
    }

    /**
     * 下载并安装Apk，会自动检测是否有缓存过的apk文件，如果有则直接提示安装。如果没有则进行下载，一旦安装就删除缓存的文件路径
     * @param updateData 更新相关信息
     * @param onShowInstallUI 默认会有个更新的提示，如果想自己实现UI，则实现这个监听器
     * @param useCache 是否使用缓存的apk文件,使用下载url作为缓存的key
     * @param installWhenDownload 下载完就进入安装
     */
    fun downloadAndInstallApk(context: Context, updateData: CommonUpdateInfo, onShowInstallUI: ((apkPath: String) -> Unit)? = null,
        useCache: Boolean = true, onDownloadProgress: ((Int)->Unit)? = null, installWhenDownload : Boolean = false) {
        //检测是否有缓存的apk路径，如果有说明已经下载过了
        val filename = "${updateData.download_url!!.md5()}.apk"
        val file = File("${DirManager.downloadDir}/${filename}")
        val cacheApkPath = sp().getString(cacheKey, "")
        if (cacheApkPath!!.isNotEmpty() && FileUtils.isFileExists(cacheApkPath) && cacheApkPath==file.absolutePath && useCache) {
            LogUtils.e("新版本Apk已存在，无需下载，路径：$cacheApkPath")
            if(installWhenDownload){
                installApk(cacheApkPath)
            }else{
                if (onShowInstallUI != null) {
                    onShowInstallUI(cacheApkPath)
                } else {
                    showDefaultUpdateUI(context, updateData, cacheApkPath)
                }
            }
            return
        }
        LogUtils.d("开始下载新版本: ${updateData.toJson()}")
        if (updateData.download_url.isNullOrEmpty()) {
            return
        }
        FileUtils.createFileByDeleteOldFile(file)
        updateData.download_url!!.http(baseUrlTag = "")
            .savePath(file.absolutePath)
            .downloadListener(onProgress = {
                onDownloadProgress?.invoke(it?.percent?:0)
                LogUtils.d("新版本下载进度：${ it?.percent}")
            })
            .get<File>(object : HttpCallback<File> {
                override fun onSuccess(t: File) {
                    LogUtils.e("新版本下载成功，路径为：${file.absolutePath}")
                    //缓存路径
                    sp().putString(cacheKey, t.absolutePath)
                    if(installWhenDownload){
                        installApk(t.absolutePath)
                    }else{
                        if (onShowInstallUI != null) {
                            onShowInstallUI(t.absolutePath)
                        } else {
                            showDefaultUpdateUI(context, updateData, t.absolutePath)
                        }
                    }
                }
                override fun onFail(e: IOException) {
                    super.onFail(e)
                    LogUtils.e("新版本下载失败：${e.localizedMessage}")
                }
            })
    }

}