package com.manchuan.tools.plugins.api

interface Properties {

    fun putString(key:String,value:String)

    fun getString(key: String): String?

}