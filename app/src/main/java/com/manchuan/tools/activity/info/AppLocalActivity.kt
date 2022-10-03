package com.manchuan.tools.activity.info

import android.graphics.Typeface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.coder.ffmpeg.annotation.CodecAttribute
import com.coder.ffmpeg.jni.FFmpegCommand
import com.gyf.immersionbar.ktx.immersionBar
import com.itxca.spannablex.spannable
import com.manchuan.tools.databinding.ActivityAppLocalBinding
import com.manchuan.tools.extensions.colorPrimary
import com.manchuan.tools.utils.UiUtils

class AppLocalActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityAppLocalBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        immersionBar {
            titleBar(binding.toolbar)
            transparentBar()
            statusBarDarkFont(!UiUtils.isDarkMode())
        }
        binding.info.text = spannable {
            "FFmpeg信息:".span {
                typeface(Typeface.DEFAULT_BOLD)
                absoluteSize(18)
                color(colorPrimary())
            }
            newline()
            "支持解码格式:".text()
            FFmpegCommand.getSupportCodec(CodecAttribute.DECODE).text()
            newline(2)
            "支持编码格式:".text()
            FFmpegCommand.getSupportCodec(CodecAttribute.ENCODE).text()
            newline(2)
            "支持的音频编码格式:".text()
            FFmpegCommand.getSupportCodec(CodecAttribute.ENCODE_AUDIO).text()
            newline(2)
            "支持的音频解码格式:".text()
            FFmpegCommand.getSupportCodec(CodecAttribute.DECODE_AUDIO).text()
            newline(2)
            "支持的视频编码格式:".text()
            FFmpegCommand.getSupportCodec(CodecAttribute.ENCODE_VIDEO).text()
            newline(2)
            "支持的视频解码格式:".text()
            FFmpegCommand.getSupportCodec(CodecAttribute.DECODE_VIDEO).text()
        }
    }
}