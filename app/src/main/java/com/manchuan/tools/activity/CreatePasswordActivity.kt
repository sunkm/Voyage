package com.manchuan.tools.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ClipboardUtils
import com.kongzue.dialogx.dialogs.PopTip
import com.manchuan.tools.databinding.ActivityCreatePasswordActvityBinding
import java.security.SecureRandom

class CreatePasswordActivity : AppCompatActivity() {
    private lateinit var passwordBinding: ActivityCreatePasswordActvityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        passwordBinding = ActivityCreatePasswordActvityBinding.inflate(layoutInflater)
        setContentView(passwordBinding.root)
        setSupportActionBar(passwordBinding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "强密码生成"
        passwordBinding.create.setOnClickListener {
            runCatching {
                passwordBinding.autocomplete1.setText(getRandomPwd(passwordBinding.seekbar1.value.toInt()))
            }.onSuccess {
                PopTip.show("生成完成")
            }
        }
        passwordBinding.imageview1.setOnClickListener {
            if (passwordBinding.autocomplete1.text.toString().isEmpty()) {
                PopTip.show("无内容")
            } else {
                ClipboardUtils.copyText(passwordBinding.autocomplete1.text.toString())
                PopTip.show("复制成功")
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private val lowStr = "abcdefghijklmnopqrstuvwxyz"
    private val specialStr = "~!@#$%^&*()_+/-=[]{};:'<>?."
    private val numStr = "0123456789"

    // 随机获取字符串字符
    private fun getRandomChar(str: String): Char {
        val random = SecureRandom()
        return str[random.nextInt(str.length)]
    }

    // 随机获取小写字符
    private fun getLowChar(): Char {
        return getRandomChar(lowStr)
    }

    // 随机获取大写字符
    private fun getUpperChar(): Char {
        return getLowChar().uppercaseChar()
    }

    // 随机获取数字字符
    private fun getNumChar(): Char {
        return getRandomChar(numStr)
    }

    // 随机获取特殊字符
    private fun getSpecialChar(): Char {
        return getRandomChar(specialStr)
    }

    //指定调用字符函数
    private fun getRandomChar(funNum: Int): Char {
        return when (funNum) {
            0 -> getLowChar()
            1 -> getUpperChar()
            2 -> getNumChar()
            else -> getSpecialChar()
        }
    }

    // 指定长度，随机生成复杂密码
    private fun getRandomPwd(num: Int): String? {
        if (num > 26 || num < 8) {
            println("长度不满足要求")
            return ""
        }
        val list: MutableList<Char> = ArrayList(num)
        list.add(getLowChar())
        list.add(getUpperChar())
        list.add(getNumChar())
        list.add(getSpecialChar())
        for (i in 4 until num) {
            val random = SecureRandom()
            val funNum: Int = random.nextInt(4)
            list.add(getRandomChar(funNum))
        }
        list.shuffle() // 打乱排序
        val stringBuilder = StringBuilder(list.size)
        for (c in list) {
            stringBuilder.append(c)
        }
        return stringBuilder.toString()
    }

}