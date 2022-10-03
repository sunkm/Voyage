package com.manchuan.tools.activity

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import com.drake.statusbar.immersive
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.manchuan.tools.R
import com.manchuan.tools.colorpicker.ColorPickerView
import com.manchuan.tools.colorpicker.builder.ColorPickerDialogBuilder
import com.manchuan.tools.databinding.ActivityMarqueeBinding
import com.manchuan.tools.view.MarqueeView
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar
import rikka.material.app.MaterialActivity
import java.util.*

class MarQueeActivity : MaterialActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var seekBar1: DiscreteSeekBar
    private lateinit var seekBar2: DiscreteSeekBar
    private lateinit var marqueeView: MarqueeView
    private lateinit var marqueeBinding: ActivityMarqueeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        marqueeBinding = ActivityMarqueeBinding.inflate(layoutInflater)
        setContentView(marqueeBinding.root)
        toolbar = marqueeBinding.toolbar
        seekBar1 = marqueeBinding.size
        seekBar2 = marqueeBinding.speed
        marquee_content = marqueeBinding.marqueeContent
        marquee_color = marqueeBinding.marqueeColor
        marquee_start = marqueeBinding.marqueeStart
        marqueeView = marqueeBinding.marqueeView
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        marqueeView.setContent("文本预览")
        marqueeView.setTextSize(seekBar1.progress.toFloat())
        marqueeView.setTextSpeed(seekBar2.progress.toFloat())
        seekBar1.setOnProgressChangeListener(object : DiscreteSeekBar.OnProgressChangeListener {
            override fun onProgressChanged(
                seekBar: DiscreteSeekBar,
                value: Int,
                fromUser: Boolean,
            ) {
                marqueeView.setTextSize(value.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: DiscreteSeekBar) {}
            override fun onStopTrackingTouch(seekBar: DiscreteSeekBar) {}
        })
        seekBar2.setOnProgressChangeListener(object : DiscreteSeekBar.OnProgressChangeListener {
            override fun onProgressChanged(
                seekBar: DiscreteSeekBar,
                value: Int,
                fromUser: Boolean,
            ) {
                marqueeView.setTextSpeed(value.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: DiscreteSeekBar) {}
            override fun onStopTrackingTouch(seekBar: DiscreteSeekBar) {}
        })
        marquee_content!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                marqueeView.setContent(marQueeContent)
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        immersive(marqueeBinding.toolbar)
        marquee_color!!.setOnClickListener { view: View? ->
            ColorPickerDialogBuilder
                .with(this@MarQueeActivity)
                .setTitle("颜色选择")
                .initialColor(this@MarQueeActivity.getColor(R.color.backgroundColor))
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setOnColorSelectedListener { }
                .setPositiveButton("确定") { _: DialogInterface?, selectedColor: Int, _: Array<Int?>? ->
                    //changeBackgroundColor(selectedColor);
                    //mActivity_qrcode_white_edit.setText(String.valueOf(selectedColor));
                    marquee_color!!.setText(selectedColor.toString())
                }
                .setNegativeButton("取消") { _: DialogInterface?, which: Int -> }
                .build()
                .show()
        }
        marqueeBinding.marqueeStart.setOnClickListener {
            if (marqueeBinding.marqueeContent.text.toString().isEmpty()) {
                marqueeBinding.marqueeContentlay.error = "不能为空"
            } else { // A类传递参数到B类
                val intent = Intent(this@MarQueeActivity, FullScreenMarQuee::class.java)
                intent.putExtra("content", marQueeContent)
                intent.putExtra("color", marQueeColor)
                intent.putExtra("size", seekBar1.progress)
                intent.putExtra("speed", seekBar2.progress)
                startActivity(intent)
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        supportActionBar!!.title = "滚动字幕"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    val marQueeContent: String
        get() = marquee_content!!.text.toString()
    private val marQueeColor: Int
        get() = Objects.requireNonNull(marquee_color!!.text).toString().toInt()
    val marQueeSize: Float
        get() = marquee_size!!.text.toString().toFloat()
    val marQueeSpeed: Float
        get() = marquee_speed!!.text.toString().toFloat()
    private var marquee_contentlay: TextInputLayout? = null
    private var marquee_content: TextInputEditText? = null
    private var mqcl: TextInputLayout? = null
    private var marquee_color: TextInputEditText? = null
    private val msize: TextInputLayout? = null
    private val marquee_size: TextInputEditText? = null
    private val mspeed: TextInputLayout? = null
    private val marquee_speed: TextInputEditText? = null
    private var marquee_start: Button? = null
    private fun initView(activity: Activity) {
        marquee_contentlay = activity.findViewById(R.id.marquee_contentlay)
        marquee_content = activity.findViewById(R.id.marquee_content)
        mqcl = activity.findViewById(R.id.mqcl)
        marquee_color = activity.findViewById(R.id.marquee_color)
        marquee_start = activity.findViewById(R.id.marquee_start)
    }

    companion object {
        const val requestCode = 10003 //自定义申请码
    }
}