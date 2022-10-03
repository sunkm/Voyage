package com.manchuan.tools.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.manchuan.tools.R
import com.manchuan.tools.items.PermissionItem

class PermissionAdapter(private val permissionItemLists: List<PermissionItem>) :
    RecyclerView.Adapter<PermissionAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var permission_ico: ImageView = view.findViewById<View>(R.id.permission_ico) as ImageView
        var permission_name: TextView = view.findViewById<View>(R.id.permission_title) as TextView
        var permission_usage: TextView = view.findViewById(R.id.permission_usage)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.permissions_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val permissions = permissionItemLists[position]
        holder.permission_ico.setImageResource(permissions.permission_ico)
        holder.permission_ico.setColorFilter(color)
        holder.permission_name.text = permissions.permission_name
        holder.permission_usage.text = permissions.permission_usage
    }

    override fun getItemCount(): Int {
        return permissionItemLists.size
    }

    companion object {
        var color = 0
        @JvmName("setColor1")
        fun setColor(mColor: Int) {
            color = mColor
        }
    }
}