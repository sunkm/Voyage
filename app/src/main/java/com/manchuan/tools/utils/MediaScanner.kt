package com.manchuan.tools.utils

import android.content.Context
import android.media.MediaScannerConnection.MediaScannerConnectionClient
import android.media.MediaScannerConnection
import android.net.Uri

class MediaScanner(context: Context?) : MediaScannerConnectionClient {
    private val conn: MediaScannerConnection = MediaScannerConnection(context, this)
    private var filePath: String? = null
    private var mimeType: String? = null

    init {
        conn.connect()
    }

    fun scanFile(path: String?, mime: String?) {
        if (conn.isConnected) conn.scanFile(path, mime) else {
            filePath = path
            mimeType = mime
        }
    }

    override fun onMediaScannerConnected() {
        if (filePath != null) conn.scanFile(filePath, mimeType)
        filePath = null
        mimeType = null
    }

    override fun onScanCompleted(path: String, uri: Uri) {}
}