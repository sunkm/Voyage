package com.manchuan.tools.activity.video

import ando.file.core.FileUri
import android.app.Activity
import android.os.Bundle
import android.os.SystemClock
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.coder.ffmpeg.annotation.MediaAttribute
import com.coder.ffmpeg.call.CommonCallBack
import com.coder.ffmpeg.jni.FFmpegCommand
import com.coder.ffmpeg.utils.FFmpegUtils
import com.dylanc.longan.design.snackbar
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.toUri
import com.dylanc.longan.toast
import com.github.dhaval2404.imagepicker.ImagePicker
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.databinding.ActivityHlsConvertBinding
import com.manchuan.tools.extensions.externalMoviesDirPath
import com.manchuan.tools.utils.UiUtils
import com.mcxiaoke.koi.ext.mediaScan
import com.mcxiaoke.koi.log.loge
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class M3U8Activity : AppCompatActivity() {

    private val binding by lazy {
        ActivityHlsConvertBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        immerseStatusBar(!UiUtils.isDarkMode())
        binding.toolbarLayout.toolbar.apply {
            setNavigationOnClickListener {
                finish()
            }
            title = "M3U8切片转视频"
        }
        binding.colorPicker.setOnClickListener {
            ImagePicker.with(this).galleryOnly().galleryMimeTypes(arrayOf("audio/x-mpegurl"))
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        }
        binding.create.setOnClickListener {
            if (path.isEmpty()) {
                snackbar("未选择文件或文件路径为空")
            } else {
                hls2Video()
            }
        }
    }

    private fun hls2Video() {
        val targetPath =
            externalMoviesDirPath.toString() + File.separator + "${SystemClock.elapsedRealtime()}.mp4"
        GlobalScope.launch {
            FFmpegCommand.runCmd(FFmpegUtils.hls2Video(path, targetPath),
                callback(targetPath))
        }
    }

    private val commandResult = StringBuilder()

    private fun callback(targetPath: String?): CommonCallBack {
        return object : CommonCallBack() {
            override fun onStart() {
                loge("视频降噪", "已开始")
                runOnUiThread {
                    WaitDialog.show("别急,正在准备中...")
                }
            }

            override fun onComplete() {
                Timber.tag("FFmpegCmd").d("onComplete")
                runOnUiThread {
                    TipDialog.show("处理完成", WaitDialog.TYPE.SUCCESS)
                    commandResult.append("处理完成,已保存到:$targetPath")
                    binding.autocomplete1.setText(commandResult)
                    mediaScan(File(targetPath).toUri())
                }
                commandResult.clear()
            }

            override fun onCancel() {
                runOnUiThread {
                    toast("用户取消")
                }
                Timber.tag("FFmpegCmd").d("Cancel")
            }

            override fun onProgress(progress: Int, pts: Long) {
                val duration: Int? = FFmpegCommand.getMediaInfo(path, MediaAttribute.DURATION)
                val progressN = pts / duration!!
                Timber.tag("FFmpegCmd").d("%s", progress.toString())
                commandResult.append("\n已处理:$progress%")
                runOnUiThread {
                    WaitDialog.show("已处理 $progress%", progressN.toFloat())
                }
            }

            override fun onError(errorCode: Int, errorMsg: String?) {
                Timber.tag("FFmpegCmd").e("%s", errorMsg)
                runOnUiThread {
                    TipDialog.show("处理失败", WaitDialog.TYPE.ERROR)
                }
            }
        }
    }


    private var path: String = ""
    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data
            when (resultCode) {
                Activity.RESULT_OK -> {
                    path = FileUri.getPathByUri(data?.data).toString()
                    binding.colorString.text = "已选择"
                }
                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                }
                else -> {
                    snackbar("选择已取消")
                }
            }
        }


}