package com.manchuan.tools.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.blankj.utilcode.util.FileUtils
import com.drake.statusbar.immersive
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.button.MaterialButton
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.databinding.ActivityCompressBinding
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar
import rikka.material.app.MaterialActivity
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.File
import java.util.*


class CompressActivity : MaterialActivity() {
    private var toolbar: Toolbar? = null
    private var path: String? = null
    private var compressBinding: ActivityCompressBinding? = null


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        compressBinding = ActivityCompressBinding.inflate(LayoutInflater.from(this))
        setContentView(compressBinding?.root)
        toolbar = compressBinding?.toolbar
        mSeekbar1 = compressBinding?.seekbar1
        mXztp = compressBinding?.xztp
        mYstp = compressBinding?.ystp
        mTp1 = compressBinding?.tp1
        mTp2 = compressBinding?.tp2
        mTxt1 = compressBinding?.txt1
        mTxt2 = compressBinding?.txt2
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "图片压缩"
            setDisplayHomeAsUpEnabled(true)
        }
        immersive(toolbar!!)
        mSeekbar1!!.max = 80
        mSeekbar1!!.min = 60
        mXztp!!.setOnClickListener {
            ImagePicker.with(this).galleryOnly().start(10002)
        }
        mYstp!!.setOnClickListener {
            if (path.isNullOrEmpty()) {
                PopTip.show("未选择图片")
            } else {
                @SuppressLint("SdCardPath") val fileName =
                    "/sdcard/Pictures/"
                Luban.with(this)
                    .load(path?.let { it1 -> File(it1) })
                    .ignoreBy(100)
                    .setTargetDir(fileName)
                    .filter { path ->
                        !(TextUtils.isEmpty(path) || path.lowercase(Locale.getDefault())
                            .endsWith(".gif"))
                    }
                    .setCompressListener(object : OnCompressListener {
                        override fun onStart() {
                            // TODO 压缩开始前调用，可以在方法内启动 loading UI
                            WaitDialog.show("压缩中...")
                        }

                        override fun onSuccess(file: File?) {
                            // TODO 压缩成功后调用，返回压缩后的图片文件
                            PopTip.show("压缩成功，图片保存在$file")
                            mTxt2!!.text = "大小:" + FileUtils.getSize(file)
                            TipDialog.show("压缩完成", WaitDialog.TYPE.SUCCESS)
                        }

                        override fun onError(e: Throwable?) {
                            // TODO 当压缩过程出现问题时调用
                            WaitDialog.dismiss()
                            PopTip.show("出现错误：$e")
                        }
                    }).launch()
            }
        }
    }
    @Deprecated("Deprecated in Java")
    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10002 && resultCode == RESULT_OK) {
            val fileUri = data!!.data
            val bp =
                BitmapFactory.decodeFile(BlackGreyPhotoActivity.getRealPathFromURI(fileUri, this))
            mTxt1!!.text = "大小:" + FileUtils.getSize(
                BlackGreyPhotoActivity.getRealPathFromURI(
                    fileUri,
                    this
                )
            )
            path = BlackGreyPhotoActivity.getRealPathFromURI(fileUri, this)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
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

    private var mTp1: ImageView? = null
    private var mTp2: ImageView? = null
    private var mSeekbar1: DiscreteSeekBar? = null
    private var mXztp: MaterialButton? = null
    private var mYstp: MaterialButton? = null
    private var mTxt1: TextView? = null
    private var mTxt2: TextView? = null
}