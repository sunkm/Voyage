package com.manchuan.tools.bean

class HistoryBean : Comparable<HistoryBean> {
    var title: String? = null
    var content: String? = null
    var image: String? = ""
    var readLink: String? = null
    var time: String? = null
    override fun compareTo(other: HistoryBean): Int {
        return time!!.compareTo(other.time!!)
    }
}