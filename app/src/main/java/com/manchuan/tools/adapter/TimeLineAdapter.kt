package com.manchuan.tools.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.vipulasri.timelineview.TimelineView
import com.lxj.androidktx.core.animateGone
import com.lxj.androidktx.core.animateVisible
import com.manchuan.tools.R
import com.manchuan.tools.databinding.ItemTimelineBinding
import com.manchuan.tools.model.ExpressModel

class TimeLineAdapter(
    private val mFeedList: List<ExpressModel.Data>,
    private var mAttributes: TimelineAttributes,
) : RecyclerView.Adapter<TimeLineAdapter.TimeLineViewHolder>() {

    private lateinit var mLayoutInflater: LayoutInflater

    override fun getItemViewType(position: Int): Int {
        return TimelineView.getTimeLineViewType(position, itemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeLineViewHolder {

        if (!::mLayoutInflater.isInitialized) {
            mLayoutInflater = LayoutInflater.from(parent.context)
        }
        val view = ItemTimelineBinding.inflate(mLayoutInflater).root
        return TimeLineViewHolder(view, viewType)
    }

    override fun onBindViewHolder(holder: TimeLineViewHolder, position: Int) {
        val timeLineModel = mFeedList[position]
        if (timeLineModel.time.isNotEmpty()) {
            holder.date.animateVisible()
            holder.date.text =
                timeLineModel.time
        } else {
            holder.date.animateGone()
        }
        holder.timeline.setMarker(
            ContextCompat.getDrawable(
                holder.itemView.context,
                R.drawable.baseline_check_circle_outline_24
            ), mAttributes.markerColor
        )
        holder.message.text = timeLineModel.context
    }

    override fun getItemCount() = mFeedList.size

    inner class TimeLineViewHolder(itemView: View, viewType: Int) :
        RecyclerView.ViewHolder(itemView) {
        val itemViews = ItemTimelineBinding.bind(itemView)
        val date = itemViews.textTimelineDate
        val message = itemViews.textTimelineTitle
        val timeline = itemViews.timeline

        init {
            timeline.initLine(viewType)
            timeline.markerSize = mAttributes.markerSize
            timeline.setMarkerColor(mAttributes.markerColor)
            timeline.isMarkerInCenter = mAttributes.markerInCenter
            timeline.markerPaddingLeft = mAttributes.markerLeftPadding
            timeline.markerPaddingTop = mAttributes.markerTopPadding
            timeline.markerPaddingRight = mAttributes.markerRightPadding
            timeline.markerPaddingBottom = mAttributes.markerBottomPadding
            timeline.linePadding = mAttributes.linePadding
            timeline.lineWidth = mAttributes.lineWidth
            timeline.setStartLineColor(mAttributes.startLineColor, viewType)
            timeline.setEndLineColor(mAttributes.endLineColor, viewType)
            timeline.lineStyle = mAttributes.lineStyle
            timeline.lineStyleDashLength = mAttributes.lineDashWidth
            timeline.lineStyleDashGap = mAttributes.lineDashGap
        }
    }

}