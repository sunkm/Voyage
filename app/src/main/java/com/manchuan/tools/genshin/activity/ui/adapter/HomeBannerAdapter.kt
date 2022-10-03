package com.manchuan.tools.genshin.activity.ui.adapter

import android.graphics.Bitmap
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.drake.net.utils.TipUtils.toast
import com.manchuan.tools.genshin.GenshinConfig
import com.manchuan.tools.genshin.bean.home.HomeInformationBean
import com.manchuan.tools.genshin.ext.sp
import com.youth.banner.adapter.BannerAdapter
import com.youth.banner.util.BannerUtils


class HomeBannerAdapter(
    val list: List<HomeInformationBean.CarouselsBean>,
    val block: (HomeInformationBean.CarouselsBean) -> Unit,
    val blockListener: (Bitmap) -> Unit,
) : BannerAdapter<HomeInformationBean.CarouselsBean, ImageAdapter.ImageHolder>(list) {

    override fun onCreateHolder(p0: ViewGroup?, p1: Int): ImageAdapter.ImageHolder {
        val image = ImageView(p0!!.context)
        image.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        BannerUtils.setBannerRound(image, 8F)
        return ImageAdapter.ImageHolder(image)
    }

    override fun onBindView(
        p0: ImageAdapter.ImageHolder?,
        p1: HomeInformationBean.CarouselsBean?,
        p2: Int,
        p3: Int,
    ) {
        val roundedCorners = RoundedCorners(14)
        //通过RequestOptions扩展功能,override:采样率,因为ImageView就这么大,可以压缩图片,降低内存消耗
        //通过RequestOptions扩展功能,override:采样率,因为ImageView就这么大,可以压缩图片,降低内存消耗
        val options = RequestOptions.bitmapTransform(roundedCorners)
        Glide.with(p0!!.imageView)
            .asBitmap()
            .load(p1!!.cover)
            .apply(options)
            .skipMemoryCache(false)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .listener(object : RequestListener<Bitmap> {
                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean,
                ): Boolean {
                    if (resource != null) {
                        blockListener(resource)
                    }
                    return false
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean,
                ): Boolean {
                    toast("图片加载失败")
                    return false
                }

            })
            .into(p0.imageView)
        p0.imageView.setOnClickListener {
            if (sp.getBoolean(GenshinConfig.SP_HOME_BANNER_JUMP_TO_ARTICLE, true)) {
                block(p1)
            }
        }
    }

}