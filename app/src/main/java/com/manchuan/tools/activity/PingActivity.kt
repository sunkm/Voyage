package com.manchuan.tools.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import com.blankj.utilcode.util.ClipboardUtils
import com.drake.statusbar.immersive
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.databinding.ActivityPingBinding
import com.stealthcopter.networktools.Ping
import com.stealthcopter.networktools.Ping.PingListener
import com.stealthcopter.networktools.ping.PingResult
import com.stealthcopter.networktools.ping.PingStats
import rikka.material.app.MaterialActivity
import java.util.Objects

class PingActivity : MaterialActivity() {
    private var toolbar: Toolbar? = null
    private var pingBinding: ActivityPingBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pingBinding = ActivityPingBinding.inflate(LayoutInflater.from(this))
        setContentView(pingBinding?.root)
        toolbar = pingBinding?.toolbar
        mAutocomplete1 = pingBinding?.autocomplete1
        mEdittext1 = pingBinding?.edittext1
        mImageview1 = pingBinding?.imageview1
        materialbutton1 = pingBinding?.materialbutton1
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        immersive(pingBinding?.toolbar!!)
        materialbutton1!!.setOnClickListener {
            if (Objects.requireNonNull(
                    mEdittext1!!.text
                ).toString().isEmpty()
            ) {
                PopTip.show("请输入网址")
            } else {
                WaitDialog.show("Ping...")
                Ping.onAddress(mEdittext1!!.text.toString()).setTimeOutMillis(1000).setTimes(15)
                    .doPing(object : PingListener {
                        override fun onFinished(p1: PingStats) {
                            runOnUiThread {
                                WaitDialog.dismiss()
                                val sb: StringBuilder = StringBuilder()
                                sb.append("--服务器地址:").append(p1.address.toString())
                                sb.append("\n--平均延迟:").append(p1.averageTimeTaken).append("ms")
                                sb.append("\n--最大延迟:").append(p1.maxTimeTaken).append("ms")
                                sb.append("\n--丢包率:").append(p1.packetsLost).append("%")
                                mAutocomplete1!!.setText(sb)
                            }
                        }

                        override fun onError(p1: Exception) {
                            runOnUiThread {
                                WaitDialog.dismiss()
                                PopTip.show("出现错误:" + p1.message)
                            }
                        }

                        override fun onResult(pingResult: PingResult) {}
                    })
            }
        }
        mImageview1!!.setOnClickListener { view: View? ->
            if (mAutocomplete1!!.text.toString().isEmpty()) {
                PopTip.show("无内容")
            } else {
                ClipboardUtils.copyText(mAutocomplete1!!.text.toString())
            }
        }
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        supportActionBar?.title = "Ping"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private var mEdittext1: TextInputEditText? = null
    private var materialbutton1: MaterialButton? = null
    private var mAutocomplete1: AutoCompleteTextView? = null
    private var mImageview1: ImageView? = null
}