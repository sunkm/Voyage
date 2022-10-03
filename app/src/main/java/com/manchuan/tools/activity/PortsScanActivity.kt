package com.manchuan.tools.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.AutoCompleteTextView
import androidx.appcompat.widget.Toolbar
import com.drake.statusbar.immersive
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.databinding.ActivityPortsScanBinding
import com.stealthcopter.networktools.PortScan
import com.stealthcopter.networktools.PortScan.PortListener
import rikka.material.app.MaterialActivity
import java.net.UnknownHostException
import java.util.*

@SuppressLint("NonConstantResourceId")
class PortsScanActivity : MaterialActivity() {
    private val binding by lazy {
        ActivityPortsScanBinding.inflate(layoutInflater)
    }
    private lateinit var toolbar: Toolbar
    private lateinit var editlay_one: TextInputLayout
    private lateinit var editlay_two: TextInputLayout
    private lateinit var wake: MaterialButton
    private lateinit var editText_one: TextInputEditText
    private lateinit var editText_two: TextInputEditText
    private lateinit var autoCompleteTextView: AutoCompleteTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        toolbar = binding.toolbar
        editlay_one = binding.textinputlayout1
        editlay_two = binding.textinputlayout2
        wake = binding.materialbutton1
        editText_one = binding.edittext1
        editText_two = binding.edittext2
        autoCompleteTextView = binding.autocomplete1
        setSupportActionBar(toolbar)
        immersive(toolbar)
        supportActionBar?.apply {
            title = "端口扫描"
            setDisplayHomeAsUpEnabled(true)
        }
        wake.setOnClickListener { view: View? ->
            if (Objects.requireNonNull(
                    editText_one.text
                ).toString().isEmpty()
            ) {
                editlay_one.error = "不能为空"
            } else if (editText_one.text.toString().isNotEmpty() && Objects.requireNonNull(
                    editText_two.text
                ).toString().isNotEmpty()
            ) {
                val ipAddress = editText_one.text.toString()
                val portAddress = editText_two.text.toString()
                // Asynchronously
                WaitDialog.show("扫描中...")
                try {
                    PortScan.onAddress(ipAddress).setTimeOutMillis(1000)
                        .setPort(portAddress.toInt()).setMethodTCP().doScan(object : PortListener {
                            @SuppressLint("SetTextI18n")
                            override fun onResult(portNo: Int, open: Boolean) {
                                autoCompleteTextView!!.setText(
                                    "端口号:$portNo,状态:" + getOpenState(
                                        open
                                    )
                                )
                            }

                            override fun onFinished(openPorts: ArrayList<Int>) {
                                // Stub: Finished scanning
                                WaitDialog.dismiss()
                                PopTip.show("扫描完成")
                            }
                        })
                } catch (e: UnknownHostException) {
                    PopTip.show("未知主机")
                }
            } else if (editText_one.text.toString().isNotEmpty() && Objects.requireNonNull(
                    editText_two!!.text
                ).toString().isEmpty()
            ) {
                val ipAddress = editText_one.text.toString()
                // Asynchronously
                WaitDialog.show("扫描中...")
                try {
                    PortScan.onAddress(ipAddress).setTimeOutMillis(1000).setPortsAll()
                        .setMethodTCP().doScan(object : PortListener {
                            override fun onResult(portNo: Int, open: Boolean) {
                            }

                            @SuppressLint("SetTextI18n")
                            override fun onFinished(openPorts: ArrayList<Int>) {
                                // Stub: Finished scanning
                                runOnUiThread {
                                    WaitDialog.dismiss()
                                    autoCompleteTextView!!.setText(
                                        "已开放端口:" + arrayOf<ArrayList<*>>(
                                            openPorts
                                        ).contentToString().replace("[[", "").replace("]]", "")
                                    )
                                }
                                PopTip.show("扫描完成")
                            }
                        })
                } catch (e: UnknownHostException) {
                    PopTip.show("未知主机")
                }
            }
        }
    }

    private fun getOpenState(state: Boolean): String {
        var states: String? = null
        states = if (state) {
            "开启"
        } else {
            "关闭"
        }
        return states
    }
}