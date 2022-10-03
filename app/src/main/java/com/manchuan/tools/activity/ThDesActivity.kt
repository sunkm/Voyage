package com.manchuan.tools.activity

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.LinearLayout
import com.blankj.utilcode.util.ClipboardUtils
import com.drake.statusbar.immersive
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.kongzue.dialogx.dialogs.PopTip
import com.manchuan.tools.databinding.ActivityThDesBinding
import com.manchuan.tools.utils.SecretUtils
import rikka.material.app.MaterialActivity
import java.util.*

class ThDesActivity : MaterialActivity() {
    private lateinit var keys: TextInputEditText
    private val binding by lazy {
        ActivityThDesBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        initView()
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "3DES加解密"
        }
        keys = binding.edittext2
        immersive(binding.toolbar)
        materialbutton1.setOnClickListener { view: View? ->
            if (strisEmpty()) {
                PopTip.show("请输入需要加密的内容")
            } else if (KeyisEmpty()) {
                PopTip.show("请输入密钥")
            } else if (!strisEmpty() && !KeyisEmpty()) {
                try {
                    mAutocomplete1.setText(
                        SecretUtils.encrypt(
                            Objects.requireNonNull(mEdittext1.text).toString().toByteArray(),
                            keys.text.toString()
                        )?.let {
                            String(
                                it
                            )
                        }
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        materialbutton2.setOnClickListener { view: View? ->
            if (strisEmpty()) {
                PopTip.show("请输入需要解密的内容")
            } else if (KeyisEmpty()) {
                PopTip.show("请输入密钥")
            } else if (!strisEmpty() && !KeyisEmpty()) {
                try {
                    mAutocomplete1.setText(
                        SecretUtils.decrypt(mEdittext1.text?.toString(),
                            keys.text.toString()
                        )?.let {
                            String(
                                it
                            )
                        }
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        mImageview1.setOnClickListener { view: View? ->
            if (mAutocomplete1.text.toString().isEmpty()) {
                PopTip.show("无内容")
            } else {
                PopTip.show("已复制")
                ClipboardUtils.copyText(mAutocomplete1.text.toString())
            }
        }
    }

    private fun strisEmpty(): Boolean {
        return mEdittext1.text.toString().isEmpty()
    }

    private fun KeyisEmpty(): Boolean {
        return Objects.requireNonNull(keys.text).toString().isEmpty()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private lateinit var mLinear2: LinearLayout
    private lateinit var mTextinputlayout1: TextInputLayout
    private lateinit var mEdittext1: TextInputEditText
    private lateinit var mLinear4: LinearLayout
    private lateinit var materialbutton1: MaterialButton
    private lateinit var materialbutton2: MaterialButton
    private lateinit var mCardview1: MaterialCardView
    private lateinit var mLinear1: LinearLayout
    private lateinit var mAutocomplete1: AutoCompleteTextView
    private lateinit var mLinear3: LinearLayout
    private lateinit var mImageview1: ImageView
    private fun initView() {
        mLinear2 = binding.linear2
        mTextinputlayout1 = binding.textinputlayout1
        mEdittext1 = binding.edittext1
        mLinear4 = binding.linear4
        materialbutton1 = binding.materialbutton1
        materialbutton2 = binding.materialbutton2
        mCardview1 = binding.cardview1
        mLinear1 = binding.linear1
        mAutocomplete1 = binding.autocomplete1
        mLinear3 = binding.linear3
        mImageview1 = binding.imageview1
    }
}