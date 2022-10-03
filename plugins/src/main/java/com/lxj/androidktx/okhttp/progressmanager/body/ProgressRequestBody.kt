/*
 * Copyright 2017 JessYan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lxj.androidktx.okhttp.progressmanager.body

import android.os.Handler
import android.os.SystemClock
import com.lxj.androidktx.okhttp.progressmanager.ProgressListener
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.*
import java.io.IOException

/**
 * ================================================
 * 继承于 [RequestBody], 通过此类获取 Okhttp 上传的二进制数据
 *
 *
 * Created by JessYan on 02/06/2017 18:05
 * [Contact me](mailto:jess.yan.effort@gmail.com)
 * [Follow me](https://github.com/JessYanCoding)
 * ================================================
 */
class ProgressRequestBody(
    handler: Handler,
    protected val mDelegate: RequestBody,
    listeners: List<ProgressListener>,
    refreshTime: Int
) : RequestBody() {
    protected var mHandler: Handler
    protected var mRefreshTime: Int
    protected val mListeners: Array<ProgressListener>?
    protected val mProgressInfo: ProgressInfo
    private var mBufferedSink: BufferedSink? = null

    init {
        mListeners = listeners.toTypedArray()
        mHandler = handler
        mRefreshTime = refreshTime
        mProgressInfo = ProgressInfo(System.currentTimeMillis())
    }

    override fun contentType(): MediaType? {
        return mDelegate.contentType()
    }

    override fun contentLength(): Long {
        try {
            return mDelegate.contentLength()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return -1
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        if (mBufferedSink == null) {
            mBufferedSink = CountingSink(sink).buffer()
        }
        try {
            mDelegate.writeTo(mBufferedSink!!)
            mBufferedSink!!.flush()
        } catch (e: IOException) {
            e.printStackTrace()
            var i = 0
            while (i < mListeners!!.size) {
                mListeners[i].onError(mProgressInfo.id, e)
                i++
            }
            throw e
        }
    }

    protected inner class CountingSink(delegate: Sink?) : ForwardingSink(delegate!!) {
        private var totalBytesRead = 0L
        private var lastRefreshTime = 0L //最后一次刷新的时间
        private var tempSize = 0L
        @Throws(IOException::class)
        override fun write(source: Buffer, byteCount: Long) {
            try {
                super.write(source, byteCount)
            } catch (e: IOException) {
                e.printStackTrace()
                var i = 0
                while (i < mListeners!!.size) {
                    mListeners[i].onError(mProgressInfo.id, e)
                    i++
                }
                throw e
            }
            if (mProgressInfo.contentLength == 0L) { //避免重复调用 contentLength()
                mProgressInfo.contentLength = contentLength()
            }
            totalBytesRead += byteCount
            tempSize += byteCount
            if (mListeners != null) {
                val curTime = SystemClock.elapsedRealtime()
                if (curTime - lastRefreshTime >= mRefreshTime || totalBytesRead == mProgressInfo.contentLength) {
                    val finalTempSize = tempSize
                    val finalTotalBytesRead = totalBytesRead
                    val finalIntervalTime = curTime - lastRefreshTime
                    for (i in mListeners.indices) {
                        val listener = mListeners[i]
                        mHandler.post { // Runnable 里的代码是通过 Handler 执行在主线程的,外面代码可能执行在其他线程
                            // 所以我必须使用 final ,保证在 Runnable 执行前使用到的变量,在执行时不会被修改
                            mProgressInfo.eachBytes = finalTempSize
                            mProgressInfo.currentbytes = finalTotalBytesRead
                            mProgressInfo.intervalTime = finalIntervalTime
                            mProgressInfo.isFinish =
                                finalTotalBytesRead == mProgressInfo.contentLength
                            listener.onProgress(mProgressInfo)
                        }
                    }
                    lastRefreshTime = curTime
                    tempSize = 0
                }
            }
        }
    }
}