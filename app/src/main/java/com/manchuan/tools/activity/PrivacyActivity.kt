package com.manchuan.tools.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dylanc.longan.safeIntentExtras
import com.manchuan.tools.databinding.ActivityPrivacyBinding
import com.manchuan.tools.extensions.readFileFromAssets
import fi.iki.elonen.NanoHTTPD
import java.io.FileNotFoundException
import java.io.IOException

class PrivacyActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityPrivacyBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.toolbar.apply {
            title = "隐私政策"
            subtitle = "正在查看离线副本"
            setNavigationOnClickListener {
                finish()
            }
        }
        val type = safeIntentExtras<Int>("type")
        when (type.value) {
            1 -> {
                binding.content.text =
                    readFileFromAssets("string/privacy.txt")
                binding.toolbar.apply {
                    title = "隐私政策"
                    subtitle = "正在查看离线副本"
                }
            }
            2 -> {
                binding.content.text =
                    readFileFromAssets("string/user_agreement.txt")
                binding.toolbar.apply {
                    title = "用户协议"
                    subtitle = "正在查看离线副本"
                }
            }
        }
    }

    inner class PrivacyServer(port: Int) : NanoHTTPD(port) {
        override fun serve(session: IHTTPSession): Response {
            try {
                val file = assets.open("/html/privacy.html").buffered()
                return newChunkedResponse(
                    Response.Status.OK,
                    "text/html",
                    file
                )
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: ResponseException) {
                e.printStackTrace()
            }
            return response404(session, session.uri)
        }

        //页面不存在，或者文件不存在时
        private fun response404(
            session: IHTTPSession?,
            url: String?,
        ): Response {
            return newFixedLengthResponse("<!DOCTYPE html><html><body>Sorry, We Can't Find $url !</body></html>\n")
        }
    }


}