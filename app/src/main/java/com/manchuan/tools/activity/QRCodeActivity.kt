package com.manchuan.tools.activity

import ando.file.core.FileUri
import android.animation.LayoutTransition
import android.app.Activity
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ImageUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.drake.channel.receiveEvent
import com.drake.channel.sendEvent
import com.drake.serialize.serialize.serialLazy
import com.dylanc.longan.activity
import com.dylanc.longan.doOnClick
import com.dylanc.longan.transparentNavigationBar
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.kongzue.drawerbox.DrawerBox
import com.lxj.androidktx.core.tip
import com.manchuan.tools.R
import com.manchuan.tools.colorpicker.ColorPickerView
import com.manchuan.tools.colorpicker.builder.ColorPickerDialogBuilder
import com.manchuan.tools.databinding.ActivityQrCodeBinding
import com.manchuan.tools.extensions.errorToast
import com.manchuan.tools.extensions.successToast
import com.manchuan.tools.model.ViewModelQrCode
import com.manchuan.tools.utils.QRCodeUtil
import rikka.material.app.MaterialActivity
import java.io.File

class QRCodeActivity : MaterialActivity() {

    private val binding by lazy {
        ActivityQrCodeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        transparentNavigationBar()
        binding.linearLayout.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        initDrawerBox()
        initEvents()
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomDrawer)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        binding.bottomDrawer.layoutTransition.enableTransitionType(LayoutTransition.CHANGE_APPEARING)
    }

    private fun initEvents() {
        receiveEvent<Bitmap>("bitmap") { bitmap ->
            "生成成功".successToast()
            Glide.with(this@QRCodeActivity).load(bitmap)
                .transition(DrawableTransitionOptions.withCrossFade()).skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE).into(binding.qrCode)
            ViewModelQrCode.isCanSave.observe(this@QRCodeActivity) {
                if (it) {
                    binding.save.visibility = View.VISIBLE
                    binding.save.setOnClickListener {
                        runCatching {
                            ImageUtils.save(bitmap,
                                Environment.getExternalStorageDirectory().absolutePath + File.separator + Environment.DIRECTORY_PICTURES + File.separator + "${SystemClock.elapsedRealtime()}.png",
                                Bitmap.CompressFormat.PNG)
                            MediaScannerConnection.scanFile(applicationContext,
                                arrayOf(Environment.getExternalStorageDirectory().absolutePath + File.separator + Environment.DIRECTORY_PICTURES),
                                arrayOf("*/*")) { s, uri ->
                            }
                        }.onSuccess {
                            tip("保存成功")
                        }.onFailure {
                            tip("保存失败")
                        }
                    }
                }
            }

        }
    }

    private lateinit var drawerBox: DrawerBox

    private var whiteColor: Int by serialLazy(Color.WHITE)
    private var blackColor: Int by serialLazy(Color.BLACK)

    private var logoBitmap: Bitmap? = null


    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data
            when (resultCode) {
                Activity.RESULT_OK -> {
                    //Image Uri will not be null for RESULT_OK
                    val fileUri = data?.data!!
                    logoBitmap = BitmapFactory.decodeFile(FileUri.getPathByUri(fileUri))
                }
                ImagePicker.RESULT_ERROR -> {
                    ImagePicker.getError(data).errorToast()
                }
                else -> {
                    "选择取消".errorToast()
                }
            }
        }

    private fun initDrawerBox() {
        val charsets = arrayOf("UTF-8", "GBK")
        val errors = arrayOf("L", "M", "Q", "H")
        var mode = "white"
        binding.choiceGroup.check(R.id.whiteAra)
        binding.colorPicker.setCardBackgroundColor(whiteColor)
        binding.colorString.typeface =
            Typeface.createFromAsset(assets, "fonts/FiraCode-Regular.ttf")
        binding.colorString.text =
            String.format("#%02X", binding.colorPicker.cardBackgroundColor.defaultColor)
        if (ColorUtils.isLightColor(binding.colorPicker.cardBackgroundColor.defaultColor)) {
            binding.colorString.setTextColor(Color.BLACK)
        } else {
            binding.colorString.setTextColor(Color.WHITE)
        }
        binding.imagePicker.doOnClick {
            ImagePicker.with(activity).galleryOnly().createIntent { intent ->
                startForProfileImageResult.launch(intent)
            }
        }
        binding.choiceGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            when {
                checkedId == R.id.whiteAra && isChecked -> {
                    binding.colorPicker.setCardBackgroundColor(whiteColor)
                    mode = "white"
                    binding.colorString.text =
                        String.format("#%02X", binding.colorPicker.cardBackgroundColor.defaultColor)
                    if (ColorUtils.isLightColor(binding.colorPicker.cardBackgroundColor.defaultColor)) {
                        binding.colorString.setTextColor(Color.BLACK)
                    } else {
                        binding.colorString.setTextColor(Color.WHITE)
                    }
                    return@addOnButtonCheckedListener
                }
                checkedId == R.id.blackAra && isChecked -> {
                    binding.colorPicker.setCardBackgroundColor(blackColor)
                    mode = "black"
                    binding.colorString.text =
                        String.format("#%02X", binding.colorPicker.cardBackgroundColor.defaultColor)
                    if (ColorUtils.isLightColor(binding.colorPicker.cardBackgroundColor.defaultColor)) {
                        binding.colorString.setTextColor(Color.BLACK)
                    } else {
                        binding.colorString.setTextColor(Color.WHITE)
                    }
                    return@addOnButtonCheckedListener
                }
            }
        }
        binding.colorPicker.setOnClickListener {
            when (mode) {
                "white" -> {
                    ColorPickerDialogBuilder.with(this@QRCodeActivity).setTitle("选择白色区块颜色")
                        .initialColor(binding.colorPicker.cardBackgroundColor.defaultColor)
                        .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE).showColorEdit(true)
                        .setPositiveButton("确定") { dialog: DialogInterface?, selectedColor: Int, allColors: Array<Int?>? ->
                            //chip2.setChipIconTint(new ColorStateList(null,new int[]{selectedColor}));
                            binding.colorPicker.setCardBackgroundColor(selectedColor)
                            whiteColor = selectedColor
                            binding.colorString.text = String.format("#%02X", selectedColor)
                            if (ColorUtils.isLightColor(selectedColor)) {
                                binding.colorString.setTextColor(Color.BLACK)
                            } else {
                                binding.colorString.setTextColor(Color.WHITE)
                            }
                        }.setNegativeButton("取消") { dialog: DialogInterface?, which: Int -> }
                        .build().show()
                }
                "black" -> {
                    ColorPickerDialogBuilder.with(this@QRCodeActivity).setTitle("选择黑色区块颜色")
                        .initialColor(binding.colorPicker.cardBackgroundColor.defaultColor)
                        .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE).showColorEdit(true)
                        .setPositiveButton("确定") { dialog: DialogInterface?, selectedColor: Int, allColors: Array<Int?>? ->
                            //chip2.setChipIconTint(new ColorStateList(null,new int[]{selectedColor}));
                            binding.colorPicker.setCardBackgroundColor(selectedColor)
                            blackColor = selectedColor
                            binding.colorString.text = String.format("#%02X", selectedColor)
                            if (ColorUtils.isLightColor(selectedColor)) {
                                binding.colorString.setTextColor(Color.BLACK)
                            } else {
                                binding.colorString.setTextColor(Color.WHITE)
                            }
                        }.setNegativeButton("取消") { dialog: DialogInterface?, which: Int -> }
                        .build().show()
                }
            }
        }
        val charsetAdapter =
            ArrayAdapter(this@QRCodeActivity, R.layout.cat_exposed_dropdown_popup_item, charsets)
        val errorAdapter =
            ArrayAdapter(this@QRCodeActivity, R.layout.cat_exposed_dropdown_popup_item, errors)
        binding.autoCompleteCharset.setAdapter(charsetAdapter)
        binding.autoCompleteError.setAdapter(errorAdapter)
        binding.create.apply {
            setOnClickListener {
                val content = binding.content.text?.toString()
                val width = binding.width.text?.toString()
                val height = binding.height.text?.toString()
                val charset = binding.autoCompleteCharset.text?.toString()
                val error = binding.autoCompleteError.text?.toString()
                val margin = binding.margin.text?.toString()
                if (content?.isEmpty()!! || width?.isEmpty()!! || height?.isEmpty()!! || margin?.isEmpty()!!) {
                    tip("请填写所有内容")
                } else {
                    QRCodeUtil.createQRCodeBitmap(content,
                        width.toInt(),
                        height.toInt(),
                        charset!!,
                        error!!,
                        margin,
                        blackColor,
                        whiteColor,
                        logoBitmap = logoBitmap,
                        binding.imageSlider.value,
                        null)?.let {
                        sendEvent(it, "bitmap")
                        ViewModelQrCode.isCanSave.postValue(true)
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}