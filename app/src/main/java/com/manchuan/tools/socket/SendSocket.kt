package com.manchuan.tools.socket

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.manchuan.tools.bean.FileBean
import java.io.File
import java.io.FileInputStream
import java.io.ObjectOutputStream
import java.net.InetSocketAddress
import java.net.Socket


/**
 * date：2018/2/24 on 18:10
 * description: 客户端发送的socket
 */
class SendSocket(
    private val mFileBean: FileBean, private val mAddress: String,
    /**
     * 监听发送进度
     */
    private val mlistener: ProgressSendListener?,
) {
    private var mFile: File? = null
    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                10 -> {
                    val progress = msg.obj as Int
                    mlistener?.onProgressChanged(mFile, progress)
                }
                20 -> mlistener?.onFinished(mFile)
                30 -> mlistener?.onFaliure(mFile)
            }
        }
    }

    fun createSendSocket() {
        try {
            val socket = Socket()
            val inetSocketAddress = InetSocketAddress(mAddress, PORT)
            socket.connect(inetSocketAddress)
            val outputStream = socket.getOutputStream()
            val objectOutputStream = ObjectOutputStream(outputStream)
            objectOutputStream.writeObject(mFileBean)
            mFile = File(mFileBean.filePath)
            val inputStream = FileInputStream(mFile)
            val size = mFileBean.fileLength
            var total: Long = 0
            val bytes = ByteArray(1024)
            var len: Int
            while (inputStream.read(bytes).also { len = it } != -1) {
                outputStream.write(bytes, 0, len)
                total += len.toLong()
                val progress = (total * 100 / size).toInt()
                Log.e(TAG, "文件发送进度：$progress")
                val message = Message.obtain()
                message.what = 10
                message.obj = progress
                mHandler.sendMessage(message)
            }
            outputStream.close()
            objectOutputStream.close()
            inputStream.close()
            socket.close()
            mHandler.sendEmptyMessage(20)
            Log.e(TAG, "文件发送成功")
        } catch (e: Exception) {
            mHandler.sendEmptyMessage(30)
            Log.e(TAG, "文件发送异常")
        }
    }

    interface ProgressSendListener {
        //当传输进度发生变化时
        fun onProgressChanged(file: File?, progress: Int)

        //当传输结束时
        fun onFinished(file: File?)

        //传输失败时
        fun onFaliure(file: File?)
    }

    companion object {
        const val TAG = "SendSocket"
        const val PORT = 10000
    }
}

