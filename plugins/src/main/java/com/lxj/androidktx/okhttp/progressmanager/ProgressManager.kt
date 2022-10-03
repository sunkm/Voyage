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
package com.lxj.androidktx.okhttp.progressmanager

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.TextUtils
import com.lxj.androidktx.okhttp.progressmanager.body.ProgressRequestBody
import com.lxj.androidktx.okhttp.progressmanager.body.ProgressResponseBody
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.util.*

/**
 * ================================================
 * ProgressManager 一行代码即可监听 App 中所有网络链接的上传以及下载进度,包括 Glide(需要将下载引擎切换为 Okhttp)的图片加载进度,
 * 基于 Okhttp 的 [Interceptor],所以使用前请确保你使用 Okhttp 或 Retrofit 进行网络请求
 * 实现原理类似 EventBus,你可在 App 中的任何地方,将多个监听器,以 `url` 地址作为标识符,注册到本管理器
 * 当此 `url` 地址存在下载或者上传的动作时,管理器会主动调用所有使用此 `url` 地址注册过的监听器,达到多个模块的同步更新
 *
 *
 * Created by JessYan on 02/06/2017 18:37
 * [Contact me](mailto:jess.yan.effort@gmail.com)
 * [Follow me](https://github.com/JessYanCoding)
 * ================================================
 */
class ProgressManager private constructor() {
    /**
     * 因为 [WeakHashMap] 将 `key` 作为弱键 (弱引用的键), 所以当 java 虚拟机 GC 时会将某个不在被引用的 `key` 回收并加入 [ReferenceQueue]
     * 在下一次操作 [WeakHashMap] 时,会比对 [ReferenceQueue] 中的 `key`
     * 将 [WeakHashMap] 中对应的 `key` 连同 `value` 一起移除,从而免去了手动 [Map.remove]
     *
     *
     * 建议你们在日常使用中,将这个 `key` 和对应的 [Activity]/[Fragment] 生命周期同步,也就使用全局变量持有
     * 最重要的是使用 String mUrl = new String("url");, 而不是 String mUrl = "url";
     * 为什么这样做? 因为如果直接使用 String mUrl = "url", 这个 `url` 字符串会被加入全局字符串常量池, 池中的字符串将不会被回收
     * 既然 `key` 没被回收, 那 [WeakHashMap] 中的值也不会被移除
     */
    private val mRequestListeners: MutableMap<String?, MutableList<ProgressListener>> =
        WeakHashMap()
    private val mResponseListeners: MutableMap<String?, MutableList<ProgressListener>> =
        WeakHashMap()
    private val mHandler //所有监听器在 Handler 中被执行,所以可以保证所有监听器在主线程中被执行
            : Handler = Handler(Looper.getMainLooper())
    private val mInterceptor: Interceptor
    private var mRefreshTime = DEFAULT_REFRESH_TIME //进度刷新时间(单位ms),避免高频率调用

    init {
        mInterceptor =
            Interceptor { chain -> wrapResponseBody(chain.proceed(wrapRequestBody(chain.request())!!)) }
    }

    /**
     * 设置 [ProgressListener.onProgress] 每次被调用的间隔时间
     *
     * @param refreshTime 间隔时间,单位毫秒
     */
    fun setRefreshTime(refreshTime: Int) {
        require(refreshTime >= 0) { "refreshTime must be >= 0" }
        mRefreshTime = refreshTime
    }

    /**
     * 将需要被监听上传进度的 `url` 注册到管理器,此操作请在页面初始化时进行,切勿多次注册同一个(内容相同)监听器
     *
     * @param url      `url` 作为标识符
     * @param listener 当此 `url` 地址存在上传的动作时,此监听器将被调用
     */
    fun addRequestListener(url: String, listener: ProgressListener) {
        checkNotNull(url, "url cannot be null")
        checkNotNull(listener, "listener cannot be null")
        var progressListeners: MutableList<ProgressListener>?
        synchronized(ProgressManager::class.java) {
            progressListeners = mRequestListeners[url]
            if (progressListeners == null) {
                progressListeners = LinkedList()
                mRequestListeners[url] = progressListeners as LinkedList<ProgressListener>
            }
        }
        progressListeners!!.add(listener)
    }

    /**
     * 将需要被监听下载进度的 `url` 注册到管理器,此操作请在页面初始化时进行,切勿多次注册同一个(内容相同)监听器
     *
     * @param url      `url` 作为标识符
     * @param listener 当此 `url` 地址存在下载的动作时,此监听器将被调用
     */
    fun addResponseListener(url: String, listener: ProgressListener) {
        checkNotNull(url, "url cannot be null")
        checkNotNull(listener, "listener cannot be null")
        var progressListeners: MutableList<ProgressListener>?
        synchronized(ProgressManager::class.java) {
            progressListeners = mResponseListeners[url]
            if (progressListeners == null) {
                progressListeners = LinkedList()
                mResponseListeners[url] = progressListeners as LinkedList<ProgressListener>
            }
        }
        progressListeners!!.add(listener)
    }

    /**
     * 当在 [ProgressRequestBody] 和 [ProgressResponseBody] 内部处理二进制流时发生错误
     * 会主动调用 [ProgressListener.onError],但是有些错误并不是在它们内部发生的
     * 但同样会引起网络请求的失败,所以向外面提供[ProgressManager.notifyOnErorr],当外部发生错误时
     * 手动调用此方法,以通知所有的监听器
     *
     * @param url `url` 作为标识符
     * @param e   错误
     */
    fun notifyOnErorr(url: String, e: Exception) {
        checkNotNull(url, "url cannot be null")
        forEachListenersOnError(mRequestListeners, url, e)
        forEachListenersOnError(mResponseListeners, url, e)
    }

    /**
     * 将 [OkHttpClient.Builder] 传入,配置一些本管理器需要的参数
     *
     * @param builder
     * @return
     */
    fun with(builder: OkHttpClient.Builder): OkHttpClient.Builder {
        checkNotNull(builder, "builder cannot be null")
        return builder
            .addNetworkInterceptor(mInterceptor)
    }

    /**
     * 将 [Request] 传入,配置一些本框架需要的参数,常用于自定义 [Interceptor]
     * 如已使用 [ProgressManager.with],就不会用到此方法
     *
     * @param request 原始的 [Request]
     * @return
     */
    fun wrapRequestBody(request: Request?): Request? {
        var request = request
        if (request == null) return request
        val key = request.url.toString()
        request = pruneIdentification(key, request)
        if (request.body == null) return request
        if (mRequestListeners.containsKey(key)) {
            val listeners: List<ProgressListener> = mRequestListeners[key]!!
            return request.newBuilder()
                .method(
                    request.method,
                    ProgressRequestBody(mHandler, request.body!!, listeners, mRefreshTime)
                )
                .build()
        }
        return request
    }

    /**
     * 如果 `url` 中含有 [.addDiffResponseListenerOnSameUrl]
     * 或 [.addDiffRequestListenerOnSameUrl] 加入的标识符,则将加入标识符
     * 从 {code url} 中删除掉,继续使用 `originUrl` 进行请求
     *
     * @param url     `url` 地址
     * @param request 原始 [Request]
     * @return 返回可能被修改过的 [Request]
     */
    private fun pruneIdentification(url: String, request: Request): Request {
        val needPrune = url.contains(IDENTIFICATION_NUMBER)
        return if (!needPrune) request else request.newBuilder()
            .url(
                url.substring(
                    0,
                    url.indexOf(IDENTIFICATION_NUMBER)
                )
            ) //删除掉标识符
            .header(
                IDENTIFICATION_HEADER,
                url
            ) //将有标识符的 url 加入 header中, 便于wrapResponseBody(Response) 做处理
            .build()
    }

    /**
     * 将 [Response] 传入,配置一些本框架需要的参数,常用于自定义 [Interceptor]
     * 如已使用 [ProgressManager.with],就不会用到此方法
     *
     * @param response 原始的 [Response]
     * @return
     */
    fun wrapResponseBody(response: Response): Response {
        var response = response
        if (response == null) return response
        var key: String? = response.request.url.toString()
        if (!TextUtils.isEmpty(response.request.header(IDENTIFICATION_HEADER))) { //从 header 中拿出有标识符的 url
            key = response.request.header(IDENTIFICATION_HEADER)
        }
        if (response.isRedirect) {
            resolveRedirect(mRequestListeners, response, key)
            val location = resolveRedirect(mResponseListeners, response, key)
            response = modifyLocation(response, location)
            return response
        }
        if (response.body == null) return response
        if (mResponseListeners.containsKey(key)) {
            val listeners: List<ProgressListener> = mResponseListeners[key]!!
            return response.newBuilder()
                .body(ProgressResponseBody(mHandler, response.body!!, listeners, mRefreshTime))
                .build()
        }
        return response
    }

    /**
     * 查看 location 是否被加入了标识符, 如果是, 则放入 [Header] 中重新定义 [Response]
     *
     * @param response 原始的 [Response]
     * @param location `location` 重定向地址
     * @return 返回可能被修改过的 [Response]
     */
    private fun modifyLocation(response: Response, location: String?): Response {
        var response = response
        if (!TextUtils.isEmpty(location) && location!!.contains(IDENTIFICATION_NUMBER)) { //将被加入标识符的新的 location 放入 header 中
            response = response.newBuilder()
                .header(LOCATION_HEADER, location)
                .build()
        }
        return response
    }

    /**
     * 当出现需要使用同一个 `url` 根据 Post 请求参数的不同而下载不同资源的情况
     * 请使用 [.addDiffResponseListenerOnSameUrl] 代替 [.addResponseListener]
     * [.addDiffResponseListenerOnSameUrl] 会返回一个加入了时间戳的新的 `url`
     * 请使用这个新的 `url` 去代替 `originUrl` 进行下载的请求即可 (当实际请求时依然使用 `originUrl` 进行网络请求)
     *
     *
     * [.addDiffResponseListenerOnSameUrl] 与 [.addDiffResponseListenerOnSameUrl]
     * 的区别在于:
     * [.addDiffResponseListenerOnSameUrl] 可以使用不同的 `key` 来自定义新的 `url`
     * [.addDiffResponseListenerOnSameUrl] 是直接使用时间戳来生成新的 `url`
     *
     *
     *
     * @param originUrl `originUrl` 作为基础并结合时间戳用于生成新的 `url` 作为标识符
     * @param listener  当加入了时间戳的新的 `url` 地址存在下载的动作时,此监听器将被调用
     * @return 加入了时间戳的新的 `url`
     */
    fun addDiffResponseListenerOnSameUrl(originUrl: String, listener: ProgressListener): String {
        return addDiffResponseListenerOnSameUrl(
            originUrl,
            (SystemClock.elapsedRealtime() + listener.hashCode()).toString(),
            listener
        )
    }

    /**
     * 当出现需要使用同一个 `url` 根据 Post 请求参数的不同而下载不同资源的情况
     * 请使用 [.addDiffResponseListenerOnSameUrl] 代替 [.addResponseListener]
     * 请使用 [.addDiffResponseListenerOnSameUrl] 会返回一个 `originUrl` 结合 `key` 生成的新的 `url`
     * 请使用这个新的 `url` 去代替 `originUrl` 进行下载的请求即可 (当实际请求时依然使用 `originUrl` 进行网络请求)
     *
     *
     * [.addDiffResponseListenerOnSameUrl] 与 [.addDiffResponseListenerOnSameUrl]
     * 的区别在于:
     * [.addDiffResponseListenerOnSameUrl] 可以使用不同的 `key` 来自定义新的 `url`
     * [.addDiffResponseListenerOnSameUrl] 是直接使用时间戳来生成新的 `url`
     *
     *
     * Example usage:
     * <pre>
     * String newUrl = ProgressManager.getInstance().addDiffResponseListenerOnSameUrl(DOWNLOAD_URL, "id", getDownloadListener());
     * new Thread(new Runnable() {
     *
     * public void run() {
     * try {
     * Request request = new Request.Builder()
     * .url(newUrl) // 请一定使用 addDiffResponseListenerOnSameUrl 返回的 newUrl 做请求
     * .build();
     *
     * Response response = mOkHttpClient.newCall(request).execute();
     * } catch (IOException e) {
     * e.printStackTrace();
     * //当外部发生错误时,使用此方法可以通知所有监听器的 onError 方法,这里也要使用 newUrl
     * ProgressManager.getInstance().notifyOnErorr(newUrl, e);
     * }
     * }
     * }).start();
    </pre> *
     *
     * @param originUrl `originUrl` 作为基础并结合 `key` 用于生成新的 `url` 作为标识符
     * @param key       `originUrl` 作为基础并结合 `key` 用于生成新的 `url` 作为标识符
     * @param listener  当 `originUrl` 结合 `key` 生成的新的 `url` 地址存在下载的动作时,此监听器将被调用
     * @return `originUrl` 结合 `key` 生成的新的 `url`
     */
    fun addDiffResponseListenerOnSameUrl(
        originUrl: String,
        key: String,
        listener: ProgressListener,
    ): String {
        val newUrl = originUrl + IDENTIFICATION_NUMBER + key
        addResponseListener(newUrl, listener)
        return newUrl
    }

    /**
     * 当出现需要使用同一个 `url` 根据 Post 请求参数的不同而上传不同资源的情况
     * 请使用 [.addDiffRequestListenerOnSameUrl] 代替 [.addRequestListener]
     * [.addDiffRequestListenerOnSameUrl] 会返回一个加入了时间戳的新的 `url`
     * 请使用这个新的 `url` 去代替 `originUrl` 进行上传的请求即可 (当实际请求时依然使用 `originUrl` 进行网络请求)
     *
     *
     * [.addDiffRequestListenerOnSameUrl] 与 [.addDiffRequestListenerOnSameUrl]
     * 的区别在于:
     * [.addDiffRequestListenerOnSameUrl] 可以使用不同的 `key` 来自定义新的 `url`
     * [.addDiffRequestListenerOnSameUrl] 是直接使用时间戳来生成新的 `url`
     *
     *
     *
     * @param originUrl `originUrl` 作为基础并结合时间戳用于生成新的 `url` 作为标识符
     * @param listener  当加入了时间戳的新的 `url` 地址存在上传的动作时,此监听器将被调用
     * @return 加入了时间戳的新的 `url`
     */
    fun addDiffRequestListenerOnSameUrl(originUrl: String, listener: ProgressListener): String {
        return addDiffRequestListenerOnSameUrl(
            originUrl,
            (SystemClock.elapsedRealtime() + listener.hashCode()).toString(),
            listener
        )
    }

    /**
     * 当出现需要使用同一个 `url` 根据 Post 请求参数的不同而上传不同资源的情况
     * 请使用 [.addDiffRequestListenerOnSameUrl] 代替 [.addRequestListener]
     * [.addDiffRequestListenerOnSameUrl] 会返回一个 `originUrl` 结合 `key` 生成的新的 `url`
     * 请使用这个新的 `url` 去代替 `originUrl` 进行上传的请求即可 (当实际请求时依然使用 `originUrl` 进行网络请求)
     *
     *
     * [.addDiffRequestListenerOnSameUrl] 与 [.addDiffRequestListenerOnSameUrl]
     * 的区别在于:
     * [.addDiffRequestListenerOnSameUrl] 可以使用不同的 `key` 来自定义新的 `url`
     * [.addDiffRequestListenerOnSameUrl] 是直接使用时间戳来生成新的 `url`
     *
     *
     * Example usage:
     * <pre>
     * String newUrl = ProgressManager.getInstance().addDiffRequestListenerOnSameUrl(UPLOAD_URL, "id", getUploadListener());
     * new Thread(new Runnable() {
     *
     * public void run() {
     * try {
     * File file = new File(getCacheDir(), "cache");
     *
     * Request request = new Request.Builder()
     * .url(newUrl) // 请一定使用 addDiffRequestListenerOnSameUrl 返回的 newUrl 做请求
     * .post(RequestBody.create(MediaType.parse("multipart/form-data"), file))
     * .build();
     *
     * Response response = mOkHttpClient.newCall(request).execute();
     * } catch (IOException e) {
     * e.printStackTrace();
     * //当外部发生错误时,使用此方法可以通知所有监听器的 onError 方法,这里也要使用 newUrl
     * ProgressManager.getInstance().notifyOnErorr(newUrl, e);
     * }
     * }
     * }).start();
    </pre> *
     *
     * @param originUrl `originUrl` 作为基础并结合 `key` 用于生成新的 `url` 作为标识符
     * @param key       `originUrl` 作为基础并结合 `key` 用于生成新的 `url` 作为标识符
     * @param listener  当 `originUrl` 结合 `key` 生成的新的 `url` 地址存在上传的动作时,此监听器将被调用
     * @return `originUrl` 结合 `key` 生成的新的 `url`
     */
    fun addDiffRequestListenerOnSameUrl(
        originUrl: String,
        key: String,
        listener: ProgressListener,
    ): String {
        val newUrl = originUrl + IDENTIFICATION_NUMBER + key
        addRequestListener(newUrl, listener)
        return newUrl
    }

    /**
     * 解决请求地址重定向后的兼容问题
     *
     * @param map      [.mRequestListeners] 或者 [.mResponseListeners]
     * @param response 原始的 [Response]
     * @param url      `url` 地址
     */
    private fun resolveRedirect(
        map: MutableMap<String?, MutableList<ProgressListener>>,
        response: Response,
        url: String?,
    ): String? {
        var location: String? = null
        val progressListeners = map[url] //查看此重定向 url ,是否已经注册过监听器
        if (progressListeners != null && progressListeners.size > 0) {
            location = response.header(LOCATION_HEADER) // 重定向地址
            if (!TextUtils.isEmpty(location)) {
                if (url!!.contains(IDENTIFICATION_NUMBER) && !location!!.contains(
                        IDENTIFICATION_NUMBER
                    )
                ) { //如果 url 有标识符,那也将标识符加入用于重定向的 location
                    location += url.substring(url.indexOf(IDENTIFICATION_NUMBER), url.length)
                }
                if (!map.containsKey(location)) {
                    map[location] = progressListeners //将需要重定向地址的监听器,提供给重定向地址,保证重定向后也可以监听进度
                } else {
                    val locationListener = map[location]!!
                    for (listener in progressListeners) {
                        if (!locationListener.contains(listener)) {
                            locationListener.add(listener)
                        }
                    }
                }
            }
        }
        return location
    }

    private fun forEachListenersOnError(
        map: Map<String?, MutableList<ProgressListener>>,
        url: String,
        e: Exception,
    ) {
        if (map.containsKey(url)) {
            val progressListeners: List<ProgressListener> = map[url]!!
            val array = progressListeners.toTypedArray()
            for (i in array.indices) {
                array[i].onError(-1, e)
            }
        }
    }

    companion object {
        @Volatile
        private var mProgressManager: ProgressManager? = null
        private const val OKHTTP_PACKAGE_NAME = "okhttp3.OkHttpClient"
        private var DEPENDENCY_OKHTTP = false
        private const val DEFAULT_REFRESH_TIME = 300
        private const val IDENTIFICATION_NUMBER = "?JessYan="
        private const val IDENTIFICATION_HEADER = "JessYan"
        private const val LOCATION_HEADER = "Location"

        init {
            val hasDependency: Boolean = try {
                Class.forName(OKHTTP_PACKAGE_NAME)
                true
            } catch (e: ClassNotFoundException) {
                false
            }
            DEPENDENCY_OKHTTP = hasDependency
        }

        //使用本管理器必须依赖 Okhttp
        val instance: ProgressManager?
            get() {
                if (mProgressManager == null) {
                    check(DEPENDENCY_OKHTTP) {  //使用本管理器必须依赖 Okhttp
                        "Must be dependency Okhttp"
                    }
                    synchronized(ProgressManager::class.java) {
                        if (mProgressManager == null) {
                            mProgressManager = ProgressManager()
                        }
                    }
                }
                return mProgressManager
            }

        fun <T> checkNotNull(`object`: T?, message: String?): T {
            if (`object` == null) {
                throw NullPointerException(message)
            }
            return `object`
        }
    }
}