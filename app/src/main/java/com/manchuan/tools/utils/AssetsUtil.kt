package com.manchuan.tools.utils

import android.content.Context
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception

object AssetsUtil {
    /**
     * 拷贝asset文件到指定路径，可变更文件名
     *
     * @param context   context
     * @param assetName asset文件
     * @param savePath  目标路径
     * @param saveName  目标文件名
     */
    fun copyFileFromAssets(
        context: Context,
        assetName: String,
        savePath: String,
        saveName: String
    ) {
        // 若目标文件夹不存在，则创建
        val dir = File(savePath)
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                Timber.d("mkdir error: %s", savePath)
                return
            }
        }

        // 拷贝文件
        val filename = "$savePath/$saveName"
        val file = File(filename)
        if (!file.exists()) {
            try {
                val inStream = context.assets.open(assetName)
                val fileOutputStream = FileOutputStream(filename)
                var byteread: Int
                val buffer = ByteArray(1024)
                while (inStream.read(buffer).also { byteread = it } != -1) {
                    fileOutputStream.write(buffer, 0, byteread)
                }
                fileOutputStream.flush()
                inStream.close()
                fileOutputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            Timber.d("[copyFileFromAssets] copy asset file: $assetName to : $filename")
        } else {
            Timber.d("[copyFileFromAssets] file is exist: %s", filename)
        }
    }

    /**
     * 拷贝asset目录下所有文件到指定路径
     *
     * @param context    context
     * @param assetsPath asset目录
     * @param savePath   目标目录
     */
    fun copyFilesFromAssets(context: Context, assetsPath: String, savePath: String) {
        try {
            // 获取assets指定目录下的所有文件
            val fileList = context.assets.list(assetsPath)
            if (fileList != null && fileList.isNotEmpty()) {
                val file = File(savePath)
                // 如果目标路径文件夹不存在，则创建
                if (!file.exists()) {
                    if (!file.mkdirs()) {
                        Timber.d("mkdir error: %s", savePath)
                        return
                    }
                }
                for (fileName in fileList) {
                    copyFileFromAssets(context, "$assetsPath/$fileName", savePath, fileName)
                }
            }
        } catch (e: Exception) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }
}