package com.manchuan.tools.activity

import android.os.Bundle
import android.os.Environment
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.drake.net.utils.scope
import com.drake.statusbar.immersive
import com.dylanc.longan.context
import com.manchuan.tools.base.BaseAlertDialogBuilder
import com.manchuan.tools.databinding.ActivityBackupWordLibraryBinding
import com.manchuan.tools.utils.AssetsUtil
import com.manchuan.tools.utils.KeepShell
import com.manchuan.tools.utils.RootUtil
import kotlinx.coroutines.Dispatchers
import java.io.File


class BackupWordLibrary : AppCompatActivity() {
    private val binding by lazy {
        ActivityBackupWordLibraryBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "备份字库"
            setDisplayHomeAsUpEnabled(true)
        }
        immersive(binding.toolbar)
        if (!RootUtil.isDeviceRooted()) {
            BaseAlertDialogBuilder(this).setTitle("警告").setMessage("设备无ROOT，无法使用该功能")
                .setCancelable(false).setPositiveButton("确定") { dialog, which ->
                    this.finish()
                }.show()
        } else {
            runCatching {
                AssetsUtil.copyFileFromAssets(context,
                    "wordlibrary.sh",
                    Environment.getExternalStorageDirectory().absolutePath + File.separator + Environment.DIRECTORY_DOWNLOADS,
                    "wordlibrary.sh")
                val folder =
                    File(Environment.getExternalStorageDirectory().absolutePath + File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator + "images_backup")
                if (!folder.exists()) {
                    folder.mkdirs()
                }
            }
            BaseAlertDialogBuilder(this).setTitle("提示").setMessage("确定要开始备份吗？如果在备份过程中出现任何问题，概不负责。")
                .setCancelable(false).setPositiveButton("确定") { dialog, which ->
                    scope(Dispatchers.IO) {
                        val result = KeepShell().doCmdSync("sh /sdcard/Download/wordlibrary.sh")
                        runOnUiThread {
                            binding.resultText.text = result
                        }
                        //KeepShellAsync(context, true).doCmd("sh /sdcard/Download/wordlibrary.sh")
                    }
                }.show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}