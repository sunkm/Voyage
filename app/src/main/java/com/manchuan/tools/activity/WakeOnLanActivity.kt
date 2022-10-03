package com.manchuan.tools.activity

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.drake.statusbar.immersive
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.kongzue.dialogx.dialogs.PopTip
import com.manchuan.tools.databinding.ActivityWakeOnLanBinding
import com.stealthcopter.networktools.WakeOnLan
import rikka.material.app.MaterialActivity

class WakeOnLanActivity : MaterialActivity() {

    private val binding by lazy {
        ActivityWakeOnLanBinding.inflate(layoutInflater)
    }

    private lateinit var editlay_one: TextInputLayout
    private lateinit var editlay_two: TextInputLayout
    private lateinit var wake: MaterialButton
    private lateinit var editText_one: TextInputEditText
    private lateinit var editText_two: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "LAN网络唤醒"
            setDisplayHomeAsUpEnabled(true)
        }
        editlay_two = binding.textinputlayout2
        editlay_one = binding.textinputlayout1
        wake = binding.materialbutton1
        editText_one = binding.edittext1
        editText_two = binding.edittext2
        immersive(binding.toolbar)
        wake.setOnClickListener { view: View? ->
            if (editText_one.text.toString().isEmpty() && editText_two.text.toString()
                    .isEmpty()
            ) {
                editlay_one.error = "不能为空"
                editlay_two.error = "不能为空"
            } else if (editText_one.text.toString().isEmpty()) {
                editlay_one.error = "不能为空"
            } else if (editText_two.text.toString().isEmpty()) {
                editlay_two.error = "不能为空"
            } else if (editText_one.text.toString().isNotEmpty() && editText_two.text.toString().isNotEmpty()
            ) {
                val ipAddress = editText_one.text.toString()
                val macAddress = editText_two.text.toString()
                Thread {
                    try {
                        WakeOnLan.sendWakeOnLan(ipAddress, macAddress)
                        PopTip.show("唤醒请求已发送")
                    } catch (e: Exception) {
                        PopTip.show("唤醒失败")
                    }
                }.start()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}