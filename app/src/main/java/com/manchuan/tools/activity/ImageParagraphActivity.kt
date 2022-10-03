package com.manchuan.tools.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.layoutmanager.HoverGridLayoutManager
import com.drake.brv.utils.grid
import com.drake.brv.utils.setup
import com.drake.net.Get
import com.drake.net.utils.scopeNetLife
import com.dylanc.longan.*
import com.kongzue.dialogx.dialogs.BottomMenu
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.R
import com.manchuan.tools.bean.images.Images
import com.manchuan.tools.databinding.ActivityImageParagraphBinding
import com.manchuan.tools.databinding.ItemsPreviewsBinding
import com.manchuan.tools.extensions.errorToast
import com.manchuan.tools.json.ImagesConvert
import com.manchuan.tools.model.WallpaperModel
import com.manchuan.tools.utils.UiUtils

class ImageParagraphActivity : AppCompatActivity() {

    private var dataList = mutableListOf<WallpaperModel>()
    private val binding by lazy {
        ActivityImageParagraphBinding.inflate(layoutInflater)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        immerseStatusBar(!UiUtils.isDarkMode())
        binding.toolbarLayout.toolbar.apply {
            setNavigationOnClickListener {
                finish()
            }
            title = "聚合图集解析"
        }
        binding.create.doOnClick {
            if (binding.time.isTextNotEmpty()) {
                scopeNetLife {
                    dataList.clear()
                    binding.recyclerView.adapter?.notifyDataSetChanged()
                    binding.state.showLoading()
                    val content =
                        Get<Images>("https://tenapi.cn/images/?url=${binding.time.textString}") {
                            converter = ImagesConvert()
                        }.await()
                    content.images.forEach {
                        //dataList.add(WallpaperModel(it))
                    }
                    binding.recyclerView.adapter?.notifyDataSetChanged()
                    binding.state.showContent()
                }.catch {
                    it.message?.errorToast()
                    binding.state.showError()
                }
            }
        }
        binding.recyclerView.grid(2, HoverGridLayoutManager.VERTICAL).setup {
            addType<WallpaperModel>(R.layout.items_previews)
            setAnimation(AnimationType.ALPHA)
            onBind {
                val binding = ItemsPreviewsBinding.bind(itemView)
                //Glide.with(context).load(getModel<WallpaperModel>().image).skipMemoryCache(true)
                //.diskCacheStrategy(SettingsLoader.diskCacheMethod).into(binding.image)
            }
            onClick(R.id.image) {
                BottomMenu.show("操作", null, listOf("下载", "设为壁纸"))
                    .setOnMenuItemClickListener { dialog, text, index ->
                        when (text) {
                            "下载" -> {
                                scopeNetLife {
                                    /*val file = Get<File>(getModel<WallpaperModel>().image) {
                                        setDownloadFileName(System.currentTimeMillis()
                                            .toString() + ".png")
                                        setDownloadMd5Verify(true)
                                        addDownloadListener(object : ProgressListener() {
                                            override fun onProgress(p: Progress) {
                                                WaitDialog.show("下载中", p.progress().toFloat())
                                            }

                                        })
                                    }.await()
                                    TipDialog.show("已保存到下载目录", WaitDialog.TYPE.SUCCESS)
                                    mediaScan(file.toUri())
                                     */
                                }.catch {
                                    TipDialog.show("下载失败", WaitDialog.TYPE.ERROR)
                                }
                            }
                            "设为壁纸" -> {
                                scopeNetLife {
                                    val filename = System.currentTimeMillis().toString() + ".png"
                                    /*
                                    val file = Get<File>(getModel<WallpaperModel>().image) {
                                        setDownloadFileName(filename)
                                        setDownloadMd5Verify(true)
                                        addDownloadListener(object : ProgressListener() {
                                            override fun onProgress(p: Progress) {
                                                WaitDialog.show("下载中", p.progress().toFloat())
                                            }

                                        })
                                    }.await()
                                    SetWallpaper.setWallpaper(context,
                                        Environment.getExternalStorageDirectory().absolutePath + File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator + filename,
                                        "com.manchuan.tools.fileprovider")

                                     */
                                }.catch {
                                    TipDialog.show("下载失败", WaitDialog.TYPE.ERROR)
                                }
                            }
                        }
                        false
                    }
            }
        }.models = dataList
    }
}