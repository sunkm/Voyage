package com.manchuan.tools.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import com.blankj.utilcode.util.ClipboardUtils
import com.drake.statusbar.immersive
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.kongzue.dialogx.dialogs.PopTip
import com.manchuan.tools.R
import com.manchuan.tools.extensions.getColorByAttr
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import rikka.material.app.MaterialActivity
import java.util.*

class SpecialTextActivity : MaterialActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_special)
        initView(this)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        immersive(toolbar!!)
        val style = arrayOf(
            "测⃠试⃠测⃠试⃠",
            "⃢测⃢试⃢测⃢试⃢",
            "a'ゞ测试测试",
            "⃘⃘测⃘⃘试⃘⃘测⃘⃘试⃘⃘",
            "⃟测⃟试⃟测⃟试⃟",
            "꯭测꯭试꯭测꯭试꯭",
            "̶̶̶̶测̶̶̶̶试̶̶̶̶测̶̶̶̶试̶̶̶̶",
            "ۣۖิۣۖิۣۖิ测ۣۖิۣۖิۣۖิ试ۣۖิۣۖิۣۖิ测ۣۖิۣۖิۣۖิ试ۣۖิۣۖิۣۖิ",
            "҉҉҉҉测҉҉҉҉试҉҉҉҉测҉҉҉҉试҉҉҉҉",
            "ζั͡ ั͡测 ั͡试 ั͡测 ั͡试 ั͡✾"
        )
        val adapter = ArrayAdapter(this, R.layout.cat_exposed_dropdown_popup_item, style)
        autocomplete1!!.setAdapter(adapter)
        val fab = ExtendedFloatingActionButton(this)
        val params = CoordinatorLayout.LayoutParams(-2, -2)
        params.gravity = Gravity.BOTTOM or Gravity.END
        params.setMargins(dp2px(20f), dp2px(20f), dp2px(20f), dp2px(20f))
        fab.layoutParams = params
        fab.text = "生成"
        fab.gravity = Gravity.CENTER
        fab.setBackgroundColor(getColorByAttr(android.R.attr.colorAccent))
        fab.iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
        fab.setIconResource(R.drawable.qiehuan)
        _coordinatorLayout!!.addView(fab)
        fab.setOnClickListener { v: View? ->
            if (edittext1!!.text.toString().isEmpty()) {
                //_Alerter("温馨提示", "输入不能为空", "#F44336");
                PopTip.show("输入不能为空")
            } else {
                (applicationContext.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                    this@SpecialTextActivity.currentFocus!!.windowToken, 2
                )
                if (autocomplete1!!.text.toString() == "测⃠试⃠测⃠试⃠") {
                    autocomplete2!!.setText(edittext1!!.text.toString().replace("", "⃠"))
                }
                if (autocomplete1!!.text.toString() == "⃢测⃢试⃢测⃢试⃢") {
                    autocomplete2!!.setText(edittext1!!.text.toString().replace("", "⃢"))
                }
                if (autocomplete1!!.text.toString() == "a'ゞ测试测试") {
                    autocomplete2!!.setText("a'ゞ" + edittext1!!.text.toString())
                }
                if (autocomplete1!!.text.toString() == "⃘⃘测⃘⃘试⃘⃘测⃘⃘试⃘⃘") {
                    autocomplete2!!.setText(edittext1!!.text.toString().replace("", "⃘⃘"))
                }
                if (autocomplete1!!.text.toString() == "⃟测⃟试⃟测⃟试⃟") {
                    autocomplete2!!.setText(edittext1!!.text.toString().replace("", "⃟"))
                }
                if (autocomplete1!!.text.toString() == "꯭测꯭试꯭测꯭试꯭") {
                    autocomplete2!!.setText(edittext1!!.text.toString().replace("", "꯭"))
                }
                if (autocomplete1!!.text.toString() == "̶̶̶̶测̶̶̶̶试̶̶̶̶测̶̶̶̶试̶̶̶̶") {
                    autocomplete2!!.setText(edittext1!!.text.toString().replace("", "̶̶̶̶̶̶̶̶"))
                }
                if (autocomplete1!!.text.toString() == "ۣۖิۣۖิۣۖิ测ۣۖิۣۖิۣۖิ试ۣۖิۣۖิۣۖิ测ۣۖิۣۖิۣۖิ试ۣۖิۣۖิۣۖิ") {
                    autocomplete2!!.setText(
                        edittext1!!.text.toString().replace("", "ۣۖิ").replace(" ", "")
                    )
                }
                if (autocomplete1!!.text.toString() == "҉҉҉҉测҉҉҉҉试҉҉҉҉测҉҉҉҉试҉҉҉҉") {
                    autocomplete2!!.setText(edittext1!!.text.toString().replace("", "҉҉҉҉"))
                }
                if (autocomplete1!!.text.toString() == "ζั͡ ั͡测 ั͡试 ั͡测 ั͡试 ั͡✾") {
                    autocomplete2!!.setText(
                        edittext1!!.text.toString().replace("", " ั͡ζั͡").replace(" ", "") + "✾"
                    )
                }
            }
        }
        imageview1!!.setOnClickListener {
            if (autocomplete2!!.text.toString().isEmpty()) {
                PopTip.show("无内容")
            } else {
                ClipboardUtils.copyText(autocomplete2!!.text.toString())
            }
        }
    }

    private fun dp2px(dpValue: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dpValue,
            this.resources.displayMetrics
        ).toInt()
    }

    @Deprecated("")
    fun getLocationX(_v: View): Int {
        val _location = IntArray(2)
        _v.getLocationInWindow(_location)
        return _location[0]
    }

    @Deprecated("")
    fun getLocationY(_v: View): Int {
        val _location = IntArray(2)
        _v.getLocationInWindow(_location)
        return _location[1]
    }

    @Deprecated("")
    fun getRandom(_min: Int, _max: Int): Int {
        val random = Random()
        return random.nextInt(_max - _min + 1) + _min
    }

    @Deprecated("")
    fun getCheckedItemPositionsToArray(_list: ListView): ArrayList<Double> {
        val _result = ArrayList<Double>()
        val _arr = _list.checkedItemPositions
        for (_iIdx in 0 until _arr.size()) {
            if (_arr.valueAt(_iIdx)) _result.add(_arr.keyAt(_iIdx).toDouble())
        }
        return _result
    }

    @Deprecated("")
    private fun getDip(_input: Int): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            _input.toFloat(),
            resources.displayMetrics
        )
    }

    @get:Deprecated("")
    val displayWidthPixels: Int
        get() = resources.displayMetrics.widthPixels

    @get:Deprecated("")
    val displayHeightPixels: Int
        get() = resources.displayMetrics.heightPixels
    private var _coordinatorLayout: CoordinatorLayout? = null
    private var _appbarLayout: AppBarLayout? = null
    private var toolbar: Toolbar? = null
    private var _linear: LinearLayout? = null
    private var sl: SmartRefreshLayout? = null
    private var linear1: LinearLayout? = null
    private var textinputlayout1: TextInputLayout? = null
    private var edittext1: TextInputEditText? = null
    private var textinputlayout2: TextInputLayout? = null
    private var autocomplete1: AutoCompleteTextView? = null
    private var cardview1: MaterialCardView? = null
    private var linear2: LinearLayout? = null
    private var autocomplete2: AutoCompleteTextView? = null
    private var linear3: LinearLayout? = null
    private var imageview1: ImageView? = null
    private fun initView(activity: Activity) {
        _coordinatorLayout = activity.findViewById(R.id._coordinatorLayout)
        _appbarLayout = activity.findViewById(R.id._appbarLayout)
        toolbar = activity.findViewById(R.id.toolbar)
        _linear = activity.findViewById(R.id._linear)
        sl = activity.findViewById(R.id.sl)
        val vscroll1 = activity.findViewById<NestedScrollView>(R.id.vscroll1)
        linear1 = activity.findViewById(R.id.linear1)
        textinputlayout1 = activity.findViewById(R.id.textinputlayout1)
        edittext1 = activity.findViewById(R.id.edittext1)
        textinputlayout2 = activity.findViewById(R.id.textinputlayout2)
        autocomplete1 = activity.findViewById(R.id.autocomplete1)
        cardview1 = activity.findViewById(R.id.cardview1)
        linear2 = activity.findViewById(R.id.linear2)
        autocomplete2 = activity.findViewById(R.id.autocomplete2)
        linear3 = activity.findViewById(R.id.linear3)
        imageview1 = activity.findViewById(R.id.imageview1)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toolbar!!.title = "特殊文本生成"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}