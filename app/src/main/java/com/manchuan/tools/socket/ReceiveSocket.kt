package com.manchuan.tools.socket

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.manchuan.tools.bean.FileBean
import com.manchuan.tools.utils.FileUtils
import com.manchuan.tools.utils.Md5Util
import java.io.*
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

class ReceiveSocket {
    private var mServerSocket: ServerSocket? = null
    private var mSocket: Socket? = null
    private var mInputStream: InputStream? = null
    private var mObjectInputStream: ObjectInputStream? = null
    private var mFileOutputStream: FileOutputStream? = null
    private var mFile: File? = null
    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                40 -> if (mListener != null) {
                    mListener!!.onSatrt()
                }
                50 -> {
                    val progress = msg.obj as Int
                    if (mListener != null) {
                        mListener!!.onProgressChanged(mFile, progress)
                    }
                }
                60 -> if (mListener != null) {
                    mListener!!.onFinished(mFile)
                }
                70 -> if (mListener != null) {
                    mListener!!.onFaliure(mFile)
                }
            }
        }
    }

    fun createServerSocket() {
        try {
            mServerSocket = ServerSocket()
            mServerSocket!!.reuseAddress = true
            mServerSocket!!.bind(InetSocketAddress(PORT))
            mSocket = mServerSocket!!.accept()
            Log.e(TAG, "客户端IP地址 : " + mSocket!!.remoteSocketAddress)
            mInputStream = mSocket!!.getInputStream()
            mObjectInputStream = ObjectInputStream(mInputStream)
            val fileBean: FileBean = mObjectInputStream!!.readObject() as FileBean
            val name: String = File(fileBean.filePath).name
            Log.e(TAG, "客户端传递的文件名称 : $name")
            Log.e(TAG, "客户端传递的MD5 : " + fileBean.md5)
            mFile = File(FileUtils().SdCardPath(name))
            mFileOutputStream = FileOutputStream(mFile)
            //开始接收文件
            mHandler.sendEmptyMessage(40)
            val bytes = ByteArray(1024)
            var len: Int
            var total: Long = 0
            var progress: Int
            while (mInputStream!!.read(bytes).also { len = it } != -1) {
                mFileOutputStream!!.write(bytes, 0, len)
                total += len.toLong()
                progress = (total * 100 / fileBean.fileLength).toInt()
                Log.e(TAG, "文件接收进度: $progress")
                val message: Message = Message.obtain()
                message.what = 50
                message.obj = progress
                mHandler.sendMessage(message)
            }
            //新写入文件的MD5
            val md5New: String = Md5Util.getMd5(mFile)
            //发送过来的MD5
            val md5Old: String = fileBean.md5
            if (md5New == md5Old) {
                mHandler.sendEmptyMessage(60)
                Log.e(TAG, "文件接收成功")
            }
            mServerSocket!!.close()
            mInputStream!!.close()
            mObjectInputStream!!.close()
            mFileOutputStream!!.close()
        } catch (e: Exception) {
            mHandler.sendEmptyMessage(70)
            Log.e(TAG, "文件接收异常:${e.message}")
        }
    }

    /**
     * 监听接收进度
     */
    private var mListener: ProgressReceiveListener? = null
    fun setOnProgressReceiveListener(listener: ProgressReceiveListener?) {
        mListener = listener
    }

    interface ProgressReceiveListener {
        //开始传输
        fun onSatrt()

        //当传输进度发生变化时
        fun onProgressChanged(file: File?, progress: Int)

        //当传输结束时
        fun onFinished(file: File?)

        //传输失败回调
        fun onFaliure(file: File?)
    }

    /**
     * 服务断开：释放内存
     */
    fun clean() {
        if (mServerSocket != null) {
            try {
                mServerSocket!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        if (mInputStream != null) {
            try {
                mInputStream!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        if (mObjectInputStream != null) {
            try {
                mObjectInputStream!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        if (mFileOutputStream != null) {
            try {
                mFileOutputStream!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        const val TAG = "ReceiveSocket"
        const val PORT = 10000
    }
}
