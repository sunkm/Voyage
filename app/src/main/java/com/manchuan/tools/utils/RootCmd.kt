package com.manchuan.tools.utils

import timber.log.Timber
import java.io.*
import java.lang.Exception
import java.lang.StringBuilder
import java.nio.charset.StandardCharsets

/**
 * @author ManChuan
 */
object RootCmd {
    private const val TAG = "RootCmd"
    private var mHaveRoot = false
    @JvmStatic
    fun haveRoot(): Boolean {
        if (!mHaveRoot) {
            val ret = execRootCmdSilent("echo test")
            if (ret != -1) {
                Timber.i("have root!")
                mHaveRoot = true
            } else {
                Timber.i("not root!")
                try {
                    execRootCmdSilent("su")
                } catch (ignored: Exception) {
                }
            }
        } else {
            Timber.i("mHaveRoot = true, have root!")
        }
        return mHaveRoot
    }

    @JvmStatic
    fun execRootCmd(cmd: String) {
        val result = StringBuilder()
        var dos: DataOutputStream? = null
        var dis: DataInputStream? = null
        try {
            val p = Runtime.getRuntime().exec("su")
            dos = DataOutputStream(p.outputStream)
            dis = DataInputStream(p.inputStream)
            Timber.i(cmd)
            dos.writeBytes(
                """
    $cmd
    
    """.trimIndent()
            )
            dos.flush()
            dos.writeBytes("exit\n")
            dos.flush()
            var line: String?
            while (dis.readLine().also { line = it } != null) {
                Timber.d(line!!)
                result.append(line)
            }
            p.waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (dos != null) {
                try {
                    dos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            if (dis != null) {
                try {
                    dis.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    // 执行命令但不关注结果输出 
    @JvmStatic
    fun execRootCmdSilent(cmd: String): Int {
        var result = -1
        var dos: DataOutputStream? = null
        try {
            val p = Runtime.getRuntime().exec("su")
            dos = DataOutputStream(p.outputStream)
            Timber.i(cmd)
            dos.writeBytes(
                """
    $cmd
    
    """.trimIndent()
            )
            dos.flush()
            dos.writeBytes("exit\n")
            dos.flush()
            p.waitFor()
            result = p.exitValue()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (dos != null) {
                try {
                    dos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return result
    }

    @JvmStatic
    fun runRootCommand(command: String) {
        var process: Process? = null
        var dataOutputStream: DataOutputStream? = null
        var dataInputStream: DataInputStream? = null
        val wifiConf = StringBuilder()
        try {
            process = Runtime.getRuntime().exec("su")
            dataOutputStream = DataOutputStream(process.outputStream)
            dataInputStream = DataInputStream(process.inputStream)
            dataOutputStream
                .writeBytes(
                    """
    $command
    
    """.trimIndent()
                )
            dataOutputStream.writeBytes("exit\n")
            dataOutputStream.flush()
            val inputStreamReader = InputStreamReader(
                dataInputStream, StandardCharsets.UTF_8
            )
            val bufferedReader = BufferedReader(
                inputStreamReader
            )
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                wifiConf.append(line)
            }
            bufferedReader.close()
            inputStreamReader.close()
            process.waitFor()
            Timber.d(process.exitValue().toString() + "")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                dataOutputStream?.close()
                dataInputStream?.close()
                assert(process != null)
                process!!.destroy()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}