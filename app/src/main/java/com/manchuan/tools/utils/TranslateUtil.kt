package com.manchuan.tools.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.interfaces.TranslateCallback
import org.json.JSONArray
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.Executors

class TranslateUtil {
    /**
     * 翻译，包含http请求，需要异步，返回""则为翻译失败
     * @param sourceLan 源语言，如en，自动检测为auto
     * @param targetLan 目标语言如zh
     * @param content 翻译文本
     * @return ""为翻译失败，其余成功
     */
    fun translate(
        context: Context?,
        sourceLan: String,
        targetLan: String,
        content: String?,
        callback: TranslateCallback?
    ) {
        val task = TranslateTask(context, sourceLan, targetLan, content, callback)
        task.executeOnExecutor(Executors.newCachedThreadPool())
    }

    /**
     * 使用异步任务来翻译，翻译完成后回调callback
     */
    internal class TranslateTask(
        @field:SuppressLint("StaticFieldLeak") var context: Context?,
        var sourceLan: String,
        var targetLan: String,
        var content: String?,
        var callback: TranslateCallback?
    ) : AsyncTask<Void?, Void?, String>() {
        private val loadingDalog: WaitDialog? = null // 这个请自己定义加载中对话框，或者干脆不使用

        @Deprecated("Deprecated in Java")
        override fun onPostExecute(result: String) {
            try {
                //loadingDalog.setMessage(str);
                WaitDialog.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (callback != null) {
                callback!!.onTranslateDone(result)
            }
            super.onPostExecute(result)
        }

        @Deprecated("Deprecated in Java")
        override fun onPreExecute() {
            super.onPreExecute()
            //String str = context.getString(R.string.translating);
        }

        @Deprecated("Deprecated in Java")
        public override fun doInBackground(vararg params: Void?): String {
            var result = ""
            if (content == null || content == "") {
                return result
            }
            try {
                var googleResult: String? = ""
                // 新建一个URL对象
                val url = URL(
                    getTranslateUrl(
                        sourceLan, targetLan, content!!
                    )
                )
                // 打开一个HttpURLConnection连接
                val urlConn = url.openConnection() as HttpURLConnection
                // 设置连接主机超时时间
                urlConn.connectTimeout = 5 * 1000
                //设置从主机读取数据超时
                urlConn.readTimeout = 5 * 1000
                // 设置是否使用缓存  默认是true
                urlConn.useCaches = false
                // 设置为Post请求
                urlConn.requestMethod = "GET"
                //urlConn设置请求头信息
                urlConn.setRequestProperty("User-Agent", USER_AGENT)
                //设置请求中的媒体类型信息。
//            urlConn.setRequestProperty("Content-Type", "application/json");
                //设置客户端与服务连接类型
//            urlConn.addRequestProperty("Connection", "Keep-Alive");
                // 开始连接
                urlConn.connect()
                // 判断请求是否成功
                val statusCode = urlConn.responseCode
                if (statusCode == 200) {
                    // 获取返回的数据
                    googleResult = streamToString(urlConn.inputStream)
                }
                // 关闭连接
                urlConn.disconnect()

                // 处理返回结果，拼接
                val jsonArray = JSONArray(googleResult).getJSONArray(0)
                for (i in 0 until jsonArray.length()) {
                    result += jsonArray.getJSONArray(i).getString(0)
                }
            } catch (e: Exception) {
//            result = "翻译失败";
                e.printStackTrace()
                result = ""
            }
            Timber.d("翻译结果：$result")
            return result
        }
    }

    fun translate(
        context: Context?,
        targetLan: String,
        content: String?,
        callback: TranslateCallback?
    ) {
        val LAN_AUTO = "auto"
        translate(context, LAN_AUTO, targetLan, content, callback)
    }

    companion object {
        private const val TRANSLATE_BASE_URL = "https://translate.googleapis.com/" // 不需要翻墙即可使用

        //    public static final String TRANSLATE_SINGLE_URL = "https://translate.google.cn/translate_a/single?client=gtx&sl=en&tl=zh&dt=t&q=Do%20not%20work%20overtime%20tonight";
        const val USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36"

        /**
         * 将输入流转换成字符串
         *
         * @param is 从网络获取的输入流
         * @return 字符串
         */
        fun streamToString(`is`: InputStream): String? {
            return try {
                val out = ByteArrayOutputStream()
                val buffer = ByteArray(1024)
                var len = 0
                while (`is`.read(buffer).also { len = it } != -1) {
                    out.write(buffer, 0, len)
                }
                out.close()
                `is`.close()
                out.toString()
            } catch (e: Exception) {
                null
            }
        }

        private fun getTranslateUrl(sourceLan: String, targetLan: String, content: String): String {
            return try {
                TRANSLATE_BASE_URL + "translate_a/single?client=gtx&sl=" + sourceLan + "&tl=" + targetLan + "&dt=t&q=" + URLEncoder.encode(
                    content,
                    "UTF-8"
                )
            } catch (e: Exception) {
                TRANSLATE_BASE_URL + "translate_a/single?client=gtx&sl=" + sourceLan + "&tl=" + targetLan + "&dt=t&q=" + content
            }
        }
    }
}