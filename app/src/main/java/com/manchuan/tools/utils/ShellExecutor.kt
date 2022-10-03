package com.manchuan.tools.utils

import java.io.IOException


object ShellExecutor {
    private var extraEnvPath: String? = ""
    private var defaultEnvPath =
        "" // /sbin:/system/sbin:/system/bin:/system/xbin:/odm/bin:/vendor/bin:/vendor/xbin

    fun setExtraEnvPath(extraEnvPath: String?) {
        ShellExecutor.extraEnvPath = extraEnvPath
    }

    // FIXME:非root模式下，默认的 TMPDIR=/data/local/tmp 变量可能会导致某些需要写缓存的场景（例如使用source指令）脚本执行失败！
    private val envPath: String?
        get() {
            // FIXME:非root模式下，默认的 TMPDIR=/data/local/tmp 变量可能会导致某些需要写缓存的场景（例如使用source指令）脚本执行失败！
            if (extraEnvPath != null && extraEnvPath!!.isNotEmpty()) {
                if (defaultEnvPath.isEmpty()) {
                    try {
                        val process = Runtime.getRuntime().exec("sh")
                        val outputStream = process.outputStream
                        outputStream.write("echo \$PATH".toByteArray())
                        outputStream.flush()
                        outputStream.close()
                        val inputStream = process.inputStream
                        val cache = ByteArray(16384)
                        val length = inputStream.read(cache)
                        inputStream.close()
                        process.destroy()
                        val path = String(cache, 0, length).trim { it <= ' ' }
                        if (path.isNotEmpty()) {
                            defaultEnvPath = path
                        } else {
                            throw RuntimeException("未能获取到\$PATH参数")
                        }
                    } catch (ex: Exception) {
                        defaultEnvPath =
                            "/sbin:/system/sbin:/system/bin:/system/xbin:/odm/bin:/vendor/bin:/vendor/xbin"
                    }
                }
                val path = defaultEnvPath
                return "PATH=" + path + ":" + extraEnvPath
            }
            return null
        }

    @Throws(IOException::class)
    private fun getProcess(run: String): Process {
        val env = envPath
        val runtime = Runtime.getRuntime()
        /*
        // 部分机型会有Aborted错误
        if (env != null) {
            return runtime.exec(run, new String[]{
                env
            });
        }
        */
        val process = runtime.exec(run)
        if (env != null) {
            val outputStream = process.outputStream
            outputStream.write("export ".toByteArray())
            outputStream.write(env.toByteArray())
            outputStream.write("\n".toByteArray())
            outputStream.flush()
        }
        return process
    }

    @get:Throws(IOException::class)
    val superUserRuntime: Process
        get() = getProcess("su")

    @get:Throws(IOException::class)
    val runtime: Process
        get() = getProcess("sh")
}