package com.manchuan.tools.activity

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.LogUtils
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.setup
import com.drake.brv.utils.staggered
import com.dylanc.longan.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.gyf.immersionbar.ktx.immersionBar
import com.itxca.spannablex.spannable
import com.jeffmony.downloader.VideoDownloadManager
import com.jeffmony.downloader.listener.DownloadListener
import com.jeffmony.downloader.listener.IDownloadInfosCallback
import com.jeffmony.downloader.model.VideoTaskItem
import com.jeffmony.downloader.model.VideoTaskState
import com.lxj.androidktx.core.animateGone
import com.lxj.androidktx.core.animateVisible
import com.lxj.androidktx.core.drawable
import com.manchuan.tools.R
import com.manchuan.tools.databinding.ActivityM3u8Binding
import com.manchuan.tools.databinding.BottomTasksM3u8Binding
import com.manchuan.tools.databinding.ItemM3u8Binding
import com.manchuan.tools.extensions.errorColor
import com.manchuan.tools.extensions.getColorByAttr
import com.manchuan.tools.utils.UiUtils


class M3u8Activity : AppCompatActivity() {

    private val binding by lazy {
        ActivityM3u8Binding.inflate(layoutInflater)
    }

    private var mLastProgressTimeStamp: Long = 0
    private var mLastSpeedTimeStamp: Long = 0
    private val tasksList = arrayListOf<VideoTaskItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        immersionBar {
            navigationBarColorInt(getColorByAttr(com.google.android.material.R.attr.colorSurface))
            titleBar(binding.toolbar)
            statusBarDarkFont(!UiUtils.isDarkMode())
        }
        binding.toolbar.apply {
            setNavigationOnClickListener {
                finish()
            }
        }
        binding.state.showContent()
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomDrawer)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        binding.bottomDrawer.layoutTransition.enableTransitionType(LayoutTransition.CHANGE_APPEARING)
        binding.jiexi.doOnClick {
            if (binding.url.isTextNotEmpty()) {
                val item = VideoTaskItem(binding.url.textString, null, randomUUIDString, "Download")
                tasksList.add(item)
                notifyChanged()
                VideoDownloadManager.getInstance().startDownload(item);
            }
        }
        binding.recyclerView.staggered(1).setup {
            addType<VideoTaskItem>(R.layout.item_m3u8)
            setAnimation(AnimationType.ALPHA)
            onBind {
                val binding = ItemM3u8Binding.bind(itemView)
                val item = getModel<VideoTaskItem>()
                binding.title.text = item.title
                when (item.taskState) {
                    VideoTaskState.PENDING, VideoTaskState.PREPARE -> {
                        //playBtn.visibility = View.INVISIBLE
                        binding.action.icon = drawable(R.drawable.ic_baseline_stop_24)
                        binding.summary.text = resources.getString(R.string.waiting)
                        binding.progress.animateGone()
                    }
                    VideoTaskState.START, VideoTaskState.DOWNLOADING -> {
                        binding.action.icon = drawable(R.drawable.ic_baseline_stop_24)
                        binding.action.doOnClick {
                            VideoDownloadManager.getInstance().pauseDownloadTask(item)
                            binding.action.icon = drawable(R.drawable.ic_baseline_play_arrow_24)
                        }
                        binding.progress.animateVisible()
                        binding.progress.isIndeterminate = false
                        binding.summary.text =
                            "已下载:${item.downloadSizeString} 速度:${item.speedString}"
                        binding.progress.setProgressCompat(item.percent.toInt(), true)
                    }
                    VideoTaskState.PAUSE -> {
                        //playBtn.visibility = View.INVISIBLE
                        binding.action.icon = drawable(R.drawable.ic_baseline_play_arrow_24)
                        binding.action.doOnClick {
                            VideoDownloadManager.getInstance().resumeDownload(item.url)
                            binding.action.icon = drawable(R.drawable.ic_baseline_stop_24)
                        }
                        binding.progress.animateGone()
                        binding.summary.text =
                            String.format(resources.getString(R.string.download_paused_downloaded_size),
                                item.downloadSizeString)
                    }
                    VideoTaskState.SUCCESS -> {
                        //playBtn.visibility = View.VISIBLE
                        binding.action.apply {
                            icon = drawable(R.drawable.ic_baseline_check_24)
                            doOnClick {

                            }
                        }
                        binding.progress.animateGone()
                        binding.summary.text =
                            String.format(resources.getString(R.string.download_completed_total_size),
                                item.downloadSizeString)
                    }
                    VideoTaskState.ERROR -> {
                        //playBtn.visibility = View.INVISIBLE
                        binding.progress.animateGone()
                        binding.action.apply {
                            icon = drawable(R.drawable.ic_baseline_error_outline_24)
                            iconTint = ColorStateList.valueOf(errorColor())
                        }
                        binding.summary.text =
                            resources.getString(R.string.download_error).spannable {
                                color(errorColor())
                            }
                    }
                    else -> {
                        //playBtn.visibility = View.INVISIBLE
                        binding.summary.text = resources.getString(R.string.not_downloaded)
                    }
                }
            }
            onClick(R.id.item) {
                val item = getModel<VideoTaskItem>()
                when (item.taskState) {
                    VideoTaskState.PENDING, VideoTaskState.PREPARE -> {

                    }
                    VideoTaskState.START, VideoTaskState.DOWNLOADING -> {
                    }
                    VideoTaskState.PAUSE -> {
                    }
                    VideoTaskState.SUCCESS -> {
                        item.filePath
                    }
                    VideoTaskState.ERROR -> {

                    }
                    else -> {

                    }
                }
            }
        }.models = tasksList
        VideoDownloadManager.getInstance().setGlobalDownloadListener(mListener);
        VideoDownloadManager.getInstance().fetchDownloadItems(infoCallback);
    }

    private val mListener: DownloadListener = object : DownloadListener() {
        override fun onDownloadDefault(item: VideoTaskItem) {
            LogUtils.w(TAG, "onDownloadDefault: $item")
            runOnUiThread {
                binding.recyclerView.adapter?.notifyItemChanged(tasksList.indexOf(item))
            }
        }

        override fun onDownloadPending(item: VideoTaskItem) {
            runOnUiThread {
                binding.recyclerView.adapter?.notifyItemChanged(tasksList.indexOf(item))
            }
        }

        override fun onDownloadPrepare(item: VideoTaskItem) {
            LogUtils.w(TAG, "onDownloadPrepare: $item")
            runOnUiThread {
                binding.recyclerView.adapter?.notifyItemChanged(tasksList.indexOf(item))
            }
        }

        override fun onDownloadStart(item: VideoTaskItem) {
            LogUtils.w(TAG, "onDownloadStart: $item")
            runOnUiThread {
                binding.recyclerView.adapter?.notifyItemChanged(tasksList.indexOf(item))
            }
        }

        override fun onDownloadProgress(item: VideoTaskItem) {
            val currentTimeStamp = System.currentTimeMillis()
            if (currentTimeStamp - mLastProgressTimeStamp > 1000) {
                LogUtils.w(TAG,
                    "onDownloadProgress: " + item.percentString + ", curTs=" + item.curTs + ", totalTs=" + item.totalTs)

                runOnUiThread {
                    binding.recyclerView.adapter?.notifyItemChanged(tasksList.indexOf(item))
                }
                mLastProgressTimeStamp = currentTimeStamp
            }
        }

        override fun onDownloadSpeed(item: VideoTaskItem?) {
            val currentTimeStamp = System.currentTimeMillis()
            if (currentTimeStamp - mLastSpeedTimeStamp > 1000) {
                runOnUiThread {
                    binding.recyclerView.adapter?.notifyItemChanged(tasksList.indexOf(item))
                }
                mLastSpeedTimeStamp = currentTimeStamp
            }
        }

        override fun onDownloadPause(item: VideoTaskItem) {
            LogUtils.w(TAG, "onDownloadPause: " + item.url)
            runOnUiThread {
                binding.recyclerView.adapter?.notifyItemChanged(tasksList.indexOf(item))
            }
        }

        override fun onDownloadError(item: VideoTaskItem) {
            LogUtils.w(TAG, "onDownloadError: " + item.url)
            runOnUiThread {
                binding.recyclerView.adapter?.notifyItemChanged(tasksList.indexOf(item))
            }
        }

        override fun onDownloadSuccess(item: VideoTaskItem) {
            LogUtils.w(TAG, "onDownloadSuccess: $item")
            runOnUiThread {
                binding.recyclerView.adapter?.notifyItemChanged(tasksList.indexOf(item))
            }
        }
    }

    class TasksBottomSheet : BottomSheetDialogFragment() {

        private val binding by lazy {
            BottomTasksM3u8Binding.inflate(layoutInflater)
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View {
            binding.title.text = tag
            return binding.root
        }
    }

    private val infoCallback: IDownloadInfosCallback = IDownloadInfosCallback { items ->
        items.forEach {
            tasksList.add(it)
            notifyChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun notifyChanged() {
        runOnUiThread { binding.recyclerView.adapter?.notifyDataSetChanged() }
    }

    override fun onDestroy() {
        super.onDestroy()
        VideoDownloadManager.getInstance().removeDownloadInfosCallback(infoCallback)
    }

}