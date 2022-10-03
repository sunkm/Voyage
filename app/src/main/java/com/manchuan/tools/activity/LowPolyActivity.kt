package com.manchuan.tools.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.drake.statusbar.immersive
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.button.MaterialButton
import com.kongzue.dialogx.dialogs.PopTip
import com.manchuan.tools.R
import com.manchuan.tools.databinding.ActivityLowpolyBinding
import com.manchuan.tools.utils.SnackToast
import com.zebrostudio.rxlowpoly.Quality
import com.zebrostudio.rxlowpoly.RxLowpoly
import rikka.material.app.MaterialActivity
import java.io.File
import java.util.*

class LowPolyActivity : MaterialActivity() {
    private var toolbar: Toolbar? = null
    private var lowpoly_image: ImageView? = null
    private var path: String? = null
    private var uri: String? = null
    private var isDone = false
    private var saved = false
    private var isSave = true
    private var path2: String? = null
    lateinit var lowpolyBinding: ActivityLowpolyBinding
    private var quality = Quality.VERY_HIGH

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lowpolyBinding = ActivityLowpolyBinding.inflate(layoutInflater)
        setContentView(lowpolyBinding.root)
        toolbar = lowpolyBinding.toolbar
        lowpoly_image = lowpolyBinding.lowpolyImage
        mLowpoly_image = lowpolyBinding.lowpolyImage
        materialbutton1 = lowpolyBinding.materialbutton1
        materialbutton2 = lowpolyBinding.materialbutton2
        materialbutton3 = lowpolyBinding.materialbutton3
        setSupportActionBar(toolbar)
        mLowpoly_image!!.scaleType = ImageView.ScaleType.FIT_CENTER
        mLowpoly_image!!.adjustViewBounds = true
        mLowpoly_image!!.setImageBitmap(null)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        immersive(toolbar!!)
        //图片高度
        materialbutton1!!.setOnClickListener {
            path = null
            isDone = false
            ImagePicker.with(this).galleryOnly().start(10000)
        }
        lowpolyBinding.toggleButton.addOnButtonCheckedListener { group, checkedId, isChecked ->
            when (checkedId) {
                R.id.very_high -> {
                    quality = Quality.VERY_HIGH
                    return@addOnButtonCheckedListener
                }
                R.id.high -> {
                    quality = Quality.HIGH
                    return@addOnButtonCheckedListener
                }
                R.id.medium -> {
                    quality = Quality.MEDIUM
                    return@addOnButtonCheckedListener
                }
                R.id.low -> {
                    quality = Quality.LOW
                    return@addOnButtonCheckedListener
                }
                R.id.very_low -> {
                    quality = Quality.VERY_LOW
                    return@addOnButtonCheckedListener
                }
            }
        }
        materialbutton2!!.setOnClickListener {
            if (mLowpoly_image!!.drawable != null && path != null) {
                runOnUiThread {
                    try {
                        if (uri != null) {
                            if (!isDone) {
                                isSave = false
                                drawableToBitamp(mLowpoly_image!!.drawable)?.let { it1 ->
                                    Glide.with(this)
                                        .load(
                                            RxLowpoly.with(this)
                                                .input(it1)
                                                .quality(quality)
                                                .generate()
                                        ).skipMemoryCache(true)
                                        .into(mLowpoly_image!!)
                                }
                            } else {
                                SnackToast.makeText(
                                    "已经制作完了，请重新选择图片",
                                    this@LowPolyActivity.window.decorView
                                )
                            }
                        }
                        isDone = true
                    } catch (ignored: Exception) {
                    }
                }
            } else {
                PopTip.show("请先选择图片")
            }
        }
        materialbutton3!!.setOnClickListener {
            if (mLowpoly_image!!.drawable != null && path != null) {
                if (!isSave) {
                    isSave = true
                    saved = false
                    PopTip.show("已保存")
                } else {
                    if (saved) {
                        PopTip.show("该图片已保存")
                    } else {
                        PopTip.show("请先选择图片然后生成")
                    }
                }
            } else {
                PopTip.show("请先选择图片")
            }
        }
    }

    private var bitmap: Bitmap? = null
    private fun drawableToBitamp(drawable: Drawable): Bitmap? {
        val bd = drawable as BitmapDrawable
        bitmap = bd.bitmap
        return bitmap
    }

    @JvmField
    var mLowpoly_image: ImageView? = null
    private var materialbutton1: MaterialButton? = null
    private var materialbutton2: MaterialButton? = null
    private var materialbutton3: MaterialButton? = null

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        supportActionBar!!.title = "LowPoly图片生成"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var mSelected: List<String?>
        if (requestCode == 10000 && resultCode == RESULT_OK) {
            val fileUri = data!!.data
            //mActivity_qrcode_blk_pic_edit.setText(listToString02(mSelected));
            mLowpoly_image!!.refreshDrawableState()
            mLowpoly_image!!.isDrawingCacheEnabled = true
            mLowpoly_image!!.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
            mLowpoly_image!!.setImageBitmap(
                BitmapFactory.decodeFile(
                    File(
                        Objects.requireNonNull(
                            BlackGreyPhotoActivity.getRealPathFromURI(fileUri, this)
                        )
                    ).toString()
                )
            )
            uri = BlackGreyPhotoActivity.getRealPathFromURI(fileUri, this)
            path = uri
            isSave = true
            saved = false
            //Log.d("Matisse", "mSelected: " + mSelected);
        }
    }
}