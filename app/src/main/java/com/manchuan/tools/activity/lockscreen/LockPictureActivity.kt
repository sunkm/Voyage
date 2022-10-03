package com.manchuan.tools.activity.lockscreen

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.drake.net.utils.scope
import com.manchuan.tools.databinding.ActivityLockPictureBinding
import com.manchuan.tools.extensions.errorToast
import com.manchuan.tools.extensions.successToast
import com.prof.rssparser.Parser
import java.nio.charset.Charset

class LockPictureActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityLockPictureBinding.inflate(layoutInflater)
    }
    private val url = "https://www.photoworld.com.cn/feed"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val parser = Parser.Builder()
            .context(this)
            .charset(Charset.defaultCharset())
            .cacheExpirationMillis(24L * 60L * 60L * 1000L) // one day
            .build()
        scope {
            runCatching {
                val channel = parser.getChannel(url)
                channel.title?.successToast()
            }.onFailure {
                it.toString().errorToast()
            }
        }
    }
}