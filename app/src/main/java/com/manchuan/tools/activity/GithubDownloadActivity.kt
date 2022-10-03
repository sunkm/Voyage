package com.manchuan.tools.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.drake.net.Get
import com.drake.net.component.Progress
import com.drake.net.interfaces.ProgressListener
import com.drake.net.utils.scopeNetLife
import com.drake.statusbar.immersive
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.databinding.ActivityGithubDownloadBinding
import java.io.File


class GithubDownloadActivity : AppCompatActivity() {
    private var githubBinding: ActivityGithubDownloadBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        githubBinding = ActivityGithubDownloadBinding.inflate(LayoutInflater.from(this))
        setContentView(githubBinding?.root)
        immersive(githubBinding?.toolbar!!)
        setSupportActionBar(githubBinding?.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Github下载加速"
        githubBinding?.materialbutton1?.setOnClickListener {
            githubBinding?.progressBar?.visibility = View.VISIBLE
            githubBinding?.progressBar?.isIndeterminate = true
            scopeNetLife {
                val file =
                    Get<File>("https://ghproxy.com/" + githubBinding?.url?.text?.toString()) {
                        addDownloadListener(object : ProgressListener() {

                            override fun onProgress(p: Progress) {
                                githubBinding?.progressBar?.isIndeterminate = false
                                githubBinding?.progressBar?.setProgressCompat(p.progress(), true)
                            }
                        })
                    }.await()
                githubBinding?.progressBar?.visibility = View.GONE
                githubBinding?.progressBar?.isIndeterminate = false
                TipDialog.show("已保存到下载目录", WaitDialog.TYPE.SUCCESS)
            }.catch {
                githubBinding?.progressBar?.visibility = View.GONE
                githubBinding?.progressBar?.isIndeterminate = false
                TipDialog.show("下载失败", WaitDialog.TYPE.ERROR)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

}