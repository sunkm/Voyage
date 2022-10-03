package com.manchuan.tools.activity

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.*
import android.media.MediaScannerConnection
import android.os.*
import android.provider.MediaStore
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.gyf.immersionbar.ImmersionBar
import com.kongzue.dialogx.dialogs.InputDialog
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.R
import com.manchuan.tools.colorpicker.ColorPickerView
import com.manchuan.tools.colorpicker.builder.ColorPickerDialogBuilder
import com.manchuan.tools.databinding.ActivityPhotoTextBinding
import com.manchuan.tools.utils.ColorUtils
import com.manchuan.tools.utils.MediaScanner
import com.manchuan.tools.utils.SnackToast.makeText
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar
import rikka.material.app.MaterialActivity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("NonConstantResourceId")
class PhotoTextActivity : MaterialActivity() {
    private val text: String? = null
    private var convertThread: ConvertThread? = null
    private var input: File? = null
    private var color = -0x1000000
    private var textBinding: ActivityPhotoTextBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        textBinding = ActivityPhotoTextBinding.inflate(LayoutInflater.from(this))
        setContentView(textBinding?.root)
        toolbar = textBinding?.toolbar
        xztp = textBinding?.xztp
        bctp = textBinding?.bctp
        nr = textBinding?.nr
        chip2 = textBinding?.chip2
        edit = textBinding?.edit
        seekbar1 = textBinding?.seekbar1
        tp = textBinding?.tp
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        ImmersionBar.with(this).statusBarColorInt(ColorUtils.statusBarColor)
            .autoDarkModeEnable(true)
            .titleBar(toolbar).transparentNavigationBar().init()
        //loading = new LoadingDialog(this);
        xztp!!.setOnClickListener { view: View? ->
            input = null
            ImagePicker.with(this).galleryOnly().start(10002)
        }
        bctp!!.setOnClickListener { view: View? ->
            if (tp!!.drawable == null) {
                makeText("请先选择图片", this@PhotoTextActivity.window.decorView)
            } else if (nr!!.text.toString() == "点击输入内容" || nr!!.text.toString().isEmpty()) {
                PopTip.show("请输入内容")
            } else if (tp!!.drawable != null && nr!!.text.toString() != "点击输入内容" && nr!!.text.toString()
                    .isNotEmpty()
            ) {
                WaitDialog.show("生成中...")
                val output = outputFile
                convertThread = ConvertThread(
                    handler,
                    input,
                    output,
                    color,
                    nr!!.text.toString(),
                    seekbar1!!.progress
                )
                convertThread!!.start()
            }
        }
        edit!!.setOnClickListener {
            InputDialog("文字内容", null, "确定", "取消", null)
                .setCancelable(false)
                .setOkButton { baseDialog: InputDialog?, v: View?, inputStr: String? ->
                    //toast("输入的内容：" + inputStr);
                    nr!!.text = inputStr
                    false
                }
                .show()
        }
        chip2!!.setOnClickListener { view: View? ->
            ColorPickerDialogBuilder
                .with(this@PhotoTextActivity)
                .setTitle("选择颜色")
                .showColorEdit(true)
                .initialColor(resources.getColor(R.color.backgroundColor))
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setPositiveButton("确定") { dialog: DialogInterface?, selectedColor: Int, allColors: Array<Int?>? ->
                    //chip2.setChipIconTint(new ColorStateList(null,new int[]{selectedColor}));
                    color = selectedColor
                }
                .setNegativeButton("取消") { dialog: DialogInterface?, which: Int -> }
                .build()
                .show()
        }
    }

    // Android 11 以下
    private val outputFile: File?
        get() {
            val dir: File
            var file: File? = null
            outputname = "PHOTO_TEXT_" + System.currentTimeMillis() + ".png"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                file = File(
                    Environment.getExternalStorageDirectory()
                        .toString() + File.separator + Environment.DIRECTORY_PICTURES, outputname
                )
            } else {
                dir = File(
                    Environment.getExternalStorageDirectory()
                        .toString() + File.separator + "HaiYan" + File.separator + "photoText"
                )
                // Android 11 以下
                if (!dir.exists()) {
                    if (!dir.mkdirs()) {
                        return null
                    }
                }
                val name = "PHOTO_TEXT_" + System.currentTimeMillis() + ".png"
                file = File(dir, name)
            }
            return file
        }
    private var outputname: String? = null

    @SuppressLint("HandlerLeak")
    private val handler: Handler = object : Handler() {
        private val mediaScanner: MediaScanner? = null
        override fun handleMessage(msg: Message) {
            if (msg.what == 1) {
                val data = msg.obj as ByteArray
                TipDialog.show("已保存", WaitDialog.TYPE.SUCCESS)
                val isFinish = true
                MediaScannerConnection.scanFile(
                    this@PhotoTextActivity, arrayOf(
                        outputFile.toString()
                    ), null, null
                )
            }
        }

        /**
         * android 11及以上保存图片到相册
         * @param context
         */
        @RequiresApi(api = Build.VERSION_CODES.Q)
        fun saveImageToGallery2(context: Context) {
            val mImageTime = System.currentTimeMillis()
            @SuppressLint("SimpleDateFormat") val imageDate =
                SimpleDateFormat("yyyyMMdd-HHmmss").format(
                    Date(mImageTime)
                )
            val SCREENSHOT_FILE_NAME_TEMPLATE = outputname //图片名称，以"winetalk"+时间戳命名
            val mImageFileName = String.format(SCREENSHOT_FILE_NAME_TEMPLATE!!, imageDate)
            val values = ContentValues()
            values.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.getExternalStorageDirectory()
                    .toString() + File.separator + Environment.DIRECTORY_PICTURES
            ) //Environment.DIRECTORY_SCREENSHOTS:截图,图库中显示的文件夹名。"dh"
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, mImageFileName)
            values.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            values.put(MediaStore.MediaColumns.DATE_ADDED, mImageTime / 1000)
            values.put(MediaStore.MediaColumns.DATE_MODIFIED, mImageTime / 1000)
            values.put(
                MediaStore.MediaColumns.DATE_EXPIRES,
                (mImageTime + DateUtils.DAY_IN_MILLIS) / 1000
            )
            values.put(MediaStore.MediaColumns.IS_PENDING, 1)
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        }
    }

    private class ConvertThread : Thread {
        private var handler: Handler
        private var `in`: File?
        private var out: File?
        private var text: String? = null
        private var fontSize: Int
        private var style = 0
        private var backColor = 0

        constructor(
            handler: Handler,
            `in`: File?,
            out: File?,
            backColor: Int,
            text: String?,
            fontSize: Int,
        ) {
            this.handler = handler
            this.`in` = `in`
            this.out = out
            this.text = text
            this.fontSize = fontSize
            style = 0
            this.backColor = backColor
        }

        constructor(handler: Handler, `in`: File?, out: File?, fontSize: Int) {
            this.handler = handler
            this.`in` = `in`
            this.out = out
            this.fontSize = fontSize
            style = 1
        }

        override fun run() {
            val data = convert(`in`, out, text, fontSize)
            handler.sendMessage(handler.obtainMessage(1, data))
        }

        /**
         * 转换
         *
         * @param input
         * @param output
         * @param text
         * @param fontSize
         */
        private fun convert(input: File?, output: File?, text: String?, fontSize: Int): ByteArray? {
            val bitmap = BitmapFactory.decodeFile(input!!.absolutePath)
            var target: Bitmap? = null
            target = if (style == 0) {
                getTextBitmap(bitmap, backColor, text, fontSize)
            } else {
                getBlockBitmap(bitmap, fontSize)
            }
            var fileOutputStream: FileOutputStream? = null
            var byteArrayOutputStream: ByteArrayOutputStream? = null
            try {
                fileOutputStream = FileOutputStream(output)
                byteArrayOutputStream = ByteArrayOutputStream()
                target.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                val data = byteArrayOutputStream.toByteArray()
                fileOutputStream.write(data, 0, data.size)
                fileOutputStream.flush()
                return data
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                if (byteArrayOutputStream != null) {
                    try {
                        byteArrayOutputStream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            return null
        }
    }

    fun listToString02(list: List<String?>?): String {
        var resultString = ""
        if (list.isNullOrEmpty()) {
            println("list内容为空！")
        } else {
            val sb = StringBuilder()
            var flag = false
            for (str in list!!) {
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

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        supportActionBar?.title = "图片文字化"
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
        if (requestCode == 10002 && resultCode == RESULT_OK) {
            val fileUri = data!!.data
            //mActivity_qrcode_blk_pic_edit.setText(listToString02(mSelected));
            //Log.d("Matisse", "mSelected: " + mSelected);
            tp!!.isDrawingCacheEnabled = true
            tp!!.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
            Glide.with(this@PhotoTextActivity).load(
                BitmapFactory.decodeFile(
                    BlackGreyPhotoActivity.getRealPathFromURI(
                        fileUri,
                        this
                    )
                )
            ).skipMemoryCache(true).into(
                tp!!
            )
            //tp.setImageBitmap();
            bctp!!.visibility = View.VISIBLE
            input = File(
                Objects.requireNonNull(
                    BlackGreyPhotoActivity.getRealPathFromURI(
                        fileUri,
                        this
                    )
                )
            )
        } else if (requestCode == 10002 && resultCode == RESULT_CANCELED) {
            //mSelected = Matisse.Companion.obtainPathResult(data);
            makeText("已取消选择", this.window.decorView)
            //mActivity_qrcode_logo_edit.setText(listToString02(mSelected));
        }
    }

    private var toolbar: Toolbar? = null
    private var tp: ImageView? = null
    private var xztp: MaterialButton? = null
    private var bctp: MaterialButton? = null
    private var chip2: Chip? = null
    private var edit: MaterialCardView? = null
    private var nr: TextView? = null
    private var seekbar1: DiscreteSeekBar? = null

    companion object {
        fun getBlockBitmap(bitmap: Bitmap?, blockSize: Int): Bitmap {
            requireNotNull(bitmap) { "Bitmap cannot be null." }
            val picWidth = bitmap.width
            val picHeight = bitmap.height
            val back = Bitmap.createBitmap(
                if (bitmap.width % blockSize == 0) bitmap.width else bitmap.width / blockSize * blockSize,
                if (bitmap.height % blockSize == 0) bitmap.height else bitmap.height / blockSize * blockSize,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(back)
            canvas.drawColor(0xfff)
            var y = 0
            while (y < picHeight) {
                var x = 0
                while (x < picWidth) {
                    val colors = getPixels(bitmap, x, y, blockSize, blockSize)
                    val paint = Paint()
                    paint.isAntiAlias = true
                    paint.color = getAverage(colors)
                    paint.style = Paint.Style.FILL
                    val left = x
                    val top = y
                    val right = x + blockSize
                    val bottom = y + blockSize
                    canvas.drawRect(
                        left.toFloat(),
                        top.toFloat(),
                        right.toFloat(),
                        bottom.toFloat(),
                        paint
                    )
                    x += blockSize
                }
                y += blockSize
            }
            return back
        }

        fun getTextBitmap(bitmap: Bitmap?, backColor: Int, text: String?, fontSize: Int): Bitmap {
            requireNotNull(bitmap) { "Bitmap cannot be null." }
            val picWidth = bitmap.width
            val picHeight = bitmap.height
            val back = Bitmap.createBitmap(
                if (bitmap.width % fontSize == 0) bitmap.width else bitmap.width / fontSize * fontSize,
                if (bitmap.height % fontSize == 0) bitmap.height else bitmap.height / fontSize * fontSize,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(back)
            canvas.drawColor(backColor)
            var idx = 0
            var y = 0
            while (y < picHeight) {
                var x = 0
                while (x < picWidth) {
                    val colors = getPixels(bitmap, x, y, fontSize, fontSize)
                    val paint = Paint()
                    paint.isAntiAlias = true
                    paint.color = getAverage(colors)
                    paint.textSize = fontSize.toFloat()
                    val fontMetrics = paint.fontMetrics
                    val padding =
                        if (y == 0) fontSize + fontMetrics.ascent else (fontSize + fontMetrics.ascent) * 2
                    canvas.drawText(text!![idx++].toString(), x.toFloat(), y - padding, paint)
                    if (idx == text.length) {
                        idx = 0
                    }
                    x += fontSize
                }
                y += fontSize
            }
            return back
        }

        private fun getPixels(bitmap: Bitmap, x: Int, y: Int, w: Int, h: Int): IntArray {
            val colors = IntArray(w * h)
            var idx = 0
            var i = y
            while (i < h + y && i < bitmap.height) {
                var j = x
                while (j < w + x && j < bitmap.width) {
                    val color = bitmap.getPixel(j, i)
                    colors[idx++] = color
                    j++
                }
                i++
            }
            return colors
        }

        /**
         * 求取多个颜色的平均值
         *
         * @param colors
         * @return
         */
        private fun getAverage(colors: IntArray): Int {
            //int alpha=0;
            var red = 0
            var green = 0
            var blue = 0
            for (color in colors) {
                red += color and 0xff0000 shr 16
                green += color and 0xff00 shr 8
                blue += color and 0x0000ff
            }
            val len = colors.size.toFloat()
            //alpha=Math.round(alpha/len);
            red = Math.round(red / len)
            green = Math.round(green / len)
            blue = Math.round(blue / len)
            return Color.argb(0xff, red, green, blue)
        }

        private fun log(log: String) {
            println("-------->Utils:$log")
        }
    }
}