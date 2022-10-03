package com.manchuan.tools.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.drake.net.Get
import com.drake.net.utils.scopeNetLife
import com.dylanc.longan.doOnClick
import com.dylanc.longan.isTextNotEmpty
import com.dylanc.longan.textString
import com.manchuan.tools.databinding.ActivityLocationInquireBinding
import com.manchuan.tools.extensions.errorToast
import com.manchuan.tools.extensions.loge
import org.apache.commons.lang3.StringUtils

class LocationInquireActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityLocationInquireBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
        binding.jiexi.doOnClick {
            if (binding.lng.isTextNotEmpty() && binding.lat.isTextNotEmpty()) {
                scopeNetLife {
                    val content =
                        Get<String>("http://api.wuxixindong.top/api/location_geocoder_address.php?lng=${binding.lng.textString}&lat=${binding.lat.textString}").await()
                    val string = StringUtils.substringBetween(
                        content,
                        "随身助手API经纬度解析\n━━━━━━━━━\n", "\n━━━━━━━━━\nTips:随身助手API技术支持"
                    )
                    loge(string)
                    binding.autocomplete1.setText(string)
                }.catch {
                    it.message?.errorToast()
                    loge(it.toString())
                }
            } else {
                "请填写完整".errorToast()
            }
        }
    }
}