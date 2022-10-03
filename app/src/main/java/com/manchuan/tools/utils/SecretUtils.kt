package com.manchuan.tools.utils

import android.annotation.SuppressLint
import com.drake.net.utils.TipUtils.toast
import java.nio.charset.Charset
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.*
import javax.crypto.spec.SecretKeySpec

/**
 * Created by Song on 2017/2/22.
 */
class SecretUtils private constructor() {
    init {
        throw UnsupportedOperationException("constrontor cannot be init")
    }

    companion object {
        /**
         * 生成秘钥
         * @return
         */
        fun generateKey(): ByteArray {
            var keyGen: KeyGenerator? = null
            try {
                keyGen = KeyGenerator.getInstance("DESede") // 秘钥生成器
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }
            keyGen!!.init(168) // 初始秘钥生成器
            val secretKey = keyGen.generateKey() // 生成秘钥
            return secretKey.encoded // 获取秘钥字节数组
        }

        /**
         * 加密
         * @return
         */
        @SuppressLint("GetInstance")
        @JvmStatic
        fun encrypt(data: ByteArray?, key: String): ByteArray? {
            val secretKey: SecretKey =
                SecretKeySpec(key.toByteArray(Charset.defaultCharset()), "DESede") // 恢复秘钥
            var cipher: Cipher?
            var cipherBytes: ByteArray? = null
            try {
                cipher = Cipher.getInstance("DESede") // 对Cipher完成加密或解密工作
                cipher.init(Cipher.ENCRYPT_MODE, secretKey) // 对Cipher初始化,加密模式
                cipherBytes = cipher.doFinal(data) // 加密数据
            } catch (e: InvalidKeyException) {
                e.printStackTrace()
            } catch (e: NoSuchPaddingException) {
                e.printStackTrace()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: IllegalBlockSizeException) {
                e.printStackTrace()
            } catch (e: BadPaddingException) {
                e.printStackTrace()
            }
            return cipherBytes
        }

        /**
         * 解密
         * @return
         */
        @JvmStatic
        @SuppressLint("GetInstance")
        fun decrypt(data: String?, key: String): ByteArray? {
            val secretKey: SecretKey =
                SecretKeySpec(key.toByteArray(Charset.defaultCharset()), "DESede") // 恢复秘钥
            val cipher: Cipher?
            var plainBytes: ByteArray? = null
            try {
                cipher = Cipher.getInstance("DESede") // 对Cipher初始化,加密模式
                cipher.init(Cipher.DECRYPT_MODE, secretKey) // 对Cipher初始化,加密模式
                plainBytes = cipher.doFinal(data?.toByteArray(Charset.defaultCharset())) // 解密数据
            } catch (e: NoSuchAlgorithmException) {
                toast(e.toString())
            } catch (e: NoSuchPaddingException) {
                toast(e.toString())
            } catch (e: BadPaddingException) {
                toast(e.toString())
            } catch (e: IllegalBlockSizeException) {
                toast(e.toString())
            } catch (e: InvalidKeyException) {
                toast(e.toString())
            }
            return plainBytes
        }
    }
}