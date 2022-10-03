package com.manchuan.tools.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import cc.shinichi.wallpaperlib.SetWallpaper
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.addModels
import com.drake.brv.utils.grid
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.Get
import com.drake.net.component.Progress
import com.drake.net.interfaces.ProgressListener
import com.drake.net.utils.scope
import com.drake.net.utils.scopeNetLife
import com.dylanc.longan.fileProviderAuthority
import com.dylanc.longan.logError
import com.dylanc.longan.toUri
import com.gyf.immersionbar.ktx.immersionBar
import com.kongzue.dialogx.dialogs.BottomMenu
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.lxj.androidktx.core.load
import com.manchuan.tools.R
import com.manchuan.tools.base.AnimationActivity
import com.manchuan.tools.databinding.ActivityWallpaperPreviewBinding
import com.manchuan.tools.databinding.ItemsPreviewsBinding
import com.manchuan.tools.extensions.externalPicturesDirPath
import com.manchuan.tools.json.SerializationConverter
import com.manchuan.tools.model.WallpaperModel
import com.manchuan.tools.utils.UiUtils
import com.mcxiaoke.koi.ext.mediaScan
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import java.io.File


class WallpaperPreviewActivity : AnimationActivity() {
    private val wallpaper by lazy {
        ActivityWallpaperPreviewBinding.inflate(layoutInflater)
    }
    private var limit: Int = 26
    private var skip: Int = 0
    private var ids: String? = null

    @SuppressLint("NotifyDataSetChanged", "InflateParams", "SdCardPath")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(wallpaper.root)
        setSupportActionBar(wallpaper.toolbar)
        immersionBar {
            titleBar(wallpaper.toolbar)
            statusBarDarkFont(!UiUtils.isDarkMode())
            transparentBar()
        }
        val intent = intent
        if (intent != null) {
            ids = intent.getStringExtra("id")
            supportActionBar?.title = intent.getStringExtra("title")
        }
        FastScrollerBuilder(wallpaper.recyclerView).useMd2Style().build()
        wallpaper.recyclerView.grid(2).setup {
            addType<WallpaperModel.Res.Vertical>(R.layout.items_previews)
            setAnimation(AnimationType.ALPHA)
            onBind {
                val binding = ItemsPreviewsBinding.bind(itemView)
                val model = getModel<WallpaperModel.Res.Vertical>()
                binding.image.load(model.img, isCrossFade = true, isForceOriginalSize = true)
            }
            onClick(R.id.image) {
                val model = getModel<WallpaperModel.Res.Vertical>()
                BottomMenu.show("操作", null, listOf("下载", "设为壁纸"))
                    .setOnMenuItemClickListener { dialog, text, index ->
                        when (text) {
                            "下载" -> {
                                scopeNetLife {
                                    val file = Get<File>(model.img) {
                                        setDownloadFileName(
                                            System.currentTimeMillis().toString() + ".png"
                                        )
                                        setDownloadDir(externalPicturesDirPath.toString())
                                        setDownloadMd5Verify(true)
                                        addDownloadListener(object : ProgressListener() {
                                            override fun onProgress(p: Progress) {
                                                runOnUiThread {
                                                    WaitDialog.show(
                                                        "下载中", p.progress().toFloat()
                                                    )
                                                }
                                            }

                                        })
                                    }.await()
                                    runOnUiThread {
                                        TipDialog.show("已保存到下载目录", WaitDialog.TYPE.SUCCESS)
                                    }
                                    mediaScan(file.toUri())
                                }.catch {
                                    TipDialog.show("下载失败", WaitDialog.TYPE.ERROR)
                                }
                            }

                            "设为壁纸" -> {
                                scopeNetLife {
                                    val filename = System.currentTimeMillis().toString() + ".png"
                                    val file = Get<File>(model.img) {
                                        setDownloadFileName(filename)
                                        setDownloadDir(externalPicturesDirPath.toString())
                                        setDownloadMd5Verify(true)
                                        addDownloadListener(object : ProgressListener() {
                                            override fun onProgress(p: Progress) {
                                                runOnUiThread {
                                                    WaitDialog.show(
                                                        "下载中", p.progress().toFloat()
                                                    )
                                                }
                                            }

                                        })
                                    }.await()
                                    WaitDialog.dismiss()
                                    SetWallpaper.setWallpaper(
                                        context, file.absolutePath, fileProviderAuthority
                                    )
                                }.catch {
                                    TipDialog.show("下载失败", WaitDialog.TYPE.ERROR)
                                }
                            }
                        }
                        false
                    }
            }
        }
        logError(ids)
        wallpaper.page.onRefresh {
            scope {
                wallpaper.recyclerView.models =
                    Get<WallpaperModel>("http://service.picasso.adesk.com/v1/vertical/category/$ids/vertical?limit=$limit&skip=0&order=new") {
                        converter = SerializationConverter("0", "code", "msg")
                    }.await().res.vertical
            }
        }.autoRefresh()
        wallpaper.page.onLoadMore {
            scope {
                skip += limit
                wallpaper.recyclerView.addModels(Get<WallpaperModel>("http://service.picasso.adesk.com/v1/vertical/category/$ids/vertical?limit=${limit}&skip=$skip&order=new") {
                    converter = SerializationConverter("0", "code", "msg")
                }.await().res.vertical)
            }
        }
    }

    @Deprecated(
        "Deprecated in Java", ReplaceWith(
            "super.onActivityResult(requestCode, resultCode, data)",
            "androidx.appcompat.app.AppCompatActivity"
        )
    )
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10000 && resultCode == RESULT_OK) {
            PopTip.show("设置成功")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}