package com.manchuan.tools.activity

import ando.file.core.FileUri
import ando.file.selector.FileSelector
import android.app.Activity
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.drake.statusbar.immersive
import com.dylanc.longan.design.snackbar
import com.github.dhaval2404.imagepicker.ImagePicker
import com.manchuan.tools.databinding.ActivityAudioFormatBinding
import com.manchuan.tools.utils.VideoProcessorUtils
import java.io.File

class AudioFormatActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityAudioFormatBinding.inflate(layoutInflater)
    }
    private var path: String = ""
    private lateinit var fileSelector: FileSelector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        immersive(binding.toolbar)
        supportActionBar?.apply {
            title = "视频提取音频"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.colorPicker.setOnClickListener {
            ImagePicker.with(this)     //Final image size will be less than 1 MB(Optional)
                .galleryOnly()
                .galleryMimeTypes(arrayOf("video/*"))
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        }
        binding.create.setOnClickListener {
            if (path.isEmpty()) {
                snackbar("未选择文件或文件路径为空")
            } else {
                val fileName = SystemClock.elapsedRealtime().toString() + ".mp3"
                VideoProcessorUtils.splitAudioFile(path,
                    Environment.getExternalStorageDirectory().absolutePath + File.separator + Environment.DIRECTORY_MUSIC + File.separator + fileName)
                snackbar("已保存到手机存储空间/Music")
            }
        }
    }

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