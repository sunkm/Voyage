package com.manchuan.tools.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ClipboardUtils
import com.drake.brv.utils.grid
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.utils.scope
import com.drake.statusbar.immersive
import com.kongzue.dialogx.dialogs.BottomMenu
import com.kongzue.dialogx.dialogs.PopTip
import com.manchuan.tools.R
import com.manchuan.tools.database.Movies
import com.manchuan.tools.database.MoviesDatabase
import com.manchuan.tools.databinding.ActivityMoviesHistoryBinding
import com.manchuan.tools.databinding.ItemMoviesBinding
import com.manchuan.tools.model.MoviesTag

class MoviesHistory : AppCompatActivity() {
    private var moviesList: List<Movies>? = null
    private var moviesListTwo: ArrayList<MoviesTag>? = null
    private val historyBinding by lazy {
        ActivityMoviesHistoryBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(historyBinding.root)
        setSupportActionBar(historyBinding.toolbar)
        moviesList = ArrayList()
        moviesListTwo = ArrayList()
        immersive(historyBinding.toolbar)
        supportActionBar?.apply {
            title = "接口2历史解析记录"
            setDisplayHomeAsUpEnabled(true)
        }
        historyBinding.recyclerView.grid(3).setup {
            addType<MoviesTag>(R.layout.item_movies)
            onBind {
                val binding = ItemMoviesBinding.bind(itemView)
                binding.name.text = getModel<MoviesTag>().title
            }
            onClick(R.id.movie) {
                BottomMenu.show("操作", null, listOf("复制链接"))
                    .setOnMenuItemClickListener { dialog, text, index ->
                        when (text) {
                            "复制链接" -> {
                                PopTip.show("已复制")
                                ClipboardUtils.copyText(getModel<MoviesTag>().url)
                            }
                        }
                        false
                    }
            }
        }
        historyBinding.page.onRefresh {
            scope {
                moviesList =
                    MoviesDatabase.getInstance(applicationContext).getMoviesDao().queryAllMovies()
                moviesListTwo?.clear()
                (0 until moviesList?.size!!).forEach { num ->
                    moviesList?.let {
                        val moviesTag = MoviesTag(it[num].title, it[num].url)
                        moviesListTwo?.add(moviesTag)
                    }
                }
                historyBinding.recyclerView.models = moviesListTwo
            }
        }.autoRefresh()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}