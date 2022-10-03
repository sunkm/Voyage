package com.manchuan.tools.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ClipboardUtils
import com.gyf.immersionbar.ImmersionBar
import com.kongzue.dialogx.dialogs.PopTip
import com.lxj.androidktx.core.decryptAES
import com.lxj.androidktx.core.encryptAES
import com.manchuan.tools.R
import com.manchuan.tools.databinding.ActivityAesCryptBinding
import com.manchuan.tools.utils.ColorUtils
import org.apache.commons.lang3.StringUtils
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class AesCrypt : AppCompatActivity() {

    private val cryptBinding by lazy {
        ActivityAesCryptBinding.inflate(layoutInflater)
    }

    private var type = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(cryptBinding.root)
        setSupportActionBar(cryptBinding.toolbar)
        ImmersionBar.with(this).autoDarkModeEnable(true)
            .statusBarColorInt(ColorUtils.statusBarColor)
            .navigationBarColorInt(ColorUtils.statusBarColor).titleBar(cryptBinding.toolbar).init()
        supportActionBar?.apply {
            title = "AES加解密"
            setDisplayHomeAsUpEnabled(true)
        }
        cryptBinding.toggleButton.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.interfaceOne -> type = 1
                    R.id.interfaceTwo -> type = 2
                }
            }
        }
        cryptBinding.jiexi.setOnClickListener {
            val string = cryptBinding.url.text.toString()
            val password = cryptBinding.password.text.toString()
            if (StringUtils.isBlank(string) || StringUtils.isBlank(password)) {
                PopTip.show("请输入内容和密钥")
            } else if (password.length > 16) {
                PopTip.show("密钥超过16位")
            } else if (password.length < 16) {
                PopTip.show("密钥必须16位，且不能包含特殊字符")
            } else if (StringUtils.isNotBlank(string)) {
                when (type) {
                    1 -> runCatching {
                        cryptBinding.info.setText(string.encryptAES(password))
                    }.onFailure {
                        PopTip.show("加密失败:${it.message}")
                    }

                    2 -> runCatching {
                        cryptBinding.info.setText(string.decryptAES(password))
                    }.onFailure {
                        PopTip.show("解密失败:${it.message}")
                    }

                    else -> {}
                }
            }
        }
        cryptBinding.imageview1.setOnClickListener {
            val string = cryptBinding.info.text.toString()
            if (StringUtils.isBlank(string)) {
                PopTip.show("无内容")
            } else {
                PopTip.show("已复制")
                ClipboardUtils.copyText(string)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    /** 密钥长度: 128, 192 or 256  */
    private val KEY_SIZE = 128

    /** 加密/解密算法名称  */
    private val ALGORITHM = "AES"

    /** 随机数生成器（RNG）算法名称  */
    private val RNG_ALGORITHM = "SHA1PRNG"

    /**
     * 生成密钥对象
     */
    @Throws(Exception::class)
    private fun generateKey(key: ByteArray): SecretKey {
        // 创建安全随机数生成器
        val random: SecureRandom = SecureRandom.getInstance(RNG_ALGORITHM)
        // 设置 密钥key的字节数组 作为安全随机数生成器的种子
        random.setSeed(key)

        // 创建 AES算法生成器
        val gen: KeyGenerator = KeyGenerator.getInstance(ALGORITHM)
        // 初始化算法生成器
        gen.init(KEY_SIZE, random)

        // 生成 AES密钥对象, 也可以直接创建密钥对象: return new SecretKeySpec(key, ALGORITHM);
        return gen.generateKey()
    }

    /**
     * 数据加密: 明文 -> 密文
     */
    @SuppressLint("GetInstance")
    @Throws(Exception::class)
    fun encrypt(plainBytes: ByteArray?, key: ByteArray): ByteArray? {
        // 生成密钥对象
        val secKey: SecretKey = generateKey(key)

        // 获取 AES 密码器
        val cipher = Cipher.getInstance(ALGORITHM)
        // 初始化密码器（加密模型）
        cipher.init(Cipher.ENCRYPT_MODE, secKey)

        // 加密数据, 返回密文
        return cipher.doFinal(plainBytes)
    }

    /**
     * 数据解密: 密文 -> 明文
     */
    @SuppressLint("GetInstance")
    @Throws(Exception::class)
    fun decrypt(cipherBytes: ByteArray?, key: ByteArray): ByteArray? {
        // 生成密钥对象
        val secKey: SecretKey = generateKey(key)

        // 获取 AES 密码器
        val cipher = Cipher.getInstance(ALGORITHM)
        // 初始化密码器（解密模型）
        cipher.init(Cipher.DECRYPT_MODE, secKey)

        // 解密数据, 返回明文
        return cipher.doFinal(cipherBytes)
    }

    /**
     * 加密文件: 明文输入 -> 密文输出
     */
    @Throws(Exception::class)
    fun encryptFile(plainIn: File, cipherOut: File, key: ByteArray) {
        aesFile(plainIn, cipherOut, key, true)
    }

    /**
     * 解密文件: 密文输入 -> 明文输出
     */
    @Throws(Exception::class)
    fun decryptFile(cipherIn: File, plainOut: File, key: ByteArray) {
        aesFile(plainOut, cipherIn, key, false)
    }

    /**
     * AES 加密/解密文件
     */
    @SuppressLint("GetInstance")
    @Throws(Exception::class)
    private fun aesFile(plainFile: File, cipherFile: File, key: ByteArray, isEncrypt: Boolean) {
        // 获取 AES 密码器
        val cipher = Cipher.getInstance(ALGORITHM)
        // 生成密钥对象
        val secKey: SecretKey = generateKey(key)
        // 初始化密码器
        cipher.init(if (isEncrypt) Cipher.ENCRYPT_MODE else Cipher.DECRYPT_MODE, secKey)

        // 加密/解密数据
        var `in`: InputStream? = null
        var out: OutputStream? = null
        try {
            if (isEncrypt) {
                // 加密: 明文文件为输入, 密文文件为输出
                `in` = FileInputStream(plainFile)
                out = FileOutputStream(cipherFile)
            } else {
                // 解密: 密文文件为输入, 明文文件为输出
                `in` = FileInputStream(cipherFile)
                out = FileOutputStream(plainFile)
            }
            val buf = ByteArray(1024)
            var len = -1

            // 循环读取数据 加密/解密
            while (`in`.read(buf).also { len = it } != -1) {
                out.write(cipher.update(buf, 0, len))
            }
            out.write(cipher.doFinal()) // 最后需要收尾
            out.flush()
        } finally {
            close(`in`)
            close(out)
        }
    }

    private fun close(c: Closeable?) {
        if (c != null) {
            try {
                c.close()
            } catch (e: IOException) {
                // nothing
            }
        }
    }

}