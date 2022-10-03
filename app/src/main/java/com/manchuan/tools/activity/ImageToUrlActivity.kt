package com.manchuan.tools.activity

import ando.file.core.FileUri
import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.blankj.utilcode.util.ClipboardUtils
import com.bumptech.glide.Glide
import com.drake.net.Post
import com.drake.net.component.Progress
import com.drake.net.interfaces.ProgressListener
import com.drake.net.utils.scopeNetLife
import com.drake.statusbar.immersive
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.appbar.MaterialToolbar
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.databinding.ActivityPhotoColoringBinding
import com.manchuan.tools.utils.SnackToast.makeText
import rikka.material.app.MaterialActivity
import java.io.File

@SuppressLint("NonConstantResourceId")
class ImageToUrlActivity : MaterialActivity() {
    private lateinit var toolbar: MaterialToolbar
    private val isNeed = false
    private val imageToUrlBinding by lazy {
        ActivityPhotoColoringBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(imageToUrlBinding.root)
        toolbar = imageToUrlBinding.toolbar
        mAdd_photo = imageToUrlBinding.addPhoto
        mNeed_lay = imageToUrlBinding.needLay
        mNeed_photo = imageToUrlBinding.needPhoto
        setSupportActionBar(toolbar)
        toolbar.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        supportActionBar?.apply {
            title = "图片转链接"
            setDisplayHomeAsUpEnabled(true)
        }
        immersive(toolbar)
        if (isNeed) {
            mNeed_lay!!.visibility = View.GONE
        } else {
            mNeed_lay!!.visibility = View.VISIBLE
            mNeed_photo!!.setOnClickListener { view: View? ->
                ImagePicker.with(this).galleryOnly().start(10003)
            }
        }
    }

    private var mNeed_photo: ImageView? = null
    private var mAdd_photo: ImageView? = null
    private var mNeed_lay: LinearLayout? = null

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10003 && resultCode == RESULT_OK) {
            val fileUri = data!!.data
            imageToUrlBinding.progressBar.visibility = View.VISIBLE
            scopeNetLife {
                val body = Post<String>("http://pic.sogou.com/pic/upload_pic.jsp") {
                    param("file", FileUri.getPathByUri(fileUri)?.let { File(it) })
                    addUploadListener(object : ProgressListener() {
                        override fun onProgress(p: Progress) {
                            imageToUrlBinding.progressBar.post {
                                imageToUrlBinding.progressBar.setProgressCompat(p.progress(),true)
                                supportActionBar?.title = "已上传:${p.progress()}% 剩余大小:${p.remainSize()}"
                            }
                        }
                    })
                }.await()
                mNeed_lay?.visibility = View.GONE
                imageToUrlBinding.progressBar.visibility = View.GONE
                imageToUrlBinding.progressBar.setProgressCompat(0,false)
                Glide.with(this@ImageToUrlActivity).load(body).into(mNeed_photo!!)
                ClipboardUtils.copyText("ImgUrl", body)
                TipDialog.show("已复制链接", WaitDialog.TYPE.SUCCESS)
                supportActionBar?.title = "图片转链接"
            }
        } else {
            makeText("上传图片失败", this@ImageToUrlActivity.window.decorView)
        }
    }

}