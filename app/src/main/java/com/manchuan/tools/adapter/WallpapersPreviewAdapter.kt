package com.manchuan.tools.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.manchuan.tools.R
import com.manchuan.tools.bean.WallpapersBean
import com.manchuan.tools.utils.SettingsLoader


class WallpapersPreviewAdapter(
    private var mContext: Context,
    private var list: List<WallpapersBean>?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_HEADER: Int = 0
    private val TYPE_LIST: Int = 1

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return TYPE_HEADER
        }
        return TYPE_LIST
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.image)
    }

    class ViewHolderHeader(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layout: LinearLayout = itemView.findViewById(R.id.layout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_HEADER) {
            val header =
                LayoutInflater.from(parent.context).inflate(R.layout.padding_toolbar, parent, false)
            return ViewHolderHeader(header)
        }
        val view = LayoutInflater.from(mContext).inflate(R.layout.items_previews, parent, false)
        return ViewHolder(view)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val manager = recyclerView.layoutManager
        if (manager is GridLayoutManager) {
            manager.spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (getItemViewType(position) == TYPE_HEADER) manager.spanCount else 1
                }
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        @SuppressLint("RecyclerView") i: Int
    ) {
        if (holder is ViewHolderHeader) {
            return
        }
        if (holder is ViewHolder) {
            holder.itemView.setOnClickListener { view: View? ->
                if (onItemClickListener != null) {
                    onItemClickListener!!.onItemClick(view, i)
                }
            }
            Glide.with(mContext)
                .load(list!![i].image)
                .skipMemoryCache(true)
                .diskCacheStrategy(SettingsLoader.diskCacheMethod)
                .transition(
                    DrawableTransitionOptions
                        .withCrossFade().crossFade()
                )
                .into(holder.image)
        }
    }

    private var onItemClickListener: OnItemClickListener? = null

    override fun getItemCount(): Int {
        return list!!.size
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    interface OnItemClickListener {
        fun onItemClick(v: View?, position: Int)
    }

}