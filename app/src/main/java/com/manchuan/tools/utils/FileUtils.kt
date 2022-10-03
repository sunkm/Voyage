package com.manchuan.tools.utils

import android.annotation.TargetApi
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import java.io.File

class FileUtils {

    fun SdCardPath(name: String): String {
        val path: String = Environment.getExternalStorageDirectory().toString() + File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator + "远航快传"
        val file = File(path)
        if (file.exists()) {
            if (!file.isDirectory) {
                file.mkdir()
            }
        } else {
            file.mkdir()
        }
        return "$path/$name"
    }

    /**
     * 根据Uri获取图片绝对路径，解决Android4.4以上版本Uri转换
     *
     * @param context
     * @param imageUri
     */
    @TargetApi(19)
    fun getAbsolutePath(context: Context?, imageUri: Uri?): String? {
        if (context == null || imageUri == null) return null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(
                context,
                imageUri)
        ) {
            if (isExternalStorageDocument(imageUri)) {
                val docId: String = DocumentsContract.getDocumentId(imageUri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else if (isDownloadsDocument(imageUri)) {
                val id: String = DocumentsContract.getDocumentId(imageUri)
                val contentUri: Uri =
                    ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(id))
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(imageUri)) {
                val docId: String = DocumentsContract.getDocumentId(imageUri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                when (type) {
                    "image" -> {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    }
                    "video" -> {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    }
                    "audio" -> {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                }
                val selection: String = MediaStore.Images.Media._ID + "=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } // MediaStore (and general)
        else if ("content".equals(imageUri.scheme, ignoreCase = true)) {
            // Return the remote address
            return if (isGooglePhotosUri(imageUri)) imageUri.lastPathSegment else getDataColumn(
                context,
                imageUri,
                null,
                null)
        } else if ("file".equals(imageUri.scheme, ignoreCase = true)) {
            return imageUri.path
        }
        return null
    }

    fun getDataColumn(
        context: Context,
        uri: Uri?,
        selection: String?,
        selectionArgs: Array<String>?,
    ): String? {
        var cursor: Cursor? = null
        val column: String = MediaStore.Images.Media.DATA
        val projection = arrayOf(column)
        try {
            cursor =
                uri?.let {
                    context.contentResolver.query(it,
                        projection,
                        selection,
                        selectionArgs,
                        null)
                }
            if (cursor != null && cursor.moveToFirst()) {
                val index: Int = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }


    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }


    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }


    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }


    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }
}