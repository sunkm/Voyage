package com.manchuan.tools.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.toBitmap
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ImageUtils
import com.drake.serialize.serialize.serialLazy
import com.drake.statusbar.immersive
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.lxj.androidktx.core.tip
import com.manchuan.tools.databinding.ActivityPhotoWaterMarkBinding
import com.maxkeppeler.sheets.color.ColorSheet
import com.watermark.androidwm.WatermarkBuilder
import com.watermark.androidwm.bean.WatermarkText
import rikka.material.app.MaterialActivity
import java.io.File
import java.util.Objects


class PhotoWaterMarkActivity : MaterialActivity() {
    private var toolbar: Toolbar? = null
    private lateinit var trans: Slider
    private lateinit var angle: Slider
    private lateinit var size: Slider
    private var addMark: Button? = null
    private var addBack: Button? = null
    private var fileImage: ImageView? = null
    private var save: ImageView? = null
    private var markContent: TextInputEditText? = null
    private lateinit var markBinding: ActivityPhotoWaterMarkBinding
    private var watermarkTextColor: Int by serialLazy(Color.BLACK)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        markBinding = ActivityPhotoWaterMarkBinding.inflate(layoutInflater)
        setContentView(markBinding.root)
        toolbar = markBinding.toolbar
        trans = markBinding.trans
        angle = markBinding.angle
        size = markBinding.size
        addBack = markBinding.addBack
        addMark = markBinding.addMark
        fileImage = markBinding.fileImage
        save = markBinding.save
        markContent = markBinding.markContent
        immersive(toolbar!!)
        addBack!!.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .start(10001)
        }
        markBinding.colorPicker.setCardBackgroundColor(watermarkTextColor)
        markBinding.colorString.text = String.format("#%02X", watermarkTextColor)
        when (ColorUtils.isLightColor(watermarkTextColor)) {
            true -> {
                markBinding.colorTitle.setTextColor(Color.BLACK)
                markBinding.colorString.setTextColor(Color.BLACK)
            }
            false -> {
                markBinding.colorTitle.setTextColor(Color.WHITE)
                markBinding.colorString.setTextColor(Color.WHITE)
            }
        }
        markBinding.colorPicker.setOnClickListener {
            ColorSheet().show(this) {
                title("选择水印文字颜色")
                onPositive { color ->
                    // Use color
                    markBinding.colorPicker.setCardBackgroundColor(color)
                    watermarkTextColor = color
                    markBinding.colorString.text = String.format("#%02X", color)
                    if (ColorUtils.isLightColor(color)) {
                        markBinding.colorTitle.setTextColor(Color.BLACK)
                        markBinding.colorString.setTextColor(Color.BLACK)
                    } else {
                        markBinding.colorTitle.setTextColor(Color.WHITE)
                        markBinding.colorString.setTextColor(Color.WHITE)
                    }
                    if (fileImage!!.drawable != null) {
                        markContent?.text?.toString()?.let { updateWatermark(it, bitmap) }
                    }
                }
            }
        }
        save!!.setOnClickListener {
            if (fileImage!!.drawable == null) {
                TipDialog.show("无背景图", WaitDialog.TYPE.ERROR)
            } else {
                runCatching {
                    ImageUtils.save(fileImage!!.drawable.toBitmap(),
                        Environment.getExternalStorageDirectory().absolutePath + File.separator + Environment.DIRECTORY_PICTURES + File.separator + "${SystemClock.elapsedRealtime()}.png",
                        Bitmap.CompressFormat.PNG)
                    MediaScannerConnection.scanFile(applicationContext,
                        arrayOf(
                            Environment.getExternalStorageDirectory().absolutePath + File.separator + Environment.DIRECTORY_PICTURES
                        ),
                        arrayOf("*/*")) { s, uri ->
                    }
                }.onSuccess {
                    tip("保存成功")
                }.onFailure {
                    tip("保存失败")
                }
            }
        }
        size.addOnChangeListener { slider, value, fromUser ->
            if (fileImage!!.drawable != null) {
                markContent?.text?.toString()?.let { updateWatermark(it, bitmap) }
            }
        }
        angle.addOnChangeListener { slider, value, fromUser ->
            if (fileImage!!.drawable != null) {
                markContent?.text?.toString()?.let { updateWatermark(it, bitmap) }
            }
        }
        trans.addOnChangeListener { slider, value, fromUser ->
            if (fileImage!!.drawable != null) {
                markContent?.text?.toString()?.let { updateWatermark(it, bitmap) }
            }
        }
    }

    private fun updateWatermark(string: String, bitmap: Bitmap) {
        val watermarkText = WatermarkText(string)
            .setRotation(angle.value.toDouble())
            .setTextSize(size.value.toDouble())
            .setTextColor(watermarkTextColor)
            .setTextAlpha(trans.value.toInt())
        WatermarkBuilder.create(this, bitmap)
            .setTileMode(true)
            .loadWatermarkText(watermarkText)
            .watermark
            .setToImageView(fileImage)
    }

    private lateinit var bitmap: Bitmap

    @Deprecated("Deprecated in Java")
    @SuppressLint("CheckResult")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10001 && resultCode == RESULT_OK && data != null) {
            bitmap = BitmapFactory.decodeFile(
                BlackGreyPhotoActivity.getRealPathFromURI(
                    data.data,
                    this
                )
            )
            if (Objects.requireNonNull(markContent!!.text).toString().isEmpty()) {
                updateWatermark("文字水印", bitmap)
            } else {
                markContent?.text?.toString()?.let { updateWatermark(it, bitmap) }
            }
        }
    }
}