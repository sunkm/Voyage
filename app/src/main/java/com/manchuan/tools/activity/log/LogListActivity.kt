package com.manchuan.tools.activity.log

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.internalFileDirPath
import com.dylanc.longan.startActivity
import com.lxj.androidktx.core.toDateString
import com.manchuan.tools.R
import com.manchuan.tools.databinding.ActivityLogListBinding
import com.manchuan.tools.databinding.ItemLogBinding
import com.manchuan.tools.utils.UiUtils
import java.io.File

class LogListActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityLogListBinding.inflate(layoutInflater)
    }

    private val fileList = mutableListOf<FileModel>()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        immerseStatusBar(!UiUtils.isDarkMode())
        binding.toolbar.toolbar.apply {
            title = "崩溃日志"
            setNavigationOnClickListener {
                finish()
            }
        }
        File(internalFileDirPath + File.separator + "tombstones").listFiles()?.forEach {
            fileList.add(FileModel(it.name, it.lastModified().toDateString(), it.absolutePath))
        }
        binding.recyclerView.linear().setup {
            addType<FileModel>(R.layout.item_log)
            onBind {
                val binding = ItemLogBinding.bind(itemView)
                val model = getModel<FileModel>(modelPosition)
                binding.title.text = model.name
                binding.summary.text = "崩溃文件创建时间:${model.time}"
            }
            onClick(R.id.item) {
                val model = getModel<FileModel>(modelPosition)
                startActivity<ViewerActivity>("file" to model.path)
            }
        }.models = fileList
    }

    data class FileModel(var name: String, var time: String, var path: String)

}