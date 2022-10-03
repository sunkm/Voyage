package com.manchuan.tools.activity

import android.app.Activity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import com.blankj.utilcode.util.ClipboardUtils
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.gyf.immersionbar.ImmersionBar
import com.kongzue.dialogx.dialogs.PopTip
import com.manchuan.tools.R
import com.manchuan.tools.utils.ColorUtils.statusBarColor
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import rikka.material.app.MaterialActivity
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.xor

class RC4EDActivity : MaterialActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rc4)
        initView(this)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
        ImmersionBar.with(this).statusBarColorInt(statusBarColor).autoDarkModeEnable(true)
            .titleBar(toolbar).init()
        initEvent()
    }

    private fun initEvent() {
        materialbutton1!!.setOnClickListener { _view: View? ->
            if (Objects.requireNonNull(
                    edittext1!!.text).toString().isEmpty()
            ) {
                PopTip.show("请输入内容")
            } else {
                if (Objects.requireNonNull(edittext2!!.text).toString().isEmpty()) {
                    PopTip.show("请输入密钥")
                } else {
                    try {
                        try {
                            autocomplete1!!.setText(RC4Util.encryRC4String(edittext1!!.text.toString(),
                                edittext2!!.text.toString(),
                                "UTF-8"))
                        } catch (e: Exception) {
                        }
                    } catch (e: Exception) {
                    }
                }
            }
        }
        materialbutton2!!.setOnClickListener { _view: View? ->
            if (Objects.requireNonNull(
                    edittext1!!.text).toString().isEmpty()
            ) {
                PopTip.show("请输入内容")
            } else {
                if (Objects.requireNonNull(edittext2!!.text).toString().isEmpty()) {
                    PopTip.show("请输入密钥")
                } else {
                    try {
                        try {
                            autocomplete1!!.setText(RC4Util.decryRC4(edittext1!!.text.toString(),
                                edittext2!!.text.toString(),
                                "UTF-8"))
                        } catch (e: UnsupportedEncodingException) {
                        }
                    } catch (e: Exception) {
                    }
                }
            }
        }
        imageview1!!.setOnClickListener { _view: View? ->
            ClipboardUtils.copyText(
                autocomplete1!!.text.toString())
            PopTip.show("复制成功")
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toolbar!!.title = "RC4加解密"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private var _coordinatorLayout: CoordinatorLayout? = null
    private var _appbarLayout: AppBarLayout? = null
    private var toolbar: Toolbar? = null
    private var _linear: LinearLayout? = null
    private var sl: SmartRefreshLayout? = null
    private var vscroll1: NestedScrollView? = null
    private var linear2: LinearLayout? = null
    private var textinputlayout1: TextInputLayout? = null
    private var edittext1: TextInputEditText? = null
    private var textinputlayout2: TextInputLayout? = null
    private var edittext2: TextInputEditText? = null
    private var linear4: LinearLayout? = null
    private var materialbutton1: MaterialButton? = null
    private var materialbutton2: MaterialButton? = null
    private var cardview1: MaterialCardView? = null
    private var linear1: LinearLayout? = null
    private var autocomplete1: AutoCompleteTextView? = null
    private var linear3: LinearLayout? = null
    private var imageview1: ImageView? = null
    private fun initView(activity: Activity) {
        _coordinatorLayout = activity.findViewById(R.id._coordinatorLayout)
        _appbarLayout = activity.findViewById(R.id._appbarLayout)
        toolbar = activity.findViewById(R.id.toolbar)
        _linear = activity.findViewById(R.id._linear)
        sl = activity.findViewById(R.id.sl)
        vscroll1 = activity.findViewById(R.id.vscroll1)
        linear2 = activity.findViewById(R.id.linear2)
        textinputlayout1 = activity.findViewById(R.id.textinputlayout1)
        edittext1 = activity.findViewById(R.id.edittext1)
        textinputlayout2 = activity.findViewById(R.id.textinputlayout2)
        edittext2 = activity.findViewById(R.id.edittext2)
        linear4 = activity.findViewById(R.id.linear4)
        materialbutton1 = activity.findViewById(R.id.materialbutton1)
        materialbutton2 = activity.findViewById(R.id.materialbutton2)
        cardview1 = activity.findViewById(R.id.cardview1)
        linear1 = activity.findViewById(R.id.linear1)
        autocomplete1 = activity.findViewById(R.id.autocomplete1)
        linear3 = activity.findViewById(R.id.linear3)
        imageview1 = activity.findViewById(R.id.imageview1)
    }

    object RC4Util {
        /**
         * RC4加密，将加密后的数据进行哈希
         * @param data 需要加密的数据
         * @param key 加密密钥
         * @param chartSet 编码方式
         * @return 返回加密后的数据
         * @throws UnsupportedEncodingException
         */
        @Throws(UnsupportedEncodingException::class)
        fun encryRC4String(data: String?, key: String?, chartSet: String?): String? {
            return if (data == null || key == null) {
                null
            } else bytesToHex(encryRC4Byte(data, key, chartSet))
        }

        /**
         * RC4加密，将加密后的字节数据
         * @param data 需要加密的数据
         * @param key 加密密钥
         * @param chartSet 编码方式
         * @return 返回加密后的数据
         * @throws UnsupportedEncodingException
         */
        @Throws(UnsupportedEncodingException::class)
        fun encryRC4Byte(data: String?, key: String?, chartSet: String?): ByteArray? {
            if (data == null || key == null) {
                return null
            }
            val bData: ByteArray
            bData = if (chartSet == null || chartSet.isEmpty()) {
                data.toByteArray()
            } else {
                data.toByteArray(charset(chartSet))
            }
            return RC4Base(bData, key)
        }

        /**
         * RC4解密
         * @param data 需要解密的数据
         * @param key 加密密钥
         * @param chartSet 编码方式
         * @return 返回解密后的数据
         * @throws UnsupportedEncodingException
         */
        @Throws(UnsupportedEncodingException::class)
        fun decryRC4(data: String?, key: String?, chartSet: String?): String? {
            return if (data == null || key == null) {
                null
            } else String(RC4Base(hexToByte(data),
                key), Charset.forName("UTF-8"))
        }

        /**
         * RC4加密初始化密钥
         * @param aKey
         * @return
         */
        private fun initKey(aKey: String): ByteArray? {
            val bkey = aKey.toByteArray()
            val state = ByteArray(256)
            for (i in 0..255) {
                state[i] = i.toByte()
            }
            var index1 = 0
            var index2 = 0
            if (bkey.isEmpty()) {
                return null
            }
            for (i in 0..255) {
                index2 = (bkey[index1] and 0xff.toByte()) + (state[i] and 0xff.toByte()) + index2 and 0xff
                val tmp = state[i]
                state[i] = state[index2]
                state[index2] = tmp
                index1 = (index1 + 1) % bkey.size
            }
            return state
        }

        /**
         * 字节数组转十六进制
         * @param bytes
         * @return
         */
        fun bytesToHex(bytes: ByteArray?): String {
            val sb = StringBuilder()
            for (aByte in bytes!!) {
                val hex = Integer.toHexString((aByte and 0xFF.toByte()).toInt())
                if (hex.length < 2) {
                    sb.append(0)
                }
                sb.append(hex)
            }
            return sb.toString()
        }

        private fun hexToByte(inHex: String): ByteArray {
            var inHex = inHex
            var hexlen = inHex.length
            val result: ByteArray
            if (hexlen % 2 == 1) {
                hexlen++
                result = ByteArray(hexlen / 2)
                inHex = "0$inHex"
            } else {
                result = ByteArray(hexlen / 2)
            }
            var j = 0
            var i = 0
            while (i < hexlen) {
                result[j] = inHex.substring(i, i + 2).toInt(16).toByte()
                j++
                i += 2
            }
            return result
        }

        /**
         * RC4解密
         * @param input
         * @param mKkey
         * @return
         */
        private fun RC4Base(input: ByteArray, mKkey: String): ByteArray {
            var x = 0
            var y = 0
            val key = initKey(mKkey)
            var xorIndex: Int
            val result = ByteArray(input.size)
            for (i in input.indices) {
                x = x + 1 and 0xff
                assert(key != null)
                y = (key!![x] and 0xff.toByte()) + y and 0xff
                val tmp = key[x]
                key[x] = key[y]
                key[y] = tmp
                xorIndex = (key[x] and 0xff.toByte()) + (key[y] and 0xff.toByte()) and 0xff
                result[i] = (input[i] xor key[xorIndex])
            }
            return result
        }
    }
}