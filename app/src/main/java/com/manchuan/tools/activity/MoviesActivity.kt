package com.manchuan.tools.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.drake.statusbar.immersive
import com.dylanc.longan.startActivity
import com.dylanc.longan.textString
import com.kongzue.dialogx.dialogs.PopTip
import com.manchuan.tools.R
import com.manchuan.tools.databinding.ActivityMoviesBinding

class MoviesActivity : AppCompatActivity() {
    private val moviesBinding by lazy {
        ActivityMoviesBinding.inflate(layoutInflater)
    }
    private var interfaceNumber: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(moviesBinding.root)
        setSupportActionBar(moviesBinding.toolbar)
        immersive(moviesBinding.toolbar)
        supportActionBar?.apply {
            title = "影视解析"
            setDisplayHomeAsUpEnabled(true)
        }
        moviesBinding.toggleButton.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.interfaceOne -> {
                        interfaceNumber = 1
                    }
                    R.id.interfaceTwo -> {
                        interfaceNumber = 2
                    }
                }
            } else {
                if (group.checkedButtonId == View.NO_ID) {
                    interfaceNumber = 1
                }
            }
        }
        moviesBinding.jiexi.setOnClickListener {
            if (TextUtils.isEmpty(moviesBinding.url.text.toString())) {
                PopTip.show("请输入内容")
            } else {
                when (interfaceNumber) {
                    1 -> {
                        val intent = Intent(this, FullscreenVideoActivity::class.java)
                        intent.putExtra(
                            "url",
                            "https://m2090.com/?url=" + moviesBinding.url.text.toString()
                        )
                        startActivity(intent)
                    }
                    2 -> {
                        startActivity<FullscreenVideoActivity>("url" to "http://jx.aidouer.net/?url=${moviesBinding.url.textString}")
                        /*WaitDialog.show("解析中...")
                        scopeNetLife {
                            val content =
                                Get<String>("https://yjx.maxed.top/video/?url=" + moviesBinding.url.text.toString()) {
                                    setCacheMode(CacheMode.REQUEST_THEN_READ)
                                    setHeader(
                                        "User-Agent",
                                        WebActivity.getUserAgent(this@MoviesActivity)
                                    )
                                }.await()
                            val json = JSON.parseObject(content)
                            if (json.getIntValue("success") == 1) {
                                WaitDialog.dismiss()
                                val movie = Movies(json.getString("title"),json.getString("url"))
                                MoviesDatabase.getInstance(applicationContext).getMoviesDao().insertMovie(movies = movie)
                            } else {
                                TipDialog.show("解析失败", WaitDialog.TYPE.ERROR)
                            }
                        }.catch {
                            WaitDialog.dismiss()
                            PopTip.show(it.message)
                        }

                         */
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.movies_history,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.history -> {
                startActivity(Intent(this,MoviesHistory::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

}