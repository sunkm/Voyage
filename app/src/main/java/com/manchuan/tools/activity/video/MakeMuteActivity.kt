package com.manchuan.tools.activity.video

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.coder.ffmpeg.annotation.MediaAttribute
import com.coder.ffmpeg.call.CommonCallBack
import com.coder.ffmpeg.jni.FFmpegCommand
import com.coder.ffmpeg.utils.FFmpegUtils
import com.dylanc.longan.doOnClick
import com.dylanc.longan.toUri
import com.dylanc.longan.toast
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.databinding.ActivityMakeMuteBinding
import com.manchuan.tools.extensions.externalAudiosDirPath
import com.mcxiaoke.koi.ext.mediaScan
import com.mcxiaoke.koi.log.loge
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class MakeMuteActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMakeMuteBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.toolbarLayout.toolbar.apply {
            setNavigationOnClickListener {
                finish()
            }
            title = "生成静音音频"
        }
        binding.create.doOnClick {
            makeMuteAudio()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun makeMuteAudio() {
        val targetPath =
            externalAudiosDirPath.toString() + File.separator + "${System.currentTimeMillis()}.mp3"
        GlobalScope.launch {
            FFmpegCommand.runCmd(FFmpegUtils.makeMuteAudio(targetPath),
                callback(targetPath))
        }
    }


    private var path: String = ""

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
                    targetPath?.let { File(it).toUri() }?.let { mediaScan(it) }
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
                var progressN = pts / duration!!
                Timber.tag("FFmpegCmd").d("%s", progress.toString())
                commandResult.append("\n已处理:$progress%")
                runOnUiThread {
                    WaitDialog.show("已处理 $progress%", progress.toFloat())
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

}