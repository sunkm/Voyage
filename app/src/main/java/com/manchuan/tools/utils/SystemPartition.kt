package com.manchuan.tools.utils

import android.annotation.SuppressLint
import com.manchuan.tools.utils.RootCmd.execRootCmd
import com.manchuan.tools.utils.RootCmd.execRootCmdSilent
import com.manchuan.tools.utils.RootCmd.haveRoot
import timber.log.Timber
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class SystemPartition private constructor() {
    init {
        Timber.tag(TAG).i("new SystemMount()")
    }

    private object SystemPartitionHolder {
        val instance = SystemPartition()
    }

    val instance: SystemPartition
        get() = SystemPartitionHolder.instance

    companion object {
        private const val TAG = "SystemMount"
        @SuppressLint("SdCardPath")
        private const val TMP_PATH = "/sdcard/mount.txt"
        private var mMountPiont: String? = null
        private var mWriteable = false

        //              Runtime.getRuntime().exec("mount > " + TMP_PATH); 
        val systemMountPiont: String?
            get() {
                var dis: DataInputStream? = null
                if (mMountPiont == null) {
                    try {
                        execRootCmd("mount > $TMP_PATH")
                        //              Runtime.getRuntime().exec("mount > " + TMP_PATH); 
                        dis = DataInputStream(FileInputStream(TMP_PATH))
                        var line: String? = null
                        var index = -1
                        while (dis.readLine().also { line = it } != null) {
                            index = line!!.indexOf(" /system ")
                            if (index > 0) {
                                mMountPiont = line!!.substring(0, index)
                                if (line!!.indexOf(" rw") > 0) {
                                    mWriteable = true
                                    Timber.i("/system is writeable !")
                                } else {
                                    mWriteable = false
                                    Timber.i("/system is readonly !")
                                }
                                break
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        if (dis != null) {
                            try {
                                dis.close()
                            } catch (e1: IOException) {
                                e1.printStackTrace()
                            }
                            dis = null
                        }
                        val f = File(TMP_PATH)
                        if (f.exists()) {
                            f.delete()
                        }
                    }
                }
                if (mMountPiont != null) {
                    Timber.tag(TAG).i("/system mount piont: %s", mMountPiont)
                } else {
                    Timber.tag(TAG).i("get /system mount piont failed !!!")
                }
                return mMountPiont
            }
        private val isWriteable: Boolean
            get() {
                mMountPiont = null
                systemMountPiont
                return mWriteable
            }

        fun remountSystem(writeable: Boolean) {
            var cmd: String? = null
            systemMountPiont
            if (mMountPiont != null && haveRoot()) {
                cmd = if (writeable) {
                    "mount -o remount,rw $mMountPiont /system"
                } else {
                    "mount -o remount,ro $mMountPiont /system"
                }
                execRootCmdSilent(cmd)
                isWriteable
            }
        }
    }
}