package com.manchuan.tools.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.manchuan.tools.R
import com.manchuan.tools.bean.WallpaperCategoryBean
import com.manchuan.tools.utils.SettingsLoader

class WallpaperCategoryAdapter
    (private var mContext: Context, private var list: List<WallpaperCategoryBean>?) :
    RecyclerView.Adapter<WallpaperCategoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.name)
        val image: ImageView = itemView.findViewById(R.id.image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: WallpaperCategoryAdapter.ViewHolder,
        @SuppressLint("RecyclerView") i: Int
    ) {
        holder.itemView.setOnClickListener { view: View? ->
            if (onItemClickListener != null) {
                onItemClickListener!!.onItemClick(view, i)
            }
        }
        holder.title.text = list!![i].name
        Glide.with(mContext)
            .load(list!![i].image)
            .skipMemoryCache(true)
            .diskCacheStrategy(SettingsLoader.diskCacheMethod).transition(
                DrawableTransitionOptions
                    .withCrossFade().crossFade()
            )
            .into(holder.image)
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