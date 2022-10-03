package com.manchuan.tools.activity.video

import ando.file.core.FileUri
import android.app.Activity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.coder.ffmpeg.annotation.MediaAttribute
import com.coder.ffmpeg.call.CommonCallBack
import com.coder.ffmpeg.jni.FFmpegCommand
import com.dylanc.longan.design.snackbar
import com.dylanc.longan.externalMoviesDirPath
import com.dylanc.longan.textString
import com.dylanc.longan.toUri
import com.dylanc.longan.toast
import com.github.dhaval2404.imagepicker.ImagePicker
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.R
import com.manchuan.tools.databinding.ActivityFormatVideoBinding
import com.mcxiaoke.koi.ext.mediaScan
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class FormatVideoActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityFormatVideoBinding.inflate(layoutInflater)
    }

    private val formats = arrayOf(
        "MP4",
        "WMV",
        "MPEG",
        "M4V",
        "MOV",
        "FLV",
        "AVI",
        "MKV",
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val formats = ArrayAdapter(this, R.layout.cat_exposed_dropdown_popup_item, formats)
        binding.formats.setAdapter(formats)
        binding.toolbarLayout.toolbar.apply {
            title = "视频格式转换"
            setNavigationOnClickListener {
                finish()
            }
        }
        binding.colorPicker.setOnClickListener {
            ImagePicker.with(this).galleryOnly().galleryMimeTypes(arrayOf("video/*"))
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        }
        binding.create.setOnClickListener {
            if (path.isEmpty()) {
                snackbar("未选择文件或文件路径为空")
            } else {
                formatVideo()
            }
        }
    }


    private var path = ""


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


    @OptIn(DelicateCoroutinesApi::class)
    private fun formatVideo() {
        val targetPath =
            externalMoviesDirPath.toString() + File.separator + "${System.currentTimeMillis()}${File.separator}"
        if (!File(targetPath).exists()) {
            File(targetPath).mkdirs()
        }
        GlobalScope.launch {
            val commands = "ffmpeg -i $path $targetPath${
                File(path).name
            }.${binding.formats.textString.lowercase()}"
            FFmpegCommand.runCmd(arrayOf(commands), callback(targetPath))
        }
    }


    private fun callback(targetPath: String?): CommonCallBack {
        return object : CommonCallBack() {
            override fun onStart() {
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

    private val commandResult = StringBuilder()


}