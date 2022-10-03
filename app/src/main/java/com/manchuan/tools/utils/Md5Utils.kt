package com.manchuan.tools.utils

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import kotlin.experimental.and


/**
 * 文件的MD5生成
 */
object Md5Util {
    fun getMd5(filePath: File?): String {
        try {
            val digest =
                MessageDigest.getInstance("MD5")
            val `in` = FileInputStream(filePath)
            var len = 0
            val buffer = ByteArray(1024)
            while (`in`.read(buffer).also { len = it } != -1) {
                digest.update(buffer, 0, len) //遍历文件,计算md5
            }
            val bs = digest.digest()
            val sb = StringBuffer()
            for (b in bs) {
                val i: Byte = b and 0xff.toByte()
                var hexString = Integer.toHexString(i.toInt())
                if (hexString.length < 2) {
                    hexString = "0$hexString"
                }
                sb.append(hexString)
            }
            return sb.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
}
