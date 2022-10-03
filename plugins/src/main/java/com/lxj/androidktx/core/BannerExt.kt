package com.lxj.androidktx.core

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.lxj.androidktx.widget.ShapeImageView
import com.manchuan.tools.plugins.R
import com.youth.banner.adapter.BannerAdapter
import com.zhpan.bannerview.BaseBannerAdapter
import com.zhpan.bannerview.BaseViewHolder

class CommonBannerAdapter(
    data: List<BannersData>, var margin: Rect = Rect(), var cornerRadius: Int = 0,
    var pageElevation: Int = 0, var onItemClick: ((Int) -> Unit)? = null,
) : BannerAdapter<Any, CommonBannerAdapter.BannerViewHolder>(data) {
    override fun onCreateHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val item = LayoutInflater.from(parent.context)
            .inflate(R.layout._ktx_adapter_common_banner, parent, false)
        return BannerViewHolder(item)
    }

    override fun onBindView(holder: BannerViewHolder, t: Any, position: Int, size: Int) {
        holder.itemView.margin(margin.left, margin.top, margin.right, margin.bottom)
        (holder.itemView as AppCompatImageView).apply {
            load((getData(position) as BannersData).image,
                isCrossFade = true,
                roundRadius = cornerRadius,
                isForceOriginalSize = true)
            elevation = pageElevation.toFloat()
            click { onItemClick?.invoke(position) }
        }
    }

    inner class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}

class BannersAdapter(
    var cornerRadius: Int = 8, var pageElevation: Int = 0,
    var onItemClick: ((Int) -> Unit)? = null,
) : BaseBannerAdapter<BannerData>() {

    override fun getLayoutId(viewType: Int): Int {
        return R.layout.banner_data
    }

    override fun bindData(
        holder: BaseViewHolder<BannerData>?,
        data: BannerData?,
        position: Int,
        pageSize: Int,
    ) {
        (holder?.findViewById<ShapeImageView>(R.id.image))?.apply {
            setup(corner = cornerRadius)
            load(data?.image,
                isCrossFade = true,
                roundRadius = cornerRadius,
                isForceOriginalSize = true)
            elevation = pageElevation.toFloat()
            click { onItemClick?.invoke(position) }
        }
        (holder?.findViewById<TextView>(R.id.title))?.apply {
            text = data?.title
        }
        (holder?.findViewById<TextView>(R.id.subtitle))?.apply {
            text = data?.summary
        }
    }
}

data class BannersData(
    var image: String,
    var title: String,
    var summary: String, var clickUrl: String? = "",
)

data class BannerData(
    var title: String,
    var summary: String,
    var image: String,
    var clickUrl: String? = "",
)