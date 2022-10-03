package com.manchuan.tools.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import com.blankj.utilcode.util.ImageUtils
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.button.MaterialButton
import com.gyf.immersionbar.ImmersionBar
import com.kongzue.dialogx.dialogs.PopTip
import com.manchuan.tools.R
import com.manchuan.tools.databinding.ActivityBlackGreyBinding
import com.manchuan.tools.utils.ColorUtils.statusBarColor
import com.manchuan.tools.utils.ImageUtil
import com.manchuan.tools.utils.SnackToast.makeText
import rikka.material.app.MaterialActivity
import java.io.File

@SuppressLint("NonConstantResourceId")
class BlackGreyPhotoActivity : MaterialActivity() {

    private var blackGreyBinding: ActivityBlackGreyBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        blackGreyBinding = ActivityBlackGreyBinding.inflate(LayoutInflater.from(this))
        setContentView(blackGreyBinding?.root)
        setSupportActionBar(blackGreyBinding?.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        ImmersionBar.with(this).statusBarColorInt(statusBarColor).transparentNavigationBar().autoDarkModeEnable(true)
            .titleBar(blackGreyBinding?.toolbar).init()
        blackGreyBinding?.xztp!!.setOnClickListener {
            ImagePicker.with(this).galleryOnly().start(10002)
        }
        blackGreyBinding?.bctp!!.setOnClickListener {
            if (blackGreyBinding?.tp!!.drawable == null) {
                makeText("请先选择图片", this@BlackGreyPhotoActivity.window.decorView)
            } else {
                val fileName = "BLACK_GREY_" + System.currentTimeMillis() + ".png"
                try {
                    ImageUtil.SaveImageToGrey(
                        this@BlackGreyPhotoActivity, ImageUtil.drawableToBitmap(
                            blackGreyBinding?.tp!!.drawable
                        ), fileName
                    )
                    PopTip.show("已保存到相册")
                } catch (e: Exception) {
                    PopTip.show("保存失败")
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var mSelected: List<String?>
        if (requestCode == 10002 && resultCode == RESULT_OK) {
            //mActivity_qrcode_blk_pic_edit.setText(listToString02(mSelected));
            //Log.d("Matisse", "mSelected: " + mSelected);
            val fileUri = data!!.data
            blackGreyBinding?.tp!!.refreshDrawableState()
            blackGreyBinding?.tp!!.setImageBitmap(
                ImageUtils.toGray(
                    BitmapFactory.decodeFile(
                        getRealPathFromURI(
                            fileUri,
                            this
                        )
                    )
                )
            )
        } else if (requestCode == 10002 && resultCode == RESULT_CANCELED) {
            //mSelected = Matisse.Companion.obtainPathResult(data);
            makeText("已取消选择", this.window.decorView)
            //mActivity_qrcode_logo_edit.setText(listToString02(mSelected));
        }
    }

    fun listToString02(list: List<String?>?): String {
        var resultString = ""
        if (list != null) {
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

    private var toolbar: Toolbar? = null
    private var mTp: ImageView? = null
    private var mXztp: MaterialButton? = null
    private var mBctp: MaterialButton? = null
    private fun initView(activity: Activity) {
        toolbar = activity.findViewById(R.id.toolbar)
        mTp = activity.findViewById(R.id.tp)
        mXztp = activity.findViewById(R.id.xztp)
        mBctp = activity.findViewById(R.id.bctp)
    }

    companion object {
        fun getRealPathFromURI(var1: Uri?, var0: Context): String? {
            @SuppressLint("ObsoleteSdkInt") val var2 = Build.VERSION.SDK_INT >= 19
            val var3: String?
            if (var2 && DocumentsContract.isDocumentUri(var0, var1)) {
                val var4: Array<String>
                val var5: String
                if (isExternalStorageDocument(var1)) {
                    var3 = DocumentsContract.getDocumentId(var1)
                    var4 = var3.split(":").toTypedArray()
                    var5 = var4[0]
                    if ("primary".equals(var5, ignoreCase = true)) {
                        return Environment.getExternalStorageDirectory().toString() + "/" + var4[1]
                    }
                } else {
                    if (isDownloadsDocument(var1)) {
                        var3 = DocumentsContract.getDocumentId(var1)
                        val var9 = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"),
                            var3.toLong()
                        )
                        return getDataColumn(var0, var9, null, null)
                    }
                    if (isMediaDocument(var1)) {
                        var3 = DocumentsContract.getDocumentId(var1)
                        var4 = var3.split(":").toTypedArray()
                        var5 = var4[0]
                        var var6: Uri? = null
                        if ("image" == var5) {
                            var6 = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        } else if ("VIDEO" == var5) {
                            var6 = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        } else if ("AUDIO" == var5) {
                            var6 = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        }
                        val var7 = "_id=?"
                        val var8 = arrayOf(var4[1])
                        return getDataColumn(var0, var6, "_id=?", var8)
                    }
                }
            } else {
                var3 = getFPUriToPath(var0, var1)
                if (!TextUtils.isEmpty(var3)) {
                    return var3
                }
                if ("content".equals(var1!!.scheme, ignoreCase = true)) {
                    return getDataColumn(var0, var1, null, null)
                }
                if ("file".equals(var1.scheme, ignoreCase = true)) {
                    return var1.path
                }
            }
            return null
        }

        private fun isExternalStorageDocument(var0: Uri?): Boolean {
            return "com.android.externalstorage.documents" == var0!!.authority
        }

        private fun isDownloadsDocument(var0: Uri?): Boolean {
            return "com.android.providers.downloads.documents" == var0!!.authority
        }

        private fun isMediaDocument(var0: Uri?): Boolean {
            return "com.android.providers.media.documents" == var0!!.authority
        }

        fun getDataColumn(var0: Context, var1: Uri?, var2: String?, var3: Array<String>?): String? {
            var var4: Cursor? = null
            val var5 = "_data"
            val var6 = arrayOf("_data")
            val var8: String
            try {
                var4 = var0.contentResolver.query(var1!!, var6, var2, var3, null as String?)
                if (var4 == null || !var4.moveToFirst()) {
                    return null
                }
                val var7 = var4.getColumnIndexOrThrow("_data")
                var8 = var4.getString(var7)
            } finally {
                var4?.close()
            }
            return var8
        }

        private fun getFPUriToPath(var0: Context, var1: Uri?): String? {
            try {
                @SuppressLint("WrongConstant", "QueryPermissionsNeeded") val var2 =
                    var0.packageManager.getInstalledPackages(8)
                val var3 = FileProvider::class.java.name
                val var4 = var2.iterator()
                while (true) {
                    while (true) {
                        var var6: Array<ProviderInfo>
                        do {
                            if (!var4.hasNext()) {
                                return null
                            }
                            val var5 = var4.next()
                            var6 = var5.providers
                        } while (false)
                        val var7 = var6
                        val var8 = var6.size
                        for (var9 in 0 until var8) {
                            val var10 = var7[var9]
                            if (var1!!.authority == var10.authority) {
                                if (var10.name.equals(var3, ignoreCase = true)) {
                                    val var11 = FileProvider::class.java
                                    try {
                                        val var12 = var11.getDeclaredMethod(
                                            "getPathStrategy",
                                            Context::class.java,
                                            String::class.java
                                        )
                                        var12.isAccessible = true
                                        val var13 =
                                            var12.invoke(null as Any?, var0, var1.authority)
                                        if (var13 != null) {
                                            val var14 =
                                                FileProvider::class.java.name + "\$PathStrategy"
                                            val var15 = Class.forName(var14)
                                            val var16 = var15.getDeclaredMethod(
                                                "getFileForUri",
                                                Uri::class.java
                                            )
                                            var16.isAccessible = true
                                            val var17 = var16.invoke(var13, var1)
                                            if (var17 is File) {
                                                return var17.absolutePath
                                            }
                                        }
                                    } catch (var19: NoSuchMethodException) {
                                        var19.printStackTrace()
                                    }
                                }
                                break
                            }
                        }
                    }
                }
            } catch (var23: Exception) {
                var23.printStackTrace()
            }
            return null
        }
    }
}