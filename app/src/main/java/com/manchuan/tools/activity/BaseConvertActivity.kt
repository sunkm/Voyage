package com.manchuan.tools.activity

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
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
import com.manchuan.tools.databinding.ActivityBaseConvertBinding
import com.manchuan.tools.utils.ColorUtils.statusBarColor
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import rikka.material.app.MaterialActivity
import java.util.*

class BaseConvertActivity : MaterialActivity() {

    private var baseConvertBinding: ActivityBaseConvertBinding? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        baseConvertBinding = ActivityBaseConvertBinding.inflate(LayoutInflater.from(this))
        setContentView(baseConvertBinding?.root)
        setSupportActionBar(baseConvertBinding?.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        ImmersionBar.with(this).statusBarColorInt(statusBarColor).autoDarkModeEnable(true)
            .titleBar(baseConvertBinding?.toolbar).init()
        baseConvertBinding?.materialbutton1!!.setOnClickListener {
            if (baseConvertBinding?.edittext1?.text.toString().isEmpty()) {
                PopTip.show("请输入需要加密的内容")
            } else {
                baseConvertBinding?.autocomplete1!!.setText(
                    Base64.getEncoder().encodeToString(
                        baseConvertBinding?.edittext1!!.text.toString().toByteArray()
                    )
                )
            }
        }
        baseConvertBinding?.materialbutton2!!.setOnClickListener {
            kotlin.runCatching {
                if (baseConvertBinding?.edittext1?.text.toString().isEmpty()) {
                    PopTip.show("请输入需要解密的内容")
                } else {
                    val decodedBytes = Base64.getDecoder()
                        .decode(baseConvertBinding?.edittext1!!.text.toString())
                    val decodedString = String(decodedBytes)
                    baseConvertBinding?.autocomplete1!!.setText(
                        decodedString
                    )
                }
            }.onFailure {
                PopTip.show("请确认输入的内容是否经过Base64加密")
            }
        }
        baseConvertBinding?.imageview1!!.setOnClickListener {
            if (baseConvertBinding?.autocomplete1!!.text.toString().isEmpty()) {
                PopTip.show("无内容")
            } else {
                PopTip.show("已复制")
                ClipboardUtils.copyText(baseConvertBinding?.autocomplete1!!.text.toString())
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private var m_coordinatorLayout: CoordinatorLayout? = null
    private var m_appbarLayout: AppBarLayout? = null
    private var mToolbar: Toolbar? = null
    private var m_linear: LinearLayout? = null
    private var mSl: SmartRefreshLayout? = null
    private var mVscroll1: NestedScrollView? = null
    private var mLinear2: LinearLayout? = null
    private var mTextinputlayout1: TextInputLayout? = null
    private var mEdittext1: TextInputEditText? = null
    private var mLinear4: LinearLayout? = null
    private var materialbutton1: MaterialButton? = null
    private var materialbutton2: MaterialButton? = null
    private var mCardview1: MaterialCardView? = null
    private var mLinear1: LinearLayout? = null
    private var mAutocomplete1: AutoCompleteTextView? = null
    private var mLinear3: LinearLayout? = null
    private var mImageview1: ImageView? = null
    private fun initView(activity: Activity) {
        m_coordinatorLayout = activity.findViewById(R.id._coordinatorLayout)
        m_appbarLayout = activity.findViewById(R.id._appbarLayout)
        mToolbar = activity.findViewById(R.id.toolbar)
        m_linear = activity.findViewById(R.id._linear)
        mSl = activity.findViewById(R.id.sl)
        mVscroll1 = activity.findViewById(R.id.vscroll1)
        mLinear2 = activity.findViewById(R.id.linear2)
        mTextinputlayout1 = activity.findViewById(R.id.textinputlayout1)
        mEdittext1 = activity.findViewById(R.id.edittext1)
        mLinear4 = activity.findViewById(R.id.linear4)
        materialbutton1 = activity.findViewById(R.id.materialbutton1)
        materialbutton2 = activity.findViewById(R.id.materialbutton2)
        mCardview1 = activity.findViewById(R.id.cardview1)
        mLinear1 = activity.findViewById(R.id.linear1)
        mAutocomplete1 = activity.findViewById(R.id.autocomplete1)
        mLinear3 = activity.findViewById(R.id.linear3)
        mImageview1 = activity.findViewById(R.id.imageview1)
    }
}