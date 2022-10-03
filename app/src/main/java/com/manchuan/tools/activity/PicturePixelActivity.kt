package com.manchuan.tools.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.drake.statusbar.immersive
import com.dylanc.longan.design.snackbar
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.kongzue.dialogx.dialogs.PopTip
import com.luoye.fpic.util.ConvertUtils
import com.manchuan.tools.R
import com.manchuan.tools.databinding.ActivityPhotoPixelBinding
import com.manchuan.tools.utils.ImageUtil
import rikka.material.app.MaterialActivity
import java.io.File
import java.util.*

class PicturePixelActivity : MaterialActivity() {
    private var toolbar: Toolbar? = null
    private var seekbar1: Slider? = null
    private var xztp: MaterialButton? = null
    private var bctp: MaterialButton? = null
    private var tp: ImageView? = null
    private var pixelBinding: ActivityPhotoPixelBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pixelBinding = ActivityPhotoPixelBinding.inflate(LayoutInflater.from(this))
        setContentView(pixelBinding?.root)
        toolbar = pixelBinding?.toolbar
        seekbar1 = pixelBinding?.seekbar1
        bctp = pixelBinding?.bctp
        xztp = pixelBinding?.xztp
        tp = pixelBinding?.tp
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        immersive(toolbar!!)
        xztp!!.setOnClickListener { view: View? ->
            ImagePicker.with(this)
                .galleryOnly()
                .start(10002)
        }
        bctp!!.setOnClickListener { view: View? ->
            if (tp!!.drawable == null) {
                snackbar("请先选择图片")
            } else {
                try {
                    tp!!.refreshDrawableState()
                    tp!!.isDrawingCacheEnabled = true
                    val fileName = "PICTURE_PIXEL_" + System.currentTimeMillis() + ".png"
                    tp!!.setImageBitmap(
                        ConvertUtils.getBlockBitmap(
                            ImageUtil.drawableToBitmap(
                                tp!!.drawable
                            ), seekbar1!!.value.toInt()
                        )
                    )
                    ImageUtil.SaveImageToPixel(
                        this@PicturePixelActivity, ImageUtil.drawableToBitmap(
                            tp!!.drawable
                        ), fileName
                    )
                    PopTip.show("已保存到相册")
                    tp!!.isDrawingCacheEnabled = false
                } catch (e: Exception) {
                    PopTip.show("保存失败")
                    tp!!.isDrawingCacheEnabled = false
                }
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        supportActionBar?.title = "图片像素化"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("WrongConstant")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10002 && resultCode == RESULT_OK) {
            val fileUri = data!!.data
            //mActivity_qrcode_blk_pic_edit.setText(listToString02(mSelected));
            //Log.d("Matisse", "mSelected: " + mSelected);
            tp!!.refreshDrawableState()
            tp!!.isDrawingCacheEnabled = true
            tp!!.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
            Glide.with(this@PicturePixelActivity).load(
                File(
                    Objects.requireNonNull(
                        BlackGreyPhotoActivity.getRealPathFromURI(
                            fileUri,
                            this
                        )
                    )
                )
            ).into(
                tp!!
            )
            //tp.setImageBitmap(BitmapFactory.decodeFile(listToString02(mSelected)));
        } else if (requestCode == 10002 && resultCode == RESULT_CANCELED) {
            //mSelected = Matisse.Companion.obtainPathResult(data);
            PopTip.show(R.mipmap.ic_launcher, "已取消选择")
            //SnackbarToast.makeText("已取消选择", this.getWindow().getDecorView());
            //mActivity_qrcode_logo_edit.setText(listToString02(mSelected));
        }
    }

    fun listToString02(list: List<String?>?): String {
        var resultString = ""
        if (null == list) {
            println("list内容为空！")
        } else {
            val sb = StringBuilder()
            var flag = false
            for (str in list) {
                if (flag) {
                    sb.append(",")
                } else {
                    flag = true
                }
                sb.append(str)
            }
            resultString = sb.toString()
            //System.out.println("最后拼接的字符串结果：" + resultString);
        }
        return resultString
    }
}