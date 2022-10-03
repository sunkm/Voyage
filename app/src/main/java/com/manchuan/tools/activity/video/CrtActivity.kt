package com.manchuan.tools.activity.video

import ando.file.core.FileUri
import android.app.Activity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dylanc.longan.design.snackbar
import com.github.dhaval2404.imagepicker.ImagePicker
import com.manchuan.tools.R
import com.manchuan.tools.databinding.ActivityCrtBinding

class CrtActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityCrtBinding.inflate(layoutInflater)
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

    private val present = arrayOf("ultrafast",
        "superfast",
        "veryfast",
        "faster",
        "fast",
        "medium",
        "slow",
        "slower",
        "veryslow",
        "placebo")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val formats = ArrayAdapter(this, R.layout.cat_exposed_dropdown_popup_item, formats)
        binding.formats.setAdapter(formats)
        val presents = ArrayAdapter(this, R.layout.cat_exposed_dropdown_popup_item, present)
        binding.bytesInput.setAdapter(presents)
        binding.toolbarLayout.toolbar.apply {
            title = "视频增加字幕"
            setNavigationOnClickListener {
                finish()
            }
        }
        binding.colorPicker.setOnClickListener {
            ImagePicker.with(this).galleryOnly().galleryMimeTypes(arrayOf("video/*"))
                .createIntent { intent ->
                    startForVideoResult.launch(intent)
                }
        }
        binding.crtPicker.setOnClickListener {
            ImagePicker.with(this).galleryOnly().galleryMimeTypes(arrayOf("*/*"))
                .createIntent { intent ->
                    startForSrtResult.launch(intent)
                }
        }
    }


    private val startForVideoResult =
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

    private val startForSrtResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data
            when (resultCode) {
                Activity.RESULT_OK -> {
                    srtpath = FileUri.getPathByUri(data?.data).toString()
                    binding.titleString.text = "已选择"
                }
                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                }
                else -> {
                    snackbar("选择已取消")
                }
            }
        }

    private var path = ""
    private var srtpath = ""


}