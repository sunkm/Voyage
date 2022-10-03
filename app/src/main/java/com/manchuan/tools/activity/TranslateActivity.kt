package com.manchuan.tools.activity

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.drake.net.Get
import com.drake.net.okhttp.trustSSLCertificate
import com.drake.net.utils.scopeNetLife
import com.drake.softinput.hideSoftInput
import com.drake.softinput.setWindowSoftInput
import com.drake.spannable.replaceSpan
import com.drake.spannable.span.ColorSpan
import com.drake.statusbar.immersive
import com.dylanc.longan.design.snackbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.kongzue.dialogx.dialogs.PopTip
import com.lxj.androidktx.core.tip
import com.manchuan.tools.R
import com.manchuan.tools.databinding.ActivityTranslateBinding
import com.manchuan.tools.extensions.accentColor
import com.manchuan.tools.extensions.applyAccentColor
import com.manchuan.tools.extensions.userAgent
import com.manchuan.tools.interfaces.TranslateCallback
import org.json.JSONArray
import rikka.material.app.MaterialActivity
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URLEncoder
import java.util.Locale

class TranslateActivity : MaterialActivity(), OnInitListener {

    override fun onInit(p1: Int) {
        if (p1 == TextToSpeech.SUCCESS) {
            val result = textToSpeech!!.setLanguage(Locale.getDefault())
            ttsParam()
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tip("语音包丢失或语音不支持")
            }
        }
    }

    private lateinit var toolbar: Toolbar
    private var textToSpeech: TextToSpeech? = null
    private var langId: List<*>? = null
    private var targetLang = "zh-CN"
    private var minLang = "auto"
    private fun ttsParam() {
        textToSpeech!!.setPitch(1.0f) // 设置音调，,1.0是常规
        textToSpeech!!.setSpeechRate(1.0f) //设定语速，1.0正常语速
    }

    private val binding by lazy {
        ActivityTranslateBinding.inflate(layoutInflater)
    }

    private val TRANSLATE_BASE_URL = "https://translate.googleapis.com/" // 不需要翻墙即可使用


    fun translate(
        targetLan: String,
        content: String?,
        callback: TranslateCallback?,
    ) {
        val LAN_AUTO = "auto"
        translate(LAN_AUTO, targetLan, content, callback)
    }

    fun translate(
        sourceLan: String,
        targetLan: String,
        content: String?,
        callback: TranslateCallback?,
    ): String {
        var result = ""
        if (content == null || content == "") {
            return result
        }
        scopeNetLife {
            val proxy = Proxy(Proxy.Type.SOCKS, InetSocketAddress("44.202.150.166", 11257))
            val string = Get<String>(
                getTranslateUrl(
                    sourceLan, targetLan, content
                )
            ) {
                setHeader("User-Agent", userAgent())
                setClient {
                    trustSSLCertificate()
                    //proxy(proxy)
                }
            }.await()
            val jsonArray = JSONArray(string).getJSONArray(0)
            for (i in 0 until jsonArray.length()) {
                result += jsonArray.getJSONArray(i).getString(0)
            }
            callback?.onTranslateDone(result)
        }
        return result
    }

    private fun getTranslateUrl(sourceLan: String, targetLan: String, content: String): String {
        return try {
            TRANSLATE_BASE_URL + "translate_a/single?client=gtx&sl=" + sourceLan + "&tl=" + targetLan + "&dt=t&q=" + URLEncoder.encode(
                content, "UTF-8"
            )
        } catch (e: Exception) {
            TRANSLATE_BASE_URL + "translate_a/single?client=gtx&sl=" + sourceLan + "&tl=" + targetLan + "&dt=t&q=" + content
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        toolbar = binding.toolbar
        initView()
        registerForContextMenu(mButton)
        KeyboardUtils.fixAndroidBug5497(this)
        KeyboardUtils.fixSoftInputLeaks(this)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
        immersive(toolbar)
        langId = listOf(*resources.getStringArray(R.array.language_id))
        if (textToSpeech == null) {
            textToSpeech = TextToSpeech(this, this)
        }
        mButton!!.setOnClickListener { view: View ->
            if (targetLang == minLang) {
                PopTip.show("不能选择同一语言")
            } else {
                showMinLangMenu(view)
            }
        }
        setWindowSoftInput(float = bottom, setPadding = true, onChanged = {

        })
        mButton1!!.setOnClickListener { view: View ->
            if (minLang == targetLang) {
                PopTip.show("不能选择同一语言")
            } else {
                showTarLangMenu(view)
            }
        }
        mImageView3?.applyAccentColor()
        mImageView4?.applyAccentColor()
        mImageView3!!.setOnClickListener { view: View? ->
            if (mTextView!!.text.toString().isEmpty()) {
                snackbar("请先翻译内容")
            } else {
                ClipboardUtils.copyText(mTextView!!.text.toString())
                snackbar("已复制")
            }
        }
        mImageView4!!.setOnClickListener { view: View? ->
            if (mTextView!!.text.toString().isEmpty()) {
                snackbar("请先翻译内容")
            } else {
                textToSpeech!!.speak(mTextView!!.text.toString(), TextToSpeech.QUEUE_FLUSH, null)
            }
        }
    }

    private var mEditText: EditText? = null
    private var mTextView: AutoCompleteTextView? = null
    private var mCard1: MaterialCardView? = null
    private var mImageView3: AppCompatImageView? = null
    private var mCard2: MaterialCardView? = null
    private var mImageView4: AppCompatImageView? = null
    private var mButton: MaterialButton? = null
    private var mButton1: MaterialButton? = null
    private var mFab: FloatingActionButton? = null
    override fun onContextItemSelected(item: MenuItem): Boolean {
        mButton!!.text = item.title
        return super.onContextItemSelected(item)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)
    }

    private fun showMinLangMenu(view: View) {
        // View当前PopupMenu显示的相对View的位置
        val popupMenu = PopupMenu(this, view)
        // menu布局
        val language: List<*> = listOf(*resources.getStringArray(R.array.language))
        language.size
        var i = 0
        while (popupMenu.menu.size() < language.size) {
            popupMenu.menu.add(0, Menu.FIRST + i, i, language[i].toString())
            i++
        }
        // menu的item点击事件
        popupMenu.setOnMenuItemClickListener { item ->
            mButton!!.text = item.title
            minLang = langId!![item.itemId - 1].toString()
            false
        }
        popupMenu.show()
    }

    private fun showTarLangMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        val language: List<*> = listOf(*resources.getStringArray(R.array.language))
        language.size
        var i = 0
        while (popupMenu.menu.size() < language.size) {
            popupMenu.menu.add(0, Menu.FIRST + i, i, language[i].toString())
            i++
        }
        popupMenu.menu.removeItem(1)
        popupMenu.setOnMenuItemClickListener { item ->
            mButton1!!.text = item.title
            targetLang = langId!![item.itemId - 1].toString()
            false
        }
        popupMenu.show()
    }

    private lateinit var bottom: LinearLayoutCompat

    private fun initView() {
        bottom = binding.bottom
        mEditText = binding.editText
        mTextView = binding.textView
        mCard1 = binding.card1
        mImageView3 = binding.imageView3
        mCard2 = binding.card2
        mImageView4 = binding.imageView4
        mButton = binding.button
        mButton1 = binding.button1
        mFab = binding.fab
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        val titles = "Google Translate".replaceSpan("Translate") {
            ColorSpan(accentColor())
        }
        supportActionBar?.apply {
            title = titles
        }
        mFab!!.setOnClickListener {
            if (mEditText!!.text.toString().isEmpty()) {
                snackbar("请输入内容")
            } else {
                translate(
                    minLang,
                    targetLang,
                    mEditText!!.text.toString(),
                    object : TranslateCallback {
                        override fun onTranslateDone(result: String?) {
                            mTextView!!.setText(result)
                        }
                    })
            }
        }
    }

    override fun onPause() {
        super.onPause()
        hideSoftInput()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}