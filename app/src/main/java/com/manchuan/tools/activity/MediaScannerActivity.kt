package com.manchuan.tools.activity

import android.animation.LayoutTransition
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.drake.statusbar.immersive
import com.lxj.androidktx.core.tip
import com.manchuan.tools.databinding.ActivityMediaScannerBinding
import java.io.File

class MediaScannerActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMediaScannerBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        immersive(binding.toolbar)
        supportActionBar?.apply {
            title = "媒体库刷新"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.layout.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        binding.fab.setOnClickListener {
            binding.progress.visibility = View.VISIBLE
            supportActionBar?.apply {
                title = "扫描中"
            }
            MediaScannerConnection.scanFile(applicationContext,
                arrayOf(
                    Environment.getExternalStorageDirectory().absolutePath + File.separator + Environment.DIRECTORY_DOWNLOADS,
                    Environment.getExternalStorageDirectory().absolutePath + File.separator + Environment.DIRECTORY_MOVIES,
                    Environment.getExternalStorageDirectory().absolutePath + File.separator + Environment.DIRECTORY_MUSIC,
                    Environment.getExternalStorageDirectory().absolutePath + File.separator + Environment.DIRECTORY_PICTURES,
                    Environment.getExternalStorageDirectory().absolutePath + File.separator + Environment.DIRECTORY_DCIM
                ),
                arrayOf("*/*")) { s, uri ->
                runOnUiThread {
                    binding.progress.visibility = View.GONE
                    supportActionBar?.apply {
                        title = "媒体库刷新"
                    }
                    tip("扫描完成")
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}