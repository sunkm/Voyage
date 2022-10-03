package com.manchuan.tools.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.manchuan.tools.R
import com.manchuan.tools.items.AppItem

class AppAdapter(
    private var mData: MutableList<AppItem>,
    private val mContext: Context
) : RecyclerView.Adapter<AppAdapter.ViewHolder>(), Filterable {
    override fun getItemCount(): Int {
        return mData.size
    }

    private var fullDataset: List<AppItem>? = null
    private var isList = true
    private var onItemClickListener: OnItemClickListener? = null
    private var onItemLongClickListener: OnItemLongClickListener? = null
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_list_app, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        holder.itemView.setOnClickListener { view ->
            if (onItemClickListener != null) {
                onItemClickListener!!.onItemClick(view, i)
            }
        }
        holder.itemView.setOnLongClickListener { p1 ->
            if (onItemLongClickListener != null) {
                onItemLongClickListener!!.onItemLongClickListener(p1, i)
            }
            true
        }
        holder.app_name.text = mData[i].app_name
        holder.package_name.text = mData[i].package_name
        Glide.with(mContext)
            .load(mData[i].app_icon)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(holder.app_icon)
    }

    fun setDataSet(list: List<AppItem>?) {
        fullDataset = list
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val oReturn = FilterResults()
                val filteredList = ArrayList<AppItem>()
                if (constraint.isEmpty()) {
                    filteredList.addAll(fullDataset!!)
                } else {
                    val filterPattern = constraint.toString().trim { it <= ' ' }
                    for (item in fullDataset!!) {
                        if (item.app_name.contains(filterPattern)) {
                            filteredList.add(item)
                        }
                    }
                }
                val results = FilterResults()
                results.values = filteredList
                return results
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                mData.clear()
                mData += results.values as MutableList<AppItem>
                notifyDataSetChanged()
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val app_name: TextView = itemView.findViewById(R.id.app_name)
        val package_name: TextView = itemView.findViewById(R.id.package_name)
        val app_icon: ImageView = itemView.findViewById(R.id.app_icon)

    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    fun setOnItemLongClickListener(onItemLongClickListener: OnItemLongClickListener?) {
        this.onItemLongClickListener = onItemLongClickListener
    }

    interface OnItemClickListener {
        fun onItemClick(v: View?, position: Int)
    }

    interface OnItemLongClickListener {
        fun onItemLongClickListener(view: View?, position: Int)
    }

    init {
        this.isList = isList
    }
}