package com.lxj.androidktx.okhttp

import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.NetworkUtils
import com.lxj.androidktx.core.toBean
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.net.URLConnection.getFileNameMap


/**
 * http扩展，使用起来像这样：
 * 协程中使用：    "http://www.baidu.com".http().get<Bean>().await()
 * 非协程中使用：  "http://www.baidu.com".http().get<Bean>(callback)
 * Create by lxj, at 2018/12/19
 * @param httpTag 请求的tag，用于取消请求的时候可以调用
 * @param baseUrlTag baseUrlTag和baseUrl一一对应，可以实现多个baseUrl；如果不想带baseUrl，可以传 OkExt.NoBaseUrl
 */
fun String.http(httpTag: Any = this, baseUrlTag: String = OkExt.DefaultUrlTag): RequestWrapper {
    val baseUrl = OkExt.baseUrlMap[baseUrlTag]
    if(baseUrlTag!= OkExt.NoBaseUrl && baseUrl==null){
        LogUtils.wTag("OkExt","tag为[${baseUrlTag}]的baseUrl不存在，请先调用OkExt.baseUrl(tag, url)方法进行配置")
    }
    return RequestWrapper(httpTag, url = "${baseUrl ?: ""}${this}")
}

/**
 * get请求，需在协程中使用。结果为空即为http请求失败，并会将失败信息打印日志。
 */
inline fun <reified T> RequestWrapper.get(): Deferred<T?> {
    return defferedRequest(buildGetRequest(), this)
}

/**
 * callback style，不在协程中使用
 */
inline fun <reified T> RequestWrapper.get(cb: HttpCallback<T>) {
    callbackRequest(buildGetRequest(), cb, this)
}

/**
 * 同步请求
 */
inline fun <reified T> RequestWrapper.getSync() = syncRequest<T>(buildGetRequest(), this)

/**
 * post请求，需在协程中使用。结果为空即为http请求失败，并会将失败信息打印日志。
 */
inline fun <reified T> RequestWrapper.post(): Deferred<T?> {
    return defferedRequest(buildPostRequest(), this)
}

/**
 * callback style，不在协程中使用
 */
inline fun <reified T> RequestWrapper.post(cb: HttpCallback<T>) {
    callbackRequest(buildPostRequest(), cb, this)
}

/**
 * 同步请求
 */
inline fun <reified T> RequestWrapper.postSync() = syncRequest<T>(buildPostRequest(), this)

/**
 * put请求，需在协程中使用。结果为空即为http请求失败，并会将失败信息打印日志。
 */
inline fun <reified T> RequestWrapper.put(): Deferred<T?> {
    return defferedRequest(buildPutRequest(), this)
}

/**
 * callback style，不在协程中使用
 */
inline fun <reified T> RequestWrapper.put(cb: HttpCallback<T>) {
    callbackRequest(buildPutRequest(), cb, this)
}

/**
 * 同步请求
 */
inline fun <reified T> RequestWrapper.putSync() = syncRequest<T>(buildPutRequest(), this)

/**
 * delete请求，需在协程中使用。结果为空即为http请求失败，并会将失败信息打印日志。
 */
inline fun <reified T> RequestWrapper.delete(): Deferred<T?> {
    return defferedRequest(buildDeleteRequest(), this)
}

/**
 * callback style，不在协程中使用
 */
inline fun <reified T> RequestWrapper.delete(cb: HttpCallback<T>) {
    callbackRequest(buildDeleteRequest(), cb, this)
}

/**
 * 同步请求
 */
inline fun <reified T> RequestWrapper.deleteSync() = syncRequest<T>(buildDeleteRequest(), this)

inline fun <reified T> defferedRequest(request: Request, reqWrapper: RequestWrapper): Deferred<T?> {
    val deferred = CompletableDeferred<T?>()
    if(!NetworkUtils.isConnected()) {
        deferred.complete(null)
        OkExt.globalFailHandler?.invoke(IOException("Network is not available!"))
        return deferred
    }
    val req = request.newBuilder().tag(reqWrapper.tag())
        .build()
    val call = OkExt.okHttpClient.newCall(req)
        .apply { OkExt.requestCache[reqWrapper.tag()] = this } //cache req
    deferred.invokeOnCompletion {
        if (deferred.isCancelled) {
            OkExt.requestCache.remove(reqWrapper.tag())
            call.cancel()
        }
    }
    try {
        val response = call.execute()
        if (response.isSuccessful && response.body != null) {
            when (T::class.java) {
                String::class.java -> deferred.complete(
                    response.body!!.string() as T
                )
                File::class.java -> {
                    val file = File(reqWrapper.savePath)
                    if (!file.exists()) file.createNewFile()
                    response.body!!.byteStream().copyTo(file.outputStream())
                    deferred.complete(file as T)
                }
                else -> deferred.complete(
                    response.body!!.string()
                        .toBean<T>(dateFormat = OkExt.dateFormat, lenient = OkExt.lenientJson)
                )
            }
        } else {
            deferred.complete(null) //not throw, pass null
            OkExt.globalFailHandler?.invoke(null)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        deferred.complete(null) //pass null
        OkExt.globalFailHandler?.invoke(e)
    } finally {
        OkExt.requestCache.remove(reqWrapper.tag())
    }
    return deferred
}

inline fun <reified T> callbackRequest(
    request: Request,
    cb: HttpCallback<T>,
    reqWrapper: RequestWrapper
) {
    if(!NetworkUtils.isConnected()) {
        cb.onFail(IOException("Network is not available!"))
        return
    }
    val req = request.newBuilder().tag(reqWrapper.tag()).build()
    OkExt.okHttpClient.newCall(req).apply {
        OkExt.requestCache[reqWrapper.tag()] = this //cache req
        enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                OkExt.requestCache.remove(reqWrapper.tag())
                cb.onFail(e)
                OkExt.globalFailHandler?.invoke(e)
            }

            override fun onResponse(call: Call, response: Response) {
                OkExt.requestCache.remove(reqWrapper.tag())
                if (response.isSuccessful && response.body != null) {
                    when (T::class.java) {
                        String::class.java -> cb.onSuccess(
                            response.body!!.string() as T
                        )
                        File::class.java -> {
                            val file = File(reqWrapper.savePath)
                            if (!file.exists()) file.createNewFile()
                            response.body!!.byteStream().copyTo(file.outputStream())
                            cb.onSuccess(file as T)
                        }
                        else -> cb.onSuccess(
                            response.body!!.string().toBean<T>(
                                dateFormat = OkExt.dateFormat,
                                lenient = OkExt.lenientJson
                            )
                        )
                    }
                } else {
                    cb.onFail(IOException("request to ${request.url} is fail; http code: ${response.code}!"))
                }
            }
        })
    }
}

inline fun <reified T> syncRequest(request: Request, reqWrapper: RequestWrapper): T? {
    if(!NetworkUtils.isConnected()) {
        OkExt.globalFailHandler?.invoke(IOException("Network is not available!"))
        return null
    }
    val req = request.newBuilder().tag(reqWrapper.tag()).build()
    val call = OkExt.okHttpClient.newCall(req)
    OkExt.requestCache[reqWrapper.tag()] = call //cache req
    try {
        val response = call.execute()
        return if (response.isSuccessful && response.body != null) {
            when (T::class.java) {
                String::class.java -> response.body!!.string() as T
                File::class.java -> {
                    val file = File(reqWrapper.savePath)
                    if (!file.exists()) file.createNewFile()
                    response.body!!.byteStream().copyTo(file.outputStream())
                    file as T
                }
                else -> response.body!!.string()
                    .toBean<T>(dateFormat = OkExt.dateFormat, lenient = OkExt.lenientJson)
            }
        } else {
            null
        }
    } catch (e: Exception) {
        OkExt.globalFailHandler?.invoke(e)
    } finally {
        OkExt.requestCache.remove(reqWrapper.tag())
    }
    return null
}


// parse some new media type.
fun File.mediaType(): String {
    return getFileNameMap().getContentTypeFor(name) ?: when (extension.toLowerCase()) {
        "json" -> "application/json"
        "js" -> "application/javascript"
        "apk" -> "application/vnd.android.package-archive"
        "md" -> "text/x-markdown"
        "webp" -> "image/webp"
        else -> "application/octet-stream"
    }
}

