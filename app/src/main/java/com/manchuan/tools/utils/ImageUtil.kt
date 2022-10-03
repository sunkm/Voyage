package com.manchuan.tools.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ImageUtil {
    /**
     * 保存图片到指定路径
     *
     * @param context
     * @param bitmap   要保存的图片
     * @param fileName 自定义图片名称
     */

    fun SaveImageToPixel(context: Context, bitmap: Bitmap, fileName: String?) {
        // 保存图片至指定路径
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val contentResolver = context.contentResolver
            val contentValues = ContentValues()
            contentValues.put(
                MediaStore.Images.ImageColumns.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + File.separator + "Flipped"
            )
            contentValues.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, fileName)
            //图片需要多传一个参数，声明图片的类型，
            contentValues.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/png")
            contentValues.put(MediaStore.Downloads.TITLE, fileName)
            val uri =
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            // 所有文件的创建都需要写数据，如果不写数据那么就生成不了文件
            try {
                val outputStream = contentResolver.openOutputStream(uri!!)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            // Android 11 以下
            val storePath =
                Environment.getExternalStorageDirectory().absolutePath + File.separator + "HaiYan" + File.separator + "pixel"
            val appDir = File(storePath)
            if (!appDir.exists()) {
                appDir.mkdir()
            }
            val file = File(appDir, fileName)
            try {
                val fos = FileOutputStream(file)
                //通过io流的方式来压缩保存图片(80代表压缩20%)
                val isSuccess = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.flush()
                fos.close()

                //发送广播通知系统图库刷新数据
                val uri = Uri.fromFile(file)
                context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun SaveImageToSkect(context: Context, bitmap: Bitmap, fileName: String?) {
        // 保存图片至指定路径
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val contentResolver = context.contentResolver
            val contentValues = ContentValues()
            contentValues.put(
                MediaStore.Images.ImageColumns.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + File.separator + "Flipped"
            )
            contentValues.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, fileName)
            //图片需要多传一个参数，声明图片的类型，
            contentValues.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/png")
            contentValues.put(MediaStore.Downloads.TITLE, fileName)
            val uri =
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            // 所有文件的创建都需要写数据，如果不写数据那么就生成不了文件
            try {
                val outputStream = contentResolver.openOutputStream(uri!!)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            // Android 11 以下
            val storePath =
                Environment.getExternalStorageDirectory().absolutePath + File.separator + "HaiYan" + File.separator + "pixel"
            val appDir = File(storePath)
            if (!appDir.exists()) {
                appDir.mkdir()
            }
            val file = File(appDir, fileName)
            try {
                val fos = FileOutputStream(file)
                //通过io流的方式来压缩保存图片(80代表压缩20%)
                val isSuccess = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.flush()
                fos.close()

                //发送广播通知系统图库刷新数据
                val uri = Uri.fromFile(file)
                context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth, drawable.intrinsicHeight,
            if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return bitmap
    }

    fun SaveImageToGrey(context: Context, bitmap: Bitmap, fileName: String?) {
        // 保存图片至指定路径
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val contentResolver = context.contentResolver
            val contentValues = ContentValues()
            contentValues.put(
                MediaStore.Images.ImageColumns.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + File.separator + "Flipped"
            )
            contentValues.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, fileName)
            //图片需要多传一个参数，声明图片的类型，
            contentValues.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/png")
            contentValues.put(MediaStore.Downloads.TITLE, fileName)
            val uri =
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            // 所有文件的创建都需要写数据，如果不写数据那么就生成不了文件
            try {
                val outputStream = contentResolver.openOutputStream(uri!!)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            // Android 11 以下
            val storePath =
                Environment.getExternalStorageDirectory().absolutePath + File.separator + "HaiYan" + File.separator + "grey"
            val appDir = File(storePath)
            if (!appDir.exists()) {
                appDir.mkdir()
            }
            val file = File(appDir, fileName)
            try {
                val fos = FileOutputStream(file)
                //通过io流的方式来压缩保存图片(80代表压缩20%)
                val isSuccess = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.flush()
                fos.close()

                //发送广播通知系统图库刷新数据
                val uri = Uri.fromFile(file)
                context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun SaveImageToDaily(context: Context, bitmap: Bitmap, fileName: String?) {
        // 保存图片至指定路径
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val contentResolver = context.contentResolver
            val contentValues = ContentValues()
            contentValues.put(
                MediaStore.Images.ImageColumns.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + File.separator + "Flipped"
            )
            contentValues.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, fileName)
            //图片需要多传一个参数，声明图片的类型，
            contentValues.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/png")
            contentValues.put(MediaStore.Downloads.TITLE, fileName)
            val uri =
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            // 所有文件的创建都需要写数据，如果不写数据那么就生成不了文件
            try {
                val outputStream = contentResolver.openOutputStream(uri!!)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            // Android 11 以下
            val storePath =
                Environment.getExternalStorageDirectory().absolutePath + File.separator + "HaiYan" + File.separator + "daily"
            val appDir = File(storePath)
            if (!appDir.exists()) {
                appDir.mkdir()
            }
            val file = File(appDir, fileName)
            try {
                val fos = FileOutputStream(file)
                //通过io流的方式来压缩保存图片(80代表压缩20%)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.flush()
                fos.close()

                //发送广播通知系统图库刷新数据
                val uri = Uri.fromFile(file)
                context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun saveImageToGallery(context: Context, bitmap: Bitmap, fileName: String?) {
        // 保存图片至指定路径
        // Android 11
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val contentResolver = context.contentResolver
            val contentValues = ContentValues()
            contentValues.put(
                MediaStore.Images.ImageColumns.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + File.separator + "Flipped"
            )
            contentValues.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, fileName)
            //图片需要多传一个参数，声明图片的类型，
            contentValues.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/png")
            contentValues.put(MediaStore.Downloads.TITLE, fileName)
            val uri =
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            // 所有文件的创建都需要写数据，如果不写数据那么就生成不了文件
            try {
                val outputStream = contentResolver.openOutputStream(uri!!)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            // Android 11 以下
            val storePath =
                Environment.getExternalStorageDirectory().absolutePath + File.separator + "HaiYan" + File.separator + "qrcode"
            val appDir = File(storePath)
            if (!appDir.exists()) {
                appDir.mkdir()
            }
            val file = File(appDir, fileName)
            try {
                val fos = FileOutputStream(file)
                //通过io流的方式来压缩保存图片(80代表压缩20%)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.flush()
                fos.close()

                //发送广播通知系统图库刷新数据
                val uri = Uri.fromFile(file)
                context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}