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
import okhttp3.ResponseBody
import okio.*
import java.io.IOException

/**
 * ================================================
 * 继承于 [ResponseBody], 通过此类获取 Okhttp 下载的二进制数据
 *
 *
 * Created by JessYan on 02/06/2017 18:25
 * [Contact me](mailto:jess.yan.effort@gmail.com)
 * [Follow me](https://github.com/JessYanCoding)
 * ================================================
 */
class ProgressResponseBody(
    handler: Handler,
    protected val mDelegate: ResponseBody,
    listeners: List<ProgressListener>,
    refreshTime: Int
) : ResponseBody() {
    protected var mHandler: Handler
    protected var mRefreshTime: Int
    protected val mListeners: Array<ProgressListener>?
    protected val mProgressInfo: ProgressInfo
    private var mBufferedSource: BufferedSource? = null

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
        return mDelegate.contentLength()
    }

    override fun source(): BufferedSource {
        if (mBufferedSource == null) {
            mBufferedSource = source(mDelegate.source()).buffer()
        }
        return mBufferedSource as BufferedSource
    }

    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            private var totalBytesRead = 0L
            private var lastRefreshTime = 0L //最后一次刷新的时间
            private var tempSize = 0L
            @Throws(IOException::class)
            override fun read(sink: Buffer, byteCount: Long): Long {
                var bytesRead = 0L
                bytesRead = try {
                    super.read(sink, byteCount)
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
                // read() returns the number of bytes read, or -1 if this source is exhausted.
                totalBytesRead += if (bytesRead != -1L) bytesRead else 0
                tempSize += if (bytesRead != -1L) bytesRead else 0
                if (mListeners != null) {
                    val curTime = SystemClock.elapsedRealtime()
                    if (curTime - lastRefreshTime >= mRefreshTime || bytesRead == -1L || totalBytesRead == mProgressInfo.contentLength) {
                        val finalBytesRead = bytesRead
                        val finalTempSize = tempSize
                        val finalTotalBytesRead = totalBytesRead
                        val finalIntervalTime = curTime - lastRefreshTime
                        for (i in mListeners.indices) {
                            val listener = mListeners[i]
                            mHandler.post { // Runnable 里的代码是通过 Handler 执行在主线程的,外面代码可能执行在其他线程
                                // 所以我必须使用 final ,保证在 Runnable 执行前使用到的变量,在执行时不会被修改
                                mProgressInfo.eachBytes =
                                    if (finalBytesRead != -1L) finalTempSize else -1
                                mProgressInfo.currentbytes = finalTotalBytesRead
                                mProgressInfo.intervalTime = finalIntervalTime
                                mProgressInfo.isFinish =
                                    finalBytesRead == -1L && finalTotalBytesRead == mProgressInfo.contentLength
                                listener.onProgress(mProgressInfo)
                            }
                        }
                        lastRefreshTime = curTime
                        tempSize = 0
                    }
                }
                return bytesRead
            }
        }
    }
}