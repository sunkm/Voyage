package com.manchuan.tools.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.gyf.immersionbar.ImmersionBar
import com.kongzue.dialogx.dialogs.PopTip
import com.manchuan.tools.R
import com.manchuan.tools.utils.ColorUtils.statusBarColor
import com.manchuan.tools.utils.ExifUtils
import java.io.File
import java.util.*

class ExifEditActivity : AppCompatActivity() {
    private val isSave = false
    private val themeInt = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exif)
        initView(this)
        ImmersionBar.with(this).statusBarColorInt(statusBarColor).autoDarkModeEnable(true).init()
        mExif_add!!.setOnClickListener { view: View? ->
            ImagePicker.with(this).galleryOnly().start(10000)
        }
        ImmersionBar.with(this)
            .statusBarColorInt(statusBarColor)
            .navigationBarColorInt(statusBarColor)
            .autoDarkModeEnable(true)
            .titleBar(appBarLayout)
            .init()
        save.setOnClickListener {
            runCatching {
            }.onFailure {
                PopTip.show("保存失败:${it.message}")
            }
        }
    }

    private var mActivity_exifScrollView: ScrollView? = null
    private var mFile_image: ImageView? = null
    private var mExif_add: ImageView? = null
    private var mActivity_exifLinearLayout: LinearLayout? = null
    private var mExif_orientation: TextView? = null
    private var mActivity_exifLinearLayout2: LinearLayout? = null
    private var mExif_date: TextView? = null
    private var mActivity_exifLinearLayout3: LinearLayout? = null
    private var mExif_make: TextView? = null
    private var mActivity_exifLinearLayout4: LinearLayout? = null
    private var mExif_model: TextView? = null
    private var mActivity_exifLinearLayout5: LinearLayout? = null
    private var mExif_flash: TextView? = null
    private var mActivity_exifLinearLayout6: LinearLayout? = null
    private var mExif_length: TextView? = null
    private var mActivity_exifLinearLayout7: LinearLayout? = null
    private var mExif_width: TextView? = null
    private var mActivity_exifLinearLayout8: LinearLayout? = null
    private var mExif_latitude: TextView? = null
    private var mActivity_exifLinearLayout9: LinearLayout? = null
    private var mExif_longitude: TextView? = null
    private var mActivity_exifLinearLayout10: LinearLayout? = null
    private var mExif_exposure: TextView? = null
    private var mActivity_exifLinearLayout11: LinearLayout? = null
    private var mExif_aperture: TextView? = null
    private var mActivity_exifLinearLayout12: LinearLayout? = null
    private var mExif_iso: TextView? = null
    private var mActivity_exifLinearLayout13: LinearLayout? = null
    private var mExif_digitized: TextView? = null
    private var mActivity_exifLinearLayout14: LinearLayout? = null
    private var mExif_altitude: TextView? = null
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var save: FloatingActionButton
    private fun initView(activity: Activity) {
        save = activity.findViewById(R.id.save)
        appBarLayout = activity.findViewById(R.id.appbar)
        mFile_image = activity.findViewById(R.id.file_image)
        mExif_add = activity.findViewById(R.id.fullscreen)
        mActivity_exifLinearLayout = activity.findViewById(R.id.activity_exifLinearLayout)
        mExif_orientation = activity.findViewById(R.id.exif_orientation)
        mActivity_exifLinearLayout2 = activity.findViewById(R.id.activity_exifLinearLayout2)
        mExif_date = activity.findViewById(R.id.exif_date)
        mActivity_exifLinearLayout3 = activity.findViewById(R.id.activity_exifLinearLayout3)
        mExif_make = activity.findViewById(R.id.exif_make)
        mActivity_exifLinearLayout4 = activity.findViewById(R.id.activity_exifLinearLayout4)
        mExif_model = activity.findViewById(R.id.exif_model)
        mActivity_exifLinearLayout5 = activity.findViewById(R.id.activity_exifLinearLayout5)
        mExif_flash = activity.findViewById(R.id.exif_flash)
        mActivity_exifLinearLayout6 = activity.findViewById(R.id.activity_exifLinearLayout6)
        mExif_length = activity.findViewById(R.id.exif_length)
        mActivity_exifLinearLayout7 = activity.findViewById(R.id.activity_exifLinearLayout7)
        mExif_width = activity.findViewById(R.id.exif_width)
        mActivity_exifLinearLayout8 = activity.findViewById(R.id.activity_exifLinearLayout8)
        mExif_latitude = activity.findViewById(R.id.exif_latitude)
        mActivity_exifLinearLayout9 = activity.findViewById(R.id.activity_exifLinearLayout9)
        mExif_longitude = activity.findViewById(R.id.exif_longitude)
        mActivity_exifLinearLayout10 = activity.findViewById(R.id.activity_exifLinearLayout10)
        mExif_exposure = activity.findViewById(R.id.exif_exposure)
        mActivity_exifLinearLayout11 = activity.findViewById(R.id.activity_exifLinearLayout11)
        mExif_aperture = activity.findViewById(R.id.exif_aperture)
        mActivity_exifLinearLayout12 = activity.findViewById(R.id.activity_exifLinearLayout12)
        mExif_iso = activity.findViewById(R.id.exif_iso)
        mActivity_exifLinearLayout13 = activity.findViewById(R.id.activity_exifLinearLayout13)
        mExif_digitized = activity.findViewById(R.id.exif_digitized)
        mActivity_exifLinearLayout14 = activity.findViewById(R.id.activity_exifLinearLayout14)
        mExif_altitude = activity.findViewById(R.id.exif_altitude)
        mActivity_exifScrollView = activity.findViewById(R.id.activity_exifScrollView)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var mSelected: List<String?>
        if (requestCode == 10000 && resultCode == RESULT_OK) {
            val fileUri = data!!.data
            Glide.with(this).load(
                File(
                    Objects.requireNonNull(
                        BlackGreyPhotoActivity.getRealPathFromURI(
                            fileUri,
                            this
                        )
                    )
                )
            ).into(
                mFile_image!!
            )
            Objects.requireNonNull(
                BlackGreyPhotoActivity.getRealPathFromURI(
                    fileUri,
                    this
                )
            ).let {
                it?.let { it1 -> ExifUtils.setPath(it1) }
            }
            mActivity_exifScrollView!!.visibility = View.VISIBLE
            save.visibility = View.VISIBLE
            mExif_orientation!!.text = ExifUtils.orientation
            mExif_date!!.text = ExifUtils.dateTime
            mExif_make!!.text = ExifUtils.make
            mExif_model!!.text = ExifUtils.model
            mExif_flash!!.text = ExifUtils.flash
            mExif_length!!.text = ExifUtils.length
            mExif_width!!.text = ExifUtils.width
            mExif_latitude!!.text = ExifUtils.latitude
            mExif_longitude!!.text = ExifUtils.longitude
            mExif_iso!!.text = ExifUtils.iso
            mExif_aperture!!.text = ExifUtils.apertrue
            mExif_digitized!!.text = ExifUtils.digitized
            mExif_altitude!!.text = ExifUtils.alititude
            mExif_exposure!!.text = ExifUtils.exposure
        }
    }

    fun listToString02(list: List<String?>?): String {
        var resultString = ""
        if (list.isNullOrEmpty()) {
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